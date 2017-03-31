package com.levylin.detailscrollview.views;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by LinXin on 2017/3/25.
 * 1.
 */
public class DetailScrollView extends ViewGroup {

    private static final String TAG = DetailScrollView.class.getSimpleName();
    private static boolean isDebug = true;

    public static final int DIRECT_BOTTOM = 1;
    public static final int DIRECT_TOP = -1;

    private IDetailListView mListView;
    private IDetailWebView mWebView;
    private Scroller mScroller;
    private float mLastY;
    private VelocityTracker mVelocityTracker;
    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mWebHeight;
    private boolean mIsBeingDragged;

    public DetailScrollView(Context context) {
        super(context);
        init(context);
    }

    public DetailScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DetailScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setVerticalScrollBarEnabled(true);
        mScroller = new Scroller(context);
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof IDetailListView) {
                mListView = (IDetailListView) child;
            } else if (child instanceof IDetailWebView) {
                mWebView = (IDetailWebView) child;
            }
        }
        if (mListView != null) {
            mListView.setScrollView(this);
        }
        if (mWebView != null) {
            mWebView.setScrollView(this);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int w = r - l;
        int h = b - t;
        mWebHeight = getWebViewHeight(h);
        int top = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.layout(0, top, w, top + h);
            top += h;
        }
    }

    private int getWebViewHeight(int height) {
        View view = getChildAt(0);
        if (view != null) {
            int h = view.getLayoutParams().height;
            if (h > 0)
                return h;
        }
        return height;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 0;
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthMode == 0 || heightMode == 0) {
            heightSize = 0;
        } else {
            widthMode = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
            heightMode = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
            for (int i = 0; i < getChildCount(); i++) {
                View child = this.getChildAt(i);
                child.measure(widthMode, heightMode);
            }
            width = widthSize;
        }
        this.setMeasuredDimension(width, heightSize);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float y = event.getY();
        boolean isAtTop = getScrollY() == mWebHeight;//MyScroll是否在头部
        boolean isAtBottom = getScrollY() == 0;//MyScroll是否在底部
        acquireVelocityTracker(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {//按下去的时候就要取消动画，在move的时候取消动画就太迟了，会造成已经进入webView滑动事件，同时触发MyScrollView的滚动事件的奇葩bug
                    mScroller.abortAnimation();
                }
                mLastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                float delta = y - mLastY;
                int dy = adjustScrollY((int) -delta);
                LogE(TAG + ".onTouchEvent.Move.......dy=" + dy + ",delta=" + delta);
                if (dy != 0) {
                    if (mListView.canScrollVertically(DIRECT_TOP) && isAtTop) {//因为ListView上滑操作导致ListView可以继续下滑，故要先ListView滑到顶部，再滑动MyScrollView
                        mListView.customScrollBy((int) -delta);
                    } else if (mWebView.canScrollVertically(DIRECT_BOTTOM) && isAtBottom) {//因为WebView下滑，导致WebView可以继续上滑，故要先让WebView滑到底部，再滑动MyScrollView
                        mWebView.customScrollBy(-(int) delta);
                    } else {//当ListView处在顶部，WebView处在底部时，滑动MyScrollView
                        customScrollBy(dy);
                    }
                } else {//dy==0代表是滑动到顶部或者底部了
                    if (delta < 0 && isAtTop) {//ListView上滑操作。。。。代表是向上滑，应该让ListView跟着向上滑
                        mListView.customScrollBy((int) -delta);
                    } else if (delta > 0 && isAtBottom) {//向下滑，让WebView跟着向下滑
                        mWebView.customScrollBy(-(int) delta);
                    }
                }
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000);
                int initialVelocity = (int) velocityTracker.getYVelocity(0);
                if ((Math.abs(initialVelocity) > mMinimumVelocity)) {
                    if (mListView.canScrollVertically(DIRECT_TOP) && isAtTop) {//因为ListView可以继续下滑，故先让丫的处理fling事件
                        mListView.startFling(-initialVelocity);
                    } else if (mWebView.canScrollVertically(DIRECT_BOTTOM) && isAtBottom) {//因为WebView可以继续上滑，故让丫的处理fling事件
                        mWebView.startFling(-initialVelocity);
                    } else {//上面两个没有处理fling事件，才轮到MyScrollView去处理
                        fling(-initialVelocity);
                    }
                }
                releaseVelocityTracker();
                break;
            case MotionEvent.ACTION_CANCEL:
                releaseVelocityTracker();
                break;
        }
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int childCount = getChildCount();
        if (childCount < 2) {
            return false;
        }
        if (!touchInView((View) mWebView, ev) && !touchInView((View) mListView, ev)) {
            return false;
        }
        LogE("onInterceptTouchEvent.getScrollY=" + getScrollY() + ",mWebHeight=" + mWebHeight + ",mScroller.isFinished()=" + mScroller.isFinished());
        boolean isCanScrollBottom = getScrollY() < mWebHeight && mScroller.isFinished();//是否可以向下滑
        boolean isCanScrollTop = getScrollY() > 0 && mScroller.isFinished();//是否可以向上滑
        final int action = ev.getAction();
        acquireVelocityTracker(ev);
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                int y = (int) ev.getY();
                mLastY = y;
                // 在Fling状态下点击屏幕
                mIsBeingDragged = !mScroller.isFinished();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int y = (int) ev.getY();
                int deltaY = (int) (y - mLastY);
                int distance = Math.abs(deltaY);
                LogE("onInterceptTouchEvent.Move.......deltaY=" + deltaY + ",mTouchSlop=" + mTouchSlop);
                if (distance > mTouchSlop) {
                    if (deltaY < 0) { // Scroll To Bottom
                        if (touchInView((View) mWebView, ev)) {
                            // 第一个View不可以继续向下滚动，否则由这个View自己处理View内的滚动
                            LogE("onInterceptTouchEvent.Move.......触摸点在第一个View...isCanScrollBottom=" + isCanScrollBottom);
                            if (!mWebView.canScrollVertically(DIRECT_BOTTOM)) {
                                if (isCanScrollBottom) {
                                    mLastY = (int) ev.getY();
                                    mIsBeingDragged = true;
                                }
                            }
                        } else if (touchInView((View) mListView, ev)) { // 触摸点在第二个View
                            LogE("onInterceptTouchEvent.Move.......触摸点在第二个View...isCanScrollBottom1=" + isCanScrollBottom);
                            if (isCanScrollBottom) {
                                mIsBeingDragged = true;
                            }
                        } else {
                            mIsBeingDragged = false;
                            mLastY = y;
                        }
                    } else if (deltaY > 0) { // Scroll To Top
                        if (touchInView((View) mWebView, ev)) {
                            LogE("onInterceptTouchEvent.Move.......触摸点在第一个View...isCanScrollTop=" + isCanScrollTop);
                            if (isCanScrollTop) {
                                mIsBeingDragged = true;
                            }
                        } else if (touchInView((View) mListView, ev)) {
                            LogE("onInterceptTouchEvent.Move.......触摸点在第二个View...isCanScrollTop=" + isCanScrollTop);
                            if (!mListView.canScrollVertically(DIRECT_TOP)) {
                                if (isCanScrollTop) {
                                    mLastY = y;
                                    mIsBeingDragged = true;
                                }
                            }
                        } else {
                            mIsBeingDragged = false;
                        }
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                mIsBeingDragged = false;
                releaseVelocityTracker();
                break;
            }
        }
        LogE("onInterceptTouchEvent.Move.......mIsBeingDragged...." + mIsBeingDragged + "," + ev.getAction());
        return mIsBeingDragged;
    }

    public void customScrollBy(int dy) {
        int oldY = getScrollY();
        scrollBy(0, dy);
//        LogE(TAG + ".customScrollBy.......oldY=" + oldY + ",getScrollY()=" + getScrollY());
        onScrollChanged(getScrollX(), getScrollY(), getScrollX(), oldY);
    }

    private boolean touchInView(View child, MotionEvent ev) {
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        final int scrollY = getScrollY();
        return !(y < child.getTop() - scrollY
                || y >= child.getBottom() - scrollY
                || x < child.getLeft()
                || x >= child.getRight());
    }

    public int adjustScrollY(int delta) {
        int dy = 0;
        int distance = Math.abs(delta);
        if (delta > 0) { // Scroll To Bottom
            View second = getChildAt(1);
            if (second != null) {
                int max = second.getTop() - getScrollY(); // 最多滚动到第二个View的顶部和Container顶部对齐
                max = Math.min(max, second.getBottom() - getScrollY() - getBottom()); // 最多滚动到第二个View的底部和Container对齐
                dy = Math.min(max, distance);
            }
        } else if (delta < 0) { // Scroll To Top
            dy = -Math.min(distance, getScrollY());
        }
        return dy;
    }

    public void fling(int velocity) {
        LogE("fling...." + velocity + ",mScroller.isFinished()=" + mScroller.isFinished());
        if (!mScroller.isFinished())
            return;
        int minY = -mWebView.getActualHeight();
        mScroller.fling(getScrollX(), getScrollY(), 0, velocity, 0, 0, minY, computeVerticalScrollRange());
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    public void computeScroll() {
        if (!mScroller.computeScrollOffset()) {
            super.computeScroll();
            return;
        }
        int oldX = getScrollX();
        int oldY = getScrollY();
        int currX = mScroller.getCurrX();
        int currY = mScroller.getCurrY();
        int curVelocity = getCappedCurVelocity();
        LogE("computeScroll...oldX=" + oldX + ",oldY=" + oldY + ",currX=" + currX + ",currY=" + currY + ",curVelocity=" + curVelocity);
        if (currY <= oldY || oldY < mWebHeight) {
            if (currY < oldY && oldY <= 0) {
                if (curVelocity != 0) {
                    LogE("webView start fling:" + (-curVelocity));
                    this.mScroller.forceFinished(true);
                    this.mWebView.startFling(-curVelocity);
                    return;
                }
            }
        } else if (currY > oldY && oldY >= mWebHeight && curVelocity != 0 && mListView.startFling(curVelocity)) {
            LogE("listView start fling:" + (-curVelocity));
            mScroller.forceFinished(true);
            return;
        }
        int dy = Math.max(0, Math.min(currY, mWebHeight));
        if (oldX != currX || oldY != currY) {
            scrollTo(currX, dy);
        }
        if (!awakenScrollBars()) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
        super.computeScroll();
    }

    @Override
    protected int computeVerticalScrollRange() {
        int webScrollRange = mWebView.computeVerticalScrollRange();
        int listScrollRange = mListView.computeVerticalScrollRange();
        return webScrollRange + listScrollRange;
    }

    private int getCappedCurVelocity() {
        return (int) this.mScroller.getCurrVelocity();
    }

    /**
     * @param event 向VelocityTracker添加MotionEvent
     * @see VelocityTracker#obtain()
     * @see VelocityTracker#addMovement(MotionEvent)
     */
    private void acquireVelocityTracker(final MotionEvent event) {
        if (null == mVelocityTracker) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    public static void LogE(String content) {
        if (isDebug) {
            Log.e(TAG, content);
        }
    }

    public static void setDebug(boolean debug) {
        isDebug = debug;
    }
}
