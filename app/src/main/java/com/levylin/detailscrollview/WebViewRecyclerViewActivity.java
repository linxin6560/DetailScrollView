package com.levylin.detailscrollview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.levylin.detailscrollview.views.DetailWebView;

import java.util.ArrayList;
import java.util.List;

public class WebViewRecyclerViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_webview_recyclerview);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.test_lv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add("测试:" + i);
        }
        recyclerView.setAdapter(new RecyclerViewAdapter(list));

        String url = "http://m.leju.com/tg/toutiao/info.html?city=xm&id=6253374704485472431&source=ttsy&source_ext=ttsy";
        DetailWebView webView = (DetailWebView) findViewById(R.id.test_webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) { //  重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
                if (url.contains("meiyou:///"))
                    return true;
                view.loadUrl(url);
                return true;
            }
        });
        webView.loadUrl(url);
    }
}
