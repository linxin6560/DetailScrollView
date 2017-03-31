package com.levylin.detailscrollview;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by LinXin on 2017/3/31.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);
        findViewById(R.id.webview_listview_btn).setOnClickListener(this);
        findViewById(R.id.webview_recyclerview_btn).setOnClickListener(this);
        findViewById(R.id.x5webview_listview_btn).setOnClickListener(this);
        findViewById(R.id.x5webview_recyclerview_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.webview_listview_btn:
                intent = new Intent(this,WebViewListViewActivity.class);
                break;
            case R.id.webview_recyclerview_btn:
                intent = new Intent(this,WebViewRecyclerViewActivity.class);
                break;
            case R.id.x5webview_listview_btn:
                intent = new Intent(this,X5WebViewListViewActivity.class);
                break;
            case R.id.x5webview_recyclerview_btn:
                intent = new Intent(this,X5WebViewRecyclerViewActivity.class);
                break;
        }
        if (intent == null)
            return;
        startActivity(intent);
    }
}
