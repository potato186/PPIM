package com.ilesson.ppim.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import static com.tencent.wxop.stat.common.StatConstants.LOG_TAG;

public class RecordingService extends Service {

    private String mFileName;
    private String mFilePath;

    private MediaRecorder mRecorder;

    private long mStartingTimeMillis;
    private long mElapsedMillis;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mFileName = intent.getStringExtra("name");
        startRecording();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mRecorder != null) {
            stopRecording();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // 开始录音
    public void startRecording() {
//        setFileNameAndPath();

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); //录音文件保存的格式，这里保存为 mp4
        mRecorder.setOutputFile(mFileName); // 设置录音文件的保存路径
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioChannels(1);
        // 设置录音文件的清晰度
        mRecorder.setAudioSamplingRate(44100);
        mRecorder.setAudioEncodingBitRate(192000);

        try {
            mRecorder.prepare();
            mRecorder.start();
            mStartingTimeMillis = System.currentTimeMillis();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private static final String TAG = "RecordingService";
    // 设置录音文件的名字和保存路径
//    public void setFileNameAndPath() {
//        File f;
//
//        do {
////            count++;
//            mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
//            mFilePath += "/SoundRecorder/" + mFileName;
//            f = new File(mFilePath);
//        } while (f.exists() && !f.isDirectory());
//    }

    // 停止录音
    public void stopRecording() {
        mRecorder.stop();
        mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis);
        mRecorder.release();

        getSharedPreferences("sp_name_audio", MODE_PRIVATE)
                .edit()
                .putString("audio_path", mFilePath)
                .putLong("elpased", mElapsedMillis)
                .apply();
//        if (mIncrementTimerTask != null) {
//            mIncrementTimerTask.cancel();
//            mIncrementTimerTask = null;
//        }

        mRecorder = null;
    }

}