package cn.com.sinosoft.webviewwithuploadimg;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import cn.com.sinosoft.logger.Logger;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.tencent.smtt.sdk.CookieSyncManager;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.os.Process.killProcess;
import static android.os.Process.myPid;

public class MainActivity extends AppCompatActivity {

    AlertDialog.Builder builder;
    AlertDialog dialog = null;//全局的
    public WebView activity_main_web;
    private LinearLayout activity_main_linear;
    public static String URL = "file:///android_asset/test/test.html";//本地文件测试
    InitWebMod initWebMod;
    //所需要申请的权限数组
    public ValueCallback<Uri> uploadFile;
    public ValueCallback<Uri[]> uploadFiles;

    // 修改上传拍照问题------start
    public String cameraFielPath;
    public Uri imageUri;
    public final static int FILECHOOSER_REQUESTCODE = 1;

    // 修改上传拍照问题------end
    private List<String> mPermissionList = new ArrayList<>();
    private final int mRequestCode = 100;//权限请求码
    int flag_qx = 1;
    String[] permissions = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    /**
     * 主界面入口版本23以上，开始判断权限
     */
    private boolean initPermission() {
        mPermissionList.clear();
        //逐个判断是否还有未通过的权限
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) !=
                    PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);//添加还未授予的权限到mPermissionList中

            }
        }
        //申请权限
        if (mPermissionList.size() > 0) {//有权限没有通过，需要申请
            ActivityCompat.requestPermissions(this, permissions, mRequestCode);
            return false;
        } else {
            if (flag_qx == 1) {
                loadWeb();
                flag_qx = 0;
            }
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        if (isSix()) {
            initPermission();
        } else {
            loadWeb();
        }
    }

    private void initView() {
        activity_main_linear = findViewById(R.id.weblayout);//web容器
    }
    /**
     * 权限都有，则添加一个webview
     */
    private void loadWeb() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        activity_main_web = new WebView(this);
        activity_main_web.setWebContentsDebuggingEnabled(true);
        activity_main_web.setLayoutParams(params);
        activity_main_linear.addView(activity_main_web);
        activity_main_web.loadUrl(URL);
        initWebMod = new InitWebMod(this, activity_main_web);
        initWebMod.initWebview();
    }

    /**
     * 权限回调，有少量手机居然内部默认授权了权限，且不走该回调！
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case mRequestCode:
                boolean hasPermissionDismiss = false;//有权限没有通过
                if (mRequestCode == requestCode) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == -1) {
                            hasPermissionDismiss = true;
                            break;
                        }
                    }
                }
                if (hasPermissionDismiss) {//如果存在没有被允许的权限
                    if (checkBYPermission()) {//监测必要权限是否开启
                        if (flag_qx == 1) {
                            loadWeb();
                            flag_qx = 0;
                        }
                    } else {
                        if (!refusePermissionByuser()) {//判断是否用户主动拒绝！
                            Logger.e(flag_qx + "监测必要权限是否开启？？？？+弹窗？？");
                            showDialog_perimission();//用户主动拒绝，弹出弹窗引导客户手动开启权限
                        } else {
                            Logger.e(flag_qx + "监测必要权限是否开启？？？？+请求？？");
                            requestPermission();//用户未主动拒绝，请求权限！
                        }
                    }
                } else {
                    if (flag_qx == 1) {
                        loadWeb();
                        flag_qx = 0;
                    }
                }
                break;
            case REQUEST_CODE_RETURN_TO_LIST://H5页面调用系统拍照，所需权限回调
                if (requestCode == REQUEST_CODE_RETURN_TO_LIST) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                        initWebMod.take();
                    }else {
                        initPermission_camera();
                    }
                }

                break;

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


    }
    /**
     * 检测权限
     * @return
     */
    public boolean checkBYPermission() {
        String[] permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };
        if (ContextCompat.checkSelfPermission(this, permissions[0]) !=
                PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, permissions[1]) !=
                PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }

    }

    /**
     * 用户是否主动拒绝权限
     * @return
     */
    public boolean refusePermissionByuser() {
        String[] permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };
        Logger.e("W=" + ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0]) + "\n"
                + "R=" + ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[1]));
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0]) && ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[1])) {
            return true;//下次再提醒
        } else {
            return false;//不再提醒
        }
    }

    /**
     * 请求权限
     */
    public void requestPermission() {
        String[] permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };
        ActivityCompat.requestPermissions(this, permissions, mRequestCode);

    }

    /**
     * 权限手动进入的弹窗
     */
    private void showDialog_perimission() {
        builder = new AlertDialog.Builder(this);
        if (dialog == null) {
            dialog =
                    builder.setTitle("提示")
                            .setMessage("已禁用必要的存储权限，无法正常运行app，请手动授予.取消将退出app！")
                            .setPositiveButton("设置", (dialog, which) -> {
                                Uri packageURI = Uri.parse("package:" + getPackageName());
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                                startActivity(intent);
                                killProcess(myPid());
                            }).setNegativeButton("取消", (dialog, which) -> {
                        dialog.dismiss();
                        killProcess(myPid());
                    })
                            .setCancelable(false)
                            .create();
        }
        dialog.show();

    }
    private List<String> mPermissionList_face = new ArrayList<>();

    public boolean initPermission_camera() {
        String[] permissions = new String[]{
                Manifest.permission.CAMERA
        };

        mPermissionList_face.clear();//清空已经允许的没有通过的权限
        //逐个判断是否还有未通过的权限
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) !=
                    PackageManager.PERMISSION_GRANTED) {
                mPermissionList_face.add(permissions[i]);//添加还未授予的权限到mPermissionList中
            }
        }
        //申请权限
        if (mPermissionList_face.size() > 0) {//有权限没有通过，需要申请
            ActivityCompat.requestPermissions(this, permissions, 222);
            return false;
        } else {
            return true;
        }
    }



    private final int REQUEST_CODE_RETURN_TO_LIST = 1000;

    /**
     * 判断版本是否23及以上，23以上需要动态获取权限
     *
     * @return
     */
    private boolean isSix() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        } else {
            return true;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (null == uploadFile && null == uploadFiles)
            return;
        //没有返回值时的处理
        if (resultCode != RESULT_OK) {
            //需要回调onReceiveValue方法防止下次无法响应js方法
            if (uploadFiles != null) {
                uploadFiles.onReceiveValue(null);
                uploadFiles = null;
            }
            if (uploadFile != null) {
                uploadFile.onReceiveValue(null);
                uploadFile = null;
            }
            return;
        }
        Uri result = null;
        if (requestCode == FILECHOOSER_REQUESTCODE) {
            if (null != data && null != data.getData()) {
                result = data.getData();
            }
            if (result == null && hasFile(cameraFielPath)) {
                result = imageUri;
            }
            //5.0以上设备的数据处理
            if (uploadFiles != null) {
                uploadFiles.onReceiveValue(new Uri[]{result});
                uploadFiles = null;
            } else if (uploadFile != null) {
                //5.0以下设备的数据处理
                uploadFile.onReceiveValue(result);
                uploadFile = null;
            }
        }

    }

    /**
     * 判断拍照的地址是否存在
     * @param cameraFielPath
     * @return
     */
    private boolean hasFile(String cameraFielPath) {
        //判断文件是否存在
        try {
            File f = new File(cameraFielPath);
            if (!f.exists()) {
                return false;
            }

        } catch (Exception e) {
            return false;
        }

        return true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (activity_main_web != null) {
                if (activity_main_linear != null) {
                    activity_main_linear.removeView(activity_main_web);
                }
                activity_main_web.stopLoading();
                activity_main_web.removeAllViewsInLayout();
                activity_main_web.removeAllViews();
                activity_main_web.setWebViewClient(null);
                CookieSyncManager.getInstance().stopSync();
                activity_main_web.destroy();
                activity_main_web = null;
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            super.onDestroy();
        }
        super.onDestroy();
        killProcess(myPid());//退出
    }

    /**
     * 返回事件处理
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
//        这里一般写一个判断webview能不能goback`````如果能就goback，不能就做其他处理```此处不想写了````

    }
}
