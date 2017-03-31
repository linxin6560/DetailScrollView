package com.levylin.detailscrollview.views.helper;

import android.view.MotionEvent;
import android.view.VelocityTracker;

import com.levylin.detailscrollview.views.DetailListView;
import com.levylin.detailscrollview.views.DetailRecyclerView;
import com.levylin.detailscrollview.views.DetailScrollView;

/**
 * Created by LinXin on 2017/3/31.
 */
public class ListViewHelper {

    private DetailScrollView mScrollView;
    private float mLastY;
    private VelocityTracker mVelocityTracker;
    private boolean isDragged = false;
    private DetailListView mListView;
    private DetailRecyclerView mRecyclerView;

    public ListViewHelper(DetailScrollView scrollView, DetailListView listView) {
        this.mScrollView = scrollView;
        this.mListView = listView;
    }

    public ListViewHelper(DetailScrollView scrollView, DetailRecyclerView recyclerView) {
        this.mScrollView = scrollView;
        this.mRecyclerView = recyclerView;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        acquireVelocityTracker(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                float nowY = ev.getRawY();
                float deltaY = nowY - mLastY;
                int dy = mScrollView.adjustScrollY((int) -deltaY);
                mLastY = nowY;
                DetailScrollView.LogE("ListViewHelper.onTouchEvent.ACTION_MOVE,.......dy=" + dy + "\n"
                        + ",deltaY=" + deltaY + "\n"
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
                    mVelocityTracker.computeCurrentVelocity(1000);
                    final float curVelocity = mVelocityTracker.getYVelocity(0);
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
}
