package com.levylin.detailscrollview.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

import com.levylin.detailscrollview.views.helper.WebViewTouchHelper;
import com.levylin.detailscrollview.views.listener.OnScrollBarShowListener;

public class DetailWebView extends WebView implements IDetailWebView {

    private WebViewTouchHelper mHelper;
    private OnScrollBarShowListener mScrollBarShowListener;

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
        setVerticalScrollBarEnabled(false);
        setOverScrollMode(OVER_SCROLL_NEVER);
    }

    public void setScrollView(DetailScrollView scrollView) {
        mHelper = new WebViewTouchHelper(scrollView, this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mHelper == null)
            return super.onTouchEvent(ev);
        if (!mHelper.onTouchEvent(ev))
            return false;
        return super.onTouchEvent(ev);
    }

    @Override
    public void customScrollBy(int dy) {
        scrollBy(0, dy);
    }

    @Override
    public void customScrollTo(int toY) {
        scrollTo(0,toY);
    }

    @Override
    public void startFling(int vy) {
        flingScroll(0, vy);
    }

    @Override
    public int customGetContentHeight() {
        return (int) (getContentHeight() * getScale());
    }

    @Override
    public int customGetWebScrollY() {
        return getScrollY();
    }

    @Override
    public int customComputeVerticalScrollRange() {
        return super.computeVerticalScrollRange();
    }

    @Override
    public void setOnScrollBarShowListener(OnScrollBarShowListener listener) {
        mScrollBarShowListener = listener;
    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        if (mScrollBarShowListener != null) {
            mScrollBarShowListener.onShow();
        }
        if (mHelper != null) {
            mHelper.overScrollBy(deltaY, scrollY, scrollRangeY);
        }
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
    }
}

