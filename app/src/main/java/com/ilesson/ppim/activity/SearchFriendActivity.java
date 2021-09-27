package com.ilesson.ppim.activity;

import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.BaseCode;
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

import io.rong.imageloader.core.DisplayImageOptions;
import io.rong.imageloader.core.ImageLoader;

/**
 * Created by potato on 2020/3/10.
 */
@ContentView(R.layout.activity_search_friend)
public class SearchFriendActivity extends BaseActivity {

    @ViewInject(R.id.search_edit)
    private EditText searchEdit;
    @ViewInject(R.id.search_layout)
    private View searchLayout;
    @ViewInject(R.id.result_layout)
    private View resultLayout;
    @ViewInject(R.id.close)
    private View close;
    @ViewInject(R.id.user_icon)
    private ImageView userIcon;
    @ViewInject(R.id.user_name)
    private TextView userName;
    @ViewInject(R.id.no_user)
    private TextView noUser;
    @ViewInject(R.id.search_key)
    private TextView searchKey;
    @ViewInject(R.id.search_listiview)
    private ListView listView;
    private List<PPUserInfo> datas;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarLightMode(this, true);
        datas = new ArrayList<>();
        token = SPUtils.get(LOGIN_TOKEN, "");
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString().trim())) {
                    searchLayout.setVisibility(View.GONE);
                    close.setVisibility(View.GONE);
                } else {
                    searchLayout.setVisibility(View.VISIBLE);
                    close.setVisibility(View.VISIBLE);
                    searchKey.setText(s.toString());
                }
            }
        });
        searchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    search();
                    return true;
                }
                return false;

            }
        });
        listView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PPUserInfo PPUserInfo = datas.get(position);
                if (null == PPUserInfo) {
                    return;
                }
                FriendDetailActivity.launch(SearchFriendActivity.this,PPUserInfo);
            }
        });
    }

    private void showCurrentKey() {
        searchLayout.setVisibility(View.VISIBLE);
        close.setVisibility(View.VISIBLE);
        resultLayout.setVisibility(View.GONE);
    }

    private void hideCurrentKey() {
        searchLayout.setVisibility(View.GONE);
        resultLayout.setVisibility(View.GONE);
    }

    @Event(value = R.id.cancel)
    private void back_btn(View view) {
        finish();
    }

    @Event(value = R.id.search_layout)
    private void search_layout(View view) {
        datas.clear();
        search();
    }

    @Event(value = R.id.close)
    private void close(View view) {
        noUser.setVisibility(View.GONE);
        close.setVisibility(View.GONE);
        searchLayout.setVisibility(View.GONE);
        listView.setVisibility(View.GONE);
        searchEdit.setText("");
    }

    ///pp/user?action=query&token=%s&target=%s
    private static final String TAG = "SearchFriendActivity";

    private void search() {
        String searchKey = searchEdit.getText().toString();
        if (TextUtils.isEmpty(searchKey)) {
            Toast.makeText(this, "搜索内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(token)) {
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.USER_URL);
        params.addBodyParameter("action", "query");
        params.addBodyParameter("token", token);
        params.addBodyParameter("target", searchKey);
        Log.d(TAG, "loadData: " + params.toString());
        showProgress();
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                removeDialog(DIALOG_PROGRESS);
                BaseCode<List<PPUserInfo>> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<List<PPUserInfo>>>() {
                        }.getType());
                if (base.getCode() == 0) {
                    List<PPUserInfo> data = base.getData();
                    searchLayout.setVisibility(View.GONE);
                    if (null == data || data.isEmpty()) {
                        noUser.setVisibility(View.VISIBLE);
                        return;
                    }
                    datas.addAll(data);
                    listView.setVisibility(View.VISIBLE);
                    listView.setAdapter(adapter);
                } else {
                    noUser.setVisibility(View.VISIBLE);
                    searchLayout.setVisibility(View.GONE);
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
                        R.layout.user_item, null);
                holder.icon = (ImageView) convertView.findViewById(R.id.thumbnail);
                holder.name = (TextView) convertView.findViewById(R.id.name);
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
    }
}
