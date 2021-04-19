package com.ilesson.ppim.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.Express;
import com.ilesson.ppim.entity.ExpressInfo;
import com.ilesson.ppim.entity.WaresOrder;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.IMUtils;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.utils.TextUtil;
import com.ilesson.ppim.view.ScrollListView;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.rong.imageloader.core.DisplayImageOptions;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.Conversation;

import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;

/**
 * Created by potato on 2020/3/10.
 */
@ContentView(R.layout.act_logistc_detail)
public class WaresLogistcDetailctivity extends BaseActivity {


    private String token;

    @ViewInject(R.id.recylerview)
    private ScrollListView recyclerView;
    @ViewInject(R.id.icon)
    private ImageView iconView;
//    @ViewInject(R.id.wares_img)
//    private ImageView waresImg;
    @ViewInject(R.id.no)
    private TextView noView;
//    @ViewInject(R.id.wares_name)
//    private TextView waresName;
    @ViewInject(R.id.copy)
    private TextView copy;
    @ViewInject(R.id.name)
    private TextView nameView;
//    @ViewInject(R.id.wares_price)
//    private TextView waresPrice;
    @ViewInject(R.id.order_no)
    private TextView orderNo;
    @ViewInject(R.id.order_address)
    private TextView orderAddress;
    private List<ExpressInfo> mList;
    private RefreshAdapter mAdapter;
    DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
    public static final String ORDER_DETAIL = "order_detail";
    private WaresOrder order;
    private String serverId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarLightMode(this, true);
        builder.cacheInMemory(true).cacheOnDisk(true);
        token = SPUtils.get(LOGIN_TOKEN, "");
        mList = new ArrayList<>();
        order = (WaresOrder) getIntent().getSerializableExtra(ORDER_DETAIL);
        LinearLayoutManager manager = new LinearLayoutManager(this);
//        manager.setStackFromEnd(true);//设置从底部开始，最新添加的item每次都会显示在最下面
        recyclerView.setLayoutManager(manager);
//        swipeLayout.setEnabled(false);
        HashMap<String, Integer> stringIntegerHashMap = new HashMap<>();
//        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.TOP_DECORATION, PPScreenUtils.dip2px(this,3));
//        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.RIGHT_DECORATION, PPScreenUtils.dip2px(this,10));
//        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.LEFT_DECORATION, PPScreenUtils.dip2px(this,10));
//        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.BOTTOM_DECORATION, PPScreenUtils.dip2px(this,3));
//        recyclerView.addItemDecoration(new RecyclerViewSpacesItemDecoration(stringIntegerHashMap));
//        postState.setText(R.string.has_post);
//        postState.setTextColor(getResources().getColor(R.color.theme_color));
        if (!TextUtils.isEmpty(order.getTransaction_id())) {
            orderNo.setText(String.format(getResources().getString(R.string.post_no),order.getTransaction_id()));
        }
        if (!TextUtils.isEmpty(order.getUaddress())) {
            orderAddress.setText(String.format(getResources().getString(R.string.post_address),order.getUaddress()));
        }

//        waresName.setText(order.getName());
//        waresPrice.setText(getString(R.string.rmb) + BigDecimalUtil.format(Double.valueOf(order.getPrice()) / 100));
//        waresQuantity.setText(order.getInfo());
//        Glide.with(WaresLogistcDetailctivity.this).load(order.getIcon()).into(waresImg);
        loadData();
    }

    private static final String TAG = "WaresOrderDetailctivity";

    @Event(value = R.id.back_btn)
    private void back_btn(View view) {
        finish();
    }

    @Event(value = R.id.call_server)
    private void call_server(View view) {
        getServer();
    }

    @Event(value = R.id.copy)
    private void copy(View view) {
        String num = noView.getText().toString();
        if (TextUtils.isEmpty(num)) {
            return;
        }
        //获取剪贴板管理器：
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
// 创建普通字符型ClipData
        ClipData mClipData = ClipData.newPlainText("Label", num);
// 将ClipData内容放到系统剪贴板里。
        cm.setPrimaryClip(mClipData);
    }

    private void loadData() {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.EXPRESS);
        params.addParameter("action", "query");
        params.addParameter("no", order.getPostno());
//        params.addParameter("no", "JD0036920686928");
//        params.addParameter("test", "true");
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
                showLogist();
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

    private void showLogist(){
        if (!TextUtils.isEmpty(order.getPostname())) {
            nameView.setText(order.getPostname());
        }
        if (!TextUtils.isEmpty(order.getPostno())) {
            noView.setText(order.getPostno());
        }
        iconView.setImageResource(R.mipmap.logist_icon);
    }
    private void getServer() {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.EXPRESS);
        params.addParameter("action", "server");
        params.addParameter("oid", order.getId());
//        params.addParameter("no", waresOrder.getTrade_no());
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
                Log.d(TAG, "getServer: " + result);
                BaseCode<String> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<String>>() {
                        }.getType());
                if (base.getCode() == 0) {
                    serverId = base.getData();
                    new IMUtils().requestShopServer(null, order.getId());
                    String serverId = TextUtil.getServerId(order.getShopkeeper());
                    RongIM.getInstance().startConversation(WaresLogistcDetailctivity.this, Conversation.ConversationType.PRIVATE,serverId,String.format(getResources().getString(R.string.custom_server),order.getName()));
//
//                    RongIM.getInstance().startConversation(WaresLogistcDetailctivity.this, Conversation.ConversationType.PRIVATE, serverId, order.getName() + getString(R.string.custom_server));
                }
//                readJson(result);
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

    private void readJson(String json) {
        try {
            BaseCode<Express> base = new Gson().fromJson(
                    json,
                    new TypeToken<BaseCode<Express>>() {
                    }.getType());
            if (base.getCode() == 0) {
                Express data = base.getData();
                if (null == data) {
                    return;
                }
                if (!TextUtils.isEmpty(data.getLogo())) {
                    Glide.with(WaresLogistcDetailctivity.this).load(data.getLogo()).into(iconView);
                }
                if (!TextUtils.isEmpty(data.getTypename())) {
                    nameView.setText(data.getTypename());
                }
                if (!TextUtils.isEmpty(data.getNumber())) {
                    noView.setText(data.getNumber());
                }
                mAdapter = new RefreshAdapter(data.getList());
                recyclerView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
            } else {
                showLogist();
            }
        } catch (Exception e) {
            showLogist();
            e.printStackTrace();
        }
    }

    class RefreshAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        List<ExpressInfo> datas = new ArrayList<>();

        private static final int TYPE_ITEM = 1;

        public RefreshAdapter(List<ExpressInfo> data) {
            this.datas = data;
        }

        /**
         * a
         * 更新加载更多状态
         *
         * @param status
         */
        public void changeMoreStatus(int status) {
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //实现ViewHolder
            View itemView = getLayoutInflater().inflate(R.layout.express_item, parent, false);
            return new ItemViewHolder(itemView);
        }


        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            //绑定数据
            if (holder instanceof ItemViewHolder) {
                ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
                ExpressInfo order = datas.get(position);
                itemViewHolder.date.setText(order.getTime());
                itemViewHolder.state.setText(order.getStatus());
                if (position == 0) {
                    itemViewHolder.tag1.setVisibility(View.GONE);
                    itemViewHolder.tag3.setVisibility(View.VISIBLE);
                } else {
                    itemViewHolder.tag3.setVisibility(View.GONE);
                }
                if (position == datas.size() - 1) {
                    itemViewHolder.tag2.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }

        @Override
        public int getItemViewType(int position) {//position是mDatas的下标
            return TYPE_ITEM;
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {

            TextView date, state, tag1, tag2, tag3;

            public ItemViewHolder(@NonNull View itemView) {
                super(itemView);
                date = itemView.findViewById(R.id.date);
                state = itemView.findViewById(R.id.state);
                tag1 = itemView.findViewById(R.id.tag1);
                tag2 = itemView.findViewById(R.id.tag2);
                tag3 = itemView.findViewById(R.id.tag3);
            }

        }
    }
}
