package com.levylin.detailscrollview.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.levylin.detailscrollview.views.helper.ListViewTouchHelper;
import com.levylin.detailscrollview.views.listener.OnScrollBarShowListener;

/**
 * Created by LinXin on 2017/3/31.
 */
public class DetailRecyclerView extends RecyclerView implements IDetailListView {

    private ListViewTouchHelper mHelper;
    private OnScrollBarShowListener mScrollBarShowListener;

    public DetailRecyclerView(Context context) {
        super(context);
    }

    public DetailRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DetailRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setOverScrollMode(OVER_SCROLL_NEVER);
    }

    @Override
    public void setScrollView(DetailScrollView scrollView) {
        mHelper = new ListViewTouchHelper(scrollView, this);
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
    public boolean startFling(int vy) {
        if (getVisibility() == GONE)
            return false;
        return fling(0, vy);
    }

    @Override
    public void setOnScrollBarShowListener(OnScrollBarShowListener listener) {
        mScrollBarShowListener = listener;
    }

    @Override
    public void scrollToFirst() {
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            ((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(0, 0);
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            ((StaggeredGridLayoutManager) layoutManager).scrollToPositionWithOffset(0, 0);
        }
    }

    @Override
    public void onScrolled(int dx, int dy) {
        if (mScrollBarShowListener != null) {
            mScrollBarShowListener.onShow();
        }
    }
}
