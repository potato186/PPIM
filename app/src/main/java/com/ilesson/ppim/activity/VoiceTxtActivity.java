package com.ilesson.ppim.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iflytek.cloud.SpeechUtility;
import com.ilesson.ppim.IlessonApp;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.PPUserInfo;
import com.ilesson.ppim.entity.Semantic;
import com.ilesson.ppim.entity.SemanticBase;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.utils.PPScreenUtils;
import com.ilesson.ppim.view.IfeyVoiceWidget1;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import java.util.List;

import io.reactivex.functions.Consumer;
import io.rong.eventbus.EventBus;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.Conversation;

import static com.ilesson.ppim.activity.LoginActivity.USER_PHONE;


/**
 * Created by potato on 2019/4/9.
 */
@ContentView(R.layout.act_assistant)
public class VoiceTxtActivity extends BaseActivity {
    //    @ViewInject(R.id.layout)
//    private View layout;
//    @ViewInject(R.id.switch_btn)
//    private TextView switchBtn;voice_icon
    @ViewInject(R.id.request_text)
    private TextView requestText;
    @ViewInject(R.id.result_text)
    private TextView resultText;
    @ViewInject(R.id.voice_icon)
    private ImageView imageRecord;
    //    @ViewInject(R.id.done)
//    private TextView done;
//    @ViewInject(R.id.scrollView)
//    private ScrollView scrollView;
//    @ViewInject(R.id.record_view)
//    private View record_container;
    @ViewInject(R.id.control_view)
    private View controlView;
    @ViewInject(R.id.input_view)
    private View inputView;
    @ViewInject(R.id.keybord_view)
    private View keybordView;
    @ViewInject(R.id.result_view)
    private View resultView;
    @ViewInject(R.id.sample)
    private View sampleView;
    //    @ViewInject(R.id.toolbar)
//    private Toolbar toolbar;
//    @ViewInject(R.id.menu)
//    private TextView menu;
    @ViewInject(R.id.edittext)
    private EditText modifyEdit;
//    @ViewInject(R.id.modify_layout)
//    private View modifyLayout;
//    @ViewInject(R.id.cancel)
//    private View cancel;
//    @ViewInject(R.id.back)
//    private View back;
//    @ViewInject(R.id.progress)
//    private ProgressBar progress;

    //    @ViewInject(R.id.anim_img)
//    private ImageView mAnimImageView;
    public IfeyVoiceWidget1 ifeyBtn;
    private Animation mAnim;
    private Animation mAnimStop;
    private String device;
    private List<PPUserInfo> ppUserInfos;
    public static final String UPUSER = "upuser";
    public static boolean CHATMODE = false;

    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        device = Settings.System.getString(getContentResolver(), Settings.System.ANDROID_ID);
        toSpeech();
        showSample();
        setListenerToRootView();
    }

    private void setListenerToRootView() {
        final View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);

                int heightDiff = rootView.getRootView().getHeight() - (r.bottom - r.top);
                if (heightDiff > PPScreenUtils.dip2px(VoiceTxtActivity.this, 200)) { // if more than 100 pixels, its probably a keyboard...
//                    showKeboard();
                    stop();
                    keyboardMode = false;
                }
            }
        });
    }

    private void showKeboard() {
        keybordView.setVisibility(View.VISIBLE);
        controlView.setVisibility(View.GONE);
        inputView.setVisibility(View.VISIBLE);
//        modifyEdit.setSelection(modifyEdit.getText().toString().length());
        showInput();
    }

    private void hideKeboard() {
        keybordView.setVisibility(View.GONE);
        controlView.setVisibility(View.VISIBLE);
        inputView.setVisibility(View.GONE);
        hideInput();
    }

    private void showResult() {
        resultView.setVisibility(View.VISIBLE);
        sampleView.setVisibility(View.GONE);
    }

    private void showSample() {
        resultView.setVisibility(View.GONE);
        sampleView.setVisibility(View.VISIBLE);
    }

    @Event(R.id.keyboard)
    private void showKeboard(View view) throws Exception {
        showKeboard();
    }

    @Event(R.id.to_voice)
    private void to_voice(View view) throws Exception {
        hideKeboard();
    }

    private boolean recording;

    private void start() {
        Log.d(TAG, "start: ");
        startRecord();
        stringBuilder = new StringBuilder();
        recording = true;
//        if(modifyEdit!=null&&modifyEdit.getVisibility()==View.GONE){
//            showEdit();
//        }
    }

    private void stop() {
        recording = false;
//        controlBtn.setText(R.string.press_speak);
//        voiceView.setVisibility(View.GONE);
        stopRecord();
//        handler.sendEmptyMessageDelayed(0,100);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopRecord();
    }

    private void initSpeech() {
        SpeechUtility.createUtility(this,
                getResources().getString(R.string.xunfei_appid));
        initIfey();
    }

    public void toSpeech() {
        ifeyBtn = new IfeyVoiceWidget1(this);
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.RECORD_AUDIO).subscribe(new Consumer<Boolean>() {
            public void accept(Boolean aBoolean) {
                if (aBoolean) {
                    initSpeech();
                } else {
                    Toast.makeText(VoiceTxtActivity.this, R.string.no_permission, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private static final String TAG = "VoiceTxtActivity";

    private StringBuilder stringBuilder = new StringBuilder();

    private void initIfey() {
        ifeyBtn.initIfey(new IfeyVoiceWidget1.MessageListener() {

            @Override
            public void onReceiverMessage(String content) {
                if (TextUtils.isEmpty(content)) {
                    return;
                }
                content = content.toLowerCase();
                handleContent(content);
                stop();
                Log.d(TAG, "onReceiverMessage: ");
//                if(recording){
//                    startRecord();
//                }
            }

            @Override
            public void onStateChanged(boolean recording) {
                if (recording) {
//                    start();
                } else {
                    recording = false;
                    stop();
                }
            }
        }, null, false);
    }

    private void startRecord() {
        ifeyBtn.start();
        recording = true;
        Log.d(TAG, "startRecord: ");
//        imageRecord.setImageResource(R.mipmap.recording);
        Glide.with(getApplicationContext()).load(R.mipmap.recording).into(imageRecord);
    }

    private void startPlayerAnim() {
//		animLayout.setVisibility(View.VISIBLE);
//		speakingView.setImageResource(R.drawable.bear_speaking);
//		animationDrawable = (AnimationDrawable) speakingView.getDrawable();
//		animationDrawable.start();
    }

    private void stopRecord() {
        if (null != ifeyBtn) {
            ifeyBtn.stop();
        }
        recording = false;
//        imageRecord.setImageResource(R.mipmap.assassin);
        Glide.with(getApplicationContext()).load(R.mipmap.voice_1).into(imageRecord);
    }

    @Event(R.id.back_btn)
    private void back(View view) throws Exception {
        finish();
    }

    @Event(R.id.close)
    private void close(View view) throws Exception {
        finish();
    }

    @Event(R.id.request_text)
    private void resultText(View view) throws Exception {
        showKeboard();
        modifyEdit.setText(requestText.getText());
    }

    @Event(R.id.send)
    private void send(View view) throws Exception {
        String key = modifyEdit.getText().toString();
        if(!TextUtils.isEmpty(key)){
            handleContent(key);
        }
    }

    private void handleContent(String content){
        requestText.setText(content);
        showResult();
        find(content);
    }
    @Event(R.id.voice_icon)
    private void record_view(View view) throws Exception {
        clickVoice();
    }

    public void clickVoice() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.RECORD_AUDIO).subscribe(new Consumer<Boolean>() {
            public void accept(Boolean aBoolean) {
                if (aBoolean) {
                    if (recording) {
                        Log.d(TAG, "record_view: stop();");
                        stop();
                    } else {
                        Log.d(TAG, "record_view: start();");
                        start();
                    }
                } else {
                    Toast.makeText(VoiceTxtActivity.this, R.string.no_permission, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean keyboardMode = true;

    public void showInput() {
        modifyEdit.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(modifyEdit, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(modifyEdit.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void find(String key) {
        if (key.contains(getResources().getString(R.string.compose_title))) {
            Intent intent = new Intent(VoiceTxtActivity.this, OutlineActivity.class);
            startActivityForResult(intent,5);
            return;
        }
        RequestParams params = new RequestParams(Constants.AI_URL);
        params.addParameter("app", "JeJblZH5");
        params.addParameter("ak", "c48a0cd52ddfce1582c722b4a0c6cf023517e54c");
        params.addParameter("token", "f9ee3925fadf67cf363bf415448f4c8e33e6f00b");
        params.addParameter("mode", "");
        params.addParameter("submode", "20");
        params.addParameter("devid", device);
        params.addParameter("userid", SPUtils.get(USER_PHONE, ""));
        params.addParameter("text", key);
        Log.d(TAG, "loadData: " + params.toString());
        showProgress();
        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: +" + result);
                try {
                    SemanticBase base = new Gson().fromJson(
                            result,
                            new TypeToken<SemanticBase>() {
                            }.getType());
                    if (base.getCode().equals("0")) {
                        Semantic semantic = base.getSemantic();
                        if (null != semantic && !TextUtils.isEmpty(semantic.getData())) {
                            String data = semantic.getData();
                            if (base.getIntent().equals("find")) {
                                PPUserInfo userInfo = IlessonApp.getInstance().getUser(data);
                                if (null != userInfo) {
                                    CHATMODE = true;
                                    EventBus.getDefault().post(new Conversation());
                                    RongIM.getInstance().startConversation(VoiceTxtActivity.this, Conversation.ConversationType.PRIVATE, userInfo.getPhone(), userInfo.getName());
                                }
                            }
                        } else {
                            resultText.setText(R.string.not_find);
                        }
                    }
                } catch (Exception e) {
                    resultText.setText(R.string.not_find);
                    e.printStackTrace();
                }
                showResult();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CHATMODE = false;
    }
}
