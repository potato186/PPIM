package com.ilesson.ppim.activity;

import static com.ilesson.ppim.activity.FriendDetailActivity.USER_INFO;
import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ilesson.ppim.IlessonApp;
import com.ilesson.ppim.R;
import com.ilesson.ppim.contactcard.activities.ContactDetailActivity;
import com.ilesson.ppim.entity.PPUserInfo;
import com.ilesson.ppim.utils.MyFileUtils;
import com.ilesson.ppim.utils.SPUtils;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.rong.eventbus.EventBus;
import io.rong.imageloader.core.DisplayImageOptions;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;
import io.rong.message.FileMessage;
import io.rong.message.ImageMessage;

/**
 * Created by potato on 2020/3/10.
 */
@ContentView(R.layout.activity_conversion_list)
public class ForwadSelectActivity extends BaseActivity {

    @ViewInject(R.id.search_listiview)
    private ListView listView;
    private List<Conversation> datas = new ArrayList<>();
    private String token;
    private static final String TAG = "ForwadSelectActivity";
    private MessageContent messageContent;
    private boolean otherFile;
    private Intent intent;
    private Uri uri;
    private List<Uri> uris = new ArrayList<>();
    public static final String HAS_LOAD = "has_load";
    public static final String INTENT_TYPE = "intent_type";
    public static final String COMPOSEMESSAGE = "composeMessage";
    public static final int PAY_SCORE = 1;
    public static final int SEND_FRIEND_CARD = 2;
    public static int ACTION_TYPE = 0;
    private PPUserInfo ppUserInfo;
    @Event(R.id.back_btn)
    private void back(View v) {
        finish();
    }

    @Event(R.id.search_edit)
    private void search_edit(View v) {
        Intent intent = getIntent();
        intent.setClass(this, SearchActivity.class);
        intent.putExtra(SearchActivity.SEARCH_TYPE,SearchActivity.SEARCH_FRIENDS);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarLightMode(this, true);
        init();
    }

    private void initData() {
        Intent intent = getIntent();
        messageContent = intent.getParcelableExtra("msg");
        String action = intent.getAction();
        if (intent.ACTION_VIEW.equals(action)) {
            intent.getType();
            Uri u = intent.getData();
            parseUri(u);
        } else if (Intent.ACTION_SEND.equals(action)) {
            Uri u = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
            parseUri(u);
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            final ArrayList<Uri> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            for (Uri uri : uris) {
                parseUri(uri);
            }
        }
        token = SPUtils.get(LOGIN_TOKEN, "");
        listView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        listView.setAdapter(adapter);
    }

    private void parseUri(Uri u) {
        String url = MyFileUtils.getPath2uri(this, u);
        uri = Uri.parse("file://" + url);
        uris.add(uri);
        otherFile = true;
    }

    private void init() {
        intent = getIntent();
        ACTION_TYPE = intent.getIntExtra(INTENT_TYPE, 0);
        Bundle bundle = intent.getExtras();
        ppUserInfo = (PPUserInfo) bundle.getSerializable(USER_INFO);
        RongIM.getInstance().getConversationList(new RongIMClient.ResultCallback<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                HashMap<String, Conversation> map = new HashMap<>();
                if (conversations != null && !conversations.isEmpty()) {
                    for (Conversation conversation : conversations) {
                        if (conversation.getConversationType() == Conversation.ConversationType.PRIVATE&&TextUtils.isEmpty(IlessonApp.getInstance().getUserByPhone(conversation.getTargetId()))) {
                            continue;
                        }
                        if (ACTION_TYPE == PAY_SCORE) {
                            if (conversation.getConversationType() == Conversation.ConversationType.PRIVATE) {
                                map.put(conversation.getTargetId(), conversation);
                                datas.add(conversation);
                            }
                        } else {
                            map.put(conversation.getTargetId(), conversation);
                            datas.add(conversation);
                        }
                    }
                }
                List<PPUserInfo> list = IlessonApp.getInstance().getDatas();
                for (PPUserInfo userInfo : list) {
                    if (map.get(userInfo.getPhone()) == null) {
                        Conversation conversation = new Conversation();
                        conversation.setTargetId(userInfo.getPhone());
                        conversation.setPortraitUrl(userInfo.getIcon());
                        conversation.setConversationTitle(userInfo.getName());
                        conversation.setConversationType(Conversation.ConversationType.PRIVATE);
                        datas.add(conversation);
                    }
                }
                initData();
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

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
            final Conversation conversation = datas.get(position);
            String title = conversation.getConversationTitle();
            String portrait = conversation.getPortraitUrl();
            if (TextUtils.isEmpty(title)) {
                title = RongContext.getInstance().getConversationTemplate(conversation.getConversationType().getName()).getTitle(conversation.getTargetId());
            }
            final String userName = title;
            if (TextUtils.isEmpty(portrait)) {
                Uri url = RongContext.getInstance().getConversationTemplate(conversation.getConversationType().getName()).getPortraitUri(conversation.getTargetId());
                portrait = url != null ? url.toString() : "";
            }
            final String userIcon = portrait;
            if (!TextUtils.isEmpty(portrait)) {
                DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
                builder.showImageOnLoading(R.mipmap.default_icon)
                        .cacheInMemory(true).cacheOnDisk(true);
                ImageLoader.getInstance().displayImage(portrait, holder.icon,
                        builder.build());
            }

            if (!TextUtils.isEmpty(title)) {
                holder.name.setText(title);
            }
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Message message = null;
                    if (ACTION_TYPE == PAY_SCORE) {
                        PPUserInfo user = new PPUserInfo(conversation.getTargetId(), userName, userIcon);
                        intent.setClass(ForwadSelectActivity.this, PayScoreActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(FriendDetailActivity.USER_INFO, user);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    } else if(ACTION_TYPE==SEND_FRIEND_CARD){
                        UserInfo userInfo = new UserInfo(ppUserInfo.getPhone(),ppUserInfo.getName(), Uri.parse(ppUserInfo.getIcon()));
                        Intent intent = new Intent(ForwadSelectActivity.this, ContactDetailActivity.class);
                        intent.putExtra("contact", userInfo);
                        intent.putExtra("conversationType", conversation.getConversationType());
                        intent.putExtra("targetId", conversation.getTargetId());
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                    else if (otherFile) {
                        for (Uri uri : uris) {
                            MessageContent msg = null;
                            if (intent.getType().contains("image")) {
                                msg = ImageMessage.obtain(uri, uri);
                            } else {
                                msg = FileMessage.obtain(uri);
                            }
                            sendFileMsg(conversation.getConversationType(), conversation.getTargetId(), msg, userName);
                        }
                    } else {
                        message = Message.obtain(conversation.getTargetId(), conversation.getConversationType(), messageContent);
                        sendMsg(message, userName);
                    }
                    finish();
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
    }

    private void sendMsg(Message message, final String title) {
        RongIM.getInstance().sendMessage(message,
                "", null, new IRongCallback.ISendMessageCallback() {
                    @Override
                    public void onAttached(Message message) {

                    }

                    @Override
                    public void onSuccess(Message message) {
                        EventBus.getDefault().post(new Conversation());
                        RongIM.getInstance().startConversation(ForwadSelectActivity.this, message.getConversationType(), message.getTargetId(), title);
                    }

                    @Override
                    public void onError(Message message, RongIMClient.ErrorCode errorCode) {

                    }
                });
    }

    private void sendFileMsg(Conversation.ConversationType type, String targetId, MessageContent fileMessage, final String title) {
        Message message = Message.obtain(targetId, type, fileMessage);
        RongIM.getInstance().sendMediaMessage(message, null, null, new IRongCallback.ISendMediaMessageCallback() {
            @Override
            public void onAttached(Message message) {

            }

            @Override
            public void onSuccess(Message message) {
                EventBus.getDefault().post(new Conversation());
                RongIM.getInstance().startConversation(ForwadSelectActivity.this, message.getConversationType(), message.getTargetId(), title);
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

    private void sendImageMsg(Conversation.ConversationType type, String targetId, ImageMessage imageMessage) {
        RongIM.getInstance().sendImageMessage(type, targetId, imageMessage, null, null, new RongIMClient.SendImageMessageCallback() {
            @Override
            public void onAttached(Message message) {

                Log.e(TAG, "-------------onAttached--------");
            }

            @Override
            public void onError(Message message, RongIMClient.ErrorCode code) {
                Log.e(TAG, "----------------onError-----" + code);
            }

            @Override
            public void onSuccess(Message message) {
                Log.e(TAG, "------------------onSuccess---");
            }

            @Override
            public void onProgress(Message message, int progress) {
                Log.e(TAG, "-----------------onProgress----" + progress);

            }
        });
    }
}
