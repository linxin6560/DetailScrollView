package com.levylin.detailscrollview.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.levylin.detailscrollview.views.helper.WebViewHelper;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewCallbackClient;

/**
 * 腾讯X5的WebView内核
 * Created by LinXin on 2017/3/31.
 */
public class DetailX5WebView extends WebView implements IDetailWebView {

    private WebViewHelper mHelper;

    public DetailX5WebView(Context context) {
        super(context);
        init();
    }

    public DetailX5WebView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public DetailX5WebView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init();
    }

    protected void init() {
        getView().setVerticalScrollBarEnabled(false);
        setOverScrollMode(OVER_SCROLL_NEVER);
        setWebViewCallbackClient(new WebViewCallbackClient() {
            @Override
            public boolean onTouchEvent(MotionEvent motionEvent, View view) {
                return super_onTouchEvent(motionEvent);
            }

            @Override
            public boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent, View view) {
                if (mHelper != null) {
                    mHelper.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
                }
                return super_overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
            }

            @Override
            public boolean dispatchTouchEvent(MotionEvent motionEvent, View view) {
                return super_dispatchTouchEvent(motionEvent);
            }

            @Override
            public void computeScroll(View view) {
                super_computeScroll();
            }

            @Override
            public void onOverScrolled(int i, int i1, boolean b, boolean b1, View view) {
                super_onOverScrolled(i, i1, b, b1);
            }

            @Override
            public boolean onInterceptTouchEvent(MotionEvent motionEvent, View view) {
                return super_onInterceptTouchEvent(motionEvent);
            }

            @Override
            public void onScrollChanged(int i, int i1, int i2, int i3, View view) {
                super_onScrollChanged(i, i1, i2, i3);
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return !(mHelper != null && !mHelper.onTouchEvent(ev)) && super.onTouchEvent(ev);
    }

    @Override
    public void setScrollView(DetailScrollView scrollView) {
        mHelper = new WebViewHelper(scrollView, this);
    }

    @Override
    public void customScrollBy(int dy) {
        getView().scrollBy(0, dy);
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
    public boolean canScrollVertically(int direction) {
        return getView().canScrollVertically(direction);
    }
}
