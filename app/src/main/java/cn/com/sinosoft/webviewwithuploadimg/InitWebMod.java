package cn.com.sinosoft.webviewwithuploadimg;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.export.external.interfaces.JsPromptResult;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.com.sinosoft.logger.Logger;
import cn.com.sinosoft.webviewwithuploadimg.utils.Eyes;

import static cn.com.sinosoft.webviewwithuploadimg.MainActivity.FILECHOOSER_REQUESTCODE;


/**
 * Created by vtion on 2015 11 25.
 */
@SuppressWarnings("deprecation")
public class InitWebMod {
    private MainActivity mainActivity;
    private WebView webView;


    public InitWebMod(MainActivity mainActivity, WebView webView) {
        this.mainActivity = mainActivity;
        this.webView = webView;
    }

    /**
     * 视频全屏参数
     */
    protected static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    public View customView;
    private FrameLayout fullscreenContainer;
    private IX5WebChromeClient.CustomViewCallback customViewCallback;

    private LZGWebChrome lzgWebChrome;
    public void initWebview() {
        webView.loadUrl(mainActivity.URL);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(false);
        webView.setWebChromeClient(new LZGWebChrome());
        webView.setWebViewClient(new LZGWebClient());

        WebSettings webSetting = webView.getSettings();
        webSetting.setJavaScriptEnabled(true);
        webView.setWebContentsDebuggingEnabled(true);

        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting.setAllowFileAccess(true);
        webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSetting.setSupportZoom(true);
        webSetting.setBuiltInZoomControls(false);
        webSetting.setUseWideViewPort(true);
        webSetting.setSupportMultipleWindows(true);
        webSetting.setAppCacheEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setGeolocationEnabled(true);
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        webSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSetting.setSavePassword(false);
        webSetting.setAllowFileAccessFromFileURLs(true);
        webSetting.setAllowUniversalAccessFromFileURLs(true);
//        webView.addJavascriptInterface(new JsUtils(mainActivity), "WebAppInterface");
        //防止跨域漏洞
        webSetting.setAllowFileAccess(false);
        webSetting.setAllowFileAccessFromFileURLs(false);
        webSetting.setAllowUniversalAccessFromFileURLs(false);

        webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSetting.setCacheMode(WebSettings.LOAD_DEFAULT);
//        webSetting.setDomStorageEnabled(true);// 开启 DOM storage API 功能
        webSetting.setDatabaseEnabled(true);//开启 database storage API 功能
        String cacheDirPath = mainActivity.getFilesDir().getAbsolutePath() + "/webcache";
        File dir=new File(cacheDirPath);
        if(!dir.exists())
            dir.mkdirs();
        webSetting.setDatabasePath(cacheDirPath);
        webSetting.setDefaultTextEncodingName("utf-8");//设置字符编码
        webSetting.setSaveFormData(true);//设置保存表单

        //这种方法在API级别18被弃用。将密码保存在WebView将在未来的版本中不支持。
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            webSetting.setSavePassword(true);//设置保存密码
        }
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



    //禁止用系统浏览器打开
    private class LZGWebClient extends WebViewClient {



        @SuppressLint({"Recycle", "NewApi"})
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Logger.e("访问的url："+url);
            if (!loadingFinished) {
                redirect = true;
            }
            loadingFinished = false;

            // 调用系统默认浏览器处理url
            view.stopLoading();
            view.loadUrl(url);
            return true;
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            loadingFinished = false;


            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }






        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
            Bitmap bitmap = getBitmapFromView(view);
            if(null != bitmap) {
                int pixel = bitmap.getPixel(15, 90);
                //获取颜色
                int a= Color.alpha(pixel);
                int redValue = Color.red(pixel);
                int greenValue = Color.green(pixel);
                int blueValue = Color.blue(pixel);
//                Log.e("RGB", "【颜色值】 #" + Integer.toHexString(pixel).toUpperCase());
                Eyes.setStatusBarColor(mainActivity,  Color.argb(a,redValue,greenValue,blueValue));
                bitmap.recycle();
            }
        }
        /**
         * 获取view的bitmap
         * @param v
         * @return
         */
        public  Bitmap getBitmapFromView(View v)
        {
            Bitmap b = Bitmap.createBitmap(50, 100, Bitmap.Config.RGB_565);
            Canvas c = new Canvas(b);
            v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
            // Draw background
            Drawable bgDrawable = v.getBackground();
            if (bgDrawable != null)
            {
                bgDrawable.draw(c);
            }
            else
            {
                c.drawColor(Color.WHITE);
            }
            // Draw view to canvas
            v.draw(c);
            return b;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (!redirect) {
                loadingFinished = true;
//                closeanime();
                //HIDE LOADING IT HAS FINISHED
            } else {

                redirect = false;
            }
            getCookies(url);

            // TODO Auto-generated method stub
            super.onPageFinished(view, url);
            //系统API在19以上的版本作了兼容。因为4.4以上系统在onPageFinished时再恢复图片加载时,如果存在多张图片引用的是相同的src时，会只有一个image标签得到加载，因而对于这样的系统我们就先直接加载。
            if (!view.getSettings().getLoadsImagesAutomatically()) {
                view.getSettings().setLoadsImagesAutomatically(true);
            }

        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            // TODO Auto-generated method stub
            super.onReceivedError(view, errorCode, description, failingUrl);
            Logger.i("InitWebMod"+"error:" + errorCode + ",description:" + description + ",failingUrl:" + failingUrl);
            view.stopLoading();
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
//            LogUtils.d("TAG", "shouldInterceptRequest-request:" + request);
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
//            LogUtils.d("TAG", "shouldIntercept-Requesturl:" + url);
//            if (!TextUtils.isEmpty(url) && Uri.parse(url).getScheme() != null) {
//                String scheme = Uri.parse(url).getScheme().trim();
//                if (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https")) {
//                    return super.shouldInterceptRequest(view, injectIsParams(url));
//                }
//            }
            return super.shouldInterceptRequest(view, url);
        }
    }


    class LZGWebChrome extends WebChromeClient {

        @Override
        public boolean onCreateWindow(WebView webView, boolean b, boolean b1, Message message) {
//            return super.onCreateWindow(webView, b, b1, message);
            WebView newWebView = new WebView(mainActivity);
            newWebView.setWebViewClient(new WebViewClient(){
                @Override
                public boolean shouldOverrideUrlLoading(WebView webView, String s) {
//                    PDFActivity.naveToDZBD(mainActivity,s,"PDF界面");
                    Logger.e("pdfurl"+s);
                    return true;
                }
            });
            WebView.WebViewTransport transport = (WebView.WebViewTransport) message.obj;
            transport.setWebView(newWebView);
            message.sendToTarget();
            return true;
        }

        @Override
        public void onProgressChanged(final WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
        }

        //扩展支持alert事件
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            Logger.e("onJsConfirm-url:"+url+",message:"+message+",result:"+result);
            AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
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
                        Logger.e("onJsConfirm-url:"+url+",message:"+message+",result:"+result);
            AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
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
                        Logger.e("onJsConfirm-url:"+url+",message:"+message+",result:"+result);
            AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
            builder.setTitle(url).setMessage(message).setPositiveButton("确定", null);
            builder.setCancelable(false);
            builder.setIcon(R.mipmap.ic_launcher);
            AlertDialog dialog = builder.create();
            dialog.show();
            result.confirm();
            return true;
        }
        // For Android 3.0+
        public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType) {
            mainActivity.uploadFile = uploadFile;
            if(mainActivity.initPermission_camera()){
                take();
            }
        }

        // For Android < 3.0
        public void openFileChooser(ValueCallback<Uri> uploadFile) {
           mainActivity.uploadFile = uploadFile;
            if(mainActivity.initPermission_camera()){
                take();
            }
        }

        // For Android  > 4.1.1
        public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture) {
            mainActivity.uploadFile = uploadFile;
            if(mainActivity.initPermission_camera()){
                take();
            }
        }

        // For Android  >= 5.0
        public boolean onShowFileChooser(WebView webView,
                                         ValueCallback<Uri[]> filePathCallback,
                                         FileChooserParams fileChooserParams) {
            mainActivity.uploadFiles = filePathCallback;
            if(mainActivity.initPermission_camera()){
                take();
            }
            return true;

        }
        /***
         * 视频播放相关的方法
         **/
        @Override
        public View getVideoLoadingProgressView() {
            FrameLayout frameLayout = new FrameLayout(mainActivity);
            frameLayout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            return frameLayout;
        }

        @Override
        public void onShowCustomView(View view, IX5WebChromeClient.CustomViewCallback callback) {
            showCustomView(view,callback);
        }

        @Override
        public void onHideCustomView() {
            hideCustomView();
        }
    }



    private void getCookies(String url) {
        CookieManager cookieManager = CookieManager.getInstance();
        String CookieStr = cookieManager.getCookie(url);
    }

    /**
     * 视频播放全屏
     **/
    private void showCustomView(View view, IX5WebChromeClient.CustomViewCallback callback) {
        // if a view already exists then immediately terminate the new one
        if (customView != null) {
            callback.onCustomViewHidden();
            return;
        }

        mainActivity.getWindow().getDecorView();

        FrameLayout decor = (FrameLayout) mainActivity.getWindow().getDecorView();
        fullscreenContainer = new FullscreenHolder(mainActivity);
        fullscreenContainer.addView(view, COVER_SCREEN_PARAMS);
        decor.addView(fullscreenContainer, COVER_SCREEN_PARAMS);
        customView = view;
        setStatusBarVisibility(false);
        customViewCallback = callback;
    }

    /**
     * 隐藏视频全屏
     */
    public void hideCustomView() {
        if (customView == null) {
            return;
        }

        setStatusBarVisibility(true);
        FrameLayout decor = (FrameLayout) mainActivity.getWindow().getDecorView();
        decor.removeView(fullscreenContainer);
        fullscreenContainer = null;
        customView = null;
        customViewCallback.onCustomViewHidden();
        webView.setVisibility(View.VISIBLE);
    }

    /**
     * 全屏容器界面
     */
    static class FullscreenHolder extends FrameLayout {

        public FullscreenHolder(Context ctx) {
            super(ctx);
            setBackgroundColor(ctx.getResources().getColor(android.R.color.black));
        }

        @Override
        public boolean onTouchEvent(MotionEvent evt) {
            return true;
        }
    }

    private void setStatusBarVisibility(boolean visible) {
        int flag = visible ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN;
        mainActivity.getWindow().setFlags(flag, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }


    boolean loadingFinished = true;
    boolean redirect = false;
    public void take() {
        //拍照图片保存位置
        File imageStorageDir = new File(mainActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "LanZhangGui");
        if (!imageStorageDir.exists()) {
            imageStorageDir.mkdirs();
        }
       mainActivity.cameraFielPath = imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        File file = new File(mainActivity.cameraFielPath);
        mainActivity.imageUri = Uri.fromFile(file);        // 指定拍照存储位置的方式调起相机
        //需要显示应用的意图列表，这个list的顺序和选择菜单上的图标顺序是相关的，请注意。
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = mainActivity.getPackageManager();
        //获取手机里所有注册相机接收意图的应用程序，放到意图列表里(无他相机，美颜相机等第三方相机)
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent i = new Intent(captureIntent);
            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            i.setPackage(packageName);
            i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            cameraIntents.add(i);
        }
        //相册选择器
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        //intent选择器
        Intent chooserIntent = Intent.createChooser(i, "选择模式");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));
        mainActivity.startActivityForResult(chooserIntent, FILECHOOSER_REQUESTCODE);
    }

}
