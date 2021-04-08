package com.ilesson.ppim.view;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.tencent.smtt.sdk.TbsReaderView;

import java.io.File;

/**
 *
 * Created by 12457 on 2017/8/29.
 */

public class SuperFileView2 extends FrameLayout {

    private static String TAG = "SuperFileView";
    private TbsReaderView mTbsReaderView;
    private Context context;

    public SuperFileView2(Context context) {
        this(context, null, 0);
    }

    public SuperFileView2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SuperFileView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTbsReaderView = new TbsReaderView(context, new TbsReaderView.ReaderCallback() {
            @Override
            public void onCallBackAction(Integer integer, Object o, Object o1) {

            }
        });
        this.addView(mTbsReaderView, new LinearLayout.LayoutParams(-1, -1));
        this.context = context;
    }


    private OnGetFilePathListener mOnGetFilePathListener;


    public void setOnGetFilePathListener(OnGetFilePathListener mOnGetFilePathListener) {
        this.mOnGetFilePathListener = mOnGetFilePathListener;
    }


    private TbsReaderView getTbsReaderView(Context context) {
        return new TbsReaderView(context, new TbsReaderView.ReaderCallback() {
            @Override
            public void onCallBackAction(Integer integer, Object o, Object o1) {

            }
        });
    }

    public void displayFile(File mFile) {
        if (mFile != null && !TextUtils.isEmpty(mFile.toString())) {
            //增加下面一句解决没有TbsReaderTemp文件夹存在导致加载文件失败
            String bsReaderTemp = Environment.getExternalStorageDirectory()+ File.separator+"TbsReaderTemp";
            File bsReaderTempFile =new File(bsReaderTemp);
            if (!bsReaderTempFile.exists()) {
                Log.d("xxx","准备创建/storage/emulated/0/TbsReaderTemp！！");
                boolean mkdir = bsReaderTempFile.mkdir();
                if(!mkdir){
                    Log.d("xxx","创建/storage/emulated/0/TbsReaderTemp失败！！！！！");
                }
            }

            //加载文件
            Bundle localBundle = new Bundle();
            Log.d("xxx",mFile.toString());
            localBundle.putString("filePath", mFile.toString());

            localBundle.putString("tempPath", bsReaderTemp);

            if (mTbsReaderView == null){
                mTbsReaderView = getTbsReaderView(context.getApplicationContext());
                String fileType = getFileType(mFile.toString());
                boolean bool = mTbsReaderView.preOpen(fileType,false);
                if (bool) {
                    try {
                        mTbsReaderView.openFile(localBundle);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }else {
                String fileType = getFileType(mFile.toString());
                boolean bool = mTbsReaderView.preOpen(fileType,false);
                if (bool) {
                    try {
                        mTbsReaderView.openFile(localBundle);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

        } else {
            Log.d("xxx","文件路径无效！");
        }


    }

    /***
     * 获取文件类型
     *
     * @param paramString
     * @return
     */
    private String getFileType(String paramString) {
        String str = "";

        if (TextUtils.isEmpty(paramString)) {
            Log.d(TAG, "paramString---->null");
            return str;
        }
        Log.d(TAG, "paramString:" + paramString);
        int i = paramString.lastIndexOf('.');
        if (i <= -1) {
            Log.d(TAG, "i <= -1");
            return str;
        }


        str = paramString.substring(i + 1);
        Log.d(TAG, "paramString.substring(i + 1)------>" + str);
        return str;
    }

    public void show() {
        if(mOnGetFilePathListener!=null){
            mOnGetFilePathListener.onGetFilePath(this);
        }
    }

    /***
     * 将获取File路径的工作，“外包”出去
     */
    public interface OnGetFilePathListener {
        void onGetFilePath(SuperFileView2 mSuperFileView2);
    }

    public void onStopDisplay() {
        if (mTbsReaderView != null) {
            mTbsReaderView.onStop();
        }
    }
}