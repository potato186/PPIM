package com.ilesson.ppim.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
import com.ilesson.ppim.adapter.RecordAdapter;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.PPUserInfo;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.MyFileUtils;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.utils.Similarity;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.rong.eventbus.EventBus;
import io.rong.imageloader.core.DisplayImageOptions;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongIM;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.message.FileMessage;
import io.rong.message.ImageMessage;

import static com.ilesson.ppim.activity.ConversationActivity.CONVERSATION_TYPE;
import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;

/**
 * Created by potato on 2020/3/10.
 */
@ContentView(R.layout.activity_search)
public class SearchActivity extends BaseActivity {

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
    @ViewInject(R.id.chat_record)
    private RecyclerView chatRecordRecyclerView;
    private List<PPUserInfo> allFriends;
    private List<PPUserInfo> result;
    private String token;
    private MessageContent messageContent;
    private boolean otherFile;
    private Intent intent;
    private String targetId;
    private String targetName;
    private Uri uri;
    private List<Uri> uris = new ArrayList<>();
    private Conversation.ConversationType conversationType;
    private RecordAdapter recordAdapter;
    private List<Message> messageList;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            if(TextUtils.isEmpty(targetId)){
                search();
            }else{
                searchChatRecord();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarLightMode(this,true);
        allFriends = new ArrayList<>();
        result = new ArrayList<>();
        messageList = new ArrayList<>();
        recordAdapter = new RecordAdapter(this, messageList,targetName);
        token = SPUtils.get(LOGIN_TOKEN,"");
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeMessages(0);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(TextUtils.isEmpty(s.toString().trim())){
                    searchLayout.setVisibility(View.GONE);
                    close.setVisibility(View.GONE);
                    listView.setVisibility(View.GONE);
                }else{
//                    searchLayout.setVisibility(View.VISIBLE);
                    close.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.VISIBLE);
                    searchKey.setText(s.toString());
                    handler.sendEmptyMessageDelayed(0,600);
                }
            }
        });
        listView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Message message=null;
                PPUserInfo PPUserInfo = result.get(position);
                if(null== PPUserInfo){
                    return;
                }
                if(otherFile){
                    for(Uri uri:uris){
                        MessageContent msg=null;
                        if(intent.getType().contains("image")){
                            msg = ImageMessage.obtain(uri,uri);
                        }else{
                            msg = FileMessage.obtain(uri);
                        }
                        sendFileMsg(Conversation.ConversationType.PRIVATE,PPUserInfo.getPhone(),msg);
                    }
                    return;
                }else{
                    message = Message.obtain(PPUserInfo.getPhone(),Conversation.ConversationType.PRIVATE,messageContent);
                    sendMsg(message);
                }
                EventBus.getDefault().post(new Conversation());
                RongIM.getInstance().startConversation(mContext, Conversation.ConversationType.PRIVATE,PPUserInfo.getPhone(),PPUserInfo.getName());
                finish();
            }
        });
        searchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    // 先隐藏键盘
                    ((InputMethodManager) searchEdit.getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(SearchActivity.this
                                            .getCurrentFocus().getWindowToken(),
                                    InputMethodManager.HIDE_NOT_ALWAYS);

                    return true;
                }
                return false;
            }
        });
        listView.setAdapter(adapter);
        requestFriendsList();
        initData();
    }

    private void searchChatRecord(){
        String searchKey = searchEdit.getText().toString();
        if(TextUtils.isEmpty(searchKey)){
            return;
        }
        RongIMClient.getInstance().searchMessages(conversationType, targetId, searchKey, 0, 0, new RongIMClient.ResultCallback<List<Message>>() {
            /**
             * 成功回调
             * @param messages 查找匹配到的消息集合
             */
            @Override
            public void onSuccess(List<Message> messages) {
                Log.d(TAG, "onSuccess: "+messages);
                messageList.clear();
                messageList.addAll(messages);
                chatRecordRecyclerView.setVisibility(View.VISIBLE);
                chatRecordRecyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
                chatRecordRecyclerView.setAdapter(recordAdapter);
            }

            /**
             * 失败回调
             * @param errorCode 错误码
             */
            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

            }
        });
    }

    private void sendMsg(Message message){
        RongIM.getInstance().sendMessage(message,
                "", null, new IRongCallback.ISendMessageCallback() {
                    @Override
                    public void onAttached(Message message) {

                    }

                    @Override
                    public void onSuccess(Message message) {

                    }

                    @Override
                    public void onError(Message message, RongIMClient.ErrorCode errorCode) {

                    }
                });
    }
    private void sendFileMsg(Conversation.ConversationType type,String targetId, MessageContent fileMessage){
        Message message = Message.obtain(targetId,type,fileMessage);
        RongIM.getInstance().sendMediaMessage(message, null, null, new IRongCallback.ISendMediaMessageCallback() {
            @Override
            public void onAttached(Message message) {

            }

            @Override
            public void onSuccess(Message message) {

            }

            @Override
            public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                Log.d(TAG, "onError: ");
            }

            @Override
            public void onProgress(Message message, int i) {

            }

            @Override
            public void onCanceled(Message message) {

            }
        });
    }

    private void initData(){
        intent = getIntent();
        messageContent = intent.getParcelableExtra("msg");
        targetId = intent.getStringExtra(ConversationActivity.TARGET_ID);
        targetName = intent.getStringExtra(ConversationActivity.TARGET_NAME);
        Bundle bundle = intent.getExtras();
        if(null!=bundle){
            conversationType = (Conversation.ConversationType) intent.getExtras().getSerializable(CONVERSATION_TYPE);
        }
        String action = intent.getAction();
        if (intent.ACTION_VIEW.equals(action)) {
            intent.getType();
            Uri u = intent.getData();
            parseUri(u);
        }
        else  if (Intent.ACTION_SEND.equals(action)) {
            Uri u = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
            parseUri(u);
        }
        else  if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            final ArrayList<Uri> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            for(Uri uri:uris){
                parseUri(uri);
            }
        }
    }
    private void parseUri(Uri u){
        String url = MyFileUtils.getPath2uri(this,u);
        uri = Uri.parse("file://" + url);
        uris.add(uri);
        otherFile = true;
    }
    private void showCurrentKey(){
        searchLayout.setVisibility(View.VISIBLE);
        close.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);
    }
    private void hideCurrentKey(){
        searchLayout.setVisibility(View.GONE);
        listView.setVisibility(View.GONE);
    }
    @Event(value = R.id.cancel)
    private void back_btn(View view) {
        finish();
    }
    @Event(value = R.id.search_layout)
    private void search_layout(View view) {
//        allFriends.clear();
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
        if(TextUtils.isEmpty(searchKey)){
            Toast.makeText(this,"搜索内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        result.clear();
        for(PPUserInfo info:allFriends){
            double s1 = Similarity.SimilarDegree(info.getName(),searchKey);
            double s2 = Similarity.SimilarDegree(info.getPhone(),searchKey);
            if(!info.getName().contains(searchKey)&&!info.getPhone().contains(searchKey)){
                continue;
            }
            info.setSimilar(s1<s2?s2:s1);
            if(info.getSimilar()>0){
                result.add(info);
            }
        }
        Collections.sort(result, new Comparator<PPUserInfo>() {

            @Override
            public int compare(PPUserInfo t1, PPUserInfo t2) {
                // TODO Auto-generated method stub
                if (t1.getSimilar()> t2.getSimilar()) {
                    return 1;
                }
                if (t1.getSimilar() <t2.getSimilar()) {
                    return -1;
                }
                return 0;
            }
        });
        adapter.notifyDataSetChanged();
        searchLayout.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
    }

    BaseAdapter adapter = new BaseAdapter() {

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (null == convertView) {
                holder = new ViewHolder();
                convertView = getLayoutInflater().inflate(
                        R.layout.search_user_item, null);
                holder.icon = (ImageView) convertView.findViewById(R.id.thumbnail);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.phone = (TextView) convertView.findViewById(R.id.phone);
//				holder.tittle = (TextView) convertView.findViewById(R.id.model);
//				holder.publish = (TextView) convertView
//						.findViewById(R.id.publish);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final PPUserInfo PPUserInfo = result.get(position);
            DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
            builder.showImageOnLoading(R.mipmap.default_icon)
                    .cacheInMemory(true).cacheOnDisk(true);
            ImageLoader.getInstance().displayImage(PPUserInfo.getIcon(), holder.icon,
                    builder.build());
            holder.name.setText(PPUserInfo.getName());
            holder.phone.setText(PPUserInfo.getPhone());
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
            return result.size();
        }
    };

    class ViewHolder {
        private ImageView icon;
        private TextView name;
        private TextView phone;
    }
    public void requestFriendsList() {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.USER_URL);
        params.addBodyParameter("action", "friend");
        String token = SPUtils.get(LOGIN_TOKEN,"");
        params.addBodyParameter("token", token);
        final String path = MD5.md5(params.toString());
        final String dir = FileUtil.getCacheDir("json").getAbsolutePath();
        Log.d(TAG, "loadData: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: " + result);
                readFriendsJson(result,dir,path);
            }


            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ex.printStackTrace();
                String result = MyFileUtils.file2String(dir + File.separator + path);
                if(TextUtils.isEmpty(result)){
                    return;
                }
                readFriendsJson(result, null, null);
            }


            @Override
            public void onCancelled(CancelledException cex) {
                cex.printStackTrace();
            }


            @Override
            public void onFinished() {
            }
        });
    }

    private void readFriendsJson(String json,String dir,String path){
        BaseCode<List<PPUserInfo>> base = new Gson().fromJson(
                json,
                new TypeToken<BaseCode<List<PPUserInfo>>>() {
                }.getType());
        if (base.getCode() == 0) {
            if (dir != null && path != null) {
                try {
                    MyFileUtils.saveFile(dir, path, json);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            List<PPUserInfo> data = base.getData();
            allFriends.addAll(data);
            if (null == data || data.isEmpty()) {
                return;
            }
        } else {
        }
    }
}
