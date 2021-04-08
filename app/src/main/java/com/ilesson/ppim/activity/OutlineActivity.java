package com.ilesson.ppim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.ilesson.ppim.R;
import com.ilesson.ppim.utils.SPUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.rong.imlib.model.Conversation;

import static com.ilesson.ppim.activity.PayScoreActivity.TARGET_ID;


/**
 * Created by potato on 2018/12/3.
 */

public class OutlineActivity extends BaseActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener{

    private static EditText mNumEditText;
    private static EditText mTitleEditText;
    private EditText mNameEditText;
    private String phone;
    private static final String TAG = "OutlineActivity";
    public static final String USER_NAME = "user_name";
    public static final String USER_GRADE = "user_grade";
    public static final String COMPOSITON_TITLE = "compositon_title";
    public static final String COMPOSITON_COUNT = "compositon_count";
    public static final String LEFT_LOCATION = "left_location";
    public static final String TOP_LOCATION = "top_location";
    public static final String CONVERSATIONTYPE = "ConversationType";
    private String mUserName;
    private String mTitle;
    private String mCount;
    private int mGrade = 1;
    private Spinner mGradeSpinner;
//    private DragView dragView;
    public static final String[] grades = {"一年级", "二年级", "三年级",
            "四年级", "五年级", "六年级"};
    public static String targetId;
    public static String title;
    public static Conversation.ConversationType conversationType;
    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.act_outline);
        setStatusBarLightMode(this,true);
        phone = SPUtils.get(LoginActivity.USER_PHONE,"");
        mGradeSpinner = findViewById(R.id.spinner_grade);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, grades);
        adapter.setDropDownViewResource(R.layout.dropdown_stytle);
        mGradeSpinner.setAdapter(adapter);
        mGradeSpinner.setOnItemSelectedListener(this);
        mNumEditText = findViewById(R.id.charno);
        mTitleEditText = findViewById(R.id.compose_title);
        mNameEditText = findViewById(R.id.author_name);
//        mIndictorImageView = findViewById(R.id.indictor);
        mCount = SPUtils.get(COMPOSITON_COUNT,"");
        mUserName = SPUtils.get(USER_NAME,"");
        mGrade = SPUtils.get(USER_GRADE,1);
        mTitle = SPUtils.get(COMPOSITON_TITLE,"");
        targetId = getIntent().getStringExtra(TARGET_ID);
        conversationType = (Conversation.ConversationType) getIntent().getSerializableExtra(CONVERSATIONTYPE);
        if(!TextUtils.isEmpty(mCount)){
            mNumEditText.setText(mCount);
        }
        if(!TextUtils.isEmpty(mUserName)){
            mNameEditText.setText(mUserName);
        }
        if(!TextUtils.isEmpty(mTitle)){
            mTitleEditText.setText(mTitle);
        }
        mGradeSpinner.setSelection(mGrade-1,true);
        findViewById(R.id.confirm).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);
        findViewById(R.id.back_btn).setOnClickListener(this);
        findViewById(R.id.collect).setOnClickListener(this);
        findViewById(R.id.histroy).setOnClickListener(this);
//        dragView = findViewById(R.id.dragview);
//        dragView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(!dragView.isDrag()){
//                    Intent intent = new Intent(OutlineActivity.this,ChatActivity.class);
//                    startActivity(intent);
//                }
//            }
//        });
//        int px = ScreenUtils.dip2px(this,50);
//        int sw = ScreenUtils.getScreenWidth(this);
//        int sh = ScreenUtils.getScreenHeight(this);
//        int left = mSharedPreferences.getInt(LEFT_LOCATION,sw-px);
//        int top = mSharedPreferences.getInt(TOP_LOCATION,sh-px);
//        if(left==0){
//            left=sw-px;
//        }
//        if(top==0){
//            top=sh-px;
//        }
//        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(px,px);
//        layoutParams.leftMargin = left;
//        layoutParams.topMargin = top;
    }
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    public void writeTxt(String str) {
        String folderName = "User";
        File sdCardDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), folderName);

        if (!sdCardDir.exists()) {
            if (!sdCardDir.mkdirs()) {

                try {
                    sdCardDir.createNewFile();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            File saveFile = new File(sdCardDir, "user.txt");

            if (!saveFile.exists()) {
                saveFile.createNewFile();
            }
            // FileOutputStream outStream =null;
            //outStream = new FileOutputStream(saveFile);

            final FileOutputStream outStream = new FileOutputStream(saveFile);

            try {
                outStream.write(str.getBytes());
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.spinner_grade:
                mGrade = position + 1;
//                gradeText.setText(grades[position]);
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveData();
//        SharedPreferences.Editor editor = mSharedPreferences.edit();
//        editor.putInt(LEFT_LOCATION, dragView.getLeftLocation());
//        editor.putInt(TOP_LOCATION, dragView.getTopLocation());
//        editor.commit();
    }

    private void saveData(){
        mCount = mNumEditText.getText().toString();
        mTitle = mTitleEditText.getText().toString();
        mUserName = mNameEditText.getText().toString();
        SPUtils.put(COMPOSITON_TITLE,mTitle);
        SPUtils.put(COMPOSITON_COUNT,mCount);
        SPUtils.put(USER_NAME,mUserName);
        SPUtils.put(USER_GRADE,mGrade);
    }

    public static void resetData(){
        SPUtils.put(COMPOSITON_TITLE,"");
        SPUtils.put(COMPOSITON_COUNT,"");
        if(null!=mNumEditText){
            mNumEditText.setText("");
        }
        if(null!=mTitleEditText){
            mTitleEditText.setText("");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm:
                saveData();
                if(TextUtils.isEmpty(mCount)){
                    showToast(R.string.not_null_num);
                    return;
                }
                if(TextUtils.isEmpty(mTitle)){
                    showToast(R.string.not_null_title);
                    return;
                }
//                if(TextUtils.isEmpty(mUserName)){
//                    showToast(R.string.not_null_name);
//                    return;
//                }
                Intent intent = new Intent(this,ComposeActivity.class);
                intent.putExtra(COMPOSITON_TITLE,mTitle);
                intent.putExtra(COMPOSITON_COUNT,mCount);
                intent.putExtra(TARGET_ID,targetId);
                intent.putExtra(CONVERSATIONTYPE,conversationType);
//                intent.putExtra(USER_NAME,mUserName);
                intent.putExtra(USER_GRADE,mGrade);
                startActivity(intent);
                break;
            case R.id.collect:
                startActivity(new Intent(this,CollectActivity.class));
                break;
            case R.id.histroy:
                startActivity(new Intent(this,HistoryActivity.class));
                break;
            case R.id.back_btn:
                finish();
                break;
            case R.id.cancel:
                finish();
                break;
        }
    }
}
