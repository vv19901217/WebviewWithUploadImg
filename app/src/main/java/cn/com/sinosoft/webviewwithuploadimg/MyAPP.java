package cn.com.sinosoft.webviewwithuploadimg;

import android.app.Application;

import com.tencent.smtt.sdk.QbSdk;

import cn.com.sinosoft.logger.Logger;

public class MyAPP extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        initX5();
    }

    private void initX5() {
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {

            @Override
            public void onViewInitFinished(boolean arg0) {
                // TODO Auto-generated method stub
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                Logger.e("MYAPP=="+ " onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {
                // TODO Auto-generated method stub
            }
        };
        //x5内核初始化接口
        QbSdk.initX5Environment(getApplicationContext(),  cb);
    }
}
