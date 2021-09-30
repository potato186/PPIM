package com.ilesson.ppim.activity;

import static com.ilesson.ppim.activity.ConversationActivity.CONVERSATION_TYPE;
import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;
import static com.ilesson.ppim.activity.LoginActivity.USER_PHONE;

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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.adapter.AllSearchAdapter;
import com.ilesson.ppim.adapter.RecordAdapter;
import com.ilesson.ppim.adapter.SearchItemAdapter;
import com.ilesson.ppim.db.ConversationDao;
import com.ilesson.ppim.db.GroupUserDao;
import com.ilesson.ppim.db.PPUserDao;
import com.ilesson.ppim.entity.AllSearchInfo;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.ConversationInfo;
import com.ilesson.ppim.entity.GroupInfo;
import com.ilesson.ppim.entity.PPUserInfo;
import com.ilesson.ppim.entity.SearchInfo;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.MyFileUtils;
import com.ilesson.ppim.utils.PPScreenUtils;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.utils.TextUtil;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

/**
 * Created by potato on 2020/3/10.
 */
@ContentView(R.layout.activity_search)
public class SearchActivity extends BaseActivity {

    @ViewInject(R.id.search_edit)
    private EditText searchEdit;
    @ViewInject(R.id.close)
    private View close;
    @ViewInject(R.id.search_type_layout)
    private View searchTypeLayout;
    @ViewInject(R.id.type_name)
    private TextView typeNameView;
    @ViewInject(R.id.no_user)
    private TextView noUser;
    @ViewInject(R.id.empty)
    private TextView emptyText;
    @ViewInject(R.id.search_listiview)
    private ListView listView;
    @ViewInject(R.id.chat_record)
    private RecyclerView chatRecordRecyclerView;
    @ViewInject(R.id.all_search)
    private RecyclerView allSearchRecyclerView;
    private List<PPUserInfo> allFriends;
    private List<AllSearchInfo> allSearchInfos;
    private List<PPUserInfo> searchFriends;
    private List<SearchInfo> searchInfos;
    private AllSearchAdapter allSearchAdapter;
    private String token;
    private String mySelfId;
    private MessageContent messageContent;
    private boolean otherFile;
    private Intent intent;
    private String targetId;
    private String targetName;
    private Uri uri;
    private List<Uri> uris = new ArrayList<>();
    private Conversation.ConversationType conversationType;
    private RecordAdapter recordAdapter;
    private SearchItemAdapter searchItemAdapter;
    private List<Message> messageList;
    private PPUserDao ppUserDao;
    private ConversationDao conversationDao;
    private GroupUserDao groupUserDao;
    public static final int SEARCH_FRIENDS=1;
    public static final int SEARCH_GROUP=2;
    public static final int SEARCH_RECORD_IN_ALL_CONVERSATIONS=3;
    public static final int SEARCH_RECORD_WITH_GARGET=4;
    public static final int SEARCH_ALL=5;
    public static final int SEARCH_RECORD_STEP=6;
    public static final String SEARCH_TYPE="search_type";
    public static final String SEARCH_TYPE_NAME="search_type_name";
    public static final String SEARCH_KEY="search_key";

    private int searchType;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case SEARCH_FRIENDS:
                    searchFriends();
                    break;
                case SEARCH_GROUP:
                    searchGroups();
                    break;
                case SEARCH_RECORD_IN_ALL_CONVERSATIONS:
                    searchConversations();
                    break;
                case SEARCH_RECORD_WITH_GARGET:
                    searchChatRecord(conversationType,targetId,searchKey,false);
                    break;
                case SEARCH_ALL:
                    searchAll();
                    break;
                case SEARCH_RECORD_STEP:
                    String targetId = (String) msg.obj;
                    ConversationInfo conversationInfo = converMap.get(targetId);
                    if(null!=conversationInfo&&!conversationInfos.contains(conversationInfo)){
                        conversationInfos.add(conversationInfo);
                        emptyText.setVisibility(View.GONE);
                        if(nolimitSize){
                            searchInfos.add(conversationInfo);
                            searchItemAdapter.notifyDataSetChanged();
                            hideProgress();
                        }else{
                            checkChildList(conversationInfos,getString(R.string.chat_record));
                            hasAddChatRecord = true;
                        }
                    }
                    if(conversationInfos.size()>=3&&!nolimitSize){
                        stopSearchRecord = true;
                    }
                    break;
            }
        }
    };
    private boolean stopSearchRecord;
    private List<ConversationInfo> conversationInfos;
    private Map<String,ConversationInfo> converMap;
    private void searchConversations() {
        stopSearchRecord=false;
        conversationDao = new ConversationDao();
        conversationInfos = new ArrayList<>();
        List<ConversationInfo> datas = conversationDao.getConversations();
        if(null!=datas&&datas.size()>0){
            for (ConversationInfo data : datas) {
                converMap.put(data.getTargetId(),data);
                if(stopSearchRecord){
                    break;
                }
                searchChatRecord(Conversation.ConversationType.setValue(data.getType()),data.getTargetId(),searchKey,true);
            }
        }
    }
    private List<GroupInfo> groupInfos;
    private void searchGroups() {
        groupUserDao = new GroupUserDao();
        groupInfos = groupUserDao.searchByKey(searchKey);
        addSearchItemData(groupInfos);
    }

    private void searchAll() {
        hasAddChatRecord=false;

        allSearchAdapter = new AllSearchAdapter(this,allSearchInfos,searchKey);
        allSearchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        allSearchRecyclerView.setAdapter(allSearchAdapter);
        searchFriends();
        checkChildList(searchFriends,getString(R.string.contact_friends));
        searchGroups();
        checkChildList(groupInfos,getString(R.string.group));
        searchConversations();
        if(allSearchInfos.size()>0)
        allSearchRecyclerView.setVisibility(View.VISIBLE);
        showEmpty(allSearchInfos);
    }
    private boolean hasAddChatRecord;
    private void checkChildList(List<? extends SearchInfo> lists,String type){

        if(null!=lists&&lists.size()>0){
            AllSearchInfo allSearchInfo = new AllSearchInfo();
            allSearchInfo.setSearchType(type);
            if(lists.size()>3){
                allSearchInfo.setSearchInfos((List<SearchInfo>) lists.subList(0,4));
            }else{
                allSearchInfo.setSearchInfos((List<SearchInfo>) lists);
            }
            if(hasAddChatRecord&&type.equals(getString(R.string.chat_record))&&allSearchInfos.size()>0){
                allSearchInfos.remove(allSearchInfos.size()-1);
            }
            allSearchInfos.add(allSearchInfo);
            allSearchAdapter.notifyDataSetChanged();
            hideProgress();
            Log.d(TAG, "checkChildList: allSearchInfos.size="+allSearchInfos.size());
            Log.d(TAG, "checkChildList: allSearchInfo type="+type);
        }
    }
    private void clearData(){
        allFriends.clear();
        allSearchInfos.clear();
        searchFriends.clear();
        searchInfos.clear();
        messageList.clear();
        converMap.clear();
    }
    private String searchKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarLightMode(this,true);
        allFriends = new ArrayList<>();
        allSearchInfos = new ArrayList<>();
        searchFriends = new ArrayList<>();
        searchInfos = new ArrayList<>();
        messageList = new ArrayList<>();
        converMap = new HashMap<>();
        recordAdapter = new RecordAdapter(this, messageList,searchKey);
        token = SPUtils.get(LOGIN_TOKEN,"");
        mySelfId = SPUtils.get(USER_PHONE,"");
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
                    close.setVisibility(View.GONE);
                    clearData();
//                    listView.setVisibility(View.GONE);
                }else{
                    close.setVisibility(View.VISIBLE);
//                    listView.setVisibility(View.VISIBLE);
                    searchKey=s.toString();
//                    showProgress();
                    handler.sendEmptyMessageDelayed(searchType,600);
                }
            }
        });
        listView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Message message=null;
                PPUserInfo PPUserInfo = searchFriends.get(position);
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
//        listView.setAdapter(adapter);
        initData();
    }

    private void searchChatRecord(Conversation.ConversationType conversationType,String targetId,String searchKey,boolean check){
        if(stopSearchRecord){
            return;
        }
        RongIMClient.getInstance().searchMessages(conversationType, targetId, searchKey, 20, 0, new RongIMClient.ResultCallback<List<Message>>() {
            /**
             * 成功回调
             * @param messages 查找匹配到的消息集合
             */
            @Override
            public void onSuccess(List<Message> messages) {
                if(check){
                    if(messages==null||messages.size()==0){
                    }else {
                        android.os.Message msg = android.os.Message.obtain();
                        msg.what=SEARCH_RECORD_STEP;
                        msg.obj=targetId;
                        handler.sendMessage(msg);
                    }
                    return;
                }
                showEmpty(messages);
                Log.d(TAG, "onSuccess: "+messages);
                messageList.clear();
                messageList.addAll(messages);
                chatRecordRecyclerView.setVisibility(View.VISIBLE);
                chatRecordRecyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
                recordAdapter.setKeyWords(searchKey);
                chatRecordRecyclerView.setAdapter(recordAdapter);
                hideProgress();
            }

            /**
             * 失败回调
             * @param errorCode 错误码
             */
            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                Log.d(TAG, "onError: "+errorCode);
            }
        });
    }
    private void showEmpty(List<? extends Object> list){
        hideProgress();
        if(list==null||list.size()==0){
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText(TextUtil.getKeyWordsColorString(mContext,String.format(getResources().getString(R.string.search_empty_tips),searchKey),searchKey));
        }else{
            emptyText.setVisibility(View.GONE);
        }
    }
    private boolean nolimitSize;
    private void initData(){
        intent = getIntent();
        messageContent = intent.getParcelableExtra("msg");
        targetId = intent.getStringExtra(ConversationActivity.TARGET_ID);
        targetName = intent.getStringExtra(ConversationActivity.TARGET_NAME);
        conversationType = Conversation.ConversationType.setValue(intent.getIntExtra(CONVERSATION_TYPE,0));
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
        searchType = intent.getIntExtra(SEARCH_TYPE,0);

        searchKey = intent.getStringExtra(SEARCH_KEY);
        String searchTypeName = intent.getStringExtra(SEARCH_TYPE_NAME);
        if(!TextUtils.isEmpty(searchTypeName)){
            if(searchTypeName.equals(getString(R.string.contact_friends))){
                searchType=SEARCH_FRIENDS;
            }else if(searchTypeName.equals(getString(R.string.group))){
                searchType=SEARCH_GROUP;
            }else if(searchTypeName.equals(getString(R.string.chat_record))){
                searchType=SEARCH_RECORD_IN_ALL_CONVERSATIONS;
            }
            nolimitSize = true;
            searchItemAdapter = new SearchItemAdapter(this,searchInfos,searchKey);
            allSearchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            allSearchRecyclerView.setAdapter(searchItemAdapter);
            allSearchRecyclerView.setVisibility(View.VISIBLE);
            typeNameView.setText(searchTypeName);
            searchTypeLayout.setVisibility(View.VISIBLE);
            allSearchRecyclerView.setPadding(PPScreenUtils.dip2px(this,15),0,0,0);
        }
        if(!TextUtils.isEmpty(searchKey)){
            searchEdit.setText(searchKey);
            searchEdit.setSelection(searchKey.length());
        }
        ppUserDao = new PPUserDao();
        allFriends.addAll(ppUserDao.getAllFriends());
//        requestFriendsList();
    }
    private void parseUri(Uri u){
        String url = MyFileUtils.getPath2uri(this,u);
        uri = Uri.parse("file://" + url);
        uris.add(uri);
        otherFile = true;
    }
    @Event(value = R.id.cancel)
    private void back_btn(View view) {
        finish();
    }
    @Event(value = R.id.search_layout)
    private void search_layout(View view) {
//        allFriends.clear();
        searchFriends();
    }
    @Event(value = R.id.close)
    private void close(View view) {
        noUser.setVisibility(View.GONE);
        close.setVisibility(View.GONE);
        listView.setVisibility(View.GONE);
        searchEdit.setText("");
    }
    private static final String TAG = "SearchFriendActivity";
    private void addSearchItemData(List<? extends SearchInfo> lists){
        if(nolimitSize){
            searchInfos.addAll(lists);
            searchItemAdapter.notifyDataSetChanged();
            hideProgress();
        }
    }
    private void searchFriends() {
        PPUserDao ppUserDao = new PPUserDao();
        searchFriends=ppUserDao.getFriendsByKey(searchKey);
        addSearchItemData(searchFriends);
//        String searchKey = searchEdit.getText().toString();
//        searchFriends.clear();
//        for(PPUserInfo info:allFriends){
//            double s1 = Similarity.SimilarDegree(info.getName(),searchKey);
//            double s2 = Similarity.SimilarDegree(info.getPhone(),searchKey);
//            if(!info.getName().contains(searchKey)&&!info.getPhone().contains(searchKey)){
//                continue;
//            }
//            info.setSimilar(s1<s2?s2:s1);
//            if(info.getSimilar()>0){
//                searchFriends.add(info);
//            }
//        }
//        Collections.sort(searchFriends, new Comparator<PPUserInfo>() {
//
//            @Override
//            public int compare(PPUserInfo t1, PPUserInfo t2) {
//                // TODO Auto-generated method stub
//                if (t1.getSimilar()> t2.getSimilar()) {
//                    return 1;
//                }
//                if (t1.getSimilar() <t2.getSimilar()) {
//                    return -1;
//                }
//                return 0;
//            }
//        });
//        if(searchType==SEARCH_FRIENDS){
//            adapter.notifyDataSetChanged();
//            listView.setVisibility(View.VISIBLE);
//        }
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
            final PPUserInfo PPUserInfo = searchFriends.get(position);
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
            return searchFriends.size();
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
        x.http().post(params, new Callback.CacheCallback<String>() {
            @Override
            public boolean onCache(String result) {
                return false;
            }

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
    public static void launch(Context context,int searchType,String searchKey){
        Intent intent = new Intent(context,SearchActivity.class);
        intent.putExtra(SEARCH_TYPE,searchType);
        intent.putExtra(SEARCH_KEY,searchKey);
    }
}
