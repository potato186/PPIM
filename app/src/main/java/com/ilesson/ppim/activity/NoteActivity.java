package com.ilesson.ppim.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.adapter.NoteAdapter;
import com.ilesson.ppim.entity.NoteContent;
import com.ilesson.ppim.entity.NoteInfo;
import com.ilesson.ppim.service.FavoriteHelper;
import com.ilesson.ppim.service.RecordingService;
import com.ilesson.ppim.utils.Dateuitls;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import java.io.File;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.functions.Consumer;
import io.rong.imkit.activity.FileManagerActivity;
import io.rong.imkit.model.FileInfo;
import io.rong.imkit.plugin.image.PictureSelectorActivity;

import static com.ilesson.ppim.service.FavoriteHelper.TYPE_FILE;
import static com.ilesson.ppim.service.FavoriteHelper.TYPE_IMAGE;
import static com.ilesson.ppim.service.FavoriteHelper.TYPE_LOCATION;
import static com.ilesson.ppim.service.FavoriteHelper.TYPE_NOTE;
import static com.ilesson.ppim.service.FavoriteHelper.TYPE_TEXT;
import static com.ilesson.ppim.service.FavoriteHelper.TYPE_VOICE;


/**
 * Created by potato on 2019/4/9.
 */
@ContentView(R.layout.act_note)
public class NoteActivity extends BaseActivity {
    @ViewInject(R.id.record_layout)
    private View recordLayout;
    @ViewInject(R.id.dot)
    private View dot;
    @ViewInject(R.id.record_time)
    private TextView recordTime;
    @ViewInject(R.id.recylerview)
    private RecyclerView recyclerView;
    private NoteAdapter noteAdapter;
    private List<NoteInfo> resultList = new ArrayList<>();

    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setStatusBarLightMode(this, true);
        noteAdapter = new NoteAdapter(this);
        NoteInfo text = new NoteInfo();
        text.setType(TYPE_TEXT);
        text.setText("");
        resultList.add(text);
        noteAdapter.setResultList(resultList);
//        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        LinearLayoutManager manager = new LinearLayoutManager(this);
//        manager.setStackFromEnd(true);//设置从底部开始，最新添加的item每次都会显示在最下面
        recyclerView.setLayoutManager(manager);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(noteAdapter);
        noteAdapter.setNoteActivity(this);
        noteAdapter.setRecyclerView(recyclerView);
    }

    @Event(R.id.back_btn)
    private void back_btn(View view) {
        exit();
    }
    @Event(R.id.stop)
    private void stop(View view) {
        stopRecord();
    }

    @Event(R.id.image_item)
    private void image_item(View view) {
        getImage();
    }

    private String path;

    @Event(R.id.voice_item)
    private void voice_item(View view) {
        if (recordLayout.getVisibility() == View.VISIBLE) {
            stopRecord();
        } else {
            hideSoftKeyboard();
            recordLayout.setVisibility(View.VISIBLE);
            recordTime.setText("00:00");
            time=0;
            String dirPath = Environment.getExternalStorageDirectory()+"/pp/";
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            path = dirPath + System.currentTimeMillis()+".mp4";
            Intent intent = new Intent(this, RecordingService.class);
            intent.putExtra("name",path);
            startService(intent);
//            AudioRecorder.getInstance().createDefaultAudio(path);
//            AudioRecorder.getInstance().startRecord(null);
            handler.sendEmptyMessageDelayed(RECORD,1000);
        }
    }

    private void stopRecord() {
        handler.removeMessages(RECORD);
        recordLayout.setVisibility(View.GONE);
        stopService(new Intent(this, RecordingService.class));
//        AudioRecorder.getInstance().stopRecord();
        resultList = noteAdapter.getResultList();
        NoteInfo noteVoice = new NoteInfo();
        noteVoice.setUrl(path);
        noteVoice.setTime(time+"");
        noteVoice.setType(TYPE_VOICE);
        noteAdapter.editIndex++;
        resultList.add(noteAdapter.editIndex, noteVoice);
        noteAdapter.notifyItemInserted(noteAdapter.editIndex);
        checkLast();
        handler.sendEmptyMessageDelayed(0, 500);
    }

    @Event(R.id.file_item)
    private void file_item(View view) {
        Intent intent = new Intent(this, FileManagerActivity.class);
        startActivityForResult(intent, 730);
    }

    @Event(R.id.location_item)
    private void location_item(View view) {
        Intent intent = new Intent(NoteActivity.this, MapLocationActivity.class);
        intent.putExtra(MapLocationActivity.NOTE_TYPE, true);
        startActivityForResult(intent, 0);
    }

    public void getImage() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Consumer<Boolean>() {
            public void accept(Boolean aBoolean) {
                Intent intent = new Intent(NoteActivity.this, PictureSelectorActivity.class);
                startActivityForResult(intent, 23);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            resultList = noteAdapter.getResultList();
            if (noteAdapter.editIndex < 1 || noteAdapter.editIndex >= resultList.size()) {
                noteAdapter.editIndex = resultList.size() - 1;
            }
            if (requestCode == 730) {
                HashSet<FileInfo> selectedFileInfos = (HashSet) data.getSerializableExtra("sendSelectedFiles");
                if (null != selectedFileInfos && selectedFileInfos.size() > 0) {
                    Iterator it = selectedFileInfos.iterator();
                    while (it.hasNext()) {
                        FileInfo fileInfo = (FileInfo) it.next();
                        NoteInfo noteFile = new NoteInfo();
                        noteFile.setUrl(fileInfo.getFilePath());
                        noteFile.setSize(fileInfo.getFileSize());
                        noteFile.setName(fileInfo.getFileName());
                        noteFile.setType(TYPE_FILE);
                        noteAdapter.editIndex++;
                        resultList.add(noteAdapter.editIndex, noteFile);
                        noteAdapter.notifyItemInserted(noteAdapter.editIndex);
                    }
                }
                checkLast();
                handler.sendEmptyMessageDelayed(0, 500);
                return;
            }
            if (resultCode == TYPE_LOCATION) {
                NoteInfo note = (NoteInfo) data.getSerializableExtra("location");
                resultList = noteAdapter.getResultList();
                resultList.add(noteAdapter.editIndex, note);
                note.setType(TYPE_LOCATION);
                noteAdapter.editIndex++;
                noteAdapter.notifyItemInserted(noteAdapter.editIndex);
                checkLast();
                handler.sendEmptyMessageDelayed(0, 5000);
                return;
            }
            String mediaList = data.getStringExtra("android.intent.extra.RETURN_RESULT");
            Gson gson = new Gson();
            Type entityType = (new TypeToken<LinkedHashMap<String, Integer>>() {
            }).getType();
            LinkedHashMap<String, Integer> linkedHashMap = (LinkedHashMap) gson.fromJson(mediaList, entityType);
            Iterator it = linkedHashMap.entrySet().iterator();
            resultList = noteAdapter.getResultList();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                NoteInfo noteImage = new NoteInfo();
                noteImage.setUrl((String) entry.getKey());
                noteImage.setType(TYPE_IMAGE);
                noteAdapter.editIndex++;
                resultList.add(noteAdapter.editIndex, noteImage);
                noteAdapter.notifyItemInserted(noteAdapter.editIndex);
            }
            checkLast();
            handler.sendEmptyMessageDelayed(0, 500);
//            noteAdapter.setResultList(resultList);
//            noteAdapter.notifyDataSetChanged();
        }
    }

    private void checkLast() {
        resultList = noteAdapter.getResultList();
        if (resultList.size() > 0) {
            NoteInfo note = resultList.get(resultList.size() - 1);
            if (note.getType()!=TYPE_TEXT) {
                NoteInfo text = new NoteInfo();
                text.setText("");
                text.setType(TYPE_TEXT);
                noteAdapter.editIndex++;
                resultList.add(text);
                noteAdapter.notifyItemInserted(noteAdapter.editIndex);
            }
        }
    }
    private static final int RECORD=1;
    private int time;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==RECORD){
                time++;
                recordTime.setText(Dateuitls.formatSeconds(time));
                handler.sendEmptyMessageDelayed(RECORD,1000);
            }else {
                noteAdapter.itemFocus();
            }
        }
    };
    public Point point = new Point();

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            point.x = (int) ev.getRawX();
            point.y = (int) ev.getRawY();
        }
        return super.dispatchTouchEvent(ev);
    }

    private static final String TAG = "NoteActivity";

    @Override
    public void onBackPressed() {
        exit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == event.getKeyCode()) {
            exit();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void exit() {
        resultList = noteAdapter.getResultList();
        if (resultList.isEmpty()) {
            finish();
            return;
        }
       if (resultList.size() <= 2) {
            boolean empty = true;
            for(NoteInfo noteInfo:resultList){
                if(noteInfo.getType()==TYPE_TEXT){
                    if (!TextUtils.isEmpty(noteInfo.getText())) {
                        empty = false;
                    }
                }else{
                    empty = false;
                }
            }
            if(empty){
                finish();
                return;
            }
        }
        List<File> files = new ArrayList<>();
        for (NoteInfo note : resultList) {
            File file = null;
            if (note.getType()==TYPE_IMAGE||note.getType()==TYPE_FILE||note.getType()==TYPE_VOICE) {
                if (note.getUrl().contains("file:///")) {
                    file = new File(URI.create(note.getUrl()));
                } else {
                    file = new File(note.getUrl());
                }
                note.setUrl(FavoriteHelper.FILE_TAG);
                files.add(file);
            }
        }
        NoteContent content = new NoteContent();
        content.setType(TYPE_NOTE);
        content.setData(resultList);
        new FavoriteHelper().post(new Gson().toJson(content), files);
//        String json = new Gson().toJson(content);
//        NoteContent cc = new Gson().fromJson(
//                json,
//                new TypeToken<NoteContent>() {
//                }.getType());
//        Log.d(TAG, "exit: "+cc);
        finish();
    }
    public void hideSoftKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

}
