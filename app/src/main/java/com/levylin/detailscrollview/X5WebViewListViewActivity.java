package com.levylin.detailscrollview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.levylin.detailscrollview.views.DetailX5WebView;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.util.ArrayList;
import java.util.List;

public class X5WebViewListViewActivity extends AppCompatActivity implements View.OnClickListener {

    private List<String> list;
    private ArrayAdapter<String> adapter;
    private String longUrl = "http://m.leju.com/tg/toutiao/info.html?city=xm&id=6253374704485472431&source=ttsy&source_ext=ttsy";
    private String shortUrl = "file:///android_asset/test";
    private DetailX5WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_x5webview_listview);
        ListView listView = (ListView) findViewById(R.id.test_lv);
        list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add("测试:" + i);
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, list);
        listView.setAdapter(adapter);


        webView = (DetailX5WebView) findViewById(R.id.test_webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) { //  重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
                view.loadUrl(url);
                return true;
            }

        });
        webView.loadUrl(longUrl);

        findViewById(R.id.webview_content_long_btn).setOnClickListener(this);
        findViewById(R.id.webview_content_short_btn).setOnClickListener(this);
        findViewById(R.id.listview_content_long_btn).setOnClickListener(this);
        findViewById(R.id.listview_content_short_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.webview_content_long_btn:
                webView.loadUrl(longUrl);
                break;
            case R.id.webview_content_short_btn:
                webView.loadUrl(shortUrl);
                break;
            case R.id.listview_content_long_btn:
                list.clear();
                for (int i = 0; i < 20; i++) {
                    list.add("测试:" + i);
                }
                adapter.notifyDataSetChanged();
                break;
            case R.id.listview_content_short_btn:
                list.clear();
                for (int i = 0; i < 5; i++) {
                    list.add("测试:" + i);
                }
                adapter.notifyDataSetChanged();
                break;
        }
    }
}
