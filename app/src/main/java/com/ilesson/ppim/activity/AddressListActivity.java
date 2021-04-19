package com.ilesson.ppim.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.AddressInfo;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.ExchangeAddress;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.PPScreenUtils;
import com.ilesson.ppim.utils.RecyclerViewSpacesItemDecoration;
import com.ilesson.ppim.utils.SPUtils;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.rong.eventbus.EventBus;
import io.rong.imageloader.core.DisplayImageOptions;

import static com.ilesson.ppim.activity.AddressActivity.SET_ADDRESS_SUCCESS;
import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;
import static com.ilesson.ppim.activity.LoginActivity.USER_PHONE;

/**
 * Created by potato on 2020/3/10.
 */
@ContentView(R.layout.activity_address_list)
public class AddressListActivity extends BaseActivity {


    private String token;

    @ViewInject(R.id.recylerview)
    private RecyclerView recyclerView;
    @ViewInject(R.id.swipeLayout)
    private SwipeRefreshLayout swipeLayout;
    @ViewInject(R.id.address_layout)
    private View addAddress;
    @ViewInject(R.id.empty_layout)
    private View emptyView;
    private int mCurrentPage;
    private int mPageRows = 20;
    private List<AddressInfo> mList;
    private RefreshAdapter mAdapter;
    private String myId;
    private boolean useAddress;
    private int moveY;
    public static final String ADDRESS_DETAIL = "address_detail";
    DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
    private AddressInfo addressInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarLightMode(this, true);
        builder.cacheInMemory(true).cacheOnDisk(true);
        token = SPUtils.get(LOGIN_TOKEN, "");
        myId = SPUtils.get(USER_PHONE, "");
        moveY = PPScreenUtils.getScreenHeight(this) / 2;
        mList = new ArrayList<>();
        mAdapter = new RefreshAdapter(mList);
        useAddress = getIntent().getBooleanExtra(ExchangeActivity.ADDRESS_INFO, false);
        addressInfo = (AddressInfo) getIntent().getSerializableExtra(ADDRESS_DETAIL);
        LinearLayoutManager manager = new LinearLayoutManager(this);
//        manager.setStackFromEnd(true);//设置从底部开始，最新添加的item每次都会显示在最下面
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(mAdapter);
        swipeLayout.setEnabled(false);
        HashMap<String, Integer> stringIntegerHashMap = new HashMap<>();
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.TOP_DECORATION, PPScreenUtils.dip2px(this, 3));
//        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.RIGHT_DECORATION,PPScreenUtils.dip2px(this,10));
//        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.LEFT_DECORATION,PPScreenUtils.dip2px(this,10));
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.BOTTOM_DECORATION, PPScreenUtils.dip2px(this, 3));
        recyclerView.addItemDecoration(new RecyclerViewSpacesItemDecoration(stringIntegerHashMap));
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requstList(true);
            }
        });
        requstList(true);
    }

    private static final String TAG = "AddressListActivity";

    @Event(value = R.id.back_btn)
    private void back_btn(View view) {
        finish();
    }

    @Event(value = R.id.create_address)
    private void create_address(View view) {
        Intent intent = new Intent(AddressListActivity.this, AddressActivity.class);
        intent.putExtra(ExchangeActivity.ADDRESS_INFO, useAddress);
        startActivityForResult(intent, ADD_ADDRESS);
    }

    private String cacheJson;

    private void requstList(final boolean loadCache) {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.ADDRESS);
        params.addParameter("action", "list");
        params.addParameter("phone", SPUtils.get(USER_PHONE, ""));
        Log.d(TAG, "search: " + params.toString());
        cacheJson = null;
        org.xutils.x.http().post(params, new Callback.CacheCallback<String>() {
            @Override
            public boolean onCache(String result) {
                Log.d(TAG, "onCache: " + loadCache);
                if (loadCache) {
                    cacheJson = result;
                }
                return false;
            }

            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "requstList: " + result);
                swipeLayout.setRefreshing(false);
                readJson(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                if (!TextUtils.isEmpty(cacheJson)) {
                    readJson(cacheJson);
                }
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
                Log.d(TAG, "onFinished: " + System.currentTimeMillis());
            }
        });
    }


    private void readJson(String result) {
        result = result.replace("default", "isDefault");
        BaseCode<List<AddressInfo>> base = new Gson().fromJson(
                result,
                new TypeToken<BaseCode<List<AddressInfo>>>() {
                }.getType());
        if (base.getCode() == 0) {
            List<AddressInfo> datas = base.getData();
            if (datas == null) {
                return;
            }
            mList.clear();
            mList.addAll(datas);
            if (mList.isEmpty()) {
                moveUp();
            } else {
                moveDown();
            }
            mAdapter.notifyDataSetChanged();
        } else {
            showToast(base.getMessage());
        }
    }

    class RefreshAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        List<AddressInfo> datas = new ArrayList<>();

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

        public RefreshAdapter(List<AddressInfo> data) {
            this.datas = data;
        }

        /**
         * a
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
            View itemView = getLayoutInflater().inflate(R.layout.address_list_item, parent, false);
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
                AddressInfo addressInfo = datas.get(position);
                if (addressInfo == null) {
                    return;
                }
                String adr = addressInfo.getAddress();
                String province = addressInfo.getProvince();
                String city = addressInfo.getCity();
                String pre = "";
                if (!TextUtils.isEmpty(province)) {
                    pre = province;
                    if (!TextUtils.isEmpty(city) && !city.equals(province)) {
                        pre = pre + city;
                    }
                }
                itemViewHolder.address.setText(pre + adr);
                itemViewHolder.phone.setText(addressInfo.getPhone());
//                if(addressInfo.getTag().equals(getString(R.string.home))){
//                    itemViewHolder.tag.setTextColor(Color.RED);
//                }else{
//                    itemViewHolder.tag.setTextColor(getResources().getColor(R.color.welcom_bg));
//                }
                itemViewHolder.tag.setText(addressInfo.getTag());
                itemViewHolder.userName.setText(addressInfo.getName());
                if (addressInfo.getIsDefault().equals("1")) {
                    itemViewHolder.imageView.setImageResource(R.mipmap.checked);
                } else {
                    itemViewHolder.imageView.setImageResource(R.mipmap.uncheck);
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

            TextView userName, phone, address, tag, delete;
            View modify, check;
            ImageView imageView;

            public ItemViewHolder(@NonNull View itemView) {
                super(itemView);
                userName = itemView.findViewById(R.id.name);
                phone = itemView.findViewById(R.id.phone);
                address = itemView.findViewById(R.id.address);
                tag = itemView.findViewById(R.id.tag);
                check = itemView.findViewById(R.id.check);
                modify = itemView.findViewById(R.id.modify);
                delete = itemView.findViewById(R.id.delete);
                imageView = itemView.findViewById(R.id.image);
                setListener(itemView);
                modify.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        modifyAddress();
                    }
                });
                check.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AddressInfo addressInfo = datas.get(getLayoutPosition());
                        if (addressInfo.getIsDefault().equals("1")) {
                            return;
                        } else {
                            setDefult(addressInfo);
                        }
                    }
                });
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (addressInfo != null && addressInfo.getId() == datas.get(getLayoutPosition()).getId()) {
                            return;
                        }
                        showDeleteDialog(datas.get(getLayoutPosition()).getId(), getLayoutPosition());
                    }
                });

            }

            private void setListener(View view) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (useAddress) {
                            setAddressResult(datas.get(getLayoutPosition()));
                        } else {
//                            modifyAddress();
                        }
                    }
                });
            }

            private void modifyAddress() {
                Intent intent = new Intent(AddressListActivity.this, AddressActivity.class);
                intent.putExtra(AddressActivity.CURRENTADDRESS, datas.get(getLayoutPosition()));
                startActivityForResult(intent, MODIFY_ADDRESS);
            }
        }
    }

    private void setAddressResult(AddressInfo addressInfo) {
        Intent intent = new Intent();
        intent.putExtra(ExchangeActivity.ADDRESS_INFO, addressInfo);
        setResult(ExchangeActivity.SET_ADDRESS_SUCCESS_TO_USE, intent);
        finish();
    }

    public static final int MODIFY_ADDRESS = 90;
    public static final int ADD_ADDRESS = 91;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (resultCode == SET_ADDRESS_SUCCESS) {
                requstList(false);
                if (true) return;
                AddressInfo addressInfo = (AddressInfo) data.getSerializableExtra(AddressActivity.CURRENTADDRESS);
                if (requestCode == MODIFY_ADDRESS) {
                    if (null == addressInfo) {
                        return;
                    }
                    for (int i = 0; i < mList.size(); i++) {
                        AddressInfo address = mList.get(i);
                        if (addressInfo.getId() == address.getId()) {
                            address.setAddress(addressInfo.getAddress());
                            address.setName(addressInfo.getName());
                            address.setPhone(addressInfo.getPhone());
                            address.setTag(addressInfo.getTag());
                            mAdapter.notifyItemChanged(i);
                            return;
                        }
                    }
                } else if (requestCode == ADD_ADDRESS) {
                    mList.add(addressInfo);
                    mAdapter.notifyItemInserted(mList.size() - 1);
//                    requstList();
                }
            } else if (resultCode == ExchangeActivity.SET_ADDRESS_SUCCESS_TO_USE) {
                AddressInfo addressInfo = (AddressInfo) data.getSerializableExtra(ExchangeActivity.ADDRESS_INFO);
                setAddressResult(addressInfo);
            }
        }
    }

    private void delete(int id, final int index) {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.ADDRESS);
        params.addParameter("action", "delete");
        params.addParameter("id", id);
        Log.d(TAG, "search: " + params.toString());
        showProgress();
        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "delete: " + result);
                BaseCode<List<AddressInfo>> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<List<AddressInfo>>>() {
                        }.getType());
                if (base.getCode() == 0) {
                    AddressInfo info = mList.remove(index);
                    mAdapter.notifyItemRemoved(index);
                    if (mAdapter.getItemCount() == 0) {
                        moveUp();
                    }
                    if (null != addressInfo && addressInfo.getId() == info.getId()) {
                        EventBus.getDefault().post(new ExchangeAddress(null));
                    }
//                    requstList();
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

            }

            @Override
            public void onFinished() {
                hideProgress();
            }
        });
    }

    private Point point = new Point();

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            point.x = (int) ev.getRawX();
            point.y = (int) ev.getRawY();
        }
        return super.dispatchTouchEvent(ev);
    }

    private void moveUp() {
        emptyView.setVisibility(View.VISIBLE);
//        ObjectAnimator.ofFloat(addAddress, "translationY", -moveY).setDuration(500).start();
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, PPScreenUtils.dip2px(this, 60));
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        addAddress.setLayoutParams(params);
        addAddress.setBackgroundColor(getResources().getColor(R.color.gray_theme));
    }

    private void moveDown() {
        emptyView.setVisibility(View.GONE);
//        ObjectAnimator.ofFloat(addAddress, "translationY", moveY).setDuration(500).start();
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, PPScreenUtils.dip2px(this, 60));
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        addAddress.setLayoutParams(params);
        addAddress.setBackgroundColor(getResources().getColor(R.color.white));
    }

    private void setDefult(final AddressInfo info) {
        //action=default&id=XXX
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.ADDRESS);
        params.addParameter("action", "default");
        params.addParameter("id", info.getId() + "");
        Log.d(TAG, "search: " + params.toString());
        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "requstList: " + result);
                for (AddressInfo addressInfo : mList) {
                    addressInfo.setIsDefault("0");
                }
                info.setIsDefault("1");
                mList.remove(info);
                mList.add(0, info);
                mAdapter.notifyDataSetChanged();
//                readJson(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
                Log.d(TAG, "onFinished: " + System.currentTimeMillis());
            }
        });
    }

    private void showDeleteDialog(final int id, final int index) {
        View view = getLayoutInflater().inflate(R.layout.practice_dialog, null);
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(view);
        TextView scoreTv = (TextView) view.findViewById(R.id.content);
        scoreTv.setText(R.string.delete_it);
        view.findViewById(R.id.left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.right_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete(id, index);
                dialog.dismiss();
            }
        });
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        android.view.WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        p.width = (int) (PPScreenUtils.getScreenWidth(this) * 0.85);
        p.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setAttributes(p);
        dialog.show();
    }
}
