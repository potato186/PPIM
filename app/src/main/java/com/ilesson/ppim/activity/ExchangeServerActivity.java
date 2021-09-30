package com.ilesson.ppim.activity;

import static com.ilesson.ppim.activity.LoginActivity.USER_PHONE;
import static com.ilesson.ppim.activity.ScoreDetailActivity.SCORE_INFO;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.AddressInfo;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.ExchangeAddress;
import com.ilesson.ppim.entity.Produces;
import com.ilesson.ppim.entity.ScoreData;
import com.ilesson.ppim.entity.ScoreInfo;
import com.ilesson.ppim.entity.ScoreShop;
import com.ilesson.ppim.entity.Selections;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.PPScreenUtils;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.utils.TextUtil;
import com.ilesson.ppim.view.RoundImageView;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.List;

import io.rong.imkit.RongIM;
import io.rong.imlib.model.Conversation;

/**
 * Created by potato on 2020/3/10.
 */
@ContentView(R.layout.activity_exchange_info)
public class ExchangeServerActivity extends BaseActivity {

    @ViewInject(R.id.has_score)
    private TextView hasScore;
    @ViewInject(R.id.product_name)
    private TextView waresName;
    @ViewInject(R.id.product_detail)
    private TextView detail;
    @ViewInject(R.id.result_image)
    private RoundImageView imageView;
    public static final String ADDRESS_INFO = "address_info";
    public static final int SET_ADDRESS_SUCCESS_TO_USE = 2;
    private ScoreInfo scoreInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarLightMode(this, true);
//        token = SPUtils.get(LOGIN_TOKEN,"");
        scoreInfo = (ScoreInfo) getIntent().getSerializableExtra(SCORE_INFO);
        if (scoreInfo == null) {
            return;
        }
        int max = 25;
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
//        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
//            @Override
//            public int getSpanSize(int position) {
//                String content = selection.getName();
//
//                return content.length();
//            }
//        });
//        recyclerView.setLayoutManager(layoutManager);
        loadData();
    }

    private static final String TAG = "ExchangeActivity";

    @Event(value = R.id.back_btn)
    private void back_btn(View view) {
        finish();
    }

    @Event(value = R.id.confirm)
    private void confirm(View view) {
        String serverId = TextUtil.getServerId(shop.getShopkeeper());
        RongIM.getInstance().startConversation(this, Conversation.ConversationType.PRIVATE,serverId,String.format(getResources().getString(R.string.custom_server),shop.getName()));
    }

    @Event(value = R.id.result_image)
    private void wares_img(View view) {
        if(null==produce){
            return;
        }
        ImagePreviewActivity.startPreview(this,produce.getImageIntro());
    }

    private int total;

    private void loadData() {
        //list&token=%s&page=%s&size=%s
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.SCORE);
        params.addParameter("action", "exchange_info");
//        params.addBodyParameter("sid", "1");
        params.addParameter("sid", scoreInfo.getSid() + "");
        String phone = SPUtils.get(USER_PHONE, "");
        params.addParameter("phone", phone);
        Log.d(TAG, "loadData: " + params.toString());
        showProgress();
        x.http().post(params, new Callback.CacheCallback<String>() {
            @Override
            public boolean onCache(String result) {
//                readJson(result);
                return false;
            }

            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "ExchangeActivity onSuccess: " + result);
                readJson(result);
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


    private List<AddressInfo> addressInfos;
    private AddressInfo addressInfo;
    private Produces produce;
    private List<Produces> produces;
    private List<Selections> selections;
    private Selections selection;
    private ScoreShop shop;
    private void readJson(String json) {
        try {
            BaseCode<ScoreData> base = new Gson().fromJson(
                    json,
                    new TypeToken<BaseCode<ScoreData>>() {
                    }.getType());
            if (base.getCode() == 0) {
                ScoreData scoreData = base.getData();
                addressInfos = scoreData.getAddress();
                shop = scoreData.getShop();
                if (addressInfos == null || addressInfos.isEmpty()) {
                    addressInfo = null;
                }else{
                    addressInfo = addressInfos.get(0);
                }
                List<Produces> produces = scoreData.getProduces();
                produce = produces.get(0);
                selections = produce.getSelections();
                waresName.setText(produce.getName());
                detail.setText(produce.getDetail());
                String text = String.format(getResources().getString(R.string.has_score), scoreInfo.getValue());
                SpannableStringBuilder style = new SpannableStringBuilder(text);
                int length = String.valueOf(scoreInfo.getValue()).length();
                style.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.second_blk_text)), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                style.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.theme_text_color)), 0, 0+length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                style.setSpan(new RelativeSizeSpan(1.2f), 0, 0+length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                hasScore.setText(style);
                Glide.with(ExchangeServerActivity.this).asBitmap().load(produce.getImageIntro()).into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        ViewGroup.LayoutParams params = imageView.getLayoutParams();

                        int maxW = (int) (PPScreenUtils.getScreenWidth(ExchangeServerActivity.this) *.9);
                        params.width = maxW;
                        params.height = resource.getHeight() * maxW / resource.getWidth();
                        imageView.setLayoutParams(params);
                        imageView.setImageBitmap(resource);
                        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    }
                });
            }else{
                showToast(base.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int mostNum;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void onEventMainThread(ExchangeAddress var) {
        addressInfo = var.getAddressInfo();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
