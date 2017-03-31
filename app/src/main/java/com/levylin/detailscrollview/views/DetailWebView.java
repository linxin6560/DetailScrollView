package com.levylin.detailscrollview.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

import com.levylin.detailscrollview.views.helper.WebViewHelper;

public class DetailWebView extends WebView implements IDetailWebView {

    private WebViewHelper mHelper;

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
        mHelper = new WebViewHelper(scrollView, this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return !(mHelper != null && !mHelper.onTouchEvent(ev)) && super.onTouchEvent(ev);
    }

    @Override
    public void customScrollBy(int dy) {
        scrollBy(0, dy);
    }

    @Override
    public void startFling(int vy) {
        flingScroll(0, vy);
    }

    @Override
    public int getActualHeight() {
        return (int) (getContentHeight() * getScale());
    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        if (mHelper != null) {
            mHelper.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
        }
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
    }

    @Override
    public int computeVerticalScrollRange() {
        return super.computeVerticalScrollRange();
    }
}

