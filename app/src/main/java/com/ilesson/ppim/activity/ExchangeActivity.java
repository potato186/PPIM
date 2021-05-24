package com.ilesson.ppim.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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
import com.ilesson.ppim.view.RoundImageView;
import com.ilesson.ppim.view.TagCloudView;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import io.rong.eventbus.EventBus;

import static com.ilesson.ppim.activity.AddressListActivity.ADDRESS_DETAIL;
import static com.ilesson.ppim.activity.LoginActivity.USER_PHONE;
import static com.ilesson.ppim.activity.ScoreDetailActivity.SCORE_INFO;

/**
 * Created by potato on 2020/3/10.
 */
@ContentView(R.layout.activity_exchange)
public class ExchangeActivity extends BaseActivity {

    @ViewInject(R.id.no_address_layout)
    private View noAddress;
    @ViewInject(R.id.address_layout)
    private View addressLayout;
    @ViewInject(R.id.tag)
    private TextView tag;
    @ViewInject(R.id.user_name)
    private TextView userName;
    @ViewInject(R.id.phone)
    private TextView phone;
    @ViewInject(R.id.address)
    private TextView addressView;
    @ViewInject(R.id.has_score)
    private TextView hasScore;
    @ViewInject(R.id.wares_name)
    private TextView waresName;
    @ViewInject(R.id.wares_quantity)
    private TextView quantity;
    @ViewInject(R.id.wares_score_price)
    private TextView scorePrice;
    @ViewInject(R.id.wares_img)
    private RoundImageView imageView;
    @ViewInject(R.id.least)
    private TextView least;
    @ViewInject(R.id.most)
    private TextView most;
    @ViewInject(R.id.minus)
    private View minus;
    @ViewInject(R.id.add)
    private View add;
    @ViewInject(R.id.tag_cloud_view)
    private TagCloudView tagCloudView;
    @ViewInject(R.id.num)
    private TextView selectNum;
    @ViewInject(R.id.exchange)
    private TextView exchangeBtn;
    @ViewInject(R.id.exchange_score_all)
    private TextView exchangeAll;
//    @ViewInject(R.id.recylerview)
//    private RecyclerView recyclerView;
    public static final String ADDRESS_INFO = "address_info";
    public static final int SET_ADDRESS_SUCCESS_TO_USE = 2;
    private ScoreInfo scoreInfo;
    private int num = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarLightMode(this, true);
        EventBus.getDefault().register(this);
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

    @Event(value = R.id.exchange)
    private void exchange(View view) {
        if(addressInfo==null){
            showToast(R.string.no_address);
            return;
        }
        exchange();
    }

    @Event(value = R.id.wares_img)
    private void wares_img(View view) {
        ImagePreviewActivity.startPreview(this,selection.getImage());
    }

    @Event(value = R.id.minus)
    private void minus(View view) {
        if (num <= selection.getMin()) {
            return;
        }
        num--;
        selectNum.setText(num + "");
        setAllScore();
    }

    @Event(value = R.id.add)
    private void add(View view) {
        if (num >= mostNum) {
            return;
        }
        num++;
        Log.d(TAG, "add: " + num + "");
        selectNum.setText(num + "");
        setAllScore();
    }

    private int total;

    public void setAllScore() {
        total = num * selection.getScoreget();
        String text = String.format(getResources().getString(R.string.all_exchange_score), total) + "";
        int length = String.valueOf(total).length();
        SpannableStringBuilder style = new SpannableStringBuilder(text);
        style.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.helptext_color)), 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        style.setSpan(new ForegroundColorSpan(Color.RED), 3, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        style.setSpan(new RelativeSizeSpan(1.2f), 3, 3 + length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        exchangeAll.setText(style);
        if (scoreInfo.getValue() < total) {
            exchangeBtn.setBackgroundColor(getResources().getColor(R.color.btn_disenable));
            exchangeBtn.setText(R.string.score_not_enough);
            exchangeBtn.setEnabled(false);
        } else {
            exchangeBtn.setBackgroundResource(R.drawable.general_red_theme_selector);
            exchangeBtn.setEnabled(true);
            exchangeBtn.setText(R.string.exchange_now);
        }
    }

    @Event(value = R.id.address_view)
    private void address_view(View view) {
        Intent intent = new Intent();
        if (addressInfo == null) {
            intent.setClass(this, AddressActivity.class);
        } else {
            intent.setClass(this, AddressListActivity.class);
            intent.putExtra(ADDRESS_DETAIL,addressInfo);
        }
        intent.putExtra(ExchangeActivity.ADDRESS_INFO, true);
        startActivityForResult(intent, 0);
    }

    private void loadData() {
        //list&token=%s&page=%s&size=%s
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.SCORE);
        params.addBodyParameter("action", "exchange_info");
//        params.addBodyParameter("sid", "1");
        params.addBodyParameter("sid", scoreInfo.getSid() + "");
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

    private void exchange() {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.SCORE);
        params.addBodyParameter("action", "exchange");
        params.addBodyParameter("subid", selection.getId() + "");
        params.addParameter("score", total + "");
        params.addParameter("num", num + "");
        params.addParameter("aid", addressInfo.getId());
        Log.d(TAG, "loadData: " + params.toString());
        showProgress();
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "exchange onSuccess: " + result);
                BaseCode base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode>() {
                        }.getType());
                if (base.getCode() == 0) {
                    scoreInfo.setValue(scoreInfo.getValue()-total);
                    EventBus.getDefault().post(scoreInfo);
                    showSuccessDialog();
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
                showAdress();
                List<Produces> produces = scoreData.getProduces();
                produce = produces.get(0);
                selections = produce.getSelections();
//                List<Selections> data = new ArrayList<>();
//                data.addAll(selections);
//                data.addAll(selections);
//                data.addAll(selections);
//                for (int i = 0; i < data.size(); i++) {
//                    Random random = new Random();
//                    String name = arr[random.nextInt(arr.length)];
//                    data.get(i).setName(name);
//                }
                RefreshAdapter adapter = new RefreshAdapter(selections);
//                recyclerView.setAdapter(adapter);
                List<String> texts = new ArrayList<>();
                for (Selections options : selections) {
                    texts.add(options.getName().replace("\n",""));
                }
                tagCloudView.setTags(texts);
                tagCloudView.setOnTagClickListener(new TagCloudView.OnTagClickListener() {
                    @Override
                    public void onTagClick(int position) {
                        showProduce(position);
                    }
                });
                showProduce(0);
            }else{
                showToast(base.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int mostNum;

    private void showProduce(int index) {
        total = 0;
        selection = selections.get(index);
        mostNum = scoreInfo.getValue() / selection.getScoreget();
        Glide.with(getApplicationContext()).load(selection.getImage()).into(imageView);
        String name = selection.getName();
        waresName.setText(name);
//        quantity.setText(selection.getDesc());
        hasScore.setText(String.format(getResources().getString(R.string.has_score_num), scoreInfo.getValue()));
        least.setText(String.format(getResources().getString(R.string.least_exchange_num), selection.getMin(), selection.getUnit()));
        most.setText(String.format(getResources().getString(R.string.most_exchange_num), mostNum, selection.getUnit()));
        String text = String.format(getResources().getString(R.string.score_price), selection.getScoreget());
        int length = String.valueOf(selection.getScoreget()).length();
        SpannableStringBuilder style = new SpannableStringBuilder(text);
        style.setSpan(new RelativeSizeSpan(1.2f), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        scorePrice.setText(style);
//        scorePrice.setText(String.format(getResources().getString(R.string.score_price),selection.getScoreget()));
        num = selection.getMin();
        selectNum.setText(num + "");
        setAllScore();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: resultCode=" + resultCode);
        if (resultCode == SET_ADDRESS_SUCCESS_TO_USE) {
            if (data != null) {
                addressInfo = (AddressInfo) data.getSerializableExtra(ADDRESS_INFO);
                showAdress();
            }
        }
    }

    private void showAdress() {
        if(null==noAddress){
            return;
        }
        if (null == addressInfo) {
            noAddress.setVisibility(View.VISIBLE);
            addressLayout.setVisibility(View.GONE);
        } else {
            noAddress.setVisibility(View.GONE);
            addressLayout.setVisibility(View.VISIBLE);
            tag.setText(addressInfo.getTag());
            userName.setText(addressInfo.getName());
            phone.setText(addressInfo.getPhone());
            String address = addressInfo.getAddress();
            if(!TextUtils.isEmpty(addressInfo.getProvince())&&!address.contains(addressInfo.getCity())){
                address=addressInfo.getProvince()+addressInfo.getCity()+address;
            }
            addressView.setText(address);
            addressInfo.setAddress(address);
        }
    }

    public void onEventMainThread(ExchangeAddress var) {
        addressInfo = var.getAddressInfo();
        showAdress();
    }

    class RefreshAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        List<Selections> datas = new ArrayList<>();

        private static final int TYPE_ITEM = 1;
        private int mLoadMoreStatus = 0;

        public RefreshAdapter(List<Selections> data) {
            this.datas = data;
        }


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = getLayoutInflater().inflate(R.layout.score_section, parent, false);
            return new ItemViewHolder(itemView);
        }


        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            //绑定数据
            if (holder instanceof ItemViewHolder) {
                ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
                Selections scoreInfo = datas.get(position);
                if (0 == position) {
                    lastItem = itemViewHolder.textView;
                    itemViewHolder.textView.setBackgroundResource(R.drawable.background_theme_corner20);
                    itemViewHolder.textView.setTextColor(getResources().getColor(R.color.white));
                }
                itemViewHolder.textView.setText(scoreInfo.getName());
            }
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }

        @Override
        public int getItemViewType(int position) {
            return TYPE_ITEM;
        }

        private void setDefaultView(TextView textView) {
            if (null == textView) {
                return;
            }
            textView.setBackgroundResource(R.drawable.background_gray_corner20);
            textView.setTextColor(getResources().getColor(R.color.gray_text333_color));
        }

        private void setSelectView(TextView textView) {
            textView.setBackgroundResource(R.drawable.background_theme_corner20);
            textView.setTextColor(getResources().getColor(R.color.white));
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {

            TextView textView;

            public ItemViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.selecion_item);
                Log.d(TAG, "ItemViewHolder:getLayoutPosition= " + getLayoutPosition());

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (lastItem == textView) {
                            return;
                        }
                        setDefaultView(lastItem);
                        setSelectView(textView);
                        lastItem = textView;
                        showProduce(getLayoutPosition());
                    }
                });
            }
        }

        private TextView lastItem;
    }

    private void showSuccessDialog(){
        View view = getLayoutInflater().inflate(R.layout.privacy_dialog,null);
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        android.view.WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        p.width = (int) (PPScreenUtils.getScreenWidth(this) * 0.85);
        p.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setAttributes(p);
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(R.string.exchange_success);
        TextView scoreTv = (TextView) view.findViewById(R.id.content);
        String tips = String.format(getResources().getString(R.string.exchange_detail),shop.getName(),selection.getName(),num,selection.getUnit());
        scoreTv.setText(tips);
        TextView left = (TextView) view.findViewById(R.id.left_btn);
        TextView right = (TextView) view.findViewById(R.id.right_btn);
        left.setText(R.string.scan_ordre);
        right.setText(R.string.continue_exchange);
        dialog.setCanceledOnTouchOutside(false);
        scoreTv.setMovementMethod(LinkMovementMethod.getInstance());
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startActivity(new Intent(ExchangeActivity.this,WareOrderListActivity.class));
                finish();
            }
        });
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
