package cn.com.sinosoft.webviewwithuploadimg;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.smtt.export.external.interfaces.JsPromptResult;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.CookieSyncManager;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import cn.com.sinosoft.logger.Logger;
import cn.com.sinosoft.webviewwithuploadimg.utils.BASE64Encoder;

public class WIndowOpenActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView name;
    private RelativeLayout btn;
    private LinearLayout weblayout;

    private final static String URLTAG="URLTAG";
    private final static String NAME="TITLENAME";

    WebView webView;
    ProgressBar progressBar;

    private static String nameTitle;
    private static String url;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            initWeb(webView);
        }
    };

    public static void naveToDZBD(Context context,String url,String name){
        Intent intent=new Intent();
        intent.setClass(context,WIndowOpenActivity.class);
        intent.putExtra(URLTAG,url);
        intent.putExtra(NAME,name);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        initView();

    }
    private void initView() {
        name=this.findViewById(R.id.name);
        weblayout=this.findViewById(R.id.weblayout);
        btn=this.findViewById(R.id.btn_back);
        progressBar = findViewById(R.id.progressbar);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        webView = new WebView(getApplicationContext());
        webView.setLayoutParams(params);
        weblayout.addView(webView);
        initdate();
        btn.setOnClickListener(this);
    }

    int flag=0;//0是pdf,1是外链
    private void initdate() {
        url=getIntent().getStringExtra(URLTAG);
        nameTitle=getIntent().getStringExtra(NAME);
        new Thread(() -> getCT(url)).start();

    }

    private void initWeb(WebView webview) {
        Logger.e("加载的地址"+url);
        if (flag==0||flag==2){
            byte[] bytes = null;
            try
            {// 获取以字符编码为utf-8的字符
                bytes = url.getBytes("UTF-8");
            }
            catch (UnsupportedEncodingException e1)
            {
                e1.printStackTrace();
            }
            if (bytes != null)
            {
                url = new BASE64Encoder().encode(bytes);// BASE64转码
                webview.loadUrl("file:///android_asset/pdfjs/web/viewer.html?file="
                        + url);
            }
        }else {
            webview.loadUrl(url);
        }
        webview.setWebViewClient(new LZGWebClient());
        webview.setWebChromeClient(new LZGWebChormClient());
        WebSettings webSetting = webView.getSettings();
        webSetting.setUseWideViewPort(true); //设置webview推荐使用的窗口
        webSetting.setLoadWithOverviewMode(true);//设置加载模式自适应

        webSetting.setJavaScriptEnabled(true);
        webview.setWebContentsDebuggingEnabled(true);
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting.setAllowFileAccess(true);
        webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSetting.setSupportZoom(true);
        webSetting.setBuiltInZoomControls(false);
        webSetting.setUseWideViewPort(true);
        webSetting.setSupportMultipleWindows(true);
        // webSetting.setLoadWithOverviewMode(true);
        webSetting.setAppCacheEnabled(true);
        // webSetting.setDatabaseEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setGeolocationEnabled(true);
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        // webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        // webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSetting.setSavePassword(false);
        webSetting.setAllowFileAccessFromFileURLs(true);
        webSetting.setAllowUniversalAccessFromFileURLs(true);
        //防止跨域漏洞
        webSetting.setAllowFileAccess(false);
        webSetting.setAllowFileAccessFromFileURLs(false);
        webSetting.setAllowUniversalAccessFromFileURLs(false);

        //4.4一下webkit安全问题
        webView.removeJavascriptInterface("searchBoxJavaBridge_");
        webView.removeJavascriptInterface("accessibility");
        webView.removeJavascriptInterface("accessibilityTraversal");

        webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSetting.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSetting.setDomStorageEnabled(true);// 开启 DOM storage API 功能
        webSetting.setDatabaseEnabled(true);//开启 database storage API 功能
        webSetting.setDefaultTextEncodingName("utf-8");//设置字符编码
        webSetting.setSaveFormData(true);//设置保存表单


        //Android 4.2 或更高。API 级别 16 +
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webSetting.setAllowUniversalAccessFromFileURLs(true);
        }
        //告诉WebView先不要自动加载图片，等页面finish后再发起图片加载API19
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webSetting.setLoadsImagesAutomatically(true);
        } else {
            webSetting.setLoadsImagesAutomatically(false);
        }
        //打开chrome调试
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        //兼容5.0以上设备API21
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.setAcceptThirdPartyCookies(webView, true);
            webSetting.setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

    }

    @Override
    public void onClick(View v) {
        finish();
    }

    @Override
    protected void onDestroy() {
        try {
            if (webView != null) {
                if (weblayout != null) {
                    weblayout.removeView(webView);
                }
                webView.stopLoading();
                webView.removeAllViewsInLayout();
                webView.removeAllViews();
                webView.setWebViewClient(null);
                CookieSyncManager.getInstance().stopSync();
                webView.destroy();
                webView = null;
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            super.onDestroy();
        }
//        super.onDestroy();

    }

    private class LZGWebClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String s) {
//            return super.shouldOverrideUrlLoading(webView, s);
            webView.loadUrl(s);
            return true;

        }
    }

    private class LZGWebChormClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView webView, int i) {
            if (i == 100) {
                progressBar.setVisibility(View.GONE);//加载完网页进度条消失
            } else {
                progressBar.setVisibility(View.VISIBLE);//开始加载网页时显示进度条
                progressBar.setProgress(i);//设置进度值
            }
            super.onProgressChanged(webView, i);

        }

        //扩展支持alert事件
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {

            Logger.e("onJsConfirm-url0:"+url+",message:"+message+",result:"+result);
            AlertDialog.Builder builder = new AlertDialog.Builder(WIndowOpenActivity.this);
//            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setTitle(url).setMessage(message).setPositiveButton("确定", null);
            builder.setCancelable(false);
            builder.setIcon(R.mipmap.ic_launcher);
            AlertDialog dialog = builder.create();
            dialog.show();
            result.confirm();
            return true;
        }

        //处理confirm弹出框
        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            Logger.e("onJsConfirm-url1:"+url+",message:"+message+",result:"+result);
            AlertDialog.Builder builder = new AlertDialog.Builder(WIndowOpenActivity.this);
//            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setTitle(url).setMessage(message).setPositiveButton("确定", null);
            builder.setCancelable(false);
            builder.setIcon(R.mipmap.ic_launcher);
            AlertDialog dialog = builder.create();
            dialog.show();
            result.confirm();
            return true;
        }

        //处理prompt弹出框
        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
            Logger.e("onJsConfirm-url2:"+url+",message:"+message+",result:"+result);
            AlertDialog.Builder builder = new AlertDialog.Builder(WIndowOpenActivity.this);
//            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setTitle(url).setMessage(message).setPositiveButton("确定", null);
            builder.setCancelable(false);
            builder.setIcon(R.mipmap.ic_launcher);
            AlertDialog dialog = builder.create();
            dialog.show();
            result.confirm();
            return true;
        }

        @Override
        public void onReceivedTitle(WebView webView, String s) {
            super.onReceivedTitle(webView, s);
            Log.e("加载的名字",s);
            if (flag==0){
                name.setText(nameTitle);
            }else {
                name.setText(s);
            }

        }
    }

    /**
     * 获取url的类型
     * @param stringurl
     * @return
     * @throws IOException
     */
    private  void getCT(String stringurl) {
        String type="";
        URL url = null;
        try {
            url = new URL(stringurl);

        URLConnection conn = url.openConnection();
        type = conn.getHeaderField("Content-Type");
            if (type.contains("pdf")||type.contains("PDF")){
                if (stringurl.substring(stringurl.length() - 4, stringurl.length()).equals(".pdf")) {
                    flag=2;

                } else {
                    flag=0;
                }
            }else {
                flag=1;
            }
            handler.sendEmptyMessage(1);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
