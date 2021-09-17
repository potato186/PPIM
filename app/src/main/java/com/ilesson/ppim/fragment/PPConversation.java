package com.ilesson.ppim.fragment;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iflytek.cloud.SpeechUtility;
import com.ilesson.ppim.R;
import com.ilesson.ppim.activity.AddressActivity;
import com.ilesson.ppim.activity.ConversationActivity;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.PayOrder;
import com.ilesson.ppim.entity.SmartOrder;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.PPScreenUtils;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.utils.TTSHelper;
import com.ilesson.ppim.view.IfeyVoiceWidget1;
import com.ilesson.ppim.view.RoundImageView;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.Event;

import java.util.List;

import io.reactivex.functions.Consumer;
import io.rong.eventbus.EventBus;
import io.rong.imkit.fragment.ConversationFragment;

import static com.ilesson.ppim.view.SwitchButton.PLAY_TTS;

public class PPConversation extends ConversationFragment implements View.OnClickListener {
    private TextView requestText;
    private TextView helpTextView;
    private TextView ttsView;
    private TextView toPay;
    private TextView toSetAddress;
    private TextView userName;
    private TextView addressView;
    private TextView phoneView;
    private TextView resultText;
    private TextView waresName;
    private TextView waresPrice;
    private TextView waresQuantity;
    private ImageView imageRecord;
    private ImageView resultImageView;
    private RoundImageView roundImageView;
    private View shopLayout;
    private View waresIntroView;
    private View showContent;
    private View voiceLayout;
    private View voiceView;
//    private DragView floatBtn;
    public IfeyVoiceWidget1 ifeyBtn;
    public static boolean CHATMODE = false;
    private TTSHelper ttsHelper;
    private String groupId;
    private boolean playTts;
    private String currentKey;
    private String helpText;
    private int screenWidth;
    private ConversationActivity activity;
    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        activity = (ConversationActivity) getActivity();
        screenWidth = PPScreenUtils.getScreenWidth(activity);
        EventBus.getDefault().register(this);
        playTts = SPUtils.get(PLAY_TTS, true);
        showPlayState();
        toSpeech();
//        showSample();
//        setListenerToRootView();
    }

    private boolean injected;
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        injected = true;
//        Log.e("BaseFragment","onCreateView");
//        Glide.with(activity).asGif().load(R.mipmap.assassin).into(floatBtn);
//        search("");
//        return x.view().inject(this, inflater, container);
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        requestText=activity.requestText;
        helpTextView=activity.helpTextView;
        ttsView=activity.ttsView;
        toPay=activity.toPay;
        toSetAddress=activity.toSetAddress;
        userName=activity.userName;
        addressView=activity.addressView;
        phoneView=activity.phoneView;
        resultText=activity.resultText;
        waresName=activity.waresName;
        waresPrice=activity.waresPrice;
        waresQuantity=activity.waresQuantity;
//        imageRecord=activity.imageRecord;
        resultImageView=activity.resultImageView;
        roundImageView=activity.roundImageView;
        shopLayout=activity.shopLayout;
        waresIntroView=activity.waresIntroView;
        showContent=activity.showContent;
        voiceLayout=activity.voiceLayout;
        toPay.setOnClickListener(this);
        toSetAddress.setOnClickListener(this);
//        voiceView.setOnClickListener(this);
        if(!TextUtils.isEmpty(activity.mTargetId)){
            if(activity.mTargetId.contains("market")){
                search("");
            }else{
                voiceLayout.setVisibility(View.GONE);
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Event(R.id.play_tts)
    private void clickPlaytts(View view) throws Exception {
        playTts = !playTts;
        showPlayState();
        SPUtils.put(PLAY_TTS, playTts);
//        String txt = resultText.getText().toString();
        if (!playTts && ttsHelper.isSpeaking()) {
            ttsHelper.stop();
        }
    }

    private void showPlayState() {
//        if (playTts) {
//            playView.setBackgroundResource(R.mipmap.sy_on);
//        } else {
//            playView.setBackgroundResource(R.mipmap.sy_of);
//        }
    }

    @Event(R.id.keyboard)
    private void showKeboard(View view) throws Exception {
//        showKeboard();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.to_pay:
                if (null != api && null != req) {
                    api.sendReq(req);
                }
                break;

            case R.id.to_set_address:
                startActivityForResult(new Intent(getActivity(), AddressActivity.class), 0);
                break;
            case R.id.shop_car:
                if (waresIntroView.getVisibility() == View.VISIBLE) {
                    return;
                }
                waresIntroView.setVisibility(View.VISIBLE);
                ttsView.setVisibility(View.GONE);
                requestText.setVisibility(View.GONE);
                showContent.setVisibility(View.GONE);
                helpTextView.setVisibility(View.VISIBLE);
                helpTextView.setText(helpText);
                break;
        }
    }
    @Event(R.id.to_pay)
    private void toPay(View view) throws Exception {
    }
    @Event(R.id.close)
    private void close(View view) throws Exception {

    }

    @Event(R.id.to_set_address)
    private void toSetAddress(View view) throws Exception {

    }

    @Event(R.id.shop_car)
    private void shop_car(View view) throws Exception {
        Log.d(TAG, "shop_car_view: ");
//        if (orderNum.getVisibility() == View.GONE) {
//            return;
//        }
    }

    private boolean recording;

    private void start() {
        startRecord();
        if (ttsHelper.isSpeaking()) {
            ttsHelper.stop();
        }
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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStop() {
        super.onStop();
        stopRecord();
    }

    private IWXAPI api;
    private PayReq req;

    private void pay(String result) {
        if (null == api) {
            api = WXAPIFactory.createWXAPI(activity, getString(R.string.wx_key));
            api.registerApp(getString(R.string.wx_key));
        }
        req = new PayReq();
        try {
            BaseCode<PayOrder> base = new Gson().fromJson(
                    result,
                    new TypeToken<BaseCode<PayOrder>>() {
                    }.getType());
            if (base.getCode() == 0) {
                PayOrder order = base.getData();
                req.appId = order.getAppid();
                req.partnerId = order.getPartnerid();
                req.prepayId = order.getPrepayid();
                req.nonceStr = order.getNoncestr();
                req.timeStamp = order.getTimestamp();
                req.packageValue = "Sign=WXPay";
                req.sign = order.getSign();
                req.extData = "app data";
                if (waresIntroView.getVisibility() == View.GONE) {
                    waresIntroView.setVisibility(View.VISIBLE);
                    showContent.setVisibility(View.GONE);
                    ttsView.setVisibility(View.GONE);
                    toPay.setVisibility(View.VISIBLE);
                } else {
                    api.sendReq(req);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void search(String key) {
        currentKey = key;
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.ORDER);
//        params.addParameter("pid", choicePrice.getId() + "");
//        params.addParameter("user", account);
        params.addParameter("action", "talk");
        params.addParameter("group", activity.mTargetId);
        params.addParameter("key", key);
        activity.showProgress();
        Log.d(TAG, "search: " + params.toString());
        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: " + result);
                try {
                    BaseCode base = new Gson().fromJson(
                            result,
                            new TypeToken<BaseCode>() {
                            }.getType());
                    if (base.getCode() == 0) {
                        if (base.getTag() == 2) {
                            pay(result);
                            return;
                        }
                        toPay.setVisibility(View.GONE);
                        toSetAddress.setVisibility(View.GONE);
                        helpTextView.setVisibility(View.GONE);
                        BaseCode<List<SmartOrder>> data = new Gson().fromJson(
                                result,
                                new TypeToken<BaseCode<List<SmartOrder>>>() {
                                }.getType());
                        List<SmartOrder> list = data.getData();
                        if (list.isEmpty()) {
                            return;
                        }
                        SmartOrder order = list.get(0);
                        if (null == order) {
                            return;
                        }
                        String tts = order.getTts();
                        if (!TextUtils.isEmpty(tts)) {
                            if (tts.endsWith("\\r\\n")) {
                                tts = tts.substring(0, tts.length() - 2);
                            }
                            ttsView.setText(tts);
                            ttsView.setVisibility(View.VISIBLE);
                        }
                        String help = order.getHelp();
                        if (!TextUtils.isEmpty(help)) {
                            helpTextView.setText(help);
                            helpTextView.setVisibility(View.VISIBLE);
                        }
                        if (!TextUtils.isEmpty(tts) && playTts) {
                            ttsHelper.startChinese(0, tts);
                        }
                        if (data.getTag() == 0) {
                            showContent(order);
                        } else if (data.getTag() == 1) {
                            showContent.setVisibility(View.GONE);
//                            orderNum.setVisibility(View.GONE);
                            userName.setText("");
                            addressView.setText("");
                            phoneView.setText("");
                            helpText = order.getHelp();
                            roundImageView.setVisibility(View.GONE);
                            if (!TextUtils.isEmpty(order.getIcon())) {
                                roundImageView.setVisibility(View.VISIBLE);
                                Glide.with(activity).load(order.getIcon()).into(roundImageView);
                            }
                            if (!TextUtils.isEmpty(order.getPname())) {
                                waresName.setText(order.getPname());
                                waresIntroView.setVisibility(View.VISIBLE);
                            } else {
                                waresIntroView.setVisibility(View.GONE);
                            }
//                            if (!TextUtils.isEmpty(order.getPrice())) {
//                                waresPrice.setText(getString(R.string.rmb) + BigDecimalUtil.format(Double.valueOf(order.getPrice()) / 100));
////                                orderNum.setVisibility(View.VISIBLE);
//                            }
                            if (!TextUtils.isEmpty(order.getOption())) {
                                waresQuantity.setText(order.getOption());
                            }
                            if (!TextUtils.isEmpty(order.getName())) {
                                userName.setText(order.getName());
                            }
                            if (!TextUtils.isEmpty(order.getAddress())) {
                                addressView.setText(order.getAddress());
                            }
                            if (!TextUtils.isEmpty(order.getPhone())) {
                                phoneView.setText(order.getPhone());
                            }
                        } else if (data.getTag() == 3) {
                            toSetAddress.setVisibility(View.VISIBLE);
                            showContent(order);
                        } else if (data.getTag() == 4) {
                            showContent.setVisibility(View.GONE);
//                            orderNum.setVisibility(View.GONE);
                            showContent(order);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ex.printStackTrace();
            }


            @Override
            public void onCancelled(CancelledException cex) {
            }


            @Override
            public void onFinished() {
                activity.hideProgress();
            }
        });
    }

    private void showContent(SmartOrder order) {
        showContent.setVisibility(View.VISIBLE);
        waresIntroView.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(order.getText())) {
            resultText.setText(order.getText());
        } else {
            resultText.setText("");
            if (TextUtils.isEmpty(order.getImgurl())) {
                showContent.setVisibility(View.GONE);
            }
        }
        if (TextUtils.isEmpty(order.getImgurl())) {
            resultImageView.setVisibility(View.GONE);
            return;
        }
        Glide.with(getActivity()).asBitmap().load(order.getImgurl()).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                ViewGroup.LayoutParams params = resultImageView.getLayoutParams();
//                int height = (int) (ScreenUtils.getScreenHeight(getActivity()) / 4.5);
                int width = (int) (screenWidth*.9);
                int height = resource.getHeight()*width/resource.getWidth();
                params.width = width;
                params.height = height;
                resultImageView.setLayoutParams(params);
                resultImageView.setImageBitmap(resource);
                resultImageView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initSpeech() {
        ttsHelper = new TTSHelper(getActivity());
        SpeechUtility.createUtility(getActivity(),
                getResources().getString(R.string.xunfei_appid));
        initIfey();
    }

    public void toSpeech() {
        ifeyBtn = new IfeyVoiceWidget1(getActivity());
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.RECORD_AUDIO).subscribe(new Consumer<Boolean>() {
            public void accept(Boolean aBoolean) {
                if (aBoolean) {
                    initSpeech();
                } else {
                    Toast.makeText(getActivity(), R.string.no_permission, Toast.LENGTH_SHORT).show();
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
            }

            @Override
            public void onStateChanged(boolean recording) {
                if (recording) {
//                    start();
                } else {
//                    stop();
                }
            }
        }, null, false);
        ifeyBtn.setOnVolumeChangeListener(new IfeyVoiceWidget1.OnVolumeChangeListener() {
            @Override
            public void onVolumeChanged(int progress, short[] data) {
//                Log.d(TAG, "onVolumeChanged: "+progress);
                switch (progress / 2) {
//                    case 0:
//                    case 1:
//                        imageRecord.setImageResource(R.mipmap.mai01);
//                        break;
//                    case 2:
//                    case 3:
//                        imageRecord.setImageResource(R.mipmap.mai02);
//                        break;
//                    case 4:
//                    case 5:
//                        imageRecord.setImageResource(R.mipmap.mai03);
//                        break;
//                    case 6:
//                    case 7:
//                    case 8:
//                        imageRecord.setImageResource(R.mipmap.mai04);
//                        break;
                }
            }
        });
        ifeyBtn.setOnTextReceiverListener(new IfeyVoiceWidget1.OnTextReceiverListener() {
            @Override
            public void TextReceiver(String text) {
            }
        });
    }

    private StringBuilder textBuilder;

    private void startRecord() {
        textBuilder = new StringBuilder();
        ifeyBtn.start();
        requestText.setText("");
        imageRecord.setImageResource(R.mipmap.mai01);
//        Glide.with(getApplicationContext()).load(R.mipmap.recording).into(imageRecord);
    }

    private void startPlayerAnim() {
//		animLayout.setVisibility(View.VISIBLE);
//		imageRecord.setImageResource(R.drawable.bear_speaking);
//		animationDrawable = (AnimationDrawable) imageRecord.getDrawable();
//		animationDrawable.start();
    }

    private void stopRecord() {
        if (null != ifeyBtn) {
            ifeyBtn.stop();
        }
//        imageRecord.setImageResource(R.mipmap.mai);
    }

    @Event(R.id.shop_car)
    private void shopCar(View view) throws Exception {
    }


    private void handleContent(String content) {
        if (requestText.getVisibility() == View.GONE) {
            requestText.setVisibility(View.VISIBLE);
        }
        requestText.setText(content);
        search(content);
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
                    Toast.makeText(getActivity(), R.string.no_permission, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ttsHelper.isSpeaking()) {
            ttsHelper.stop();
        }
    }
    @Override
    public boolean showMoreClickItem() {
        //是否在长按时显示更多选项
        //return true 显示，false 不显示
        return true;
    }


//        @Override
//    public List<IClickActions> getMoreClickActions() {
//        List<IClickActions> actions = new ArrayList();
//        actions.addAll(super.getMoreClickActions());
//        actions.add(1, new FavClickActions());
//        return actions;
//    }

}
