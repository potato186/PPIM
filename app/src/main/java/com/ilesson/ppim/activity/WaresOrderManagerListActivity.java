package com.ilesson.ppim.activity;

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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.WaresOrder;
import com.ilesson.ppim.utils.BigDecimalUtil;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.Dateuitls;
import com.ilesson.ppim.utils.RecyclerViewSpacesItemDecoration;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.utils.PPScreenUtils;

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
import static com.ilesson.ppim.activity.LoginActivity.USER_PHONE;

/**
 * Created by potato on 2020/3/10.
 */
@ContentView(R.layout.activity_shopkeeper_order_list)
public class WaresOrderManagerListActivity extends BaseActivity {

    private String token;

    @ViewInject(R.id.recylerview)
    private RecyclerView recyclerView;
    @ViewInject(R.id.swipeLayout)
    private SwipeRefreshLayout swipeLayout;
    @ViewInject(R.id.empty_layout)
    private View emptyLayout;
    private int mCurrentPage;
    private int mPageRows = 2000;
    private List<WaresOrder> mList;
    private RefreshAdapter mAdapter;
    private String myId;
    public  static final String WARESORDER = "WaresOrder";
    DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarLightMode(this,true);
        builder.cacheInMemory(true).cacheOnDisk(true);
        token = SPUtils.get(LOGIN_TOKEN,"");
        myId = SPUtils.get(USER_PHONE,"");
        mList = new ArrayList<>();
        mAdapter = new RefreshAdapter(mList);
        LinearLayoutManager manager = new LinearLayoutManager(this);
//        manager.setStackFromEnd(true);//设置从底部开始，最新添加的item每次都会显示在最下面
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(mAdapter);
//        swipeLayout.setEnabled(false);
        HashMap<String, Integer> stringIntegerHashMap = new HashMap<>();
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.TOP_DECORATION, PPScreenUtils.dip2px(this,3));
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.RIGHT_DECORATION, PPScreenUtils.dip2px(this,10));
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.LEFT_DECORATION, PPScreenUtils.dip2px(this,10));
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.BOTTOM_DECORATION, PPScreenUtils.dip2px(this,3));
        recyclerView.addItemDecoration(new RecyclerViewSpacesItemDecoration(stringIntegerHashMap));
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData(true);
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
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.SHOPKEEPER);
        params.addBodyParameter("action", "list");
        params.addBodyParameter("page", mCurrentPage+"");
        params.addBodyParameter("size", mPageRows+"");
        String phone = SPUtils.get(USER_PHONE,"");
        params.addParameter("phone", phone);
        Log.d(TAG, "loadData: "+params.toString());
        showProgress();
        x.http().post(params, new Callback.CacheCallback<String>() {
            @Override
            public boolean onCache(String result) {
                readJson(result,clear);
                return false;
            }

            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: "+result);
                readJson(result,clear);
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

    private void readJson(String json,boolean clear) {
        try{
            BaseCode<List<WaresOrder>> base = new Gson().fromJson(
                    json,
                    new TypeToken<BaseCode<List<WaresOrder>>>() {
                    }.getType());
            if (base.getCode() == 0) {
                List<WaresOrder> data = base.getData();
                if(clear){
                    mList.clear();
                }
                mList.addAll(data);
                mAdapter.notifyDataSetChanged();
                if(mList.isEmpty()){
                    emptyLayout.setVisibility(View.VISIBLE);
                }else{
                    emptyLayout.setVisibility(View.GONE);
                }
            } else {
            }
        }catch (Exception e){
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

        /**a
         * 更新加载更多状态
         *
         * @param status
         */
        public void changeMoreStatus(int status) {
            mLoadMoreStatus = status;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //实现ViewHolder
//            if (viewType == TYPE_ITEM) {
            View itemView = getLayoutInflater().inflate(R.layout.ware_order_list_item, parent, false);
            return new ItemViewHolder(itemView);
//            } else if (viewType == TYPE_FOOTER) {
//                View itemView = mInflater.inflate(R.layout.foot_view, parent, false);
//                return new FooterViewHolder(itemView);
//            }else{
//                View itemView = mInflater.inflate(R.layout.note_type_item, parent, false);
//                return new TopViewHolder(itemView);
//            }
        }


        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            //绑定数据
            if (holder instanceof ItemViewHolder) {
                ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
                WaresOrder order = datas.get(position);
                Glide.with(WaresOrderManagerListActivity.this).load(order.getIcon()).into(itemViewHolder.pic);
                itemViewHolder.address.setText(order.getUaddress());
                itemViewHolder.logiscticName.setText(order.getPostname());
                itemViewHolder.orderNo.setText(order.getTransaction_id());
                itemViewHolder.orderTime.setText(Dateuitls.getFormatOrderTime(Long.valueOf(order.getPay_date())));
                itemViewHolder.phone.setText(order.getUphone());
                itemViewHolder.userName.setText(order.getUname());
                itemViewHolder.waresName.setText(order.getName());
                itemViewHolder.quantity.setText(order.getInfo());
                 if(order.getTrade_no().startsWith("ex")){
                    String text = String.format(getResources().getString(R.string.score_price), Integer.valueOf(order.getPrice()));
                    int length = order.getPrice().length();
                    SpannableStringBuilder style = new SpannableStringBuilder(text);
                    style.setSpan(new RelativeSizeSpan(1.2f), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    itemViewHolder.price.setText(style);
                    itemViewHolder.orderInfo.setText(R.string.score_exchange_order);
                }else{
                    itemViewHolder.price.setText(getString(R.string.rmb) + BigDecimalUtil.format(Double.valueOf(order.getPrice()) / 100));
                    itemViewHolder.orderInfo.setText(R.string.buy_order);
                }
                if(TextUtils.isEmpty(order.getPostno())){
                    itemViewHolder.logisticNoView.setVisibility(View.GONE);
                }else{
                    itemViewHolder.logiscticNo.setText(order.getPostno());
                    itemViewHolder.logisticNoView.setVisibility(View.VISIBLE);
                }
                if(TextUtils.isEmpty(order.getPostdate())){
                    itemViewHolder.postTime.setText(R.string.unpost_state);
                }else{
                    itemViewHolder.postTime.setText(Dateuitls.getFormatOrderTime(Long.valueOf(order.getPostdate())));
                }
                if(TextUtils.isEmpty(order.getPostname())){
                    itemViewHolder.orderState.setText(R.string.to_post);
                    itemViewHolder.logisticNameView.setVisibility(View.GONE);
                    itemViewHolder.orderState.setTextColor(getResources().getColor(R.color.theme_color));
                }
                else{
                    itemViewHolder.orderState.setText(R.string.has_post);
                    itemViewHolder.orderState.setTextColor(getResources().getColor(R.color.gray_text_color));
                    itemViewHolder.logiscticName.setText(order.getPostname());
                    itemViewHolder.logisticNameView.setVisibility(View.VISIBLE);
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
//            else if (position + 1 == getItemCount()) {
//                //最后一个item设置为footerView
//                return TYPE_FOOTER;
//            } else {
            return TYPE_ITEM;
//            }
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {

            TextView userName, orderNo,phone,address,logiscticName,logiscticNo,
                    orderTime,postTime,waresName,price,quantity,orderState,orderInfo;
            View logisticNameView, logisticNoView;
            ImageView pic;
            public ItemViewHolder(@NonNull View itemView) {
                super(itemView);
                userName = itemView.findViewById(R.id.consignee);
                pic = itemView.findViewById(R.id.wares_img);
                waresName = itemView.findViewById(R.id.wares_name);
                price = itemView.findViewById(R.id.wares_price);
                quantity = itemView.findViewById(R.id.wares_quantity);
                orderNo = itemView.findViewById(R.id.order_num);
                phone = itemView.findViewById(R.id.phone);
                address = itemView.findViewById(R.id.address);
                logiscticName = itemView.findViewById(R.id.logistics_name);
                logiscticNo = itemView.findViewById(R.id.logistics_no);
                orderTime = itemView.findViewById(R.id.pay_time);
                postTime = itemView.findViewById(R.id.post_time);
                logisticNameView = itemView.findViewById(R.id.logistics_name_view);
                logisticNoView = itemView.findViewById(R.id.logistics_no_view);
                orderState = itemView.findViewById(R.id.order_state);
                orderInfo = itemView.findViewById(R.id.order_info);
                orderState.setVisibility(View.VISIBLE);
                setListener(orderState);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        WaresOrder order = datas.get(getLayoutPosition());
                        RongIM.getInstance().startConversation(WaresOrderManagerListActivity.this, Conversation.ConversationType.PRIVATE,order.getUphone(),order.getUname());
                    }
                });
            }

            private void setListener(View view){
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(WaresOrderManagerListActivity.this,ModifyLogisticActivity.class);
                        intent.putExtra(WARESORDER,mList.get(getLayoutPosition()));
                        startActivityForResult(intent,0);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==ModifyLogisticActivity.MODIFY_ORDER_SUCCESS){
            if(null!=data){
                WaresOrder waresOrder = (WaresOrder) data.getSerializableExtra(WARESORDER);
                if(null==waresOrder){
                    return;
                }
                for(int i = 0;i<mList.size();i++){
                    WaresOrder order = mList.get(i);
                    if(order.getTransaction_id().equals(waresOrder.getTransaction_id())){
                        order.setPostno(waresOrder.getPostno());
                        order.setPostname(waresOrder.getPostname());
                        order.setPostdate(System.currentTimeMillis()+"");
                        mAdapter.notifyItemChanged(i);
                        return;
                    }
                }
            }
        }
    }
}
