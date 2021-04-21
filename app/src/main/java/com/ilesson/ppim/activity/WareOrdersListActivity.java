/*
package com.ilesson.ppim.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.WaresOrder;
import com.ilesson.ppim.utils.BigDecimalUtil;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.PPScreenUtils;
import com.ilesson.ppim.utils.RecyclerViewSpacesItemDecoration;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.utils.TextUtil;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.rong.imkit.RongIM;
import io.rong.imlib.model.Conversation;

import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;
import static com.ilesson.ppim.activity.LoginActivity.USER_PHONE;

*/
/**
 * Created by potato on 2020/3/10.
 *//*

@ContentView(R.layout.activity_wares_order_list)
public class WareOrdersListActivity extends BaseActivity {


    private String token;

    @ViewInject(R.id.recylerview)
    private RecyclerView recyclerView;
    @ViewInject(R.id.empty_layout)
    private View emptyLayout;
    @ViewInject(R.id.swipeLayout)
    private SwipeRefreshLayout swipeLayout;
    private int mCurrentPage;
    private int mPageRows = 10;
    private List<WaresOrder> mList;
    private RefreshAdapter mAdapter;
    private String myId;
    private boolean loading;
    private boolean noNext;
    private WaresOrder footData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarLightMode(this, true);
        token = SPUtils.get(LOGIN_TOKEN, "");
        myId = SPUtils.get(USER_PHONE, "");
        mList = new ArrayList<>();
        mAdapter = new RefreshAdapter(mList);
        LinearLayoutManager manager = new LinearLayoutManager(this);
//        manager.setStackFromEnd(true);//设置从底部开始，最新添加的item每次都会显示在最下面
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(mAdapter);
        footData = new WaresOrder();
//        swipeLayout.setEnabled(false);
        HashMap<String, Integer> stringIntegerHashMap = new HashMap<>();
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.TOP_DECORATION, PPScreenUtils.dip2px(this, 3));
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.RIGHT_DECORATION, PPScreenUtils.dip2px(this, 10));
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.LEFT_DECORATION, PPScreenUtils.dip2px(this, 10));
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.BOTTOM_DECORATION, PPScreenUtils.dip2px(this, 3));
        recyclerView.addItemDecoration(new RecyclerViewSpacesItemDecoration(stringIntegerHashMap));
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData(true);
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                int totalItemCount = layoutManager.getItemCount();

                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();

                if (!loading&&!noNext&& totalItemCount < (lastVisibleItem + Constants.VISIBLE_THRESHOLD)
                ) {
                    mCurrentPage++;
                    mList.add(footData);
                    loadData(false);
                    loading = true;
                }
            }
        });
        loadData(true);
    }

    private static final String TAG = "OrderListActivity";

    @Event(value = R.id.back_btn)
    private void back_btn(View view) {
        finish();
    }

    private void loadData(final boolean clear) {
        //list&token=%s&page=%s&size=%s
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.ORDER);
        params.addBodyParameter("action", "list");
        params.addBodyParameter("page", mCurrentPage + "");
        params.addBodyParameter("size", mPageRows + "");
        String phone = SPUtils.get(USER_PHONE, "");
        params.addParameter("phone", phone);
        Log.d(TAG, "loadData: " + params.toString());
        showProgress();
        x.http().post(params, new Callback.CacheCallback<String>() {
            @Override
            public boolean onCache(String result) {
                readJson(result, clear);
                return false;
            }

            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "loadData onSuccess: " + result);
                readJson(result, clear);
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
                if(mList.contains(footData)){
                    mList.remove(footData);
                }
                loading = false;
//                swipeLayout.setRefreshing(false);
                hideProgress();
            }
        });
    }

    private void readJson(String json, boolean clear) {
        try {
            BaseCode<List<WaresOrder>> base = new Gson().fromJson(
                    json,
                    new TypeToken<BaseCode<List<WaresOrder>>>() {
                    }.getType());
            if (base.getCode() == 0) {
                List<WaresOrder> data = base.getData();
                if(data==null||data.size()<mPageRows){
                    noNext = true;
                }else{
                    noNext = false;
                }
                if (clear) {
                    mList.clear();
                }
                mList.addAll(data);
                mAdapter.notifyDataSetChanged();
                if (mList.isEmpty()) {
                    emptyLayout.setVisibility(View.VISIBLE);
                } else {
                    emptyLayout.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class RefreshAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        List<WaresOrder> datas = new ArrayList<>();

        private static final int TYPE_TOP = 0;
        private static final int TYPE_ITEM = 1;
        private static final int TYPE_FOOTER = 2;

        //上拉加载更多
        public static final int PULLUP_LOAD_MORE = 0;
        //正在加载中
        public static final int LOADING_MORE = 1;
        //没有加载更多 隐藏
        public static final int NO_LOAD_MORE = 2;

        //上拉加载更多状态-默认为0
        private int mLoadMoreStatus = 0;

        public RefreshAdapter(List<WaresOrder> data) {
            this.datas = data;
        }

        */
/**
         * a
         * 更新加载更多状态
         *
         * @param status
         *//*

        public void changeMoreStatus(int status) {
            mLoadMoreStatus = status;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //实现ViewHolder
            if (viewType == TYPE_ITEM) {
                View itemView = getLayoutInflater().inflate(R.layout.ware_order_item, parent, false);
                return new ItemViewHolder(itemView);
            } else if (viewType == TYPE_FOOTER) {
                View itemView = getLayoutInflater().inflate(R.layout.foot_view, parent, false);
                return new FooterViewHolder(itemView);
            }
//            else{
//                View itemView = mInflater.inflate(R.layout.note_type_item, parent, false);
//                return new TopViewHolder(itemView);
//            }
            return null;
        }


        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            //绑定数据
            if (holder instanceof ItemViewHolder) {
                ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
                WaresOrder order = datas.get(position);
                Glide.with(WareOrdersListActivity.this).load(order.getIcon()).into(itemViewHolder.pic);
                itemViewHolder.waresName.setText(order.getName());
                itemViewHolder.waresNum.setText(String.format(getResources().getString(R.string.num_format), order.getNum()));
                itemViewHolder.waresInfo.setText(order.getSubDesc());
                if (!TextUtils.isEmpty(order.getFei())) {
                    itemViewHolder.express.setText(String.format(getResources().getString(R.string.express_fee), BigDecimalUtil.format(Double.valueOf(order.getFei()) / 100)));
                }
                if (order.getTrade_no().startsWith("ex")) {
                    String text = String.format(getResources().getString(R.string.score_price), Integer.valueOf(order.getPerPrice()));
                    int length = order.getPrice().length();
                    SpannableStringBuilder style = new SpannableStringBuilder(text);
                    style.setSpan(new RelativeSizeSpan(1.2f), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                    itemViewHolder.price.setText(style);
                    itemViewHolder.unitPrice.setText(text);
                    itemViewHolder.orderInfo.setText(R.string.score_exchange_order);
                    itemViewHolder.waresPrice.setVisibility(View.GONE);
                    itemViewHolder.allPrice.setText(String.format(getResources().getString(R.string.all_pay_score), Integer.valueOf(order.getPrice())));
                } else {
                    itemViewHolder.waresPrice.setVisibility(View.VISIBLE);
                    double price = Double.valueOf(order.getNum()) * Double.valueOf(order.getPerPrice());
                    itemViewHolder.waresPrice.setText(String.format(getResources().getString(R.string.wares_price), BigDecimalUtil.format(price / 100)));
                    itemViewHolder.unitPrice.setText(String.format(getResources().getString(R.string.rmb_format), BigDecimalUtil.format(Double.valueOf(order.getPerPrice()) / 100)));
                    itemViewHolder.orderInfo.setText(R.string.buy_order);
                    String allPrice = String.format(getResources().getString(R.string.all_fee), BigDecimalUtil.format(Double.valueOf((double) price / 100)) + "");
                    itemViewHolder.allPrice.setText(allPrice);
                }

                if (TextUtils.isEmpty(order.getPostdate())) {
                    itemViewHolder.checkLogistc.setVisibility(View.GONE);
                    itemViewHolder.state.setText(R.string.no_post);
                    itemViewHolder.confirm.setVisibility(View.GONE);
                } else {
                    itemViewHolder.state.setText(R.string.has_post);
                    itemViewHolder.checkLogistc.setVisibility(View.VISIBLE);
                    itemViewHolder.confirm.setVisibility(View.VISIBLE);
                }
                if (!TextUtils.isEmpty(order.getConfirm_post())) {
                    itemViewHolder.state.setText(R.string.confirm_take_post);
                    itemViewHolder.confirm.setVisibility(View.GONE);
                } else {
                    if (!TextUtils.isEmpty(order.getPostdate())) {
                        itemViewHolder.confirm.setVisibility(View.VISIBLE);
                    }
                }
            }
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }

        @Override
        public int getItemViewType(int position) {//position是mDatas的下标
//            if(position==0){
//                return TYPE_TOP;
//            }
//            else
            if (TextUtils.isEmpty(datas.get(position).getUphone())) {
                //最后一个item设置为footerView
                return TYPE_FOOTER;
            } else {
                return TYPE_ITEM;
            }
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {

            TextView userName, orderNo, phone, address, waresInfo, state, express, allPrice, waresName, waresPrice, waresQuantity, orderInfo, callServer, confirm, checkLogistc, waresNum, unitPrice;
            View logisticNameView, logisticNoView;
            ImageView pic;

            public ItemViewHolder(@NonNull View itemView) {
                super(itemView);
//                userName = itemView.findViewById(R.id.consignee);
                pic = itemView.findViewById(R.id.wares_img);
                waresName = itemView.findViewById(R.id.wares_name);
                waresPrice = itemView.findViewById(R.id.wares_price);
                waresQuantity = itemView.findViewById(R.id.wares_quantity);
                unitPrice = itemView.findViewById(R.id.unit_price);
                allPrice = itemView.findViewById(R.id.all_price);
                express = itemView.findViewById(R.id.express_fee_price);
                waresNum = itemView.findViewById(R.id.num);
                orderInfo = itemView.findViewById(R.id.order_info);
                callServer = itemView.findViewById(R.id.call_server);
                confirm = itemView.findViewById(R.id.confirm);
                state = itemView.findViewById(R.id.order_state);
                waresInfo = itemView.findViewById(R.id.wares_info);
                checkLogistc = itemView.findViewById(R.id.check_logistc);
                allPrice.setTextColor(getResources().getColor(R.color.gray_text333_color));
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        WaresOrder order = datas.get(getLayoutPosition());
                        Intent intent = new Intent(WareOrdersListActivity.this, WaresOrderDetailctivity.class);
                        intent.putExtra(WaresOrderDetailctivity.ORDER_DETAIL, order);
                        startActivity(intent);
                    }
                });
                callServer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        WaresOrder order = datas.get(getLayoutPosition());
//                        serverId = base.getData();
//                        new IMUtils().requestShopServer(null,order.getId());

                        String serverId = TextUtil.getServerId(order.getShopkeeper());
                        RongIM.getInstance().startConversation(WareOrdersListActivity.this, Conversation.ConversationType.PRIVATE, serverId, String.format(getResources().getString(R.string.custom_server), order.getName()));
//                        RongIM.getInstance().startConversation(WareOrderListActivity.this, Conversation.ConversationType.PRIVATE,order.getShopkeeper(),order.getName()+getString(R.string.custom_server));
                    }
                });
                checkLogistc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        WaresOrder order = datas.get(getLayoutPosition());
                        Intent intent = new Intent(WareOrdersListActivity.this, WaresLogistcDetailctivity.class);
                        intent.putExtra(WaresOrderDetailctivity.ORDER_DETAIL, order);
                        startActivity(intent);
                    }
                });
                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showConfirm(confirm, datas.get(getLayoutPosition()));
                    }
                });
            }

        }

        class FooterViewHolder extends RecyclerView.ViewHolder {
            View layout;

            public FooterViewHolder(View itemView) {
                super(itemView);
                layout = itemView.findViewById(R.id.layout);
            }
        }
    }

    private void showConfirm(final View confirm, final WaresOrder order) {
        View view = getLayoutInflater().inflate(R.layout.confirm_take_delivery_dialog, null);
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        android.view.WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        p.width = (int) (PPScreenUtils.getScreenWidth(this) * 0.85);
        p.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setAttributes(p);
        Glide.with(getApplicationContext()).load(order.getIcon()).into((ImageView) view.findViewById(R.id.wares_img));
        view.findViewById(R.id.left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.right_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmOrder(confirm, order);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void confirmOrder(final View confirm, final WaresOrder order) {
        //确认收货：https://pp.fangnaokeji.com:9443/pp/order?action=confirm&oid=689
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.ORDER);
        params.addBodyParameter("action", "confirm_post");
        params.addBodyParameter("oid", order.getId());
        Log.d(TAG, "loadData: " + params.toString());
        showProgress();
        x.http().post(params, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: " + result);
                BaseCode base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode>() {
                        }.getType());
                if (base.getCode() == 0) {
                    order.setConfirm_post("1");
                    int index = mList.indexOf(order);
                    mAdapter.notifyItemChanged(index);
                    confirm.setVisibility(View.GONE);
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
                swipeLayout.setRefreshing(false);
                hideProgress();
            }
        });
    }

     class DataAdapter extends BaseQuickAdapter<WaresOrder, BaseViewHolder> {
        private Context context;
        public DataAdapter(Context context, @Nullable List<WaresOrder> data) {
            super(R.layout.ware_order_item, data);
            this.context=context;
        }

        @Override
        protected void convert(BaseViewHolder helper, WaresOrder order) {
//            helper.setText(R.id.MyTV1,item.getTitle());     //通过id
//            ImageView view = helper.getView(R.id.IMage1);
//            Glide.with(context).load(item.getImage()).into(view);
//            helper.addOnClickListener(R.id.IMage1);    //添加子元素点击事件
            Glide.with(WareOrdersListActivity.this).load(order.getIcon()).into((ImageView) helper.getView(R.id.wares_img));
            helper.setText(R.id.wares_name,order.getName()).setText(R.id.num,String.format(getResources().getString(R.string.num_format), order.getNum()))
            .setText(R.id.wares_info,order.getSubDesc());
            TextView waresPrice = helper.getView(R.id.);
            if (!TextUtils.isEmpty(order.getFei())) {
                helper.setText(R.id.express_fee_price,String.format(getResources().getString(R.string.express_fee), BigDecimalUtil.format(Double.valueOf(order.getFei()) / 100)));
            }
            if (order.getTrade_no().startsWith("ex")) {
                String text = String.format(getResources().getString(R.string.score_price), Integer.valueOf(order.getPerPrice()));
                int length = order.getPrice().length();
                SpannableStringBuilder style = new SpannableStringBuilder(text);
                style.setSpan(new RelativeSizeSpan(1.2f), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                    itemViewHolder.price.setText(style);
                helper.setText(R.id.unit_price,text);
                helper.setText(R.id.order_info,R.string.score_exchange_order);
                itemViewHolder.waresPrice.setVisibility(View.GONE);
                itemViewHolder.allPrice.setText(String.format(getResources().getString(R.string.all_pay_score), Integer.valueOf(order.getPrice())));
            } else {
                itemViewHolder.waresPrice.setVisibility(View.VISIBLE);
                double price = Double.valueOf(order.getNum()) * Double.valueOf(order.getPerPrice());
                itemViewHolder.waresPrice.setText(String.format(getResources().getString(R.string.wares_price), BigDecimalUtil.format(price / 100)));
                itemViewHolder.unitPrice.setText(String.format(getResources().getString(R.string.rmb_format), BigDecimalUtil.format(Double.valueOf(order.getPerPrice()) / 100)));
                itemViewHolder.orderInfo.setText(R.string.buy_order);
                String allPrice = String.format(getResources().getString(R.string.all_fee), BigDecimalUtil.format(Double.valueOf((double) price / 100)) + "");
                itemViewHolder.allPrice.setText(allPrice);
            }

            if (TextUtils.isEmpty(order.getPostdate())) {
                itemViewHolder.checkLogistc.setVisibility(View.GONE);
                itemViewHolder.state.setText(R.string.no_post);
                itemViewHolder.confirm.setVisibility(View.GONE);
            } else {
                itemViewHolder.state.setText(R.string.has_post);
                itemViewHolder.checkLogistc.setVisibility(View.VISIBLE);
                itemViewHolder.confirm.setVisibility(View.VISIBLE);
            }
            if (!TextUtils.isEmpty(order.getConfirm_post())) {
                itemViewHolder.state.setText(R.string.confirm_take_post);
                itemViewHolder.confirm.setVisibility(View.GONE);
            } else {
                if (!TextUtils.isEmpty(order.getPostdate())) {
                    itemViewHolder.confirm.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
*/
