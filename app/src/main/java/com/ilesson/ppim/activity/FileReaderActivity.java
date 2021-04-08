package com.ilesson.ppim.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.ilesson.ppim.R;
import com.tencent.smtt.sdk.TbsReaderView;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.io.File;

@ContentView(R.layout.act_readfile)
public class FileReaderActivity extends BaseActivity {
//    @ViewInject(R.id.readview)
//    private TbsReaderView readerView;
    @ViewInject(R.id.layout)
    private FrameLayout layout;
    public static final String FILE_PATH="file_path";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String path = getIntent().getStringExtra(FILE_PATH);
        openFile(path);
    }
    private void openFile(String path) {
        TbsReaderView readerView = new TbsReaderView(this, new TbsReaderView.ReaderCallback() {
            @Override
            public void onCallBackAction(Integer integer, Object o, Object o1) {

            }
        });
        layout.addView(readerView);
        //通过bundle把文件传给x5,打开的事情交由x5处理
        Bundle bundle = new Bundle();
        //传递文件路径
        bundle.putString(TbsReaderView.KEY_FILE_PATH, path);
        //加载插件保存的路径
        bundle.putString(TbsReaderView.KEY_TEMP_PATH, Environment.getExternalStorageDirectory() + File.separator + "temp");
        //加载文件前的初始化工作,加载支持不同格式的插件
        boolean b = readerView.preOpen(getFileType(path), false);
        if (b) {
            readerView.openFile(bundle);
        }else{
            Toast.makeText(this,"不支持",Toast.LENGTH_LONG).show();
        }
    }
    private String parseFormat(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
    private void displayFile(String path) {
        TbsReaderView readerView = new TbsReaderView(this, new TbsReaderView.ReaderCallback() {
            @Override
            public void onCallBackAction(Integer integer, Object o, Object o1) {

            }
        });
        Bundle bundle = new Bundle();
        bundle.putString("filePath", path);
        bundle.putString("tempPath", Environment.getExternalStorageDirectory()
                .getPath());
        boolean result = readerView.preOpen(parseFormat(path), false);
        if (result) {
            readerView.openFile(bundle);
        } else {
//
//            File file = new File(getLocalFile().getPath());
//            if (file.exists()) {
//                Intent openintent = new Intent();
//                openintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                String type = getMIMEType(file);
//                // 设置intent的data和Type属性。
//                openintent.setDataAndType(/* uri */Uri.fromFile(file), type);
//                // 跳转
//                startActivity(openintent);
//                finish();
//            }
        }
        layout.addView(readerView);
    }
    private String getFileType(String path) {
        String str = "";

        if (TextUtils.isEmpty(path)) {
            return str;
        }
        int i = path.lastIndexOf('.');
        if (i <= -1) {
            return str;
        }
        str = path.substring(i + 1);
        return str;
    }
}
