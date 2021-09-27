package com.ilesson.ppim.fragment;

import static com.ilesson.ppim.view.SwitchButton.PLAY_TTS;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iflytek.cloud.SpeechUtility;
import com.ilesson.ppim.R;
import com.ilesson.ppim.activity.ImagePreviewActivity;
import com.ilesson.ppim.activity.LoginActivity;
import com.ilesson.ppim.activity.MainActivity;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.SmartOrder;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.PPScreenUtils;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.utils.TTSHelper;
import com.ilesson.ppim.utils.TencenRecognize;
import com.ilesson.ppim.view.DragView;
import com.ilesson.ppim.view.IfeyVoiceWidget1;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.List;

import io.reactivex.functions.Consumer;

@ContentView(R.layout.fragment_ai)
public class AiFragment extends BaseFragment implements TencenRecognize.OnRecognizeListener {
    private static final String TAG = "AiFragment";
    @ViewInject(R.id.play_tts)
    private TextView playView;
    @ViewInject(R.id.request_text)
    private TextView requestText;
    @ViewInject(R.id.tts_modify)
    public TextView ttsModify;
    @ViewInject(R.id.help_text)
    public TextView helpTextView;
    @ViewInject(R.id.tts)
    private TextView ttsTv;
    @ViewInject(R.id.edittext)
    private EditText editText;
    @ViewInject(R.id.voice_layout)
    private View voiceLayout;
    @ViewInject(R.id.edit_layout)
    private View editLayout;
//    @ViewInject(R.id.scrollview)
//    private ScrollView scrollView;

    @ViewInject(R.id.result_image)
    private ImageView resultImageView;
    @ViewInject(R.id.floatBtn)
    private DragView floatBtn;
    private static final String FLOATX = "floatx111";
    private static final String FLOATY = "floaty111";
    private boolean recording;
    public TTSHelper ttsHelper;
//    private TencenRecognize tencenRecognize;
    private static final String INIT_COUNT = "init_count";
    private MainActivity mainActivity;
    public IfeyVoiceWidget1 ifeyBtn;
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        mainActivity = (MainActivity) getActivity();
        playTts = SPUtils.get(PLAY_TTS, true);
//        tencenRecognize = TencenRecognize.getInstance();
//        tencenRecognize.initTencenVoice(mainActivity);
//        tencenRecognize.setOnRecognizeListener(this);
        voiceAnim = AnimationUtils.loadAnimation(mainActivity, R.anim.voice_view_anim);
        showPlayState();
        initTts();
        floatBtn.setOnPressListener(new DragView.OnPressListener() {
            @Override
            public void onPressUp(boolean isDrag) {

            }

            @Override
            public void onPressDown() {
                editLayout.setVisibility(View.GONE);
                    if (recording) {
                        stopXunfei(false);
                    } else {
                        toSpeech();
                    }
            }

            @Override
            public void onDoubleClick() {
                canShow=true;
                editLayout.setVisibility(View.VISIBLE);
                editText.requestFocus();
                PPScreenUtils.showInput(editText);
            }
        });
        floatBtn.setOnLocationListener(new DragView.OnLocationListener() {
            @Override
            public void onLocation(int l, int t) {
                SPUtils.put(FLOATX, l);
                SPUtils.put(FLOATY, t);
                setVoiceBtnLocation();
            }
        });
        handler.sendEmptyMessageDelayed(0, 200);
        initXunfeiSpeech();
        setListenerToRootView();
    }
    private int diff;
    private void setListenerToRootView() {
        final View rootView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);

                int heightDiff = rootView.getRootView().getHeight() - (r.bottom - r.top);
                if(diff==heightDiff){
                    return;
                }
                diff = heightDiff;
                Log.d(TAG, "onGlobalLayout: "+heightDiff);
                if (heightDiff > PPScreenUtils.getScreenHeight(getActivity()) / 3) { // if more than 100 pixels, its probably a keyboard...
                    if(!canShow){
                        PPScreenUtils.hideShowKeyboard(mainActivity);
                    }
                    setEditLayout(heightDiff);
                } else {
                    editLayout.setVisibility(View.GONE);
                }
            }
        });
    }
    private void initTts() {
        ttsHelper = new TTSHelper(mainActivity);
        ttsHelper.setOnTTSFinish(new TTSHelper.OnTTSFinish() {
            @Override
            public void onTTSFinish(int type) {
            }

            @Override
            public void onTTSstart() {

            }
        });
    }

    private void hideVoice() {
        closeVoice();
    }

    private boolean playTts;

    public void playTTsEvent() {
        playTts = !playTts;
        showPlayState();
        SPUtils.put(PLAY_TTS, playTts);
        stopTts();
    }

    public void stopTts(){
        if (ttsHelper!=null) {
            ttsHelper.stop();
        }
    }

    private void showPlayState() {
        if (playTts) {
            playView.setBackgroundResource(R.mipmap.sy_on);
        } else {
            playView.setBackgroundResource(R.mipmap.sy_of);
        }
    }

    private void closeVoice() {
        voiceLayout.setVisibility(View.GONE);
        ttsTv.setText("");
        if (ttsHelper!=null) {
            ttsHelper.stop();
        }
        if (recording) {
            stopXunfei(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopVoice();
    }

    public void stopVoice(){
        stopTts();
        if (recording) {
            stopXunfei(false);
        }
    }
    public void toSpeech() {
        RxPermissions rxPermissions = new RxPermissions(this);

        rxPermissions.requestEach(Manifest.permission.RECORD_AUDIO)
                .subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(Permission permission) throws Exception {
                        if (permission.granted) {
                            // 用户已经同意该权限
                            if (recording) {
                                stopXunfei(false);
                            } else {
//                                start();
                                startXunfei();
                            }
                        } else if (permission.shouldShowRequestPermissionRationale) {
                        } else {
                            getPermission();
                        }
                    }
                });
    }

    public void getPermission() {
        AlertDialog.Builder alertDialog = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alertDialog = new AlertDialog.Builder(mainActivity, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            alertDialog = new AlertDialog.Builder(mainActivity);
        }
        alertDialog.setTitle("权限设置")
                .setMessage("应用缺乏录音权限，是否前往手动授予该权限？")
                .setPositiveButton("前往", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", mainActivity.getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();
        alertDialog.show();
    }

    @Event(value = R.id.voice_layout)
    private void voice_layout(View v) {
        closeVoice();
        if(editLayout.getVisibility()==View.VISIBLE)
        PPScreenUtils.hideShowKeyboard(mainActivity);
    }
    private boolean inputType;
    private boolean canShow;
    @Event(value = R.id.confirm)
    private void confirm(View v) {
        String text = editText.getText().toString();
        if(!TextUtils.isEmpty(text)){
            inputType = true;
            floatBtn.requestFocus();
            editText.clearFocus();
            if (ttsHelper!=null) {
                ttsHelper.stop();
            }
            handleContent(text);
        }
    }

    @Event(value = R.id.play_tts)
    private void play_tts(View v) {
        playTTsEvent();
    }

    @Event(value = R.id.close)
    private void close(View v) {
        closeVoice();
    }
    @Event(value = R.id.ai_layout)
    private void ai_layout(View v) {
        closeVoice();
        if(editLayout.getVisibility()==View.VISIBLE)
        PPScreenUtils.hideShowKeyboard(mainActivity);
    }

    public void request(String key) {

        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.TALK);
        if (TextUtils.isEmpty(key)) {
            params.addParameter("action", "init");
        } else {
            params.addParameter("action", "talk");
            params.addParameter("key", key);
        }
        Log.d(TAG, "loadData: " + params.toString());
        if(inputType){
            mainActivity.showProgress();
        }
        x.http().post(params, new Callback.CommonCallback<String>() {
            @SuppressLint("StringFormatMatches")
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: " + result);
                BaseCode<List<SmartOrder>> data = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<List<SmartOrder>>>() {
                        }.getType());
                if (data.getCode() == 0) {
                    List<SmartOrder> list = data.getData();
                    if (list.isEmpty()) {
                        return;
                    }
                    if(inputType){
                        editText.setText("");
                        editText.clearFocus();
                        canShow = false;
                    }
                    helpTextView.setVisibility(View.GONE);
                    resultImageView.setVisibility(View.GONE);
                    SmartOrder order = list.get(0);
                    String tts = order.getTts();

                    String ttsm = data.getBak();
                    if (!TextUtils.isEmpty(ttsm)) {
                        String text = getResources().getString(R.string.modify_tts);
                        SpannableStringBuilder style = new SpannableStringBuilder(text + ttsm);
                        style.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.theme_color)), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        ttsModify.setText(style);
                        ttsModify.setVisibility(View.VISIBLE);
                    } else {
                        ttsModify.setVisibility(View.GONE);
                    }
                    String help = order.getHelp().trim();
                    if (!TextUtils.isEmpty(help)) {
                        helpTextView.setText(help);
                        helpTextView.setVisibility(View.VISIBLE);
                        voiceLayout.setVisibility(View.VISIBLE);
                    }
                    boolean play = true;
                    if (TextUtils.isEmpty(key)) {
                        requestText.setVisibility(View.GONE);
                        String phone = SPUtils.get(LoginActivity.USER_PHONE, "");
                        int count = SPUtils.get(INIT_COUNT + phone, 0);
                        Log.d(TAG, "onSuccess: count="+count);
                        if (count < 3) {
                            SPUtils.put(INIT_COUNT + phone, count + 1);
                        }else{
                            play = false;
                        }
                    } else {
                        requestText.setVisibility(View.VISIBLE);
                    }
                    if (!TextUtils.isEmpty(tts)) {
                        ttsTv.setText(tts);
                        ttsTv.setVisibility(View.VISIBLE);
                        voiceLayout.setVisibility(View.VISIBLE);
                        Log.d(TAG, "onSuccess: play="+play);
                        Log.d(TAG, "onSuccess: playTts="+playTts);
                        if(play){
                            if (playTts) {
                                if("en".equals(order.getTtsType())){
                                    ttsHelper.startEnglish(0,tts);
                                }else{
                                    ttsHelper.startChinese(0,tts);
                                }
                            }
                        }
                    }

                    String imgUrl = order.getImgurl();
                    if (!TextUtils.isEmpty(imgUrl)) {
                        Glide.with(mainActivity).asBitmap().load(imgUrl).into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                ViewGroup.LayoutParams params = resultImageView.getLayoutParams();
                                int width = 0;
                                int height = 0;

                                int screenWidth = PPScreenUtils.getScreenWidth(mainActivity);
                                int maxW = (int) (screenWidth * .85);
                                if ((double) resource.getWidth() / (double) resource.getHeight() > 1.2) {
                                    width = maxW;
                                    height = resource.getHeight() * width / resource.getWidth();
                                } else {
                                    int calW = PPScreenUtils.dip2px(mainActivity, resource.getWidth());
                                    if (calW < maxW) {
                                        width = calW;
                                        height = PPScreenUtils.dip2px(mainActivity, resource.getHeight());
                                    }
                                }
                                params.width = width;
                                params.height = height;
                                resultImageView.setLayoutParams(params);
                                resultImageView.setImageBitmap(resource);
                                resultImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                                resultImageView.setVisibility(View.VISIBLE);
                            }
                        });
                        resultImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ImagePreviewActivity.startPreview(mainActivity, order.getImgurl());
                            }
                        });
                    }
                } else {
                    mainActivity.showToast(data.getMessage());
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
                floatBtn.setBackgroundResource(imgs[0]);
                floatBtn.clearAnimation();
                if(inputType){
                    mainActivity.hideProgress();
                }
//                setVoiceBtnLocation();
            }
        });
    }

    private void handleContent(String content) {
        if (voiceLayout.getVisibility() == View.GONE && !TextUtils.isEmpty(content)) {
            ttsTv.setVisibility(View.GONE);
            voiceLayout.setVisibility(View.VISIBLE);
        }
        content = content.replace("，", "").replace("。", "").replace("！", "").replace("？", "");
        requestText.setText(content);
        request(content);
    }

    public void clickVoice() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.RECORD_AUDIO).subscribe(new Consumer<Boolean>() {
            public void accept(Boolean aBoolean) {
                if (aBoolean) {
                    if (recording) {
                        stop(true);
                    } else {
                        start();
                    }
                } else {
                    Toast.makeText(mainActivity, R.string.no_permission, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void start() {
//        tencenRecognize.start();
        handler.removeMessages(2);
        LinearInterpolator lir = new LinearInterpolator();
        voiceAnim.setInterpolator(lir);
        floatBtn.startAnimation(voiceAnim);
        if (ttsHelper!=null) {
            ttsHelper.stop();
        }
        recording = true;
    }

    private void showVolume(int volume) {
        int p = (last + volume) / 2;
        last = p;
        index = volume;
        handler.sendEmptyMessage(1);
    }

    private void stop(boolean showDialog) {
        recording = false;

//        tencenRecognize.stop();
        handler.removeMessages(2);
        if (showDialog) {
            floatBtn.setBackgroundResource(R.drawable.home_dialog);
            AnimationDrawable anim = (AnimationDrawable) floatBtn.getBackground();
            anim.start();
        } else {
            floatBtn.setBackgroundResource(imgs[0]);
            floatBtn.clearAnimation();
        }
    }

    private boolean stoped;

    @Override
    public void onStop() {
        super.onStop();
        if (recording) {
            stopXunfei(false);
        }
    }

    private int[] imgs = {R.mipmap.speak_module0, R.mipmap.speak_module1, R.mipmap.speak_module2, R.mipmap.speak_module3, R.mipmap.speak_module4, R.mipmap.speak_module5, R.mipmap.speak_module6, R.mipmap.speak_module7, R.mipmap.speak_module7};


    private int last = 0;
    private int index = 0;
    Animation voiceAnim;
    private int current = 0;
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
//                int x = SPUtils.get(FLOATX, PPScreenUtils.getScreenWidth(mainActivity) - PPScreenUtils.dip2px(mainActivity, 120));
//                int y = SPUtils.get(FLOATY, PPScreenUtils.getScreenHeight(mainActivity) - PPScreenUtils.dip2px(mainActivity, 120));
//                floatBtn.setLocation(x, y);
                setVoiceBtnLocation();
            } else if (msg.what == 1) {
                handler.removeMessages(2);
                if (!recording) {
                    return;
                }
                if (index < 0) index = 0;
                if (index > imgs.length - 1) index = imgs.length - 1;
                floatBtn.setBackgroundResource(imgs[index]);
                handler.sendEmptyMessageDelayed(2, 100);
                current = index - 1;
            } else if (msg.what == 2) {
                if (!recording) {
                    return;
                }
                if (current < index - 1) {
                    current = index;
                }
                if (current < 0) current = 0;
                if (current > imgs.length - 1) current = imgs.length - 1;
                floatBtn.setBackgroundResource(imgs[current]);
                handler.sendEmptyMessageDelayed(2, 50);
            } else if (msg.what == 3) {
                String content = (String) msg.obj;
                handleContent(content);
                stop(TextUtils.isEmpty(content) ? false : true);
            }else if (msg.what == 4) {
                stop(false);
            }else if (msg.what == 5) {
                PPScreenUtils.hideShowKeyboard(mainActivity);
            }
        }
    };

    private void setVoiceBtnLocation() {
        int maxX = PPScreenUtils.getScreenWidth(mainActivity) - PPScreenUtils.dip2px(mainActivity, 120);
        int maxY = PPScreenUtils.getScreenHeight(mainActivity) - PPScreenUtils.dip2px(mainActivity, 150);
        int x = SPUtils.get(FLOATX, PPScreenUtils.getScreenWidth(mainActivity) / 2 - PPScreenUtils.dip2px(mainActivity, 60));
        int y = SPUtils.get(FLOATY, maxY);
        if (x > maxX) {
            x = maxX;
        }
        if (y > maxY) {
            y = maxY;
        }
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) floatBtn.getLayoutParams();
        layoutParams.leftMargin = x;
        layoutParams.topMargin = y;
        floatBtn.setLayoutParams(layoutParams);
        floatBtn.setVisibility(View.VISIBLE);
    }
    private void setEditLayout(int margin){
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) editLayout.getLayoutParams();
        layoutParams.bottomMargin=margin-mainActivity.bottomHeight-PPScreenUtils.getStatusBarHeight(mainActivity);
        editLayout.setLayoutParams(layoutParams);
    }
    @Override
    public void onFinish(String content) {
        if (TextUtils.isEmpty(content)) {
            handler.sendEmptyMessage(4);
            return;
        }
        content = content.toLowerCase();
        Message message = Message.obtain();
        message.obj = content;
        message.what = 3;
        handler.sendMessage(message);
    }

    @Override
    public void onVolume(int volume) {
        showVolume(volume / 3);
    }

    private void initXunfeiSpeech() {
        ttsHelper = new TTSHelper(mainActivity);
        ttsHelper.setOnTTSFinish(new TTSHelper.OnTTSFinish() {
            @Override
            public void onTTSFinish(int type) {
            }

            @Override
            public void onTTSstart() {

            }
        });
        SpeechUtility.createUtility(mainActivity,
                getResources().getString(R.string.xunfei_appid));
        initIfey();
    }


    private void initIfey() {
        ifeyBtn = new IfeyVoiceWidget1(mainActivity);
        ifeyBtn.initIfey(new IfeyVoiceWidget1.MessageListener() {

            @Override
            public void onReceiverMessage(String content) {
                if (null != ifeyBtn) {
                    ifeyBtn.stop();
                }
                if (TextUtils.isEmpty(content)) {
                    return;
                }
                inputType=false;
                content = content.toLowerCase();
                handleContent(content);
                stopXunfei(true);
            }

            @Override
            public void onStateChanged(boolean state) {
                if (state) {
//                    start();
                } else {
                    recording = false;
                    stopXunfei(false);
                    ifeyBtn.stop();
                }
            }
        }, null, false);
        ifeyBtn.setOnVolumeChangeListener(new IfeyVoiceWidget1.OnVolumeChangeListener() {
            @Override
            public void onVolumeChanged(int progress, short[] data) {
                showVolume(progress);
            }
        });
        ifeyBtn.setOnTextReceiverListener(new IfeyVoiceWidget1.OnTextReceiverListener() {
            @Override
            public void TextReceiver(String text) {
            }
        });
    }


    private void startXunfei() {

        recording = true;
        ifeyBtn.start();
        handler.removeMessages(2);
        LinearInterpolator lir = new LinearInterpolator();
        voiceAnim.setInterpolator(lir);
        floatBtn.startAnimation(voiceAnim);
        if (ttsHelper!=null) {
            ttsHelper.stop();
        }
    }

    private void stopXunfei(boolean showDialog) {
        recording = false;
        handler.removeMessages(2);
        setVoiceBtnLocation();
        ifeyBtn.stop();
        if (showDialog) {
            floatBtn.setBackgroundResource(R.drawable.shop_dialog);
            AnimationDrawable anim = (AnimationDrawable) floatBtn.getBackground();
            anim.start();
        } else {
            floatBtn.setBackgroundResource(imgs[0]);
            floatBtn.clearAnimation();
        }
    }
}
