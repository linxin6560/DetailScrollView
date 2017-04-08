package com.levylin.detailscrollview;

import android.app.Application;

import com.tencent.smtt.sdk.QbSdk;

/**
 * Created by LinXin on 2017/3/23.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        QbSdk.initX5Environment(getApplicationContext(), new QbSdk.PreInitCallback() {
            @Override
            public void onCoreInitFinished() {
                System.out.println("111111111111111111111111111111111111111111111111111111onCoreInitFinished....");
            }

            @Override
            public void onViewInitFinished(boolean b) {
                System.out.println("11111111111111111111111111111111111111111111111111111111onViewInitFinished...." + b);
            }
        });
    }
}
