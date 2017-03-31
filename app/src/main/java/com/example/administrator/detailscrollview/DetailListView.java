package com.example.administrator.detailscrollview;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.ListView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by LinXin on 2017/3/23.
 */
public class DetailListView extends ListView {

    private Method reportScrollStateChangeMethod;
    private Method startMethod;
    private Object mFlingRunnable;
    private DetailScrollView mScrollView;
    private float mLastY;
    private VelocityTracker mVelocityTracker;
    private boolean isDragged = false;

    public DetailListView(Context context) {
        super(context);
        init();
    }

    public DetailListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DetailListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected void init() {
        setVerticalScrollBarEnabled(false);
        setOverScrollMode(OVER_SCROLL_NEVER);
        if (VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            setFriction(ViewConfiguration.getScrollFriction());
            try {
                Field field = AbsListView.class.getDeclaredField("mFlingRunnable");
                field.setAccessible(true);
                mFlingRunnable = field.get(this);
                Class clazz = Class.forName("android.widget.AbsListView.FlingRunnable");
                startMethod = clazz.getDeclaredMethod("start", Integer.TYPE);
                startMethod.setAccessible(true);
                reportScrollStateChangeMethod = AbsListView.class.getDeclaredMethod("reportScrollStateChange", Integer.TYPE);
                reportScrollStateChangeMethod.setAccessible(true);
            } catch (Throwable v0) {
                v0.printStackTrace();
                mFlingRunnable = null;
                startMethod = null;
                reportScrollStateChangeMethod = null;
            }
        }
    }

    public boolean startFling(int velocityY) {
        if (isCanScroll()) {
            if (VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fling(velocityY);
                return true;
            } else if (reportScrollStateChangeMethod != null && startMethod != null) {
                try {
                    reportScrollStateChangeMethod.invoke(this, OnScrollListener.SCROLL_STATE_FLING);
                    startMethod.invoke(mFlingRunnable, velocityY);
                    return true;
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }
        return false;
    }

    public boolean isCanScroll() {
        return ((mFlingRunnable != null && reportScrollStateChangeMethod != null && startMethod != null)
                || VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

    public void setScrollView(DetailScrollView mScrollView) {
        this.mScrollView = mScrollView;
    }

    @Override
    public int computeVerticalScrollRange() {
        return super.computeVerticalScrollRange();
    }

    @Override
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
                DetailScrollView.LogE("MyListViewV9.onTouchEvent.ACTION_MOVE,.......dy=" + dy + ",deltaY=" + deltaY + ",canScrollVertically=" + canScrollVertically(DetailScrollView.DIRECT_TOP));
                if (!canScrollVertically(DetailScrollView.DIRECT_TOP) && dy != 0) {
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
        return super.onTouchEvent(ev);
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