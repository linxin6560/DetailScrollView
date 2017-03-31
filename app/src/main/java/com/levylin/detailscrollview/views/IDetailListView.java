package com.levylin.detailscrollview.views;

/**
 * Created by LinXin on 2017/3/31.
 */
public interface IDetailListView {

    void setScrollView(DetailScrollView scrollView);

    boolean canScrollVertically(int direction);

    void customScrollBy(int dy);

    boolean startFling(int vy);

    int computeVerticalScrollRange();
}
