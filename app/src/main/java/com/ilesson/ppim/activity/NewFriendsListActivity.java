package com.ilesson.ppim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.FriendAccept;
import com.ilesson.ppim.entity.PPUserInfo;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.SPUtils;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import io.rong.eventbus.EventBus;
import io.rong.imageloader.core.DisplayImageOptions;
import io.rong.imageloader.core.ImageLoader;

import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;

/**
 * Created by potato on 2020/3/10.
 */
@ContentView(R.layout.activity_new_friend_list)
public class NewFriendsListActivity extends BaseActivity {

    @ViewInject(R.id.search_listiview)
    private ListView listView;
    private List<PPUserInfo> datas;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarLightMode(this, true);
        token = SPUtils.get(LOGIN_TOKEN, "");
        requestNewFriends();
        datas = new ArrayList<>();
    }

    private static final String TAG = "NewFriendsListActivity";

    @Event(value = R.id.back_btn)
    private void back_btn(View view) {
        finish();
    }

    @Event(value = R.id.search_edit)
    private void search_edit(View view) {
        startActivity(new Intent(this, SearchFriendActivity.class));
    }

    private void requestNewFriends() {
        //https://www.lesson1234.com:9443/pp/user?action=friend_pre&token=
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.USER_URL);
        params.addBodyParameter("action", "friend_pre");
        params.addBodyParameter("token", token);
        Log.d(TAG, "loadData: " + params.toString());
        showProgress();
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: " + result);
                BaseCode<List<PPUserInfo>> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<List<PPUserInfo>>>() {
                        }.getType());
                if (base.getCode() == 0) {
                    List<PPUserInfo> data = base.getData();
                    if (null == data || data.isEmpty()) {
                        return;
                    }
                    datas.addAll(data);
                    listView.setVisibility(View.VISIBLE);
                    listView.setAdapter(adapter);
                } else {
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

    private void accept(String phone, final TextView view) {
        //action=friend_accept&token=%s&target=%s
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.USER_URL);
        params.addBodyParameter("action", "friend_accept");
        params.addBodyParameter("token", token);
        params.addBodyParameter("target", phone);
        Log.d(TAG, "loadData: " + params.toString());
        showProgress();
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                BaseCode base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode>() {
                        }.getType());
                if (base.getCode() == 0) {
                    EventBus.getDefault().post(new FriendAccept());
                    Toast.makeText(NewFriendsListActivity.this, "添加成功", Toast.LENGTH_LONG).show();
                    view.setText(R.string.has_add);
                } else {
                    Toast.makeText(NewFriendsListActivity.this, "添加失败", Toast.LENGTH_LONG).show();
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

    BaseAdapter adapter = new BaseAdapter() {

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (null == convertView) {
                holder = new ViewHolder();
                convertView = getLayoutInflater().inflate(
                        R.layout.add_user_item, null);
                holder.icon = (ImageView) convertView.findViewById(R.id.thumbnail);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.accept = (TextView) convertView.findViewById(R.id.accept);
//				holder.tittle = (TextView) convertView.findViewById(R.id.model);
//				holder.publish = (TextView) convertView
//						.findViewById(R.id.publish);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final PPUserInfo PPUserInfo = datas.get(position);
            DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
            builder.showImageOnLoading(R.mipmap.default_icon)
                    .cacheInMemory(true).cacheOnDisk(true);
            ImageLoader.getInstance().displayImage(PPUserInfo.getIcon(), holder.icon,
                    builder.build());
            holder.name.setText(PPUserInfo.getName());
            final TextView view = holder.accept;
            holder.accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!view.getText().toString().equals(getResources().getString(R.string.has_add))) {
                        accept(PPUserInfo.getPhone(), view);
                    }
                }
            });
            return convertView;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public int getCount() {
            return datas.size();
        }
    };

    class ViewHolder {
        private ImageView icon;
        private TextView name;
        private TextView accept;
    }
}
