package com.ilesson.ppim.update;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.ilesson.ppim.BuildConfig;
import com.ilesson.ppim.R;
import com.ilesson.ppim.utils.NotificationUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.ilesson.ppim.update.Consts.APK_DOWNLOAD_URL;


public class ApkUpdateService extends IntentService {
    private static final int BUFFER_SIZE = 10 * 1024; // 8k ~ 32K
    private static final String TAG = "ApkUpdateService";
    private String channelId="update";
    private NotificationManager mNotifyManager;
    private Builder mBuilder;

    public ApkUpdateService() {
        super("ApkUpdateService");
    }

//    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onHandleIntent(Intent intent) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            initNotify();
        }else{
            NotificationUtils.showNotification(getString(getApplicationInfo().labelRes),"",0,channelId,10,100);
        }
        String urlStr = intent.getStringExtra(APK_DOWNLOAD_URL).replace("http","https");
        Log.d(TAG, "onHandleIntent: " + urlStr);
        File dir = StorageUtils.getCacheDirectory(this);
        String apkName = urlStr.substring(urlStr.lastIndexOf("/") + 1, urlStr.length());
        final File apkFile = new File(dir, apkName);
        HttpUtils utils = new HttpUtils();
        utils.download(urlStr, apkFile.getAbsolutePath(), true, new RequestCallBack<File>(){


            @Override
            public void onStart() {
                super.onStart();
                System.out.println("开始下载了 ");
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading) {
                super.onLoading(total, current, isUploading);
                int progress = (int) (current * 100L / total);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    updateProgress(progress);
                }else{
                    NotificationUtils.showNotification(getString(getApplicationInfo().labelRes),getString(R.string.download_progress, progress),0,channelId,progress,100);
                }
                System.out.println("正在 下载中  "+progress);
            }

            @Override
            public void onSuccess(ResponseInfo responseInfo) {
                autoInstallApk(ApkUpdateService.this,apkFile);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
//                Notification noti = mBuilder.build();
//                noti.flags = Notification.FLAG_AUTO_CANCEL;
//                mNotifyManager.notify(0, noti);
                }else{
//                NotificationUtils.showNotification(getString(getApplicationInfo().labelRes),"点击安装",0,channelId);
                    NotificationUtils.cancleNotification(0);
                }
                System.out.println("下载成功");
            }


            @Override
            public void onFailure(HttpException error, String msg) {

                System.out.println("下载失败");
            }


        });
        if(true)return;
        InputStream in = null;
        FileOutputStream out = null;
        try {
            URL url = new URL(urlStr);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(false);
            urlConnection.setConnectTimeout(10 * 1000);
            urlConnection.setReadTimeout(10 * 1000);
//			urlConnection.setRequestProperty("Connection", "Keep-Alive");
//			urlConnection.setRequestProperty("Charset", "UTF-8");
//			urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");

            urlConnection.connect();
            long bytetotal = urlConnection.getContentLength();
            long bytesum = 0;
            int byteread = 0;
            in = urlConnection.getInputStream();
//            File dir = StorageUtils.getCacheDirectory(this);
//            String apkName = urlStr.substring(urlStr.lastIndexOf("/") + 1, urlStr.length());
//            File apkFile = new File(dir, apkName);
            out = new FileOutputStream(apkFile);
            byte[] buffer = new byte[BUFFER_SIZE];

            int oldProgress = 0;

            while ((byteread = in.read(buffer)) != -1) {
                bytesum += byteread;
                out.write(buffer, 0, byteread);

                int progress = (int) (bytesum * 100L / bytetotal);
                // 如果进度与之前进度相等，则不更新，如果更新太频繁，否则会造成界面卡顿
                if (progress != oldProgress) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                        updateProgress(progress);
                    }else{
                        NotificationUtils.showNotification(getString(getApplicationInfo().labelRes),this.getString(R.string.download_progress, progress),0,channelId,progress,100);
                    }
                }
                oldProgress = progress;
            }
            autoInstallApk(this,apkFile);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
//                Notification noti = mBuilder.build();
//                noti.flags = Notification.FLAG_AUTO_CANCEL;
//                mNotifyManager.notify(0, noti);
            }else{
//                NotificationUtils.showNotification(getString(getApplicationInfo().labelRes),"点击安装",0,channelId);
                NotificationUtils.cancleNotification(0);
            }

        } catch (Exception e) {
            Log.e(TAG, "download apk file error", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void autoInstallApk(Context context,File file) {
        try {
            String[] args2 = {"chmod", "777", file.getAbsolutePath()};
            Runtime.getRuntime().exec(args2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        } else {
            // 声明需要的临时的权限
            // 第二个参数，即第一步中配置的authorities
            Uri contentUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID+".FileProvider", file);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        }
        context.startActivity(intent);
    }

    private void updateProgress(int progress) {
        //"正在下载:" + progress + "%"
        mBuilder.setContentText(this.getString(R.string.download_progress, progress)).setProgress(100, progress, false);
        //setContentInent如果不设置在4.0+上没有问题，在4.0以下会报异常
        PendingIntent pendingintent = PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setContentIntent(pendingintent);
        mNotifyManager.notify(0, mBuilder.build());
    }

    private void initNotify(){
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new Builder(this);

        String appName = getString(getApplicationInfo().labelRes);
        int icon = getApplicationInfo().icon;
        mBuilder.setContentTitle(appName).setSmallIcon(icon);
    }
}
