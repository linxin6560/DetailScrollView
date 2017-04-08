package com.levylin.detailscrollview.views;

import com.levylin.detailscrollview.views.listener.OnScrollBarShowListener;

/**
 * Created by LinXin on 2017/3/31.
 */
public interface IDetailWebView {

    void setScrollView(DetailScrollView scrollView);

    boolean canScrollVertically(int direction);

    void customScrollBy(int dy);

    void customScrollTo(int toY);

    int customGetContentHeight();

    int customGetWebScrollY();

    int customComputeVerticalScrollRange();

    void startFling(int vy);

    void setOnScrollBarShowListener(OnScrollBarShowListener listener);
}
