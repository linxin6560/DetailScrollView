package com.levylin.detailscrollview.views.helper;

import android.content.Context;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

import com.levylin.detailscrollview.views.DetailListView;
import com.levylin.detailscrollview.views.DetailRecyclerView;
import com.levylin.detailscrollview.views.DetailScrollView;

/**
 * 列表点击帮助类
 * Created by LinXin on 2017/3/31.
 */
public class ListViewTouchHelper {

    private DetailScrollView mScrollView;
    private float mLastY;
    private VelocityTracker mVelocityTracker;
    private boolean isDragged = false;
    private DetailListView mListView;
    private DetailRecyclerView mRecyclerView;
    private int maxVelocity;
    private float deltaY;

    public ListViewTouchHelper(DetailScrollView scrollView, DetailListView listView) {
        this.mScrollView = scrollView;
        this.mListView = listView;
        init(scrollView.getContext());
    }

    public ListViewTouchHelper(DetailScrollView scrollView, DetailRecyclerView recyclerView) {
        this.mScrollView = scrollView;
        this.mRecyclerView = recyclerView;
        init(scrollView.getContext());
    }

    private void init(Context context) {
        ViewConfiguration configuration = ViewConfiguration.get(context);
        maxVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    public boolean onTouchEvent(MotionEvent ev) {
        acquireVelocityTracker(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getRawY();
                DetailScrollView.LogE("ListViewTouchHelper.onTouchEvent.ACTION_MOVE.......mLastY=" + mLastY);
                break;
            case MotionEvent.ACTION_MOVE:
                float nowY = ev.getRawY();
                if (mLastY == 0) {
                    mLastY = nowY;
                }
                deltaY = nowY - mLastY;
                int dy = mScrollView.adjustScrollY((int) -deltaY);
                mLastY = nowY;
                DetailScrollView.LogE("ListViewTouchHelper.onTouchEvent.ACTION_MOVE,.......dy=" + dy + "\n"
                        + ",deltaY=" + deltaY + "\n"
                        + ",nowY=" + nowY + "\n"
                        + ",mLastY=" + mLastY + "\n"
                        + ",ListView.canScrollVertically=" + canScrollVertically(DetailScrollView.DIRECT_TOP) + "\n"
                        + ",mScrollView.canScrollVertically=" + mScrollView.canScrollVertically(DetailScrollView.DIRECT_TOP) + "\n"
                        + ",mScrollView.getScrollY=" + mScrollView.getScrollY());
                if ((!canScrollVertically(DetailScrollView.DIRECT_TOP) && dy < 0)//向下滑，当列表不能滚动时，滑动DetailScrollView
                        || (mScrollView.canScrollVertically(DetailScrollView.DIRECT_TOP) && dy > 0)) {//向上滑，当DetailScrollView能继续上滑时，DetailScrollView上滑
                    mScrollView.customScrollBy(dy);
                    isDragged = true;
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!canScrollVertically(DetailScrollView.DIRECT_TOP) && isDragged) {
                    mVelocityTracker.computeCurrentVelocity(1000, maxVelocity);
                    float curVelocity = mVelocityTracker.getYVelocity(0);
                    DetailScrollView.LogE("ListViewTouchHelper.onTouchEvent.ACTION_UP......curVelocity =" + curVelocity + ",deltaY=" + deltaY);
                    if (curVelocity * deltaY > 0) {//deltaY>0，手指是向下滑动，对应的scrollerview应该向上滚，则curVelocity应该小于0。有时候会莫明出现curVelocity>0的情况，所以加上这个判断，保证两者正负必须相反
                        DetailScrollView.LogE("ListViewTouchHelper.onTouchEvent.ACTION_UP....curVelocity * deltaY >0");
                        curVelocity = -curVelocity;
                    }
                    DetailScrollView.LogE("ListViewTouchHelper.onTouchEvent.ACTION_UP......ScrollView.fling:" + curVelocity);
                    mScrollView.fling(-(int) curVelocity);
                }
                mLastY = 0;
                isDragged = false;
                releaseVelocityTracker();
                break;
        }
        return true;
    }

    private boolean canScrollVertically(int direct) {
        if (mListView != null) {
            return mListView.canScrollVertically(direct);
        }
        return mRecyclerView.canScrollVertically(direct);
    }


    private void acquireVelocityTracker(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
    }

    private void releaseVelocityTracker() {
        if (mVelocityTracker == null)
            return;
        mVelocityTracker.recycle();
        mVelocityTracker = null;
    }

    /**
     * ListView和RecyclerView的fling过渡到WebView
     *
     * @param isIdle 滑动是否暂停
     */
    public void onScrollStateChanged(boolean isIdle) {
        boolean isCanScrollTop = canScrollVertically(DetailScrollView.DIRECT_TOP);
        DetailScrollView.LogE("DetailRecyclerView.onScrollStateChanged....isCanScrollTop=" + isCanScrollTop + ",isIdle=" + isIdle + ",deltaY=" + deltaY);
        if (!isCanScrollTop && isIdle && deltaY > 0) {
            int velocity = getCurVelocity();
            DetailScrollView.LogE("DetailRecyclerView.onScrollStateChanged....mScrollView.fling:" + (-velocity));
            mScrollView.fling(-velocity);
        }
        if (isIdle) {
            deltaY = 0;
        }
    }

    /**
     * 获取当前滑动速度
     *
     * @return 当前速度
     */
    private int getCurVelocity() {
        if (mRecyclerView != null) {
            return mRecyclerView.getCurrVelocity();
        } else if (mListView != null) {
            return mListView.getCurrVelocity();
        }
        return 0;
    }
}
