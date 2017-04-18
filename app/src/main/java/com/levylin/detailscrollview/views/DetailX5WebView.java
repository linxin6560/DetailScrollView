package com.levylin.detailscrollview.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.levylin.detailscrollview.views.helper.WebViewTouchHelper;
import com.levylin.detailscrollview.views.listener.OnScrollBarShowListener;
import com.tencent.smtt.sdk.WebViewCallbackClient;

/**
 * 腾讯X5的WebView内核
 * Created by LinXin on 2017/3/31.
 */
public class DetailX5WebView extends X5WebView implements IDetailWebView {

    private WebViewTouchHelper mHelper;
    private OnScrollBarShowListener mScrollBarShowListener;

    public DetailX5WebView(Context context) {
        super(context);
        init();
    }

    public DetailX5WebView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    protected void init() {
        getView().setVerticalScrollBarEnabled(false);
        getView().setOverScrollMode(OVER_SCROLL_NEVER);
        if (getX5WebViewExtension() == null) {
            initSystemCoreWebView();
        }
    }

    /**
     * 初始化系统浏览器
     */
    private void initSystemCoreWebView() {
        setWebViewCallbackClient(new WebViewCallbackClient() {
            @Override
            public boolean onTouchEvent(MotionEvent ev, View view) {
                return super_onTouchEvent(ev);
            }

            @Override
            public boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent, View view) {
                //系统内核显示进度条和计算速度走这边
                boolean b = super_overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
                if (mScrollBarShowListener != null) {
                    mScrollBarShowListener.onShow();
                }
                if (mHelper != null) {
                    mHelper.overScrollBy(deltaY, scrollY, scrollRangeY);
                }
                return b;
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
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        //X5内核的显示进度条和计算速度走这边
        if (mScrollBarShowListener != null) {
            mScrollBarShowListener.onShow();
        }
        int deltaY = t - oldt;
        int height = getHeight() - getPaddingTop() - getPaddingBottom();
        if (mHelper != null) {
            mHelper.overScrollBy(deltaY, oldt, computeVerticalScrollRange() - height);//计算速度
        }
    }

    @Override
    public void setScrollView(DetailScrollView scrollView) {
        mHelper = new WebViewTouchHelper(scrollView, this);
    }

    @Override
    public void customScrollBy(int dy) {
        getView().scrollBy(0, dy);
    }

    @Override
    public void customScrollTo(int toY) {
        getView().scrollTo(0, toY);
    }

    @Override
    public void startFling(int vy) {
        DetailScrollView.LogE("DetailX5WebView...startFling:" + (-vy));
        flingScroll(0, vy);
    }

    @Override
    public int customGetContentHeight() {
        return (int) (getContentHeight() * getScale());
    }

    @Override
    public int customGetWebScrollY() {
        return getWebScrollY();
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
    public boolean canScrollVertically(int direction) {
        return getView().canScrollVertically(direction);
    }
}
