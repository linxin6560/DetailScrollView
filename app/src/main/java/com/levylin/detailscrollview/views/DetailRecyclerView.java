package com.levylin.detailscrollview.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.levylin.detailscrollview.views.helper.ListViewHelper;

/**
 * Created by LinXin on 2017/3/31.
 */
public class DetailRecyclerView extends RecyclerView implements IDetailListView {

    private ListViewHelper mHelper;

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
        mHelper = new ListViewHelper(scrollView, this);
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
        return fling(0, vy);
    }
}
