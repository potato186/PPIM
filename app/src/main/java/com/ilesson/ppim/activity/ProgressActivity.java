package com.ilesson.ppim.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.DownBack;
import com.ilesson.ppim.entity.DownloadProgressInfo;
import com.ilesson.ppim.entity.StopDown;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import java.text.DecimalFormat;

import io.rong.eventbus.EventBus;


/**
 * Created by potato on 2019/4/9.
 */
@ContentView(R.layout.update_down_progress_dialog)
public class ProgressActivity extends BaseActivity {
    @ViewInject(R.id.update_progress)
    private TextView progressView;
    @ViewInject(R.id.percent)
    private TextView percentView;
    @ViewInject(R.id.pb_progressbar)
    private ProgressBar progressbar;
    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setStatusBarLightMode(this, true);
        EventBus.getDefault().register(this);
    }

    private String totalSize;
    private int progressWidth;
    public void onEventMainThread(DownloadProgressInfo downloadProgressInfo){
        if (downloadProgressInfo.isCancel()||downloadProgressInfo.getProgress()==100) {
            finish();
            return;
        }

        DecimalFormat df = new DecimalFormat( "###################.#");
        if(totalSize==null){
            totalSize = df.format(downloadProgressInfo.getTotal()/1024f/1024f);
        }
        String currentSize = df.format(downloadProgressInfo.getCurrent()/1024f/1024f);
        progressView.setText(String.format(getResources().getString(R.string.down_app_progress),currentSize,totalSize));
//        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) percentView.getLayoutParams();
//        if(progressWidth==0){
//            progressView.post(() -> progressWidth = progressView.getMeasuredWidth());
//        }
//        layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
//        layoutParams.leftMargin= (int) ((double)downloadProgressInfo.getCurrent()*progressWidth/(double)downloadProgressInfo.getTotal());
//        percentView.setLayoutParams(layoutParams);
        percentView.setText(downloadProgressInfo.getProgress()+"%");
        progressbar.setProgress(downloadProgressInfo.getProgress());
    }

    @Event(R.id.update_confim_btn)
    private void update_confim_btn(View view) {
        EventBus.getDefault().post(new DownBack());
        finishDialog();
    }
    @Event(R.id.update_confim_cancel)
    private void update_confim_cancel(View view) {
        EventBus.getDefault().post(new StopDown());
        finishDialog();
    }

    private void finishDialog(){
        finish();
        overridePendingTransition(0, 0);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
