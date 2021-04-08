package com.ilesson.ppim.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.widget.Toast;

import com.ilesson.ppim.BuildConfig;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by potato on 2017/3/28.
 */

public class AppUtils {
    /**
     * 获取App具体设置
     *
     * @param activity 上下文
     */
    public static void getAppDetailsSettings(Activity activity, int requestCode) {
        getAppDetailsSettings(activity, activity.getPackageName(), requestCode);
    }

    /**
     * 获取App具体设置
     *
     * @param activity     上下文
     * @param packageName 包名
     */
    public static void getAppDetailsSettings(Activity activity, String packageName, int requestCode) {
        if (TextUtils.isEmpty(packageName)) return;
        activity.startActivityForResult(
                getAppDetailsSettingsIntent(packageName), requestCode);
    }

    /**
     * 获取App具体设置的意图
     *
     * @param packageName 包名
     * @return intent
     */
    public static Intent getAppDetailsSettingsIntent(String packageName) {
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setData(Uri.parse("package:" + packageName));
        return intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    /**
     * 通过任务管理器杀死进程
     * 需添加权限 {@code <uses-permission android:name="android.permission.RESTART_PACKAGES"/>}</p>
     *
     * @param context
     */
    public static void restart(Context context) {
        int currentVersion = android.os.Build.VERSION.SDK_INT;
        if (currentVersion > android.os.Build.VERSION_CODES.ECLAIR_MR1) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(startMain);
            System.exit(0);
        } else {// android2.1
            ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
            am.restartPackage(context.getPackageName());
        }
    }
    public static void openFile(Context activity, String url, Uri uri) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        String tp = "";
        if (url.endsWith("ppt")) {
            tp = "application/vnd.ms-powerpoint";
        } else if (url.endsWith("pdf")) {
            tp = "application/pdf";
        } else if (url.endsWith("mp3")) {
            tp = "audio/x-mpeg";
        } else if (url.endsWith("mp4")) {
            tp = "video/mp4";
        } else if (url.endsWith("doc")) {
            tp = "application/msword";
        } else if (url.endsWith("docx")) {
            tp = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (url.endsWith("xls")) {
            tp = "application/vnd.ms-excel";
        } else if (url.endsWith("xlsx")) {
            tp = "application/vnd.ms-excel";
        } else {
            tp = "*/*";
        }
        try{
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                intent.setDataAndType(uri, tp);
            } else {
                Uri contentUri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID+".FileProvider", new File(url));
                intent.setDataAndType(contentUri, tp);
            }
            activity.startActivity(intent);
        }catch (Exception e){
            Toast.makeText(activity,"不能打开此类文件!", Toast.LENGTH_LONG).show();
        }
    }
    public static String getDate(String date){
        SimpleDateFormat format0 = new SimpleDateFormat("yyyy/MM/dd HH:mm EEEE", Locale.CHINA);
        String time = format0.format(Long.valueOf(date));
        return time;
    }
}
