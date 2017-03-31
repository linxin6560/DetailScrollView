package com.example.administrator.detailscrollview;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.webkit.WebView;

import java.util.LinkedList;

public class DetailWebView extends WebView {

    private DetailScrollView mScrollView;
    private float mLastY;
    private VelocityTracker mVelocityTracker;
    private float mMaxVelocity;
    private boolean isDragged = false;
    private LinkedList<SpeedItem> speedItems;

    public DetailWebView(Context context) {
        super(context);
        init();
    }

    public DetailWebView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public DetailWebView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init();
    }

    protected void init() {
        speedItems = new LinkedList<>();
        setVerticalScrollBarEnabled(false);
        setOverScrollMode(OVER_SCROLL_NEVER);
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mMaxVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    public void setScrollView(DetailScrollView scrollView) {
        this.mScrollView = scrollView;
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
                float distance = nowY - mLastY;
                int dy = mScrollView.adjustScrollY((int) -distance);
                mLastY = nowY;
                if (!canScrollVertically(DetailScrollView.DIRECT_BOTTOM) && dy != 0) {
                    mScrollView.customScrollBy(dy);
                    isDragged = true;
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!canScrollVertically(DetailScrollView.DIRECT_BOTTOM) && isDragged) {
                    mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                    final float curVelocity = mVelocityTracker.getYVelocity(0);
                    mScrollView.fling(-(int) curVelocity);
                }
                speedItems.clear();
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

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        if (speedItems.size() >= 10) {
            speedItems.removeFirst();
        }
        if (deltaY + scrollY < scrollRangeY) {
            speedItems.add(new SpeedItem(deltaY, SystemClock.uptimeMillis()));
        } else if (!speedItems.isEmpty()) {
            int totalDeltaY = 0;
            for (SpeedItem speedItem : speedItems) {
                totalDeltaY += speedItem.deltaY;
            }
            int speed = ((int) (speedItems.getLast().time - speedItems.getFirst().time));
            speedItems.clear();
            if (speed > 0 && totalDeltaY != 0) {
                speed = totalDeltaY / speed * 1000;
                mScrollView.fling(speed);
            }
        }
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
    }

    @Override
    public int computeVerticalScrollRange() {
        return super.computeVerticalScrollRange();
    }

    private class SpeedItem {
        int deltaY;
        long time;

        public SpeedItem(int arg1, long arg2) {
            this.deltaY = arg1;
            this.time = arg2;
        }

        @Override
        public String toString() {
            return "SpeedItem{" +
                    "deltaY=" + deltaY +
                    ", time=" + time +
                    '}';
        }
    }
}

