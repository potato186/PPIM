package com.ilesson.ppim.activity;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.custom.ComposeMessage;
import com.ilesson.ppim.entity.ComposeCollectInfo;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.utils.WxShareUtils;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import io.rong.eventbus.EventBus;
import io.rong.imkit.RongIM;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;

import static com.ilesson.ppim.activity.CollectActivity.COLLECT_CANCEL;
import static com.ilesson.ppim.activity.CollectActivity.TYPE_COMPOSE;
import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;
import static com.ilesson.ppim.activity.OutlineActivity.conversationType;
import static com.ilesson.ppim.activity.OutlineActivity.targetId;
import static com.ilesson.ppim.utils.Constants.SHARE_LINK;

/**
 * Created by potato on 2019/5/8.
 */

public class WebActivity extends BaseActivity {

    private WebView mWebview;
//    private History mHistory;
    private String phone;
    private String url;
    private ImageView collectImg;
    public static final String URL = "url";
    public static final String TITLE = "title";
    public static final String SCORE = "score";
    public static final String WORD_NUM = "word_num";
    public static final String UUID = "uuid";
    public static final String GRADE = "grade";
    private String title;
    private String score;
    private String wordNum;
    private String uuid;
    private String grade;

    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.act_history_result);
        setStatusBarLightMode(this,true);
        phone = SPUtils.get(LoginActivity.USER_PHONE,"");
        mWebview = findViewById(R.id.webview);
        Intent intent = getIntent();
        collectImg = findViewById(R.id.collect);
        WebSettings set = mWebview.getSettings();
        set.setJavaScriptEnabled(true);
        set.setSupportZoom(true);
        set.setBuiltInZoomControls(true);
        set.setUseWideViewPort(true);
        set.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        set.setLoadWithOverviewMode(true);
        mWebview.setWebChromeClient(new WebChromeClient() {

        });

        mWebview.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(WxShareUtils.SHARE_URL);
                intent.setData(content_url);
                startActivity(intent);
                return true;
            }

            @Override
            public void onPageFinished(WebView webView, String s) {
                super.onPageFinished(webView, s);
                hideProgress();
            }
        });
        uuid = intent.getStringExtra(UUID);
        title = intent.getStringExtra(TITLE);
        wordNum = intent.getStringExtra(WORD_NUM);
        score = intent.getStringExtra(SCORE);
        grade = intent.getStringExtra(GRADE);
        url = SHARE_LINK+uuid;
        if(TextUtils.isEmpty(uuid)){
            findViewById(R.id.menu).setVisibility(View.GONE);
            url = intent.getStringExtra(URL);
            mWebview.loadUrl(url);
        }else{
            mWebview.loadUrl(url+"&history=true");
        }
        TextView titleTv = findViewById(R.id.title);
        titleTv.setText(title);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBackResult();
            }
        });
        findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSendDialog();
            }
        });
//        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ComposeMessage message = new ComposeMessage();
//                message.setTitle(title);
//                message.setUuid(uuid);
//                message.setCount(wordNum);
//                message.setGrade(grade);
//                message.setScore(score);
//                new ComposeMsgUtils().showSendDialog(WebActivity.this,message);
//            }
//        });
        collectImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isCollected){
                    unCollect();
                }else{
                    collect();
                }
            }
        });
        showProgress();
//        checkCollect();
    }
    @Override
    public void onBackPressed() {
        setBackResult();
    }

    private void setBackResult(){
        Intent it = new Intent();
        int result = 0;
        if(!isCollected){
            result = COLLECT_CANCEL;
        }
        setResult(result, it);
        finish();
    }
    private static final String TAG = "WebActivity";


    private int cId=-1;
    private void collect() {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.COMPOSITION_URL);
        params.addParameter("action", "fav_add");
        String token = SPUtils.get(LOGIN_TOKEN, "");
        params.addParameter("token", token);
        params.addParameter("key", "ilesson");
        params.addParameter("type", TYPE_COMPOSE+"");
        params.addParameter("id", "");
        params.addParameter("uuid", uuid);
        params.addParameter("title", title);
        params.addParameter("desc", String.format(getResources().getString(R.string.des_format), grade,score,wordNum));
        Log.d(TAG, "loadData: " + params.toString());
        showProgress();
        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "collect onSuccess: "+result);
                ComposeCollectInfo base = new Gson().fromJson(
                        result,
                        new TypeToken<ComposeCollectInfo>() {
                        }.getType());
                if (base.getCode() == 0) {
                    cId = base.getData();
                    collectImg.setImageResource(R.mipmap.collected);
                    isCollected = true;
                    showToast(R.string.collect_success);
                }else{
                    showToast(base.getMessage());
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ex.printStackTrace();
                removeDialog(DIALOG_PROGRESS);
            }

            @Override
            public void onCancelled(CancelledException cex) {
                cex.printStackTrace();
                removeDialog(DIALOG_PROGRESS);
            }

            @Override
            public void onFinished() {
            }
        });
    }

    private void unCollect() {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.COMPOSITION_URL);
        params.addParameter("action", "fav_rm");
        String token = SPUtils.get(LOGIN_TOKEN, "");
        params.addParameter("token", token);
        params.addParameter("id", cId + "");
        Log.d(TAG, "loadData: " + params.toString());
        showDialog(DIALOG_LONDING, null);
        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "unCollect onSuccess: "+result);
                ComposeCollectInfo base = new Gson().fromJson(
                        result,
                        new TypeToken<ComposeCollectInfo>() {
                        }.getType());
//                if (base.getCode() == 0) {
//                    collectImg.setImageResource(R.drawable.uncollect);
//                    isCollected = false;
//                    Tools.showToastLong(WebActivity.this,R.string.collect_cancel);
//                }else{
//                    Tools.showToastLong(WebActivity.this,base.getMessage());
//                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ex.printStackTrace();
                removeDialog(DIALOG_PROGRESS);
            }

            @Override
            public void onCancelled(CancelledException cex) {
                cex.printStackTrace();
                removeDialog(DIALOG_PROGRESS);
            }

            @Override
            public void onFinished() {
            }
        });
    }

    private boolean isCollected;
/*
    private void checkCollect() {
        //action=fav_exist&phone=%s&type=%s&id=%s&uuid=%s
        RequestParams params = new RequestParams(Const.BASE_SERVER + Const.SEARCH_COMPOSITION_URL);
        params.addParameter("action", "fav_exist");
        params.addParameter("phone", phone);
        params.addParameter("type", HISTORY_TYPE+"");
        params.addParameter("id", "");
        params.addParameter("uuid", mHistory.getUuid());
        Log.d(TAG, "loadData: " + params.toString());
        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "checkCollect onSuccess: "+result);
                CollectInfo base = new Gson().fromJson(
                        result,
                        new TypeToken<CollectInfo>() {
                        }.getType());
                if (base.getCode() == 0) {
                    cId = base.getData();
                    if (base.getData() == -1) {
                        isCollected = false;
                        collectImg.setImageResource(R.drawable.uncollect);
                    } else {
                        collectImg.setImageResource(R.drawable.collected);
                        isCollected = true;
                    }
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
    }*/
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
        view.findViewById(R.id.collect).setVisibility(View.GONE);
        View sendCurrent = view.findViewById(R.id.send_current);
        View send = view.findViewById(R.id.send_to_other);
        TextView sendText = view.findViewById(R.id.send_text);
        if(TextUtils.isEmpty(targetId)){
            sendCurrent.setVisibility(View.GONE);
            sendText.setText(R.string.send);
        }
        sendCurrent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSendDialog.dismiss();
                ComposeMessage message = new ComposeMessage();
                message.setTitle(title);
                message.setUuid(uuid);
                message.setCount(wordNum);
                message.setGrade(grade);
                message.setScore(score);
                io.rong.imlib.model.Message msg = io.rong.imlib.model.Message.obtain(targetId, conversationType, message);
                RongIM.getInstance().sendMessage(msg,
                        "", null, new IRongCallback.ISendMessageCallback() {
                            @Override
                            public void onAttached(io.rong.imlib.model.Message message) {
                                Log.d(TAG, "onAttached: "+message);
                            }

                            @Override
                            public void onSuccess(io.rong.imlib.model.Message message) {
                                EventBus.getDefault().post(new Conversation());
                                RongIM.getInstance().startConversation(WebActivity.this, conversationType, targetId, "");
                            }

                            @Override
                            public void onError(io.rong.imlib.model.Message message, RongIMClient.ErrorCode errorCode) {
                                Log.d(TAG, "onError: "+errorCode);
                            }
                        });

            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSendDialog.dismiss();
                ComposeMessage message = new ComposeMessage();
                message.setTitle(title);
                message.setUuid(uuid);
                message.setCount(wordNum);
                message.setGrade(grade);
                message.setScore(score);
                io.rong.imlib.model.Message msg = io.rong.imlib.model.Message.obtain(targetId, conversationType, message);
                Intent intent = new Intent(WebActivity.this, ForwadSelectActivity.class);
                intent.putExtra("msg",message);
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
                if(isCollected){
                    unCollect();
                }else{
                    collect();
                }
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
        mShareDialog = new Dialog(WebActivity.this, R.style.dialog_bottom_full);
        mShareDialog.setCanceledOnTouchOutside(true);
        mShareDialog.setCancelable(true);
        Window window = mShareDialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.share_animation);
        View view = View.inflate(WebActivity.this, R.layout.lay_share, null);
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
    private void share(int type){
        WxShareUtils.shareWeb(WebActivity.this, type,url,title,String.format(getResources().getString(R.string.des_format), grade + "", score,wordNum).replace("//","  "));
    }

    private void dismissShareDialog() {
        if (mShareDialog != null&&mShareDialog.isShowing()) {
            mShareDialog.dismiss();
        }
    }
}
