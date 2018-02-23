package com.levylin.detailscrollview.views;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.OverScroller;

import com.levylin.detailscrollview.views.helper.ListViewTouchHelper;
import com.levylin.detailscrollview.views.listener.OnScrollBarShowListener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by LinXin on 2017/3/23.
 */
public class DetailListView extends ListView implements IDetailListView, AbsListView.OnScrollListener {

    private Method reportScrollStateChangeMethod;
    private Method startMethod;
    private Object mFlingRunnable;
    private OverScroller mScroller;
    private ListViewTouchHelper mHelper;
    private OnScrollBarShowListener mScrollBarShowListener;

    public DetailListView(Context context) {
        super(context);
        init();
    }

    public DetailListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DetailListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected void init() {
        setOnScrollListener(this);
        setVerticalScrollBarEnabled(false);
        setOverScrollMode(OVER_SCROLL_NEVER);
        setFriction(ViewConfiguration.getScrollFriction());
        try {
            Field field = AbsListView.class.getDeclaredField("mFlingRunnable");
            field.setAccessible(true);
            mFlingRunnable = field.get(this);
            Class<?> clazz = mFlingRunnable.getClass();
            Field scrollerField = clazz.getDeclaredField("mScroller");
            scrollerField.setAccessible(true);
            mScroller = (OverScroller) scrollerField.get(mFlingRunnable);
            startMethod = clazz.getDeclaredMethod("start", Integer.TYPE);
            startMethod.setAccessible(true);
            reportScrollStateChangeMethod = AbsListView.class.getDeclaredMethod("reportScrollStateChange", Integer.TYPE);
            reportScrollStateChangeMethod.setAccessible(true);
        } catch (Throwable t) {
            t.printStackTrace();
            mFlingRunnable = null;
            startMethod = null;
            reportScrollStateChangeMethod = null;
            mScroller = null;
        }
    }

    @Override
    public boolean startFling(int vy) {
        if (getVisibility() == GONE)
            return false;
        if (VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fling(vy);
            return true;
        } else if (reportScrollStateChangeMethod != null && startMethod != null) {
            try {
                reportScrollStateChangeMethod.invoke(this, OnScrollListener.SCROLL_STATE_FLING);
                startMethod.invoke(mFlingRunnable, vy);
                return true;
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void setOnScrollBarShowListener(OnScrollBarShowListener listener) {
        mScrollBarShowListener = listener;
    }

    @Override
    public void scrollToFirst() {
        setSelectionFromTop(0, 0);
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
        smoothScrollBy(dy, 0);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        mHelper.onScrollStateChanged(scrollState == SCROLL_STATE_IDLE);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (mScrollBarShowListener != null) {
            mScrollBarShowListener.onShow();
        }
    }

    /**
     * 获取滑动速度
     *
     * @return
     */
    public int getCurrVelocity() {
        int velocity = 0;
        DetailScrollView.LogE("DetailListView.getCurrVelocity...mScroller=" + mScroller);
        if (mScroller != null) {
            velocity = (int) mScroller.getCurrVelocity();
        }
        DetailScrollView.LogE("DetailListView.getCurrVelocity...velocity=" + velocity);
        return velocity;
    }
}