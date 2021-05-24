package com.ilesson.ppim.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.Options;
import com.ilesson.ppim.entity.PaySuccess;
import com.ilesson.ppim.entity.Produce;
import com.ilesson.ppim.entity.Shop;
import com.ilesson.ppim.entity.SpecilInfo;
import com.ilesson.ppim.entity.WaresDetialData;
import com.ilesson.ppim.utils.BigDecimalUtil;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.PPScreenUtils;
import com.ilesson.ppim.utils.RecyclerViewSpacesItemDecoration;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.utils.TextUtil;
import com.ilesson.ppim.view.ScrollListView;
import com.youth.banner.Banner;
import com.youth.banner.loader.ImageLoader;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.rong.eventbus.EventBus;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.Conversation;

import static com.ilesson.ppim.activity.LoginActivity.USER_PHONE;

/**
 * Created by potato on 2020/3/10.
 */
@ContentView(R.layout.activity_wares_detail)
public class WareDetailActivity extends BaseActivity {


    @ViewInject(R.id.options_recyclerview)
    private ScrollListView optionsRecyclerview;
    @ViewInject(R.id.detail_recylerview)
    private ScrollListView detailRecylerview;
    @ViewInject(R.id.result_image)
    private ImageView resultImageView;
    @ViewInject(R.id.product_name)
    private TextView productName;
    @ViewInject(R.id.banner)
    private Banner banner;
    private String myId;
    private String groupId;
    private Shop shop;
    public static final String PRODUCT_ID = "product_id";
    private int width;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarLightMode(this, true);
        EventBus.getDefault().register(this);
        width = PPScreenUtils.getScreenWidth(WareDetailActivity.this);
//        token = SPUtils.get(LOGIN_TOKEN,"");
        myId = SPUtils.get(USER_PHONE, "");
        groupId = getIntent().getStringExtra(PRODUCT_ID);
        LinearLayoutManager manager = new LinearLayoutManager(this){@Override
        public boolean canScrollVertically() {
            return false;
        }};
//        manager.setStackFromEnd(true);//设置从底部开始，最新添加的item每次都会显示在最下面
        optionsRecyclerview.setLayoutManager(manager);
        detailRecylerview.setLayoutManager(new LinearLayoutManager(this){@Override
        public boolean canScrollVertically() {
            return false;
        }});

        HashMap<String, Integer> stringIntegerHashMap = new HashMap<>();
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.TOP_DECORATION, PPScreenUtils.dip2px(this, 4));
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.BOTTOM_DECORATION, PPScreenUtils.dip2px(this, 4));
        optionsRecyclerview.addItemDecoration(new RecyclerViewSpacesItemDecoration(stringIntegerHashMap));

        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.TOP_DECORATION, PPScreenUtils.dip2px(this, 9));
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.BOTTOM_DECORATION, PPScreenUtils.dip2px(this, 9));
        detailRecylerview.addItemDecoration(new RecyclerViewSpacesItemDecoration(stringIntegerHashMap));
        detailRecylerview.setHasFixedSize(true);
        detailRecylerview.setNestedScrollingEnabled(false);
        loadData();
    }

    private static final String TAG = "WareDetailActivity";

    @Event(value = R.id.back_btn)
    private void back_btn(View view) {
        finish();
    }

    @Event(value = R.id.buy_btn)
    private void tobuy(View view) {
        if (null == produce) {
            return;
        }
        Intent intent = new Intent(this, BuyActivity.class);
        intent.putExtra(PRODUCT_ID, produce.getId());
        startActivity(intent);
    }

    @Event(value = R.id.call_server)
    private void callServer(View view) {
        if (shop == null) {
            return;
        }

        String serverId = TextUtil.getServerId(shop.getShopkeeper());
        RongIM.getInstance().startConversation(this, Conversation.ConversationType.PRIVATE,serverId,String.format(getResources().getString(R.string.custom_server),shop.getName()));
//        RongIM.getInstance().startConversation(this, Conversation.ConversationType.PRIVATE, shop.getShopkeeper(), shop.getName() + getResources().getString(R.string.custom_server));
    }

    private void loadData() {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.PRODUCE);
        params.addBodyParameter("action", "detail");
        params.addBodyParameter("produce", groupId);
        Log.d(TAG, "loadData: " + params.toString());
        showProgress();
        x.http().post(params, new Callback.CacheCallback<String>() {
            @Override
            public boolean onCache(String result) {
                readJson(result);
                return false;
            }

            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: " + result);
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

    private WaresDetialData waresDetialData;
    private Produce produce;

    private void readJson(String json) {
        try {
            BaseCode<WaresDetialData> base = new Gson().fromJson(
                    json,
                    new TypeToken<BaseCode<WaresDetialData>>() {
                    }.getType());
            if (base.getCode() == 0) {
                waresDetialData = base.getData();
                List<Options> options = waresDetialData.getOptions();
                optionsRecyclerview.setAdapter(new OptionsAdapter(options));
                HashMap<String, String> map = waresDetialData.getAttributes();
                List<SpecilInfo> specilInfos = new ArrayList();
                Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
                Map.Entry<String, String> entry;
                while (iterator.hasNext()) {
                    SpecilInfo specilInfo = new SpecilInfo();
                    entry = iterator.next();
                    specilInfo.setKey(entry.getKey() + ": ");
                    specilInfo.setValue(entry.getValue());
                    specilInfos.add(specilInfo);
                }
                produce = waresDetialData.getProduce();
                shop = waresDetialData.getShop();
                detailRecylerview.setAdapter(new SpecilAdapter(specilInfos));
                productName.setText(produce.getName());
                showBanner(options);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showBanner(List<Options> options) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        params.width = width;
        params.height = width;
        banner.setLayoutParams(params);
        List images = new ArrayList();
        for (Options option : options) {
            images.add(option.getImage());
        }
        banner.setImageLoader(new GlideImageLoader());
        //设置图片集合
        banner.setImages(images);
        banner.start();
    }

    class OptionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        List<Options> datas = new ArrayList<>();

        private static final int TYPE_ITEM = 1;

        public OptionsAdapter(List<Options> data) {
            this.datas = data;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = getLayoutInflater().inflate(R.layout.product_options_item, parent, false);
            return new OptionsAdapter.ItemViewHolder(itemView);
        }


        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            //绑定数据
            if (holder instanceof OptionsAdapter.ItemViewHolder) {
                OptionsAdapter.ItemViewHolder itemViewHolder = (OptionsAdapter.ItemViewHolder) holder;
                final Options order = datas.get(position);
                Glide.with(getApplicationContext()).load(order.getImage()).into(itemViewHolder.pic);
                itemViewHolder.waresName.setText(order.getName());
                itemViewHolder.waresPrice.setText(String.format(getResources().getString(R.string.rmb_format), BigDecimalUtil.format(Double.valueOf(order.getPrice()) / 100)) + "/" + order.getUnit());
                itemViewHolder.pic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ImagePreviewActivity.startPreview(WareDetailActivity.this,order.getImage());
                    }
                });
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

        class ItemViewHolder extends RecyclerView.ViewHolder {

            TextView waresName, waresPrice;
            ImageView pic;

            public ItemViewHolder(@NonNull View itemView) {
                super(itemView);
                pic = itemView.findViewById(R.id.wares_img);
                waresName = itemView.findViewById(R.id.wares_name);
                waresPrice = itemView.findViewById(R.id.price);
            }
        }
    }

    class SpecilAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        List<SpecilInfo> datas = new ArrayList<>();

        private static final int TYPE_ITEM = 1;

        public SpecilAdapter(List<SpecilInfo> data) {
            this.datas = data;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = getLayoutInflater().inflate(R.layout.single_text_item, parent, false);
            return new ItemViewHolder(itemView);
        }


        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ItemViewHolder) {
                ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
                SpecilInfo info = datas.get(position);
                itemViewHolder.textView.setText(TextUtil.getSpecialText(WareDetailActivity.this, info.getKey(), info.getValue()));
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

        class ItemViewHolder extends RecyclerView.ViewHolder {

            TextView textView;

            public ItemViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.textView);
            }
        }
    }

    class GlideImageLoader extends ImageLoader {
        @Override
        public void displayImage(Context context, final Object path, final ImageView resultImageView) {
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
//                params.width = width;
//                params.height = width;
//                resultImageView.setLayoutParams(params);
            Glide.with(context)
                    .load(path)
//                    .fitCenter()
                    .into(resultImageView);
            resultImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImagePreviewActivity.startPreview(WareDetailActivity.this,path.toString());
                }
            });


//            Glide.with(WareDetailActivity.this).asBitmap().load(produce.getImage_intro()).into(new SimpleTarget<Bitmap>() {
//                @Override
//                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
//                    ViewGroup.LayoutParams params = resultImageView.getLayoutParams();
//                    params.width = width;
//                    params.height = width;
//                    resultImageView.setLayoutParams(params);
//                    resultImageView.setImageBitmap(resource);
//                    resultImageView.setScaleType(ImageView.ScaleType.FIT_XY);
//                    resultImageView.setVisibility(View.VISIBLE);
//                }
//            });
        }
    }

    public void onEventMainThread(PaySuccess var) {
        if (!isFinishing()) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
