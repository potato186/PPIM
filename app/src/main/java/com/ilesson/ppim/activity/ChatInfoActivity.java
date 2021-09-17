package com.ilesson.ppim.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.FreshConversation;
import com.ilesson.ppim.entity.ModifyGroupNike;
import com.ilesson.ppim.entity.PPUserInfo;
import com.ilesson.ppim.entity.ResetGroupName;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.IMUtils;
import com.ilesson.ppim.utils.PPScreenUtils;
import com.ilesson.ppim.utils.RecyclerViewSpacesItemDecoration;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.view.ScrollListView;
import com.ilesson.ppim.view.SwitchButton;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.rong.eventbus.EventBus;
import io.rong.imkit.RongIM;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;

import static com.ilesson.ppim.activity.AvatarActivity.MODIFY_SUCCESS;
import static com.ilesson.ppim.activity.ContactActivity.HAS_MEMBERS;
import static com.ilesson.ppim.activity.ContactActivity.INVATE_GROUP_TYPE;
import static com.ilesson.ppim.activity.ContactActivity.REMOVE_GROUP_TYPE;
import static com.ilesson.ppim.activity.ContactActivity.REMOVE_RESULT;
import static com.ilesson.ppim.activity.ContactActivity.SELECT_ACTION;
import static com.ilesson.ppim.activity.ModifyNameActivity.MODIFY_CONTENT;
import static com.ilesson.ppim.activity.ModifyNameActivity.MODIFY_GROUP;
import static com.ilesson.ppim.activity.ModifyNameActivity.MODIFY_NIKE_IN_GROUP;
import static com.ilesson.ppim.activity.ModifyNameActivity.MODIFY_RESULT;
import static com.ilesson.ppim.activity.ModifyNameActivity.MODIFY_TYPE;
import static com.ilesson.ppim.activity.MoreMemberActivity.GROUP_MEMBER;
import static com.ilesson.ppim.view.SwitchButton.PLAY_TTS;


/**
 * Created by potato on 2019/4/9.
 */
@ContentView(R.layout.act_chat_info)
public class ChatInfoActivity extends BaseActivity{
    @ViewInject(R.id.disturb_switch)
    private Switch disturbSwitch;
    @ViewInject(R.id.message_top_switch)
    private Switch messageTopSwitch;
    @ViewInject(R.id.title)
    private TextView titleTextView;
    @ViewInject(R.id.group_name)
    private TextView groupNameTextView;
    @ViewInject(R.id.tag_name)
    private TextView tagNameTextview;
    @ViewInject(R.id.nike_name)
    private TextView nikeNameView;
    @ViewInject(R.id.delete)
    private TextView delete;
    @ViewInject(R.id.shop_list_layout)
    private View shopList;
    @ViewInject(R.id.quit)
    private View quit;
    @ViewInject(R.id.more_member)
    private View moreMember;
    @ViewInject(R.id.voice_switch)
    private SwitchButton switchButton;
    @ViewInject(R.id.recylerview)
    private ScrollListView recylerView;
    private DataAdapter adapter;
    private List<PPUserInfo> datas;
    private static final String TAG = "ChatInfoActivity";
    public static final String GROUP_ID = "group_id";
    public static final String GROUP_NAME = "group_name";
    public static final String GROUP_ICON = "group_icon";
    public static final String NIKE_NAME = "nike_name";
    public static final String GROUP_TAG = "group_tag";
    public static final String ISOWNER = "isOwner";
    private String groupName;
    private String nikeName="";
    private String groupId;
    private String groupTag;
    private boolean modifyed;
    private PPUserInfo addUser,deleteUser;
    private String groupIcon;
    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setStatusBarLightMode(this,true);
        EventBus.getDefault().register(this);
        groupId = getIntent().getStringExtra(GROUP_ID);
        groupTag = getIntent().getStringExtra(GROUP_TAG);
        groupName = getIntent().getStringExtra(GROUP_NAME);
        if(!TextUtils.isEmpty(groupTag)){
            groupName = groupName.replace(groupTag,"").replace("(","").replace(")","");
        }
        isOwner = getIntent().getBooleanExtra(ISOWNER,false);
        nikeName = SPUtils.get(NIKE_NAME,"");
        groupIcon = getIntent().getStringExtra(GROUP_ICON);
        if(isOwner){
            delete.setVisibility(View.VISIBLE);
            if(groupId.contains("market")){
                shopList.setVisibility(View.VISIBLE);
            }
        }

        nikeNameView.setText(nikeName);
        groupNameTextView.setText(groupName);
        if(!TextUtils.isEmpty(groupTag)){
            tagNameTextview.setText(groupTag);
        }
        datas = new ArrayList<>();
        adapter = new DataAdapter(datas);
        recylerView.setLayoutManager(new GridLayoutManager(this,5));

        HashMap<String, Integer> stringIntegerHashMap = new HashMap<>();
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.TOP_DECORATION, PPScreenUtils.dip2px(this, 4));
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.BOTTOM_DECORATION, PPScreenUtils.dip2px(this, 4));
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.TOP_DECORATION, PPScreenUtils.dip2px(this, 6));
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.BOTTOM_DECORATION, PPScreenUtils.dip2px(this, 6));

        recylerView.addItemDecoration(new RecyclerViewSpacesItemDecoration(stringIntegerHashMap));
        recylerView.setAdapter(adapter);
        addUser = new PPUserInfo();
        addUser.setName("add");
        deleteUser = new PPUserInfo();
        deleteUser.setName("remove");
        boolean state = SPUtils.get(PLAY_TTS,true);
        if(state){
            switchButton.open(1);
        }else {
            switchButton.close(1);
        }
        switchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton buttonView, boolean isChecked) {
                SPUtils.put(PLAY_TTS, isChecked);
            }
        });
        requestGroupInfo();
        RongIMClient.getInstance().getConversationNotificationStatus(Conversation.ConversationType.GROUP, groupId, new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {
            /**
             * 成功回调
             * @param status 消息提请状态
             */
            @Override
            public void onSuccess(Conversation.ConversationNotificationStatus status) {
                disturbSwitch.setChecked(status== Conversation.ConversationNotificationStatus.NOTIFY?false:true);
                disturbSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        Conversation.ConversationNotificationStatus notificationStatus = isChecked?Conversation.ConversationNotificationStatus.DO_NOT_DISTURB:Conversation.ConversationNotificationStatus.NOTIFY;
                        RongIMClient.getInstance().setConversationNotificationStatus(Conversation.ConversationType.GROUP, groupId, notificationStatus, new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {

                            /**
                             * 成功回调
                             * @param status 消息提请状态
                             */
                            @Override
                            public void onSuccess(Conversation.ConversationNotificationStatus status) {
                                EventBus.getDefault().post(new FreshConversation());
                                RongIM.getInstance().setConversationNotificationStatus(Conversation.ConversationType.GROUP,
                                        groupId, status, new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {
                                            @Override
                                            public void onSuccess(Conversation.ConversationNotificationStatus conversationNotificationStatus) {
                                            }

                                            @Override
                                            public void onError(RongIMClient.ErrorCode errorCode) {
                                            }
                                        });
                            }

                            /**
                             * 错误回调
                             * @param errorCode 错误码
                             */
                            @Override
                            public void onError(RongIMClient.ErrorCode errorCode) {

                            }
                        });
                    }
                });
            }

            /**
             * 错误回调
             * @param errorCode 错误码
             */
            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

            }
        });

        messageTopSwitch.setChecked(UserSttingActivity.isTop);
        messageTopSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                UserSttingActivity.isTop=isChecked;
                RongIMClient.getInstance().setConversationToTop(Conversation.ConversationType.GROUP, groupId, isChecked, false, new
                        RongIMClient.ResultCallback<Boolean>() {
                            /**
                             * 成功回调
                             */
                            @Override
                            public void onSuccess(Boolean success) {
                                EventBus.getDefault().post(new FreshConversation());
                            }
                            /**
                             * 错误回调
                             */
                            @Override
                            public void onError(RongIMClient.ErrorCode ErrorCode) {

                            }
                        }) ;
            }
        });
    }

    @Event(R.id.back_btn)
    private void back_btn(View view){
        out();
    }
    @Event(R.id.shop_list_layout)
    private void shop_list_layout(View view){
        startActivity(new Intent(this,ShopKeeperOrderListActivity.class));
    }
    @Event(R.id.more_member)
    private void more_member(View view){
        startActivity(new Intent(this,MoreMemberActivity.class).putExtra(GROUP_MEMBER,(Serializable)ppUserInfos));
    }
    @Event(R.id.search_record)
    private void search_record(View view){
        Intent intent = new Intent(this,SearchActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(ConversationActivity.CONVERSATION_TYPE, Conversation.ConversationType.GROUP);
        intent.putExtras(bundle);
        intent.putExtra(ConversationActivity.TARGET_ID,groupId);
        intent.putExtra(ConversationActivity.TARGET_NAME, groupName);
        startActivity(intent);
    }
    @Event(R.id.code_layout)
    private void code_layout(View view){
        Intent intent = new Intent(ChatInfoActivity.this,UserCodeActivity.class);
        intent.putExtra(GROUP_ID, groupId);
        startActivity(intent);
    }
    @Event(R.id.group_name_layout)
    private void group_name(View view){
        if(!isOwner&&groupId.contains("market")){
            return;
        }
        Intent intent = new Intent(ChatInfoActivity.this,ModifyNameActivity.class);
        intent.putExtra(GROUP_ID, groupId);
        intent.putExtra(MODIFY_TYPE, MODIFY_GROUP);
        intent.putExtra(ChatInfoActivity.GROUP_NAME, groupName);
        requstCode = MODIFY_GROUP;
        startActivityForResult(intent,MODIFY_GROUP);
    }
    @Event(R.id.tag_layout)
    private void tag_layout(View view){
        Intent intent = new Intent(ChatInfoActivity.this,ModifyGroupTagNameActivity.class);
        intent.putExtra(GROUP_ID, groupId);
        intent.putExtra(GROUP_ICON, groupIcon);
//        intent.putExtra(MODIFY_TYPE, MODIFY_GROUP);
        intent.putExtra(ChatInfoActivity.GROUP_NAME, groupTag);
//        requstCode = MODIFY_GROUP;
        startActivity(intent);
    }
    @Event(R.id.nike_layout)
    private void nike_layout(View view){
        Intent intent = new Intent(ChatInfoActivity.this,ModifyNikeNameActivity.class);
        intent.putExtra(GROUP_ID, groupId);
        intent.putExtra(GROUP_ICON, groupIcon);
        intent.putExtra(MODIFY_TYPE, MODIFY_NIKE_IN_GROUP);
        intent.putExtra(MODIFY_CONTENT, nikeName);
        requstCode = MODIFY_NIKE_IN_GROUP;
        startActivityForResult(intent,MODIFY_NIKE_IN_GROUP);
    }
    private int requstCode;
    @Event(R.id.quit)
    private void quit(View view){
        showQuitDialog();
    }
    @Event(R.id.delete)
    private void delete(View view){
        showDeleteDialog();
    }
    @Override
    public void onBackPressed() {
        out();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == event.getKeyCode()) {
            out();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void out(){
        if(modifyed&&MODIFY_GROUP==requstCode){
            Intent intent = new Intent();
            intent.putExtra(MODIFY_RESULT, groupName);
            setResult(MODIFY_SUCCESS,intent);
        }
        finish();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== MODIFY_SUCCESS){
            if(requestCode==MODIFY_GROUP){
                groupName = data.getStringExtra(MODIFY_RESULT);
                groupNameTextView.setText(groupName);
                modifyed = true;
                return;
            }
            if(requestCode==MODIFY_NIKE_IN_GROUP){
                String name = data.getStringExtra(MODIFY_RESULT);
                nikeNameView.setText(name);
                for (int i = 0; i < datas.size(); i++) {
                    PPUserInfo ppUserInfo = datas.get(i);
                    if(ppUserInfo.getPhone().equals(myPhone)){
                        ppUserInfo.setName(name);
                        adapter.notifyItemChanged(i);
                        return;
                    }
                }
                return;
            }
        }
        if(resultCode== REMOVE_RESULT){
            ArrayList<PPUserInfo> hasMembers =  (ArrayList<PPUserInfo>) data.getSerializableExtra(HAS_MEMBERS);
            datas.removeAll(hasMembers);
            adapter.notifyDataSetChanged();
            String text = String.format(getResources().getString(R.string.chat_info), datas.size());
            titleTextView.setText(text);
//            freshInfo(hasMembers);
        }
    }

    private void showQuitDialog(){
        View view = getLayoutInflater().inflate(R.layout.practice_dialog,null);
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(view);
//        TextView title = (TextView) view.findViewById(R.id.title);
        TextView scoreTv = (TextView) view.findViewById(R.id.content);
        scoreTv.setText(R.string.quit_tip);
        view.findViewById(R.id.left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.right_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitGroup();
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
    private void showDeleteDialog(){
        View view = getLayoutInflater().inflate(R.layout.practice_dialog,null);
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        android.view.WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        p.width = (int) (PPScreenUtils.getScreenWidth(this) * 0.85);
        p.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setAttributes(p);
//        TextView title = (TextView) view.findViewById(R.id.title);
        TextView scoreTv = (TextView) view.findViewById(R.id.content);
        scoreTv.setText(R.string.delete_group_tip);
        view.findViewById(R.id.left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.right_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteGroup();
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    public void exitGroup() {
        String token = SPUtils.get(LoginActivity.LOGIN_TOKEN,"");
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.GROUP_URL);
        params.addBodyParameter("action", "quit");
        params.addBodyParameter("token", token);
        params.addBodyParameter("group", groupId);
        Log.d(TAG, "exitGroup: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: =" + result);
                BaseCode base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode>() {
                        }.getType());
                if (base.getCode() == 0) {
//                    SPUtils.put(phone+groupId,false);
                    RongIM.getInstance().removeConversation(Conversation.ConversationType.GROUP,groupId,null);
                    startActivity(new Intent(ChatInfoActivity.this,MainActivity.class));
                } else {
                    Toast.makeText(ChatInfoActivity.this,base.getMessage(),Toast.LENGTH_LONG).show();
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
            }
        });
    }
    public void deleteGroup() {
        String token = SPUtils.get(LoginActivity.LOGIN_TOKEN,"");
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.GROUP_URL);
        params.addBodyParameter("action", "dismiss");
        params.addBodyParameter("token", token);
        params.addBodyParameter("group", groupId);
        Log.d(TAG, "exitGroup: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: =" + result);
                BaseCode base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode>() {
                        }.getType());
                if (base.getCode() == 0) {
                    RongIM.getInstance().removeConversation(Conversation.ConversationType.GROUP,groupId,null);
                    new IMUtils().sendTextMsg(groupId,"该群已解散");
                    startActivity(new Intent(ChatInfoActivity.this,MainActivity.class));
                } else {
                    Toast.makeText(ChatInfoActivity.this,base.getMessage(),Toast.LENGTH_LONG).show();
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
            }
        });
    }
    private boolean isOwner;
    public void requestGroupInfo() {
        ///pp/group?action=info&token=%s&group=%s
        String token = SPUtils.get(LoginActivity.LOGIN_TOKEN,"");
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.GROUP_URL);
        params.addParameter("action", "list");
        params.addParameter("token", token);
        params.addParameter("page", 0);
        params.addParameter("size", 20000);
        params.addParameter("group", groupId);
        showProgress();
        Log.d(TAG, "requestGroupInfo: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: =" + result);
                BaseCode<List<PPUserInfo>> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<List<PPUserInfo>>>() {
                        }.getType());
                if (base.getCode() == 0) {
                    List<PPUserInfo> list = base.getData();
                    freshInfo(list);
                } else {
                    Toast.makeText(ChatInfoActivity.this,base.getMessage(),Toast.LENGTH_LONG).show();
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
    private List<PPUserInfo> ppUserInfos;
    private void freshInfo(List<PPUserInfo> list){
        ppUserInfos = list;
        datas.clear();
        if(list.size()>20){
            datas.addAll(list.subList(0,20));
            moreMember.setVisibility(View.VISIBLE);
        }else{
            datas.addAll(list);
            moreMember.setVisibility(View.GONE);
        }
        if(!groupId.contains("market")){
            datas.add(addUser);
        }else{
            quit.setVisibility(View.GONE);
            delete.setVisibility(View.GONE);
        }
        if(isOwner){
            if(!groupId.contains("market")){
                datas.add(deleteUser);
            }
        }
        adapter.notifyDataSetChanged();
        String text = String.format(getResources().getString(R.string.chat_info), list.size());
        titleTextView.setText(text);
    }

    public void onEventMainThread(ModifyGroupNike var) {
        groupTag = var.getNikeName();
        tagNameTextview.setVisibility(View.VISIBLE);
        tagNameTextview.setText(var.getNikeName());
    }
    public void onEventMainThread(ResetGroupName var) {
        groupNameTextView.setText(var.getName());
    }
    class DataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<PPUserInfo> data;

        private static final int TYPE_ITEM = 1;

        public DataAdapter(List<PPUserInfo> list) {
            this.data = list;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = getLayoutInflater().inflate(R.layout.group_user_item, parent, false);
            return new ItemViewHolder(itemView);
        }


        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ItemViewHolder) {
                final PPUserInfo userInfo = data.get(position);
                ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
                if(TextUtils.isEmpty(userInfo.getName())){
                    itemViewHolder.imageView.setImageResource(R.mipmap.unselect);
                }
                itemViewHolder.name.setText("");
                if("add".equals(userInfo.getName())){
                    itemViewHolder.imageView.setImageResource(R.drawable.add_member_selector);
                }else if("remove".equals(userInfo.getName())){
                    itemViewHolder.imageView.setImageResource(R.drawable.remove_selector);
                }else{
//                    Glide.with(ChatInfoActivity.this).asBitmap().load(userInfo.getIcon()).into(itemViewHolder.imageView);
                    itemViewHolder.imageView.setAvatar(userInfo.getIcon(),R.mipmap.default_icon);
                    itemViewHolder.name.setText(userInfo.getName());
                    if(userInfo.getPhone().equals(myPhone)){
                        nikeName = userInfo.getName();
                        nikeNameView.setText(nikeName);
                    }
                }
            }
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }

        @Override
        public int getItemViewType(int position) {
            return TYPE_ITEM;
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {

            private AsyncImageView imageView;
            private TextView name;

            public ItemViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.icon);
                name = itemView.findViewById(R.id.name);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        Intent intent = new Intent(ChatInfoActivity.this,FriendDetailActivity.class);
//                        intent.putExtra(FriendDetailActivity.USER_ID,datas.get(getLayoutPosition()).getPhone());
//                        startActivity(intent);
                        int position = getLayoutPosition();
                        Intent intent = new Intent();
                        intent.setClass(ChatInfoActivity.this, ContactActivity.class);
                        List<PPUserInfo> list = ppUserInfos;
                        PPUserInfo self = null;
                        for (int i=0;i<list.size();i++){
                            if(list.get(i).getName().equals(SPUtils.get(LoginActivity.USER_NAME,""))){
                                self = list.get(i);
                                break;
                            }
                        }
                        if(null!=self){
                            list.remove(self);
                        }
                        intent.putExtra(HAS_MEMBERS, (Serializable) list);
                        intent.putExtra(GROUP_ID, groupId);
                        if(datas.get(position).getName().equals("add")){
                            intent.putExtra(SELECT_ACTION, INVATE_GROUP_TYPE);
                            startActivityForResult(intent,0);
                            return;
                        }else if(datas.get(position).getName().equals("remove")){
                            intent.putExtra(SELECT_ACTION, REMOVE_GROUP_TYPE);
                            startActivityForResult(intent,0);
                            return;
                        }else{
                            PPUserInfo userInfo = datas.get(position);
                            intent = new Intent(ChatInfoActivity.this,FriendDetailActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(FriendDetailActivity.USER_INFO, userInfo);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                    }
                });
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
