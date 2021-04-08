package com.ilesson.ppim.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.activity.MainActivity;
import com.ilesson.ppim.activity.PayPwdActivity;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.BusinessGroup;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.RecyclerViewSpacesItemDecoration;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.utils.PPScreenUtils;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.rong.eventbus.EventBus;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.Conversation;

import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;
import static com.ilesson.ppim.activity.LoginActivity.USER_PHONE;
import static com.ilesson.ppim.custom.MyExtensionModule.shopGroup;

/**
 * Created by potato on 2016/4/12.
 */
@ContentView(R.layout.frag_group)
public class GroupFragment extends BaseFragment {
    private static final String TAG = "GroupFragment";
    private MainActivity mainActivity;

    @ViewInject(R.id.recylerview)
    private RecyclerView recyclerView;
    @ViewInject(R.id.swipeLayout)
    private SwipeRefreshLayout swipeLayout;
    private List<BusinessGroup> resultList = new ArrayList<>();
    private RefreshAdapter adapter;
    private String token;
    private String phone;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        EventBus.getDefault().register(this);
        token = SPUtils.get("token", "");
        phone = SPUtils.get(USER_PHONE, "");
        adapter = new RefreshAdapter(resultList);
        loadData(true);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
//        manager.setStackFromEnd(true);//设置从底部开始，最新添加的item每次都会显示在最下面
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
//        swipeLayout.setEnabled(false);
        HashMap<String, Integer> stringIntegerHashMap = new HashMap<>();
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.TOP_DECORATION, PPScreenUtils.dip2px(getActivity(),2));
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.RIGHT_DECORATION, PPScreenUtils.dip2px(getActivity(),15));
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.LEFT_DECORATION, PPScreenUtils.dip2px(getActivity(),15));
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.BOTTOM_DECORATION, PPScreenUtils.dip2px(getActivity(),2));
        recyclerView.addItemDecoration(new RecyclerViewSpacesItemDecoration(stringIntegerHashMap));
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData(true);
            }
        });
    }
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.frag_group1, container,false);
//        listView1 = view.findViewById(R.id.list1);
//        Log.d(TAG, "onCreateView: "+listView1);
//        return view;
//    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
    public void onEventMainThread(Object obj) {
    }

    private void loadData(final boolean fresh) {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.SHOP);
        params.addParameter("action","shop_list");
        String token = SPUtils.get(LOGIN_TOKEN,"");
        params.addParameter("token", token);
        Log.d(TAG, "loadData: " + params.toString());
        x.http().post(params, new Callback.CacheCallback<String>() {
            @Override
            public boolean onCache(String result) {
                showList(result,fresh);
                return false;
            }

            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: " + result);
                showList(result,fresh);
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
                if(swipeLayout!=null&&swipeLayout.isRefreshing()){
                    swipeLayout.setRefreshing(false);
                }
            }
        });
    }
    private void showList(String result,boolean fresh){
        BaseCode<List<BusinessGroup>> base = new Gson().fromJson(
                result,
                new TypeToken<BaseCode<List<BusinessGroup>>>() {
                }.getType());
        if(base.getCode()==0){
            List<BusinessGroup> list = base.getData();
            if(null!=base.getData()){
                if(fresh){
                    resultList.clear();
                }
                resultList.addAll(list);
                adapter.notifyDataSetChanged();
            }
        }
    }
    private void joinGroup(final BusinessGroup group) {
        mainActivity.showProgress();
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.SHOP);
        params.addParameter("action","shop_join");
        params.addParameter("group",group.getGroup());
        Log.d(TAG, "loadData: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: " + result);
                BaseCode base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode>() {
                        }.getType());
                if(base.getCode()==0){
//                    SPUtils.put(phone+group.getGroup(),true);
//                    interGroup(group);
                }
            }


            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                interGroup(group);
                ex.printStackTrace();
            }


            @Override
            public void onCancelled(CancelledException cex) {
                cex.printStackTrace();
            }


            @Override
            public void onFinished() {
                mainActivity.hideProgress();
            }
        });
    }

    class RefreshAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        Context mContext;
        LayoutInflater mInflater;
        List<BusinessGroup> NoteInfos = new ArrayList<>();

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

        public RefreshAdapter(List<BusinessGroup> data) {
            this.NoteInfos = data;
            mInflater = LayoutInflater.from(getActivity());
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
                View itemView = mInflater.inflate(R.layout.business_group_item, parent, false);
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
                BusinessGroup group = NoteInfos.get(position);
                Glide.with(getActivity()).load(group.getLogo()).into(itemViewHolder.pic);
                itemViewHolder.des.setText(group.getDesc());
                itemViewHolder.name.setText(group.getName());
            }
        }

        @Override
        public int getItemCount() {
            return NoteInfos.size();
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

            TextView name, des,enter;
            ImageView pic;
            public ItemViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.business_name);
                des = itemView.findViewById(R.id.business_des);
                enter = itemView.findViewById(R.id.enter);
                pic = itemView.findViewById(R.id.business_icon);
                initListener(itemView);
                initListener(enter);
            }

            private void initListener(View itemView) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BusinessGroup group = resultList.get(getLayoutPosition());
//                        boolean joined = SPUtils.get(phone+group.getGroup(),false);
//                        if(joined){
                            interGroup(group);
//                        }else{
//                            joinGroup(group);
//                        }
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

        class TopViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            public TopViewHolder(View itemView) {
                super(itemView);
                itemView.findViewById(R.id.pic).setOnClickListener(this);
                itemView.findViewById(R.id.link).setOnClickListener(this);
                itemView.findViewById(R.id.file).setOnClickListener(this);
                itemView.findViewById(R.id.chat_record).setOnClickListener(this);
                itemView.findViewById(R.id.voice).setOnClickListener(this);
                itemView.findViewById(R.id.note).setOnClickListener(this);
                itemView.findViewById(R.id.location).setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.pic:

                        break;
                }
            }
        }
    }

    private void interGroup(BusinessGroup group){
        joinGroup(group);
        shopGroup = true;
        RongIM.getInstance().startConversation(getActivity(), Conversation.ConversationType.GROUP,group.getGroup(),group.getName());
    }
    private void showDialog() {
        View view = getLayoutInflater().inflate(R.layout.practice_dialog,null);
        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(view).create();
        TextView title = (TextView) view.findViewById(R.id.title);
        TextView content = (TextView) view.findViewById(R.id.content);
        TextView left = (TextView) view.findViewById(R.id.left_btn);
        TextView right = (TextView) view.findViewById(R.id.right_btn);
        content.setText(R.string.create_group_tip);
        title.setText(R.string.create_group_title);
        left.setText(R.string.create_group_now);
        right.setText(R.string.create_group_next);
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void active(){
        Intent intent = new Intent(mainActivity, PayPwdActivity.class);
        intent.putExtra(PayPwdActivity.ACTIVE_PASSWORD, true);
        mainActivity.startActivityForResult(intent, 0);
    }
}
