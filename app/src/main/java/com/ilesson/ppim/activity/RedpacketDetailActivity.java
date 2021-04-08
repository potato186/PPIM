package com.ilesson.ppim.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.custom.RedPacketMessage;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.CheckRedpacketData;
import com.ilesson.ppim.entity.RedInfo;
import com.ilesson.ppim.entity.RedpacketDetail;
import com.ilesson.ppim.entity.SplitList;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.Dateuitls;
import com.ilesson.ppim.utils.IMUtils;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.view.MenuListView;
import com.ilesson.ppim.view.RoundImageView;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.rong.imageloader.core.DisplayImageOptions;
import io.rong.imageloader.core.ImageLoader;

import static com.ilesson.ppim.activity.ChatInfoActivity.GROUP_ID;
import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;
import static com.ilesson.ppim.activity.LoginActivity.USER_PHONE;

/**
 * Created by potato on 2020/3/10.
 */
@ContentView(R.layout.act_red_detail)
public class RedpacketDetailActivity extends BaseActivity {


    private String token;
    @ViewInject(R.id.listview)
    private MenuListView mListView;
    @ViewInject(R.id.scrollview)
    private ScrollView scrollview;
    @ViewInject(R.id.sender_icon)
    private RoundImageView userIcon;
    @ViewInject(R.id.sender_name)
    private TextView senderName;
    @ViewInject(R.id.detail)
    private TextView detailText;
    @ViewInject(R.id.money)
    private TextView myMoney;
    @ViewInject(R.id.split_layout)
    private View splitLayout;
    private int mCurrentPage;
    private int mPageRows = 20;
    private List<SplitList> mList;
    private Adapter mAdapter;
    private String myId;
    DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
    private static final String TAG = "RedpacketDetailActivity";
    public static final String RED_MESSAGE = "red_message";
    public static final String CAN_SPLIT = "can_split";
    private boolean canSplit;
    private RedPacketMessage redPacketMessage;
    private String groupId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarLightMode(this,true);
        builder.cacheInMemory(true).cacheOnDisk(true);
        token = SPUtils.get(LOGIN_TOKEN,"");
        myId = SPUtils.get(LoginActivity.USER_PHONE,"");
        Intent intent = getIntent();
        canSplit = intent.getBooleanExtra(CAN_SPLIT,false);
        redPacketMessage = intent.getParcelableExtra(RED_MESSAGE);
        if(null==redPacketMessage){
            return;
        }
        senderName.setText(redPacketMessage.getSenderName()+"的红包");
        Glide.with(getApplicationContext()).load(redPacketMessage.getSenderIcon()).into(userIcon);
        groupId = intent.getStringExtra(GROUP_ID);
        mList = new ArrayList<>();
        loadData();
    }

    @Event(value = R.id.back_btn)
    private void back_btn(View view) {
        finish();
    }
    private void loadData() {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.MONEY_URL);
        if(canSplit){
            params.addParameter("action", "split");
        }else{
            params.addParameter("action", "info");
        }
        params.addParameter("token", token);
        params.addParameter("group", groupId);
        params.addParameter("id", redPacketMessage.getRedpacketId());
        Log.d(TAG, "loadData: "+params.toString());
        showProgress();
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: "+result);
                hideProgress();
                if(canSplit){
                    readJson(result);
                }else{
                    readDetailJson(result);
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

    private int bestOne;
    private boolean hasRed;
    private void readJson(String json) {
        BaseCode<CheckRedpacketData> base = new Gson().fromJson(
                json,
                new TypeToken<BaseCode<CheckRedpacketData>>() {
                }.getType());
        if (base.getCode() == 0) {
            CheckRedpacketData data = base.getData();
            RedInfo redInfo = data.getRedInfo();
            setData(redInfo);
            IMUtils.login(this,SPUtils.get(LoginActivity.LOGIN_TOKEN,""));
        }
    }
    private void readDetailJson(String json) {
        BaseCode<RedInfo> base = new Gson().fromJson(
                json,
                new TypeToken<BaseCode<RedInfo>>() {
                }.getType());
        if (base.getCode() == 0) {
            RedInfo redInfo = base.getData();
            RedpacketDetail detailInfo = redInfo.getInfo();
            setData(redInfo);
        }
    }
    long firstOne=Long.MAX_VALUE;
    private void setData(RedInfo redInfo){
        RedpacketDetail detailInfo = redInfo.getInfo();
        List<SplitList> list = redInfo.getList();
        List<Long> times = new ArrayList<>();
        String phone = SPUtils.get(USER_PHONE,"");
        String mySplitMoney="";
        int splitMoney = 0;

        for(SplitList split:list){
            long time = Long.valueOf(split.getDate());
            times.add(time);
            int money = Integer.valueOf(split.getMoney());

            if(money>bestOne){
                bestOne = money;
            }
            if(phone.equals(split.getPhone())){
                mySplitMoney = split.getMoney();
            }
            splitMoney+=money;
        }
        for(SplitList split:list){
            long time = Long.valueOf(split.getDate());
            int money = Integer.valueOf(split.getMoney());
            if(money==bestOne){
                if(time<firstOne){
                    firstOne=time;
                }
            }
        }
        if(detailInfo.getCount()!=list.size()){
            String detail = String.format(getResources().getString(R.string.has_split_info)
                    ,list.size(),detailInfo.getCount(),splitMoney,detailInfo.getMoney());
            detailText.setText(detail);
            hasRed = true;
        }else{
            String time = Dateuitls.getDiffTime(detailInfo.getDate(),Collections.max(times));
            String result = String.format(getResources().getString(R.string.no_split_info)
                    ,detailInfo.getCount(),time);
            detailText.setText(result);
            hasRed = false;
        }
        if(!TextUtils.isEmpty(mySplitMoney)){
            splitLayout.setVisibility(View.VISIBLE);
            myMoney.setText(mySplitMoney);
        }
        mList.addAll(list);
        mAdapter = new Adapter(mList);
        mListView.setAdapter(mAdapter);
        scrollview.post(new Runnable() {
            @Override
            public void run() {
                scrollview.fullScroll(ScrollView.FOCUS_UP);
            }
        });
//         scrollView.post(new Runnable() {
//   
//          @Override
//  public void run() {
//  // TODO Auto-generated method stub
//  
//  }
// });
    }
    class Adapter extends BaseAdapter {

        List<SplitList> list;

        public Adapter(List<SplitList> list) {
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
                        R.layout.item_red_detail, null);
                holder.imageView = convertView
                        .findViewById(R.id.user_icon);
                holder.time = convertView
                        .findViewById(R.id.time);
                holder.name = convertView
                        .findViewById(R.id.user_name);
                holder.count = convertView
                        .findViewById(R.id.money);
                holder.best = convertView
                        .findViewById(R.id.best);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            SplitList info = list.get(position);
            holder.time.setText(Dateuitls.getFormatTime(Long.valueOf(info.getDate())));
            holder.count.setText(info.getMoney());
            holder.name.setText(info.getName());
            if(!hasRed&&Long.valueOf(info.getDate())==firstOne){
                holder.best.setVisibility(View.VISIBLE);
            }else{
                holder.best.setVisibility(View.GONE);
            }
            ImageLoader.getInstance().displayImage(info.getIcon(), holder.imageView,
                    builder.build());
            return convertView;
        }

    }

    class ViewHolder {
        private TextView name;
        private TextView time;
        private TextView count;
        private View best;
        private RoundImageView imageView;
    }
}
