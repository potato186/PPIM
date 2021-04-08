package com.ilesson.ppim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.ilesson.ppim.entity.ScoreInfo;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.PPScreenUtils;
import com.ilesson.ppim.utils.RecyclerViewSpacesItemDecoration;
import com.ilesson.ppim.utils.SPUtils;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.rong.eventbus.EventBus;
import io.rong.imageloader.core.DisplayImageOptions;

import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;
import static com.ilesson.ppim.activity.LoginActivity.USER_PHONE;

/**
 * Created by potato on 2020/3/10.
 */
@ContentView(R.layout.activity_integra_list)
public class ScoreListActivity extends BaseActivity {


    private String token;

    @ViewInject(R.id.recylerview)
    private RecyclerView recyclerView;
    @ViewInject(R.id.empty_layout)
    private View emptyLayout;
    @ViewInject(R.id.swipeLayout)
    private SwipeRefreshLayout swipeLayout;
    private int mCurrentPage;
    private int mPageRows = 2000;
    private List<ScoreInfo> mList;
    private RefreshAdapter mAdapter;
    private String myId;
    DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarLightMode(this,true);
        EventBus.getDefault().register(this);
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
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.TOP_DECORATION, PPScreenUtils.dip2px(this,5));
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.RIGHT_DECORATION, PPScreenUtils.dip2px(this,10));
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.LEFT_DECORATION, PPScreenUtils.dip2px(this,10));
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.BOTTOM_DECORATION, PPScreenUtils.dip2px(this,5));
        recyclerView.addItemDecoration(new RecyclerViewSpacesItemDecoration(stringIntegerHashMap));
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData(true);
            }
        });
        loadData(true);
    }

    private static final String TAG = "IntegraListActivity";
    @Event(value = R.id.back_btn)
    private void back_btn(View view) {
        finish();
    }
    private void loadData(final boolean clear) {
        //list&token=%s&page=%s&size=%s
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.SCORE);
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
            BaseCode<List<ScoreInfo>> base = new Gson().fromJson(
                    json,
                    new TypeToken<BaseCode<List<ScoreInfo>>>() {
                    }.getType());
            if (base.getCode() == 0) {
                List<ScoreInfo> data = base.getData();
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
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    class RefreshAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        List<ScoreInfo> datas = new ArrayList<>();

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

        public RefreshAdapter(List<ScoreInfo> data) {
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
            View itemView = getLayoutInflater().inflate(R.layout.integra_item, parent, false);
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
                ScoreInfo scoreInfo = datas.get(position);
                itemViewHolder.businessName.setText(scoreInfo.getName());
                itemViewHolder.integra.setText(scoreInfo.getValue()+getResources().getString(R.string.score_));
                Glide.with(getApplicationContext()).load(scoreInfo.getLogo()).into(itemViewHolder.img);
            }
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }

        @Override
        public int getItemViewType(int position) {
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

            TextView businessName, integra,detail,exchange;
            ImageView img;
            public ItemViewHolder(@NonNull View itemView) {
                super(itemView);
                businessName = itemView.findViewById(R.id.business_name);
                integra = itemView.findViewById(R.id.integra);
                detail = itemView.findViewById(R.id.detail);
                exchange = itemView.findViewById(R.id.exchange);
                img = itemView.findViewById(R.id.img);
                detail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ScoreInfo order = datas.get(getLayoutPosition());
                        Intent intent = new Intent(ScoreListActivity
                                .this,ScoreDetailActivity.class);
                        intent.putExtra(ScoreDetailActivity.SCORE_INFO,order);
                        startActivity(intent);
                    }
                });
                exchange.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        position = getLayoutPosition();
                        ScoreInfo order = datas.get(position);
                        Intent intent = new Intent(ScoreListActivity
                                .this,ExchangeActivity.class);
                        intent.putExtra(ScoreDetailActivity.SCORE_INFO,order);
                        startActivity(intent);
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
    private int position;
    public void onEventMainThread(ScoreInfo scoreInfo) {
        ScoreInfo info = mList.get(position);
        info.setValue(scoreInfo.getValue());
        mAdapter.notifyItemChanged(position);
    }

}
