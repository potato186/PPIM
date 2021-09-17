package com.ilesson.ppim.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;


import com.ilesson.ppim.R;
import com.ilesson.ppim.utils.SPUtils;

import org.xutils.x;

import java.util.ArrayList;

import static com.ilesson.ppim.IlessonApp.FONT_INDEX;
import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;
import static com.ilesson.ppim.activity.LoginActivity.USER_PHONE;
import static com.ilesson.ppim.activity.TextSizeShowActivity.FONT_SCALE;

public class BaseActivity extends FragmentActivity {

    protected Context mContext;

    // For Android 6.0
    private PermissionsResultListener mListener;
    //申请标记值
    public static final int REQUEST_CODE_ASK_PERMISSIONS = 100;
    //手动开启权限requestCode
    public static final int SETTINGS_REQUEST_CODE = 200;
    //拒绝权限后是否关闭界面或APP
    private boolean mNeedFinish = false;
    //界面传递过来的权限列表,用于二次申请
    private ArrayList<String> mPermissionsList = new ArrayList<>();
    //必要全选,如果这几个权限没通过的话,就无法使用APP
    protected static final ArrayList<String> FORCE_REQUIRE_PERMISSIONS = new ArrayList<String>() {
        {
            add(Manifest.permission.CAMERA);
            add(Manifest.permission.READ_EXTERNAL_STORAGE);
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            add(Manifest.permission.INTERNET);
            add(Manifest.permission.ACCESS_NETWORK_STATE);
            add(Manifest.permission.RECORD_AUDIO);
            add(Manifest.permission.RECEIVE_SMS);
        }
    };
    public boolean setStatusBarLightMode(Activity activity, boolean isFontColorDark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isFontColorDark) {
                // 沉浸式
                // activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                //非沉浸式
                activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                //非沉浸式
                activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
            return true;
        }
        return false;
    }
    public void initStatusBar() {
        Window window = this.getWindow();
        //获取当前手机SDK版本号大于或等于Build.VERSION_CODES.KITKAT（安卓4.4）时：
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //允许页面可以拉伸到顶部状态栏并且定义顶部状态栏透名
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);//是设置状态栏为全透明。
//            window.setNavigationBarColor(Color.TRANSPARENT); //设置虚拟键为透明
            //设置全屏显示,
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }
    public static final int DIALOG_LONDING = 0x01;
    public static final int DIALOG_PROGRESS = 0x02;
    @Override
    public Dialog onCreateDialog(int id, Bundle args) {
        switch (id) {
            case DIALOG_PROGRESS:
                return makeProgressDialog();
        }
        return super.onCreateDialog(id, args);
    }
    private ProgressDialog progressDialog;
    public void showProgress(){
        if(null==dialog){
            initProgressDialog();
        }
        dialog.show();
    }
    public void hideProgress(){
        if(null!=dialog){
            dialog.dismiss();
            dialog.cancel();
            dialog.hide();
        }
    }
    private Dialog dialog;
    private void initProgressDialog() {
        dialog = new Dialog(this, R.style.dialog_no_background);
        View view = getLayoutInflater().inflate(R.layout.progress_dialog, null);
        dialog.setContentView(view);
        dialog.setCancelable(true);
    }
    public Dialog makeProgressDialog() {
        Dialog dialog = new Dialog(this, R.style.dialog_no_background);
        View view = getLayoutInflater().inflate(R.layout.progress_dialog, null);
        dialog.setContentView(view);
        dialog.setCancelable(true);
        return dialog;
    }
    /**
     * 权限允许或拒绝对话框
     *
     * @param permissions 需要申请的权限
     * @param needFinish  如果必须的权限没有允许的话，是否需要finish当前 Activity
     * @param callback    回调对象
     */
    protected void requestPermission(final ArrayList<String> permissions, final boolean needFinish,
                                     final PermissionsResultListener callback) {
        if (permissions == null || permissions.size() == 0) {
            return;
        }
        mNeedFinish = needFinish;
        mListener = callback;
        mPermissionsList = permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //获取未通过的权限列表
            ArrayList<String> newPermissions = checkEachSelfPermission(permissions);
            if (newPermissions.size() > 0) {// 是否有未通过的权限
                requestEachPermissions(newPermissions.toArray(new String[newPermissions.size()]));
            } else {// 权限已经都申请通过了
                if (mListener != null) {
                    mListener.onPermissionGranted();
                }
            }
        } else {
            if (mListener != null) {
                mListener.onPermissionGranted();
            }
        }
    }

    /**
     * 申请权限前判断是否需要声明
     *
     * @param permissions
     */
    private void requestEachPermissions(String[] permissions) {
        if (shouldShowRequestPermissionRationale(permissions)) {// 需要再次声明
//            showRationaleDialog(permissions);
            ActivityCompat.requestPermissions(BaseActivity.this, permissions,
                    REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            ActivityCompat.requestPermissions(BaseActivity.this, permissions,
                    REQUEST_CODE_ASK_PERMISSIONS);
        }
    }

    /**
     * 弹出声明的 Dialog
     *
     * @param permissions
     */
    private void showRationaleDialog(final String[] permissions) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示")
                .setMessage("为了应用可以正常使用，请您点击确认申请权限。")
                .setPositiveButton("确认",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(BaseActivity.this, permissions,
                                        REQUEST_CODE_ASK_PERMISSIONS);
                            }
                        })
                .setNegativeButton("取消",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
//                                if (mNeedFinish) finish();
                            }
                        })
                .setCancelable(false)
                .show();
    }

    /**
     * 检察每个权限是否申请
     *
     * @param permissions
     * @return newPermissions.size > 0 表示有权限需要申请
     */
    private ArrayList<String> checkEachSelfPermission(ArrayList<String> permissions) {
        ArrayList<String> newPermissions = new ArrayList<String>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                newPermissions.add(permission);
            }
        }
        return newPermissions;
    }

    /**
     * 再次申请权限时，是否需要声明
     *
     * @param permissions
     * @return
     */
    private boolean shouldShowRequestPermissionRationale(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 申请权限结果的回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS && permissions != null) {
            // 获取被拒绝的权限列表
            ArrayList<String> deniedPermissions = new ArrayList<>();
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permission);
                }
            }
            // 判断被拒绝的权限中是否有包含必须具备的权限
            ArrayList<String> forceRequirePermissionsDenied =
                    checkForceRequirePermissionDenied(FORCE_REQUIRE_PERMISSIONS, deniedPermissions);
            if (forceRequirePermissionsDenied != null && forceRequirePermissionsDenied.size() > 0) {
                // 必备的权限被拒绝，
                if (mNeedFinish) {
//                    showToast("请在设置中修改权限");
//                    showPermissionSettingDialog();
                } else {
                    if (mListener != null) {
                        mListener.onPermissionDenied();
                    }
                }
            } else {
                // 不存在必备的权限被拒绝，可以进首页
                if (mListener != null) {
                    mListener.onPermissionGranted();
                }
            }
        }
    }

    /**
     * 检查回调结果
     *
     * @param grantResults
     * @return
     */
    private boolean checkEachPermissionsGranted(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private ArrayList<String> checkForceRequirePermissionDenied(
            ArrayList<String> forceRequirePermissions, ArrayList<String> deniedPermissions) {
        ArrayList<String> forceRequirePermissionsDenied = new ArrayList<>();
        if (forceRequirePermissions != null && forceRequirePermissions.size() > 0
                && deniedPermissions != null && deniedPermissions.size() > 0) {
            for (String forceRequire : forceRequirePermissions) {
                if (deniedPermissions.contains(forceRequire)) {
                    forceRequirePermissionsDenied.add(forceRequire);
                }
            }
        }
        return forceRequirePermissionsDenied;
    }

    public String myPhone;
    public static FragmentActivity currentActivity;
    public void setCurrentActivity(FragmentActivity currentActivity){
        this.currentActivity = currentActivity;
    }

    public FragmentActivity getCurrentActivity() {
        return currentActivity;
    }
    public String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        token = SPUtils.get(LOGIN_TOKEN, "");
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//            StatusBarUtil.setStatusBarColor(this,android.R.color.white);
//        }
        x.view().inject(this);
        mContext = this;
        if(TextUtils.isEmpty(myPhone)){
            myPhone = SPUtils.get(USER_PHONE,"");
        }
//        StatService.setDebugOn(true);
//        StatService.setSendLogStrategy(this, SendStrategyEnum.APP_START, 1, false);
//        StatService.onPageStart(this,"启动");
    }

    @Override
    public void onResume() {
        super.onResume();
//        StatService.onResume(this);
//        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    public void showToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
    public void showToast(int res){
        Toast.makeText(this, getResources().getString(res), Toast.LENGTH_LONG).show();
    }
    public interface PermissionsResultListener {
        void onPermissionGranted();

        void onPermissionDenied();
    }

    //重写字体缩放比例 api<25
    @Override
    public Resources getResources() {
        Resources res =super.getResources();
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
            Configuration config = res.getConfiguration();
            config.fontScale= SPUtils.get(FONT_INDEX, 1.0f)*FONT_SCALE+1;//1 设置正常字体大小的倍数
            res.updateConfiguration(config,res.getDisplayMetrics());
        }
        return res;
    }
    //重写字体缩放比例  api>25
    @Override
    protected void attachBaseContext(Context newBase) {
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.N){
            final Resources res = newBase.getResources();
            final Configuration config = res.getConfiguration();
            config.fontScale=SPUtils.get(FONT_INDEX, 1.0f)*FONT_SCALE+1;
            final Context newContext = newBase.createConfigurationContext(config);
            super.attachBaseContext(newContext);
        }else{
            super.attachBaseContext(newBase);
        }
    }


    public boolean isNeedResetFontSize = true;
//    /**
//     * 重写 getResource 方法，防止系统字体影响
//     */
//    @Override
//    public Resources getResources() {//禁止app字体大小跟随系统字体大小调节
//        Resources resources = super.getResources();
////        if (isNeedResetFontSize) {
////            if (resources != null && resources.getConfiguration().fontScale != 1.0f) {
//                android.content.res.Configuration configuration = resources.getConfiguration();
//                float scale = SPUtils.get(FONT_SCALE, 1.0f);
//                configuration.fontScale = scale;
//                resources.updateConfiguration(configuration, resources.getDisplayMetrics());
////            }
////        }
//        return resources;
//    }
//    /**
//     * 修改字体大小
//     *
//     * @param spKey
//     */
//    public void changeFontSize(String spKey) {
//        float scale = 1.0f;
//        Configuration c = getResources().getConfiguration();
//        if (!TextUtils.isEmpty(spKey)) {
//            scale = SPUtils.get(spKey, 1.0f);
//        }
//        c.fontScale = scale;
////        DisplayMetrics metrics = new DisplayMetrics();
////        getWindowManager().getDefaultDisplay().getMetrics(metrics);
////        metrics.scaledDensity = c.fontScale * metrics.density;
//        getResources().updateConfiguration(c, getResources().getDisplayMetrics());
//    }
}
