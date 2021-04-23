package com.ilesson.ppim.update;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ilesson.ppim.BuildConfig;
import com.ilesson.ppim.R;
import com.ilesson.ppim.activity.ProgressActivity;
import com.ilesson.ppim.entity.DownBack;
import com.ilesson.ppim.entity.DownloadProgressInfo;
import com.ilesson.ppim.entity.StopDown;
import com.ilesson.ppim.utils.NotificationUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import java.io.File;
import java.text.DecimalFormat;

import io.rong.eventbus.EventBus;

import static com.ilesson.ppim.update.Consts.APK_DOWNLOAD_URL;


public class ApkUpdateService extends Service {
    private static final int BUFFER_SIZE = 10 * 1024; // 8k ~ 32K
    private static final String TAG = "ApkUpdateService";
    private String channelId="update";
    private NotificationManager mNotifyManager;
    private Builder mBuilder;
    private DownloadProgressInfo downloadProgressInfo;
//    public ApkUpdateService() {
//        super("ApkUpdateService");
//    }
    private boolean launched;
//    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            initNotify();
        }else{
//            NotificationUtils.showNotification(getString(getApplicationInfo().labelRes),"",0,channelId,10,100);
        }
        String urlStr = intent.getStringExtra(APK_DOWNLOAD_URL);
        Log.d(TAG, "onHandleIntent: " + urlStr);
        File dir = StorageUtils.getCacheDirectory(this);
        String apkName = urlStr.substring(urlStr.lastIndexOf("/") + 1, urlStr.length());
        final File apkFile = new File(dir, apkName);
        HttpUtils utils = new HttpUtils();
        RequestCallBack callback = new RequestCallBack<File>(){
            @Override
            public void onStart() {
                super.onStart();
                System.out.println("开始下载了 ");
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading) {
                super.onLoading(total, current, isUploading);
                int progress = (int) (current * 100L / total);
                if(backgroud){
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                        updateProgress(progress);
                    }else{
                        NotificationUtils.showNotification(getString(getApplicationInfo().labelRes),getString(R.string.download_progress, progress),0,channelId,progress,100);
                    }
                }else {
                    if(null==downloadProgressInfo){
                        downloadProgressInfo = new DownloadProgressInfo();
                    }
                    downloadProgressInfo.setCurrent(current);
                    downloadProgressInfo.setTotal(total);
                    downloadProgressInfo.setProgress(progress);
                    if(launched){
                        EventBus.getDefault().post(downloadProgressInfo);
                    }else{
                        launched=true;
                        startActivity(new Intent(ApkUpdateService.this, ProgressActivity.class));
                    }
//                    showDialog(total,current,progress);
                }
                System.out.println("正在 下载中  "+progress);
            }
            @Override
            public void onSuccess(ResponseInfo responseInfo) {
                done(apkFile);
                System.out.println("下载成功");
            }
            @Override
            public void onFailure(HttpException error, String msg) {
                if(msg.equals("maybe the file has downloaded completely")){
                    done(apkFile);
                }
                System.out.println("下载失败");
            }
        };
        httpHandler = utils.download(urlStr, apkFile.getAbsolutePath(), true, callback);
        return flags;
    }
    public void onEventMainThread(StopDown stopDown){
        Log.d(TAG, "onEventMainThread: StopDown");
        httpHandler.cancel();
    }
    public void onEventMainThread(DownBack downBack){
        Log.d(TAG, "onEventMainThread: downBack");
        backgroud =true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
    }

    private HttpHandler<File> httpHandler;
    private AlertDialog dialog;
    private boolean backgroud;
    private TextView progressView;
    private TextView percentView;
    private String totalSize;
    private int progressWidth;
    private void showDialog(long total, long current,int progress){
        if(null==dialog){
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.update_down_progress_dialog,null);
            dialog = new AlertDialog.Builder(this)
                    .setView(view).create();
            progressView =  view.findViewById(R.id.update_progress);
            percentView =  view.findViewById(R.id.percent);
            TextView right_btn =  view.findViewById(R.id.update_confim_btn);
            view.findViewById(R.id.update_confim_cancel).setOnClickListener(v -> {
                httpHandler.cancel();
                dialog.dismiss();
            });
            right_btn.setOnClickListener(v -> {
                backgroud = true;
                dialog.dismiss();
            });
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            dialog.show();
        }
        DecimalFormat df = new DecimalFormat( "###################.#");
        if(totalSize==null){
            totalSize = df.format(total/1024f/1024f);
        }
        String currentSize = df.format(current/1024f/1024f);
        progressView.setText(String.format(getResources().getString(R.string.down_app_progress),currentSize,totalSize));
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) percentView.getLayoutParams();
        if(progressWidth==0){
            progressView.post(() -> progressWidth = progressView.getMeasuredWidth());
        }
        layoutParams.leftMargin= (int) ((double)current*progressWidth/(double)total);
        percentView.setLayoutParams(layoutParams);
        percentView.setText(progress+"%");
    }

    private void done(File apkFile){
        autoInstallApk(ApkUpdateService.this,apkFile);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
//                Notification noti = mBuilder.build();
//                noti.flags = Notification.FLAG_AUTO_CANCEL;
//                mNotifyManager.notify(0, noti);
        }else{
//                NotificationUtils.showNotification(getString(getApplicationInfo().labelRes),"点击安装",0,channelId);
            NotificationUtils.cancleNotification(0);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
