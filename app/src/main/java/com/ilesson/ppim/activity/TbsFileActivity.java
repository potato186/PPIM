package com.ilesson.ppim.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ilesson.ppim.R;
import com.ilesson.ppim.utils.AppUtils;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsReaderView;
import com.tencent.smtt.sdk.ValueCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;

/**
 * Created by zyt on 2018/7/11.
 * WebView基础类
 */
//@ContentView(R.layout.act_common_webview)
public class TbsFileActivity extends BaseActivity {
    //    @ViewInject(R.id.rl_root)
    private RelativeLayout rlRoot;
    TbsReaderView tbsReaderView;
    //是否打开文件a
    private boolean isOpenFile;
    private TextView tv_download;
    private TextView titleTv;
    private RelativeLayout rl_tbsView;    //rl_tbsView为装载TbsReaderView的视图
    private ProgressBar progressBar_download;
    private DownloadManager mDownloadManager;
    private long mRequestId;
    private DownloadObserver mDownloadObserver;
    private String mFileUrl = "", mFileName, fileName;
    public static final String FILE_URL = "file_url";
    public static final String FILE_NAME = "file_name";
    public static final String FILE_LOCAL_URL = "file_local_url";
    private String localUrl;
    @SuppressLint("WrongViewCast")
    protected void initView() {
        Intent intent = getIntent();
        mFileUrl = intent.getStringExtra(FILE_URL);
        mFileName = intent.getStringExtra(FILE_NAME);
        localUrl = intent.getStringExtra(FILE_LOCAL_URL);
        tbsReaderView = new TbsReaderView(this, readerCallback);
        rlRoot = findViewById(R.id.rl_root);
        progressBar_download = findViewById(R.id.progressBar_download);
        rlRoot.addView(tbsReaderView, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        titleTv = findViewById(R.id.titletv);
        titleTv.setText(mFileName);
        tv_download = findViewById(R.id.tv_download);
        rl_tbsView = findViewById(R.id.rl_tbsView);
        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if (TextUtils.isEmpty(mFileUrl) && TextUtils.isEmpty(localUrl)) {
            finish();
            return;
        }
        if (!TextUtils.isEmpty(localUrl)) {
            File file = new File(localUrl);
            if (file.exists()) {
                openFile(localUrl);
                return;
            }
        }
        if (isLocalExist()) {
            openFile(null);
        } else {
            if (!mFileUrl.contains("http")) {
                new AlertDialog.Builder(TbsFileActivity.this)
                        .setTitle("温馨提示:")
                        .setMessage("文件的url地址不合法，无法进行下载")
                        .setCancelable(true)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                return;
                            }
                        }).create().show();
            }
            startDownload();
        }
    }

    private static final String TAG = "TbsFileActivity";
    private void openFile(String url) {
        tv_download.setText("打开文件");
        tv_download.setVisibility(View.GONE);
        if(TextUtils.isEmpty(url)){
            url = getLocalFile().getPath();
        }
        if (mFileName.endsWith(".rar") || mFileName.endsWith(".zip") || mFileName.endsWith(".mp3")) {
            Uri  userPickedUri = Uri.fromFile(new File(url));
            AppUtils.openFile(getApplicationContext(), url, userPickedUri);
            finish();
            return;
        }
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("local", "true");
        JSONObject Object = new JSONObject();
        try
        {
            Object.put("pkgName",getApplicationContext().getPackageName());
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        params.put("menuData",Object.toString());
//        int ret = QbSdk.openFileReader(this, url, params, null);
//        QbSdk.getMiniQBVersion(this);
//        finish();
        final String finalUrl = url;
        QbSdk.openFileReader(this, url, params, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String s) {
                Log.d(TAG, "onReceiveValue: "+s);
                
                if(s.contains("Close")){
                    finish();
                }
                open = true;
            }
        });
//        finish();

//        Bundle bundle = new Bundle();
//        bundle.putString("filePath", url);
//        bundle.putString("tempPath", Environment.getExternalStorageDirectory()
//                .getPath());
//        boolean result = tbsReaderView.preOpen(parseFormat(mFileName), false);
//        if (result) {
//            tbsReaderView.openFile(bundle);
//        }
    }
    private boolean open;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            finish();
        }
    };
    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(open){
            Log.d(TAG, "onRestart: open");
//            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }
    private String parseFormat(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    private String parseName(String url) {
        String fileName = null;
        try {
            fileName = url.substring(url.lastIndexOf("/") + 1);
        } finally {
            if (TextUtils.isEmpty(fileName)) {
                fileName = String.valueOf(System.currentTimeMillis());
            }
        }
        return fileName;
    }

    TbsReaderView.ReaderCallback readerCallback = new TbsReaderView.ReaderCallback() {
        @Override
        public void onCallBackAction(Integer integer, Object o, Object o1) {

        }
    };


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void finish() {
        super.finish();
    }


    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        tbsReaderView.onStop();
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
        if (mDownloadObserver != null) {
            getContentResolver().unregisterContentObserver(mDownloadObserver);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        setStatusBarLightMode(this, true);
        setContentView(R.layout.act_common_webview);
//        QbSdk.forceSysWebView();
        initView();
    }

    /**
     * 下载文件
     */
    @SuppressLint("NewApi")
    private void startDownload() {
        mDownloadObserver = new DownloadObserver(new Handler());
        getContentResolver().registerContentObserver(
                Uri.parse("content://downloads/my_downloads"), true,
                mDownloadObserver);

        mDownloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        //将含有中文的url进行encode
        String fileUrl = toUtf8String(mFileUrl);
        try {

            DownloadManager.Request request = new DownloadManager.Request(
                    Uri.parse(fileUrl));
            request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS, mFileName);
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
            mRequestId = mDownloadManager.enqueue(request);
            rl_tbsView.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void queryDownloadStatus() {
        DownloadManager.Query query = new DownloadManager.Query()
                .setFilterById(mRequestId);
        Cursor cursor = null;
        try {
            cursor = mDownloadManager.query(query);
            if (cursor != null && cursor.moveToFirst()) {
                // 已经下载的字节数
                long currentBytes = cursor
                        .getLong(cursor
                                .getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                // 总需下载的字节数
                long totalBytes = cursor
                        .getLong(cursor
                                .getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                // 状态所在的列索引
                int status = cursor.getInt(cursor
                        .getColumnIndex(DownloadManager.COLUMN_STATUS));
                tv_download.setText("下载中...(" + formatKMGByBytes(currentBytes)
                        + "/" + formatKMGByBytes(totalBytes) + ")");
                // 将当前下载的字节数转化为进度位置
                int progress = (int) ((currentBytes * 1.0) / totalBytes * 100);
                progressBar_download.setProgress(progress);

                Log.i("downloadUpdate: ", currentBytes + " " + totalBytes + " "
                        + status + " " + progress);
                if (DownloadManager.STATUS_SUCCESSFUL == status
                        && tv_download.getVisibility() == View.VISIBLE) {
                    tv_download.setVisibility(View.GONE);
                    rl_tbsView.setVisibility(View.GONE);
                    tv_download.performClick();
                    if (isLocalExist()) {
                        tv_download.setVisibility(View.GONE);
                        openFile(null);
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private boolean isLocalExist() {
        return getLocalFile().exists();
    }

    private File getLocalFile() {
        return new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                mFileName);
    }


    @SuppressLint("Override")
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private class DownloadObserver extends ContentObserver {

        private DownloadObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            queryDownloadStatus();
        }
    }

    /**
     * 将字节数转换为KB、MB、GB
     *
     * @param size 字节大小
     * @return
     */
    private String formatKMGByBytes(long size) {
        StringBuffer bytes = new StringBuffer();
        DecimalFormat format = new DecimalFormat("###.00");
        if (size >= 1024 * 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0 * 1024.0));
            bytes.append(format.format(i)).append("GB");
        } else if (size >= 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0));
            bytes.append(format.format(i)).append("MB");
        } else if (size >= 1024) {
            double i = (size / (1024.0));
            bytes.append(format.format(i)).append("KB");
        } else if (size < 1024) {
            if (size <= 0) {
                bytes.append("0B");
            } else {
                bytes.append((int) size).append("B");
            }
        }
        return bytes.toString();
    }

    /**
     * 将url进行encode，解决部分手机无法下载含有中文url的文件的问题（如OPPO R9）
     *
     * @param url
     * @return
     * @author xch
     */
    private String toUtf8String(String url) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < url.length(); i++) {
            char c = url.charAt(i);
            if (c >= 0 && c <= 255) {
                sb.append(c);
            } else {
                byte[] b;
                try {
                    b = String.valueOf(c).getBytes("utf-8");
                } catch (Exception ex) {
                    System.out.println(ex);
                    b = new byte[0];
                }
                for (int j = 0; j < b.length; j++) {
                    int k = b[j];
                    if (k < 0)
                        k += 256;
                    sb.append("%" + Integer.toHexString(k).toUpperCase());
                }
            }
        }
        return sb.toString();
    }

}
