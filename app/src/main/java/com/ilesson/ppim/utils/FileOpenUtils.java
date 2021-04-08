package com.ilesson.ppim.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;

import com.ilesson.ppim.BuildConfig;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;

public class FileOpenUtils {
    /**
     * 使用自定义方法打开文件
     */
    public static void openFile(Context activityFrom, Uri uri) {
        Intent intent = new Intent();
        String type = getMimeTypeFromFile(uri.getPath());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //  此处注意替换包名，
//            Uri contentUri = FileProvider.getUriForFile(activityFrom, BuildConfig.APPLICATION_ID+".FileProvider", file);
//            Log.e("file_open", " uri   " + contentUri.getPath());
            intent.setDataAndType(uri, type);
//            intent.setDataAndType(contentUri, "image/*");
        } else {
            intent.setDataAndType(uri, type);//也可使用 Uri.parse("file://"+file.getAbsolutePath());
        }

        //以下设置都不是必须的
        intent.setAction(Intent.ACTION_VIEW);// 系统根据不同的Data类型，通过已注册的对应Application显示匹配的结果。
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//系统会检查当前所有已创建的Task中是否有该要启动的Activity的Task
        //若有，则在该Task上创建Activity；若没有则新建具有该Activity属性的Task，并在该新建的Task上创建Activity。
        intent.addCategory(Intent.CATEGORY_DEFAULT);//按照普通Activity的执行方式执行
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activityFrom.startActivity(intent);
    }

    /**
     * 使用自定义方法获得文件的MIME类型
     */
    public static String getMimeTypeFromFile(String fName) {
        String type = "*/*";
//        String fName = file.getName();
        //获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex > 0) {
            //获取文件的后缀名
            String end = fName.substring(dotIndex, fName.length()).toLowerCase(Locale.getDefault());
            //在MIME和文件类型的匹配表中找到对应的MIME类型。
            HashMap<String, String> map = MyMimeMap.getMimeMap();
            if (!TextUtils.isEmpty(end) && map.keySet().contains(end)) {
                type = map.get(end);
            }
        }
        Log.i("bqt", "我定义的MIME类型为：" + type);
        return type;
    }
}