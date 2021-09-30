package com.ilesson.ppim.activity;

import static com.ilesson.ppim.activity.ChatInfoActivity.GROUP_ID;
import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.PPUserInfo;
import com.ilesson.ppim.entity.PublishNote;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.IMUtils;
import com.ilesson.ppim.utils.PPScreenUtils;
import com.ilesson.ppim.utils.SPUtils;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import io.rong.eventbus.EventBus;


/**
 * Created by potato on 2019/4/9.
 */
@ContentView(R.layout.act_group_note)
public class GroupNoteActivity extends BaseActivity {
    @ViewInject(R.id.save)
    private TextView saveBtn;
    @ViewInject(R.id.title)
    private TextView title;
    @ViewInject(R.id.note_text)
    private TextView noteText;
    @ViewInject(R.id.note_edit)
    private EditText noteEdit;
    public static final int MODIFY_NAME = 0;
    public static final int MODIFY_GROUP = 1;
    public static final int MODIFY_REAL_NAME = 2;
    public static final int MODIFY_SYMBL = 3;
    public static final int MODIFY_NIKE_IN_GROUP = 4;
    public static final String MODIFY_TYPE = "modify_type";
    public static final String GROUP_NOTE = "group_note";
    private int type;
    private String groupId;
    private String groupNote;
    private String nameSymbl;
    private boolean isOwner;

    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setStatusBarLightMode(this,true);
        type = getIntent().getIntExtra(MODIFY_TYPE, 0);
        groupId = getIntent().getStringExtra(GROUP_ID);
        groupNote = getIntent().getStringExtra(GROUP_NOTE);
        saveBtn.setEnabled(false);
        isOwner = getIntent().getBooleanExtra(ChatInfoActivity.ISOWNER, false);
        if(null== groupNote){
            groupNote ="";
        }
        noteEdit.setText(groupNote);
        noteText.setText(groupNote);
        if (isOwner) {
            noteEdit.setVisibility(View.VISIBLE);
            noteText.setVisibility(View.GONE);
            noteEdit.setSelection(groupNote.length());
        }else{
            noteEdit.setVisibility(View.GONE);
            noteText.setVisibility(View.VISIBLE);
        }
        noteEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals(groupNote)) {
                    saveBtn.setTextColor(getResources().getColor(R.color.color_999999));
                    saveBtn.setBackgroundResource(R.drawable.background_gray_corner5);
                    saveBtn.setEnabled(false);
                } else {
                    saveBtn.setTextColor(getResources().getColor(R.color.white));
                    saveBtn.setBackgroundResource(R.drawable.theme_gray_corer5_btn_selector);
                    saveBtn.setEnabled(true);
                }
            }
        });
    }
    public static void launch(Context context,String groupId,String note,boolean isOwner){
        Intent intent = new Intent(context,GroupNoteActivity.class);
        intent.putExtra(GROUP_ID,groupId);
        intent.putExtra(GROUP_NOTE,note);
        intent.putExtra(ChatInfoActivity.ISOWNER, isOwner);
        context.startActivity(intent);
    }
    @Event(R.id.save)
    private void save(View view) {
        String name = noteEdit.getText().toString().trim();
        if(name.equals(groupNote)){
            return;
        }
//        if (TextUtils.isEmpty(name)) {
//            return;
//        }
        showPublishDialog(name);
    }

    private static final String TAG = "ModifyNameActivity";

    private void publish(final String note) {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.GROUP_URL);
        String token = SPUtils.get(LOGIN_TOKEN, "");
        params.addParameter("token", token);
        params.addParameter("action", "broadcast");
        params.addParameter("broadcast", note);
        params.addParameter("group", groupId);
        showProgress();
        Log.d(TAG, "loadData: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: " + result);
                BaseCode<PPUserInfo> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<PPUserInfo>>() {
                        }.getType());
                if (base.getCode() == 0) {
                    StringBuilder stringBuilder = new StringBuilder(getResources().getString(R.string.group_note));
                    stringBuilder.append("\n").append(note);
                    if(!TextUtils.isEmpty(note))
                    new IMUtils().sendTextMsg(groupId,stringBuilder.toString());
                    EventBus.getDefault().post(new PublishNote(note));
                    finish();
                }
            }


            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ex.printStackTrace();
            }


            @Override
            public void onCancelled(CancelledException cex) {
                cex.printStackTrace();
            }


            @Override
            public void onFinished() {
                hideProgress();
            }
        });
    }

    private void showPublishDialog(String content) {
        View view = getLayoutInflater().inflate(R.layout.practice_dialog, null);
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        android.view.WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        p.width = (int) (PPScreenUtils.getScreenWidth(this) * 0.85);
        p.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setAttributes(p);
        TextView textView = (TextView) view.findViewById(R.id.content);
        TextView publish = (TextView) view.findViewById(R.id.right_btn);
        if(TextUtils.isEmpty(content)){
            textView.setText(R.string.clear_group_note_tips);
            publish.setText(R.string.rc_dialog_button_clear);
        }else{
            textView.setText(R.string.group_note_tips);
            publish.setText(R.string.publish);
        }
        view.findViewById(R.id.left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.right_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publish(content);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Event(R.id.back_btn)
    private void back_btn(View view) {
        finish();
    }

}
