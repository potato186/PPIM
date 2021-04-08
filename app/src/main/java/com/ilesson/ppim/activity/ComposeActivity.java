package com.ilesson.ppim.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RotateDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.ilesson.ppim.R;
import com.ilesson.ppim.custom.ComposeMessage;
import com.ilesson.ppim.entity.ComposeCollectInfo;
import com.ilesson.ppim.entity.ComposeResultBase;
import com.ilesson.ppim.entity.ComposeResultData;
import com.ilesson.ppim.entity.LocalMedia;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.utils.WxShareUtils;
import com.ilesson.ppim.view.IfeyVoiceWidget;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;
import io.rong.eventbus.EventBus;
import io.rong.imkit.RongIM;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;

import static com.ilesson.ppim.activity.CollectActivity.TYPE_COMPOSE;
import static com.ilesson.ppim.activity.ImageSelectorActivity.EXTRA_ALREADY_NUM;
import static com.ilesson.ppim.activity.ImageSelectorActivity.REQUEST_OUTPUT;
import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;
import static com.ilesson.ppim.activity.OutlineActivity.CONVERSATIONTYPE;
import static com.ilesson.ppim.activity.PayScoreActivity.TARGET_ID;
import static com.ilesson.ppim.activity.TakePhoto.RESULT_PATH;
import static com.ilesson.ppim.utils.Constants.SHARE_LINK;

public class ComposeActivity extends BaseActivity implements View.OnClickListener {

    private TextView mModelTextView;
    private TextView mStartTalkTextView;
    private View mVoiceLayout;
    private View input_layout;
    private View mVoiceView;
    private EditText mEditText;
    private String mPhone;
    private String mContent;
    private WebView mWebview;
    private TextView mSwitchBtn;
    private TextView sendBtn;
    private View menu;
    private View resultView;
    private ComposeResultData mComposeResultData;
    private static final String TAG = "ComposeActivity";
    private static final String COMPOSE_CONTENT = "compose_content";
    private String mUserName;
    private String mTitle;
    private String mCount;
    private int mGrade = 1;
    private ImageView mAnimImageView;
    private TextView mRecordingTv;
    private SpeechRecognizer mAsr;
    private Toast mToast;
    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    private RotateDrawable mCmdAnim;
    private TextView tips;
    public static final String REQUEST_ACTION = "request_action";
    // 缓存
    private SharedPreferences mSharedPreferences;
    private Animation mAnim;
    private Animation mAnimStop;
    private EditText mTitleView;
    public IfeyVoiceWidget ifeyBtn;

    private Conversation.ConversationType conversationType;
    private String targetId;

    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.act_compose);
        setStatusBarLightMode(this, true);
        mSharedPreferences = getSharedPreferences(getPackageName(),
                MODE_PRIVATE);
        ifeyBtn = new IfeyVoiceWidget(this);
        mPhone = SPUtils.get(LoginActivity.USER_PHONE, "");
        Intent intent = getIntent();
        mUserName = intent.getStringExtra(OutlineActivity.USER_NAME);
        mTitle = intent.getStringExtra(OutlineActivity.COMPOSITON_TITLE);
        mCount = intent.getStringExtra(OutlineActivity.COMPOSITON_COUNT);
        mGrade = intent.getIntExtra(OutlineActivity.USER_GRADE, 1);
        conversationType = (Conversation.ConversationType) intent.getSerializableExtra(CONVERSATIONTYPE);
        targetId = intent.getStringExtra(TARGET_ID);
        mTitleView = findViewById(R.id.title);
        mModelTextView = findViewById(R.id.model);
        input_layout = findViewById(R.id.input_layout);
        mVoiceLayout = findViewById(R.id.voice_layout);
        resultView = findViewById(R.id.result_view);
        mVoiceView = findViewById(R.id.record_view);
        menu = findViewById(R.id.menu);
        sendBtn = findViewById(R.id.send);
        mVoiceView.setOnClickListener(this);
        mModelTextView.setOnClickListener(this);
        menu.setOnClickListener(this);
        sendBtn.setOnClickListener(this);
        mTitleView.setText(mTitle);
        mEditText = findViewById(R.id.compose_text);
        mStartTalkTextView = findViewById(R.id.start_voice);
        mContent = mSharedPreferences.getString(COMPOSE_CONTENT, "");
        if (!TextUtils.isEmpty(mContent)) {
            mEditText.setText(mContent);
        }
        mEditText.setSelection(mContent.length());
        mEditText.setOnClickListener(this);
        mTitleView.clearFocus();
        mEditText.requestFocus();
        SpannableString leaveMessageStr = new SpannableString(" " + getResources().getText(R.string.hint_content));
        Drawable writePenDraw = getResources().getDrawable(R.mipmap.pen02);
        writePenDraw.setBounds(0, 0, writePenDraw.getIntrinsicWidth(),
                writePenDraw.getIntrinsicHeight());
        ImageSpan imageSpan = new ImageSpan(writePenDraw, ImageSpan.ALIGN_BASELINE);
        leaveMessageStr.setSpan(imageSpan, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        mEditText.setHint(leaveMessageStr);
        mAnimImageView = findViewById(R.id.anim_img);
        mAnim = AnimationUtils.loadAnimation(this,
                R.anim.rotate);
        mAnimStop = AnimationUtils.loadAnimation(this,
                R.anim.stop_rotate);
        LinearInterpolator interpolator = new LinearInterpolator(); // 设置匀速旋转，在xml文件中设置会出现卡顿
        mAnim.setInterpolator(interpolator);
        mSwitchBtn = findViewById(R.id.switch_btn);
        mWebview = findViewById(R.id.webview);
        WebSettings set = mWebview.getSettings();
        set.setJavaScriptEnabled(true);
        set.setSupportZoom(true);
        set.setBuiltInZoomControls(true);
        set.setUseWideViewPort(true);
        set.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        set.setLoadWithOverviewMode(true);
        mWebview.setWebChromeClient(new WebChromeClient() {
        });
//        mWebview.setWebViewClient(client);
        mWebview.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                collect();
                return true;
            }
        });
        mWebview.loadUrl("file:///android_asset/x.html");

        findViewById(R.id.comfirm_btn).setOnClickListener(this);
        findViewById(R.id.back_btn).setOnClickListener(this);
        mSwitchBtn.setOnClickListener(this);
//        checkPermissions();
        loadLocal();
        toSpeech();
    }

    private String mHtmlContent = "";

    private void loadLocal() {
        InputStream inputStream = null;
        try {
            inputStream = getAssets().open("x.html");
            int size = inputStream.available();
            int len = -1;
            byte[] bytes = new byte[size];
            inputStream.read(bytes);
            inputStream.close();
            mHtmlContent = new String(bytes);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void initSpeech() {
        SpeechUtility.createUtility(this,
                getResources().getString(R.string.xunfei_appid));
        initIfey(true);
    }

    public void toSpeech() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.RECORD_AUDIO).subscribe(new Consumer<Boolean>() {
            public void accept(Boolean aBoolean) {
                if (aBoolean) {
                    initSpeech();
                } else {
                    Toast.makeText(ComposeActivity.this, R.string.no_permission, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void toCamera() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.CAMERA).subscribe(new Consumer<Boolean>() {
            public void accept(Boolean aBoolean) {
                if (aBoolean) {
                    showPhotoDialog();
                } else {
                    Toast.makeText(ComposeActivity.this, R.string.no_permission, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initIfey(boolean longVoice) {
        ifeyBtn.initIfey(new IfeyVoiceWidget.MessageListener() {

            @Override
            public void onReceiverMessage(String content) {
                if (TextUtils.isEmpty(content)) {
                    return;
                }
                content = content.toLowerCase();
                if (!TextUtils.isEmpty(content)) {
                    int index = mEditText.getSelectionStart();
                    String before = mEditText.getText().toString();
                    StringBuilder builder = new StringBuilder();
                    builder.append(before);
                    builder.insert(index, content);
                    mEditText.setText(builder.toString());
                    mEditText.setSelection(before.length() + content.length());
                }
                Log.d(TAG, "onReceiverMessage: ");
                startRecord();
            }

            @Override
            public void onStateChanged(boolean recording) {
                if (recording) {
                }
            }
        }, null, longVoice);
    }

    private boolean mStartRecord = false;

    private void startRecord() {
        Log.d(TAG, "startRecord: ");
        ifeyBtn.start();
        mStartTalkTextView.setText(R.string.start_talk);
        mAnimImageView.startAnimation(mAnim);
        mStartRecord = true;
    }

    private void stopRecord() {
        ifeyBtn.stop();
        mStartRecord = false;
        mStartTalkTextView.setText("");
        mAnimImageView.startAnimation(mAnimStop);
    }

    private static final int SHOWHTML = 0;
    private static final int ERROR = -1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOWHTML:
                    resultView.setVisibility(View.VISIBLE);
                    input_layout.setVisibility(View.GONE);
                    mWebview.loadDataWithBaseURL("about:blank", mHtmlContent, "text/html", "utf-8", null);
                    if (pgstate == 0) {
                        mWebview.loadUrl("javascript:hideDesc()");
                    }
                    mEditText.setText("");
                    SPUtils.put(COMPOSE_CONTENT, "");
                    OutlineActivity.resetData();
//                    sendBtn.setVisibility(View.VISIBLE);
                    menu.setVisibility(View.VISIBLE);
                    break;
                case ERROR:
                    Toast.makeText(ComposeActivity.this, (String) msg.obj, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };
    private String mScore;
    private int pgstate;
    private String count;
    private String des;

    private void xgTest() {
        String text = mEditText.getText().toString();
        text = text.replace("\t\t\t\t", "").replace("%", "@@");
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(ComposeActivity.this, "正文不能为空！", Toast.LENGTH_LONG).show();
            return;
        }
        String compose = text;
        try {
            compose = URLDecoder.decode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        final String composeText = compose.replace("@@", "%");
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.COMPOSITION_URL);
        params.addParameter("action", "modify");
        String token = SPUtils.get(LOGIN_TOKEN, "");
        params.addParameter("token", token);
        params.addParameter("title", mTitle);
        params.addParameter("text", composeText);
        params.addParameter("grade", mGrade + "");
        params.addParameter("number", compose.length() + "");
        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                hideProgress();
                Log.d(TAG, "modify---onSuccess: " + result);
                ComposeResultBase base = new Gson().fromJson(
                        result,
                        new TypeToken<ComposeResultBase>() {
                        }.getType());
                if (base.getCode() == 0) {
                    ComposeResultData data = base.getData();
                    mComposeResultData = data;
                    mScore = data.getScore() + "";
                    String desctext = data.getDesctext();
                    if (TextUtils.isEmpty(data.getDesctext())) {
                        count = mEditText.getText().toString().length() + "";
                    } else {
                        count = desctext.substring(desctext.indexOf("总字数") + 3, desctext.indexOf("用时")).replace("：", "").replace("，", "").replace(":", "").replace(",", "");
                    }
                    mHtmlContent = mHtmlContent.replace("{score}", mScore)
                            .replace("{fmtext}", data.getFmtext())
                            .replace("{number}", count)
                            .replace("{pgtext}", data.getPgtext())
                            .replace("{xgtext}", data.getXgtext())
                            .replace("{desctext}", desctext);
                    pgstate = data.getPgstate();
                    des = String.format(getResources().getString(R.string.des_format), mGrade + "", mScore, count);
                    mHandler.sendEmptyMessage(SHOWHTML);
                } else {
                    Message msg = new Message();
                    msg.obj = base.getMessage();
                    msg.what = ERROR;
                    mHandler.sendMessage(msg);
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
            }
        });
        showProgress();
    }

    private void collect() {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.COMPOSITION_URL);
        params.addParameter("action", "fav_add");
        String token = SPUtils.get(LOGIN_TOKEN, "");
        params.addParameter("token", token);
        params.addParameter("key", "ilesson");
        params.addParameter("type", TYPE_COMPOSE + "");
        params.addParameter("id", "");
        params.addParameter("uuid", mComposeResultData.getUuid());
        params.addParameter("title", mTitle);
        params.addParameter("desc", des);
        Log.d(TAG, "loadData: " + params.toString());
        showProgress();
        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "collect onSuccess: " + result);
                ComposeCollectInfo base = new Gson().fromJson(
                        result,
                        new TypeToken<ComposeCollectInfo>() {
                        }.getType());
                if (base.getCode() == 0) {
                    showToast(R.string.collect_success);
                } else {
                    showToast(base.getMessage());
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRecord();
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(COMPOSE_CONTENT, mEditText.getText().toString());
        editor.commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_btn:
                finish();
                break;
            case R.id.menu:
                showSendDialog();
                break;
//            case R.id.send:
//                ComposeMessage message = new ComposeMessage();
//                message.setTitle(mTitle);
//                message.setUuid(mComposeResultData.getUuid());
//                message.setCount(count);
//                message.setScore(mScore);
//                message.setGrade(mGrade + "");
//                new ComposeMsgUtils().showSendDialog(this,message);
//                break;
            case R.id.model:
                requestPm();
                break;
            case R.id.switch_btn:
                if (mVoiceLayout.getVisibility() == View.GONE) {
                    mVoiceLayout.setVisibility(View.VISIBLE);
                }
//                mModelTextView.setText(R.string.voice_input);
                hintKeyBoard();
                if (!mStartRecord) {
                    Log.d(TAG, "onClick: switch_btn");
                    startRecord();
                }
                break;
            case R.id.comfirm_btn:
                xgTest();
                stopRecord();
                hideSoftInput();
                break;
            case R.id.record_view:
                if (mStartRecord) {
                    stopRecord();
                } else {
                    Log.d(TAG, "onClick: record_view");
                    startRecord();
                }
                break;
            case R.id.compose_text:
                mVoiceLayout.setVisibility(View.GONE);
//                mModelTextView.setText(R.string.keyboard_input);
                stopRecord();
                break;
        }
    }

    public void hintKeyBoard() {
        //拿到InputMethodManager
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        //如果window上view获取焦点 && view不为空
        if (imm.isActive() && getCurrentFocus() != null) {
            //拿到view的token 不为空
            if (getCurrentFocus().getWindowToken() != null) {
                //表示软键盘窗口总是隐藏，除非开始时以SHOW_FORCED显示。
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }


    public static final int REQUEST_CODE_TAKE_PICTURE = 11;
    public static final int REQUEST_CODE_PIKE_PICTURE = 12;
    public static final int REQUEST_IDENTIFY_PICTURE = 13;

    private void showPhotoDialog() {
        if (mPhotoDialog == null) {
            initPhotoDialog();
        }
        mPhotoDialog.show();
    }

    private Dialog mPhotoDialog;

    private void initPhotoDialog() {
        mPhotoDialog = new Dialog(this);
        mPhotoDialog.setCanceledOnTouchOutside(true);
        mPhotoDialog.setCancelable(true);
        Window window = mPhotoDialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        View view = View.inflate(this, R.layout.select_pic, null);
        view.findViewById(R.id.take_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPhotoDialog.dismiss();
                Intent intent = new Intent(ComposeActivity.this, TakePhoto.class);
                startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
            }
        });
        view.findViewById(R.id.pike_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPhotoDialog.dismiss();
                Intent intent = new Intent(ComposeActivity.this, ImageSelectorActivity.class);
                intent.putExtra(EXTRA_ALREADY_NUM, 0);
                startActivityForResult(intent, REQUEST_CODE_PIKE_PICTURE);
            }
        });
        window.setContentView(view);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);//设置横向全屏
    }

    private void showSendDialog() {
        if (mSendDialog == null) {
            initSendDialog();
        }
        mSendDialog.show();
    }

    private Dialog mSendDialog;

    private void initSendDialog() {
        mSendDialog = new Dialog(this);
        mSendDialog.setCanceledOnTouchOutside(true);
        mSendDialog.setCancelable(true);
        Window window = mSendDialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        View view = View.inflate(this, R.layout.select_send_item, null);
        View sendCurrent = view.findViewById(R.id.send_current);
        View send = view.findViewById(R.id.send_to_other);
        TextView sendText = view.findViewById(R.id.send_text);
        if (TextUtils.isEmpty(targetId)) {
            sendCurrent.setVisibility(View.GONE);
            sendText.setText(R.string.send);
        }
        sendCurrent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSendDialog.dismiss();
                ComposeMessage message = new ComposeMessage();
                message.setTitle(mTitle);
                message.setUuid(mComposeResultData.getUuid());
                message.setCount(count);
                message.setScore(mScore);
                message.setGrade(mGrade + "");
                io.rong.imlib.model.Message msg = io.rong.imlib.model.Message.obtain(targetId, conversationType, message);
                RongIM.getInstance().sendMessage(msg,
                        "", null, new IRongCallback.ISendMessageCallback() {
                            @Override
                            public void onAttached(io.rong.imlib.model.Message message) {
                                Log.d(TAG, "onAttached: " + message);
                            }

                            @Override
                            public void onSuccess(io.rong.imlib.model.Message message) {
                                EventBus.getDefault().post(new Conversation());
                                RongIM.getInstance().startConversation(ComposeActivity.this, conversationType, targetId, "");
                                Log.d(TAG, "onSuccess: " + message);
                            }

                            @Override
                            public void onError(io.rong.imlib.model.Message message, RongIMClient.ErrorCode errorCode) {
                                Log.d(TAG, "onError: " + errorCode);
                            }
                        });

            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSendDialog.dismiss();
                ComposeMessage message = new ComposeMessage();
                String des = mComposeResultData.getXgtext().replace("<br>", "");
                message.setDes(des);
                message.setTitle(mTitle);
                message.setUuid(mComposeResultData.getUuid());
                message.setCount(count);
                message.setScore(mScore);
                message.setGrade(mGrade + "");
                Log.d(TAG, "onClick: " + message);
                io.rong.imlib.model.Message msg = io.rong.imlib.model.Message.obtain(targetId, conversationType, message);
                Intent intent = new Intent(ComposeActivity.this, ForwadSelectActivity.class);
                intent.putExtra("msg", message);
                startActivity(intent);
            }
        });
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSendDialog.dismiss();
            }
        });
        view.findViewById(R.id.collect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSendDialog.dismiss();
                collect();
            }
        });
        view.findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSendDialog.dismiss();
                showShareDialog();
            }
        });
        window.setContentView(view);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);//设置横向全屏
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (null == data) {
            return;
        }
        if (requestCode == REQUEST_CODE_TAKE_PICTURE) {
//            startImg(mImageUri.toString());
            String url = data.getStringExtra(RESULT_PATH);
            List<LocalMedia> datas = new ArrayList<>();
            datas.add(new LocalMedia(url));
            startImg(datas);
        } else if (requestCode == REQUEST_CODE_PIKE_PICTURE) {
//            String url = data.getStringExtra(PhotoActivity.IMAGE_URL);
            List<LocalMedia> datas = (ArrayList<LocalMedia>) data.getSerializableExtra(REQUEST_OUTPUT);
//            Uri imageUri = data.getData();
            startImg(datas);
        } else if (requestCode == REQUEST_IDENTIFY_PICTURE) {
            String content = data.getStringExtra(PhotoActivity.CONTENT_RESULT);
            String title = data.getStringExtra(PhotoActivity.TITLE_RESULT);
//            if (!TextUtils.isEmpty(title)) {
//                mTitleView.setText(title);
//            }
            if (!TextUtils.isEmpty(content)) {
                String compose = mEditText.getText().toString() + content;
                mEditText.setText(compose);
                mEditText.setSelection(compose.length());
            }
        }
    }

    private void startImg(List<LocalMedia> datas) {
        Intent intent = new Intent(ComposeActivity.this, PhotoActivity.class);
//        intent.putExtra(PhotoActivity.IMAGE_URL,imgUrl);
        intent.putStringArrayListExtra(REQUEST_OUTPUT, (ArrayList) datas);
        startActivityForResult(intent, REQUEST_IDENTIFY_PICTURE);
    }

    public void requestPm() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.CAMERA).subscribe(new Consumer<Boolean>() {
            public void accept(Boolean aBoolean) {
                if (aBoolean) {
                    showPhotoDialog();
                } else {
                    //只要有一个权限被拒绝，就会执行
                    Toast.makeText(ComposeActivity.this, R.string.permission, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static final String wxkey = "wx66a83c9866ffe41c";
//    public static final String wxkey_demo = "wxb4ba3c02aa476ea1";

    private void hideSoftInput() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(mEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showShareDialog() {
        if (mShareDialog == null) {
            initShareDialog();
        }
        mShareDialog.show();
    }

    /**
     * 初始化分享弹出框
     */
    private Dialog mShareDialog;

    private void initShareDialog() {
        mShareDialog = new Dialog(ComposeActivity.this, R.style.dialog_bottom_full);
        mShareDialog.setCanceledOnTouchOutside(true);
        mShareDialog.setCancelable(true);
        Window window = mShareDialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.share_animation);
        View view = View.inflate(ComposeActivity.this, R.layout.lay_share, null);
        view.findViewById(R.id.weixin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissShareDialog();
                share(SendMessageToWX.Req.WXSceneSession);
            }
        });
        view.findViewById(R.id.pyq).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissShareDialog();
                share(SendMessageToWX.Req.WXSceneTimeline);
            }
        });
        view.findViewById(R.id.favorite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissShareDialog();
                share(SendMessageToWX.Req.WXSceneFavorite);
            }
        });
        window.setContentView(view);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);//设置横向全屏
    }

    private void share(int type) {
        WxShareUtils.shareWeb(ComposeActivity.this, type, SHARE_LINK + mComposeResultData.getUuid(), mComposeResultData.getTitle(), des.replace("//", "  "));
    }

    private void dismissShareDialog() {
        if (mShareDialog != null && mShareDialog.isShowing()) {
            mShareDialog.dismiss();
        }
    }
}
