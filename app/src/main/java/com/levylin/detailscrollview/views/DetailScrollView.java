package com.levylin.detailscrollview.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Scroller;

import com.levylin.detailscrollview.R;
import com.levylin.detailscrollview.views.listener.OnScrollBarShowListener;

import java.lang.reflect.Method;

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
    private int mMinimumVelocity;
    private int maxScrollY;
    private boolean mIsBeingDragged;
    private MyScrollBarShowListener listener;

    private int oldScrollY;
    private int oldWebViewScrollY;

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
        setScrollbarFadingEnabled(true);
        setWillNotDraw(false);//awakenScrollBars的时候会调用invalidate，设置这个为false，可以使invalidate调用draw方法，从而达到画进度条
        mScroller = new Scroller(context);
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        listener = new MyScrollBarShowListener();

        try {
            //显示滚动条
            TypedArray a = context.obtainStyledAttributes(R.styleable.View);
            Method method = View.class.getDeclaredMethod("initializeScrollbars", TypedArray.class);
            method.setAccessible(true);
            method.invoke(this, a);
            a.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            mListView.setOnScrollBarShowListener(listener);
        }
        if (mWebView != null) {
            mWebView.setScrollView(this);
            mWebView.setOnScrollBarShowListener(listener);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        final int parentLeft = getPaddingLeft();
        int parentHeight = b - t;
        int lastBottom = getPaddingTop();
        maxScrollY = 0;

        int webHeight = 0;
        int listHeight = 0;
        int otherHeight = 0;

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

                final int width = child.getMeasuredWidth();
                final int height = child.getMeasuredHeight();

                int childLeft = parentLeft + lp.leftMargin;
                int childTop = lastBottom + lp.topMargin;
                child.layout(childLeft, childTop, childLeft + width, childTop + height);
                lastBottom = childTop + height + lp.bottomMargin;
                maxScrollY += lp.topMargin;
                maxScrollY += lp.bottomMargin;
                if (child instanceof IDetailWebView) {
                    webHeight = height;
                } else if (child instanceof IDetailListView) {
                    listHeight = height;
                } else {
                    otherHeight += height;
                }
            }
            if (webHeight + listHeight + otherHeight <= parentHeight) {//总高度小于父容器高度
                maxScrollY = 0;
            } else if (webHeight < parentHeight && listHeight < parentHeight) {//网页高度和列表高度都小于父容器高度
                maxScrollY = webHeight + otherHeight + listHeight - parentHeight;
            } else if (webHeight + otherHeight < parentHeight) {//网页高度+其他高度<父容器高度，列表高度>=父容易高度，MyScrollView最多滑动到列表顶部与父容器的顶部重合
                //所以可滑动的距离为网页高度+其他高度
                maxScrollY = webHeight + otherHeight;
            } else {//其他情况，要让MyScrollView最多滑动到列表底部与父容器底部重合,所以可滑动的距离为列表的高度+其他高度
                maxScrollY = listHeight + otherHeight;
            }
            System.out.println("webHeight=" + webHeight + ",listHeight=" + listHeight + ",otherHeight=" + otherHeight + ",maxScrollY=" + maxScrollY);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float y = event.getY();
        boolean isAtTop = getScrollY() >= maxScrollY;//MyScroll是否在头部
        boolean isAtBottom = getScrollY() <= 0;//MyScroll是否在底部
        acquireVelocityTracker(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {//按下去的时候就要取消动画，在move的时候取消动画就太迟了，会造成已经进入webView滑动事件，同时触发MyScrollView的滚动事件的奇葩bug
                    mScroller.abortAnimation();
                }
                mLastY = y;
                LogE(TAG + ".onTouchEvent.DOWN.......mLastY=" + mLastY);
                break;
            case MotionEvent.ACTION_MOVE:
                float delta = y - mLastY;
                int dy = adjustScrollY((int) -delta);
                LogE(TAG + ".onTouchEvent.Move.......dy=" + dy + ",delta=" + delta + ",y=" + y + ",mLastY=" + mLastY);
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
        boolean isCanScrollBottom = getScrollY() < maxScrollY && mScroller.isFinished();//是否可以向下滑
        boolean isCanScrollTop = getScrollY() > 0 && mScroller.isFinished();//是否可以向上滑
        LogE("onInterceptTouchEvent.getScrollY=" + getScrollY() + ",maxScrollY=" + maxScrollY + ",mScroller.isFinished()=" + mScroller.isFinished() + ",isCanScrollBottom=" + isCanScrollBottom + ",isCanScrollTop=" + isCanScrollTop);
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
                LogE("onInterceptTouchEvent.Move.......deltaY=" + deltaY);
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
                        LogE("onInterceptTouchEvent.Move.......触摸点在第二个View...isCanScrollBottom=" + isCanScrollBottom);
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
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                mIsBeingDragged = false;
                releaseVelocityTracker();
                break;
            }
        }
        LogE("onInterceptTouchEvent........mIsBeingDragged=" + mIsBeingDragged + "," + ev.getAction());
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
        int dy;
        if (delta + getScrollY() >= maxScrollY) {
            dy = maxScrollY - getScrollY();
        } else if (delta + getScrollY() <= 0) {
            dy = 0;
        } else {
            dy = delta;
        }
        LogE(TAG + ".adjustScrollY...finally...dy=" + dy + ",delta=" + delta);
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
        LogE("computeScroll...oldY=" + oldY + ",currY=" + currY + ",maxScrollY=" + maxScrollY + ",curVelocity=" + curVelocity);
        if (currY < oldY && oldY <= 0) {
            if (curVelocity != 0) {
                LogE("computeScroll...webView start fling:" + (-curVelocity));
                this.mScroller.forceFinished(true);
                this.mWebView.startFling(-curVelocity);
                return;
            }
        } else if (currY > oldY && oldY >= maxScrollY && curVelocity != 0 && mListView.startFling(curVelocity)) {
            LogE("computeScroll...listView start fling:" + (-curVelocity));
            mScroller.forceFinished(true);
            return;
        }
        int toY = Math.max(0, Math.min(currY, maxScrollY));
        if (oldX != currX || oldY != currY) {
            LogE("computeScroll...scrollTo.." + toY);
            scrollTo(currX, toY);
        }
        if (!awakenScrollBars()) {//这句一定要执行，否则会导致mScroller永远不会finish，从而导致一些莫名其妙的bug
            ViewCompat.postInvalidateOnAnimation(this);
        }
        super.computeScroll();
    }

    /**
     * 显示区域的高度
     *
     * @return
     */
    @Override
    protected int computeVerticalScrollExtent() {
        try {
            int webExtent = ViewUtils.computeVerticalScrollExtent((View) mWebView);
            int listExtent = ViewUtils.computeVerticalScrollExtent((View) mListView);
            return webExtent + listExtent;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.computeVerticalScrollExtent();
    }

    /**
     * 已经向下滚动的距离，为0时表示已处于顶部。
     *
     * @return
     */
    protected int computeVerticalScrollOffset() {
        try {
            int webOffset = ViewUtils.computeVerticalScrollOffset((View) mWebView);
            int listOffset = ViewUtils.computeVerticalScrollOffset((View) mListView);
            return webOffset + getScrollY() + listOffset;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.computeVerticalScrollOffset();
    }

    /**
     * 整体的高度，注意是整体，包括在显示区域之外的。
     *
     * @return
     */
    @Override
    protected int computeVerticalScrollRange() {
        try {
            int webScrollRange = ViewUtils.computeVerticalScrollRange((View) mWebView);
            int listScrollRange = ViewUtils.computeVerticalScrollRange((View) mListView);
            return webScrollRange + maxScrollY + listScrollRange;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.computeVerticalScrollRange();
    }

    @Override
    public boolean canScrollVertically(int direction) {
        LogE(TAG + ".canScrollVertically.getScrollY()=" + getScrollY() + ",mWebHeight=" + maxScrollY + ",direction=" + direction);
        if (direction > 0) {
            return getScrollY() > 0;
        } else {
            return getScrollY() < maxScrollY;
        }
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

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    /**
     * 跳转到列表区域，如果已经在列表区域，则跳回去，如果滚动动画没有结束，则无响应
     */
    public void scrollToListView() {
        if (!mScroller.isFinished()) {
            return;
        }
        View webView = (View) mWebView;
        int dy;
        int webViewToY;
        int scrollY = getScrollY();
        if (scrollY >= maxScrollY) {
            dy = oldScrollY - maxScrollY;
            webViewToY = oldWebViewScrollY;
        } else {
            dy = maxScrollY - scrollY;
            oldScrollY = scrollY;
            if (webView instanceof DetailX5WebView) {
                oldWebViewScrollY = ((DetailX5WebView) webView).getWebScrollY();
            } else {
                oldWebViewScrollY = webView.getScrollY();
            }
            webViewToY = mWebView.getActualHeight();
        }
        mScroller.startScroll(getScrollX(), getScrollY(), 0, dy);
        if (webView instanceof DetailX5WebView) {
            ((DetailX5WebView) webView).getView().scrollTo(0, webViewToY);
        } else {
            webView.scrollTo(0, webViewToY);
        }
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public static void LogE(String content) {
        if (isDebug) {
            Log.e(TAG, content);
        }
    }

    public static void setDebug(boolean debug) {
        isDebug = debug;
    }

    private class MyScrollBarShowListener implements OnScrollBarShowListener {

        private long mOldTimeMills;

        @Override
        public void onShow() {
            long timeMills = AnimationUtils.currentAnimationTimeMillis();
            if (timeMills - mOldTimeMills > 16) {
                mOldTimeMills = timeMills;
                awakenScrollBars();
            }
        }
    }
}
