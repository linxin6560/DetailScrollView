package com.levylin.detailscrollview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.levylin.detailscrollview.views.DetailScrollView;
import com.levylin.detailscrollview.views.DetailX5WebView;

import java.util.ArrayList;
import java.util.List;

public class X5WebViewRecyclerViewActivity extends AppCompatActivity implements View.OnClickListener {

    private DetailScrollView mScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_x5webview_recyclerview);
        mScrollView = (DetailScrollView) findViewById(R.id.test_sv);
        findViewById(R.id.move_to_list).setOnClickListener(this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.test_lv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add("测试:" + i);
        }
        recyclerView.setAdapter(new RecyclerViewAdapter(list));

        String url = "http://m.leju.com/tg/toutiao/info.html?city=xm&id=6253374704485472431&source=ttsy&source_ext=ttsy";
        DetailX5WebView webView = (DetailX5WebView) findViewById(R.id.test_webview);
        com.tencent.smtt.sdk.WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new com.tencent.smtt.sdk.WebViewClient() {
            public boolean shouldOverrideUrlLoading(com.tencent.smtt.sdk.WebView view, String url) { //  重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
                view.loadUrl(url);
                return true;
            }
        });
        webView.loadUrl(url);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.move_to_list:
                mScrollView.toggleScrollToListView();
                break;
        }
    }
}
