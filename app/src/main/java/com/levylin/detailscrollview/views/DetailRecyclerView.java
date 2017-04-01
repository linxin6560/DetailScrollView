package com.levylin.detailscrollview.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.levylin.detailscrollview.views.helper.ListViewTouchHelper;

/**
 * Created by LinXin on 2017/3/31.
 */
public class DetailRecyclerView extends RecyclerView implements IDetailListView {

    private ListViewTouchHelper mHelper;

    public DetailRecyclerView(Context context) {
        super(context);
    }

    public DetailRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DetailRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setScrollView(DetailScrollView scrollView) {
        mHelper = new ListViewTouchHelper(scrollView, this);
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
    public boolean startFling(int vy) {
        if (getVisibility() == GONE)
            return false;
        return fling(0, vy);
    }
}
