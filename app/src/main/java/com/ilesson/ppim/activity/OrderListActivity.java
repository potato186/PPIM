package com.ilesson.ppim.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.Order;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.IMUtils;
import com.ilesson.ppim.utils.MyFileUtils;
import com.ilesson.ppim.utils.SPUtils;

import org.xutils.common.Callback;
import org.xutils.common.util.FileUtil;
import org.xutils.common.util.MD5;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.rong.imageloader.core.DisplayImageOptions;
import io.rong.imageloader.core.ImageLoader;

import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;

/**
 * Created by potato on 2020/3/10.
 */
@ContentView(R.layout.activity_order_list)
public class OrderListActivity extends BaseActivity {


    private String token;

    @ViewInject(R.id.list_view)
    private PullToRefreshListView mListView;
    private int mCurrentPage;
    private int mPageRows = 20;
    private List<Order> mList;
    private Adapter mAdapter;
    private String myId;
    DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarLightMode(this,true);
        builder.cacheInMemory(true).cacheOnDisk(true);
        token = SPUtils.get(LOGIN_TOKEN,"");
        myId = SPUtils.get(LoginActivity.USER_PHONE,"");
        mList = new ArrayList<>();
        mAdapter = new Adapter(mList);
        mListView = findViewById(R.id.list_view);
        mListView.setAdapter(mAdapter);
        mListView
                .setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

                    @Override
                    public void onPullDownToRefresh(
                            PullToRefreshBase<ListView> refreshView) {
                        mCurrentPage = 0;
                        loadData(true);
                    }

                    @Override
                    public void onPullUpToRefresh(
                            PullToRefreshBase<ListView> refreshView) {
                        mCurrentPage++;
                        loadData(false);
                    }
                });
        initIndicator();
        loadData(false);
    }
    private void initIndicator() {
        ILoadingLayout startLabels = mListView
                .getLoadingLayoutProxy(true, false);
        startLabels.setPullLabel("下拉加载");
        startLabels.setRefreshingLabel("正在刷新...");
        startLabels.setReleaseLabel("松开加载");

        ILoadingLayout endLabels = mListView.getLoadingLayoutProxy(
                false, true);
        endLabels.setPullLabel("上拉加载");
        endLabels.setRefreshingLabel("正在刷新...");
        endLabels.setReleaseLabel("松开加载");
    }

    private static final String TAG = "OrderListActivity";
    @Event(value = R.id.back_btn)
    private void back_btn(View view) {
        finish();
    }
    private void loadData(final boolean clear) {
        //list&token=%s&page=%s&size=%s
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.ASSET_URL);
        params.addBodyParameter("action", "list");
        params.addBodyParameter("token", token);
        params.addBodyParameter("page", mCurrentPage+"");
        params.addBodyParameter("size", mPageRows+"");
        params.setCacheMaxAge(1000*60*60);
        Log.d(TAG, "loadData: "+params.toString());
        final String path = MD5.md5(params.toString());
        final String dir = FileUtil.getCacheDir("json").getAbsolutePath();
        showProgress();
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: "+result);
                hideProgress();
                readJson(result, dir, path, clear);
            }
            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ex.printStackTrace();
                String result = MyFileUtils.file2String(dir + File.separator + path);
                if(TextUtils.isEmpty(result)){
                    return;
                }
                readJson(result, null, null, clear);
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

    private void readJson(String json, String dir, String path, boolean clear) {
        BaseCode<List<Order>> base = new Gson().fromJson(
                json,
                new TypeToken<BaseCode<List<Order>>>() {
                }.getType());
        if (base.getCode() == 0) {
            if (dir != null && path != null) {
                try {
                    MyFileUtils.saveFile(dir, path, json);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            List<Order> data = base.getData();
            if (clear) {
                mList.clear();
            }
            mList.addAll(data);
            mAdapter.notifyDataSetChanged();
            mListView.onRefreshComplete();
        } else {
        }
    }
    class Adapter extends BaseAdapter {

        List<Order> list;

        public Adapter(List<Order> list) {
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("ResourceAsColor")
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (null == convertView) {
                holder = new ViewHolder();
                convertView = getLayoutInflater().inflate(
                        R.layout.order_item, null);
                holder.method = convertView
                        .findViewById(R.id.method);
                holder.time = convertView
                        .findViewById(R.id.time);
                holder.count = convertView
                        .findViewById(R.id.count);
                holder.from = convertView
                        .findViewById(R.id.from);
                holder.number = convertView
                        .findViewById(R.id.number);
                holder.imageView = convertView
                        .findViewById(R.id.icon);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Order order = list.get(position);
            String money = order.getAsset();
            String tag="";
            if(order.getTarget().equals(myId)){
                money="+"+money;
                tag = "-来自";
                holder.count.setTextColor(getResources().getColor(R.color.theme_color));
                holder.count.setText("+"+(double)Math.round(Double.valueOf(money)*1000)/1000);
            }else{
                money="-"+money;
                tag = "-转给";
                holder.count.setTextColor(getResources().getColor(R.color.gray_text333_color));
                holder.count.setText((double)Math.round(Double.valueOf(money)*1000)/1000+"");
            }
            holder.from.setText(tag+order.getName());
            if(order.getMethod().contains("红包退回")||order.getMethod().contains("发出群红包")){
                holder.method.setText(order.getMethod());
            }else{
                holder.method.setText(order.getMethod()+tag+order.getName());
            }

            holder.number.setText(order.getUuid());
            holder.time.setText(IMUtils.getDate(order.getDate()));

            ImageLoader.getInstance().displayImage(order.getIcon(), holder.imageView,
                    builder.build());
            return convertView;
        }

    }

    class ViewHolder {
        private TextView method;
        private TextView time;
        private TextView count;
        private TextView from;
        private TextView number;
        private ImageView imageView;
    }
}
