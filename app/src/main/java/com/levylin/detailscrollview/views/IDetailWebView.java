package com.levylin.detailscrollview.views;

/**
 * Created by LinXin on 2017/3/31.
 */
public interface IDetailWebView {

    void setScrollView(DetailScrollView scrollView);

    boolean canScrollVertically(int direction);

    void customScrollBy(int dy);

    void startFling(int vy);

    int getActualHeight();

    int computeVerticalScrollRange();
}
