package com.ilesson.ppim.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.PPUserInfo;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.IMUtils;
import com.ilesson.ppim.utils.PPScreenUtils;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.view.RoundImageView;
import com.ilesson.ppim.view.ScrollGridView;
import com.ilesson.ppim.view.SwitchButton;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.rong.imageloader.core.DisplayImageOptions;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.Conversation;

import static com.ilesson.ppim.activity.AvatarActivity.MODIFY_SUCCESS;
import static com.ilesson.ppim.activity.ContactActivity.HAS_MEMBERS;
import static com.ilesson.ppim.activity.ContactActivity.INVATE_GROUP_TYPE;
import static com.ilesson.ppim.activity.ContactActivity.REMOVE_GROUP_TYPE;
import static com.ilesson.ppim.activity.ContactActivity.REMOVE_RESULT;
import static com.ilesson.ppim.activity.ContactActivity.SELECT_ACTION;
import static com.ilesson.ppim.activity.ModifyNameActivity.MODIFY_GROUP;
import static com.ilesson.ppim.activity.ModifyNameActivity.MODIFY_RESULT;
import static com.ilesson.ppim.activity.ModifyNameActivity.MODIFY_TYPE;
import static com.ilesson.ppim.view.SwitchButton.PLAY_TTS;


/**
 * Created by potato on 2019/4/9.
 */
@ContentView(R.layout.act_chat_info)
public class ChatInfoActivity extends BaseActivity{
    @ViewInject(R.id.title)
    private TextView titleTextView;
    @ViewInject(R.id.group_name)
    private TextView groupName;
    @ViewInject(R.id.delete)
    private TextView delete;
    @ViewInject(R.id.shop_list_layout)
    private View shopList;
    @ViewInject(R.id.quit)
    private View quit;
    @ViewInject(R.id.voice_switch)
    private SwitchButton switchButton;
    @ViewInject(R.id.gridview)
    private ScrollGridView gridView;
    private UserAdapter adapter;
    private List<PPUserInfo> datas;
    private static final String TAG = "ChatInfoActivity";
    public static final String GROUP_ID = "group_id";
    public static final String GROUP_NAME = "group_name";
    public static final String ISOWNER = "isOwner";
    private String name;
    private String groupId;
    DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
    private boolean modifyed;
    private PPUserInfo addUser,deleteUser;
    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setStatusBarLightMode(this,true);
        groupId = getIntent().getStringExtra(GROUP_ID);
        name = getIntent().getStringExtra(GROUP_NAME);
        isOwner = getIntent().getBooleanExtra(ISOWNER,false);
        if(isOwner){
            delete.setVisibility(View.VISIBLE);
            shopList.setVisibility(View.VISIBLE);
        }
        groupName.setText(name);
        datas = new ArrayList<>();
        adapter = new UserAdapter(datas);
        gridView.setAdapter(adapter);
        gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
//        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                PPUserInfo userInfo = datas.get(position);
//                Intent intent = new Intent(ChatInfoActivity.this,FriendDetailActivity.class);
//                Bundle bundle = new Bundle();
//                bundle.putSerializable(FriendDetailActivity.USER_INFO, userInfo);
//                intent.putExtras(bundle);
//                startActivity(intent);
//            }
//        });
        builder.cacheInMemory(true).cacheOnDisk(true);
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
//        searchGroupMember();
        requestGroupInfo();
    }

    @Event(R.id.back_btn)
    private void back_btn(View view){
        out();
    }
    @Event(R.id.shop_list_layout)
    private void shop_list_layout(View view){
        startActivity(new Intent(this,ShopKeeperOrderListActivity.class));
    }
    @Event(R.id.code_layout)
    private void code_layout(View view){
        Intent intent = new Intent(ChatInfoActivity.this,UserCodeActivity.class);
        intent.putExtra(GROUP_ID, groupId);
        startActivity(intent);
    }
    @Event(R.id.group_name_layout)
    private void group_name(View view){
        Intent intent = new Intent(ChatInfoActivity.this,ModifyNameActivity.class);
        intent.putExtra(GROUP_ID, groupId);
        intent.putExtra(MODIFY_TYPE, MODIFY_GROUP);
        intent.putExtra(ChatInfoActivity.GROUP_NAME, name);
        startActivityForResult(intent,0);
    }
    @Event(R.id.quit)
    private void quit(View view){
        showQuitDialog();
    }
    @Event(R.id.delete)
    private void delete(View view){
        showDeleteDialog();
    }
    @Event(value=R.id.gridview,type=AdapterView.OnItemClickListener.class)
    private void item(AdapterView<?> parent, View view, int position, long id){
        Intent intent = new Intent();
        intent.setClass(ChatInfoActivity.this, ContactActivity.class);
        List<PPUserInfo> list = new ArrayList<>();
        list.addAll(datas);
        list.remove(addUser);
        list.remove(deleteUser);
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
        }else if(datas.get(position).getName().equals("remove")){
            intent.putExtra(SELECT_ACTION, REMOVE_GROUP_TYPE);
            startActivityForResult(intent,0);
            PPUserInfo userInfo = datas.get(position);
        }else{
            PPUserInfo userInfo = datas.get(position);
            intent = new Intent(ChatInfoActivity.this,FriendDetailActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(FriendDetailActivity.USER_INFO, userInfo);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        Log.d(TAG, "item: "+datas.get(position).getName());
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
        if(modifyed){
            Intent intent = new Intent();
            intent.putExtra(MODIFY_RESULT,name);
            setResult(MODIFY_SUCCESS,intent);
        }
        finish();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== MODIFY_SUCCESS){
            name = data.getStringExtra(MODIFY_RESULT);
            groupName.setText(name);
            modifyed = true;
            return;
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
        params.addParameter("size", 3000);
        params.addParameter("group", groupId);
        showProgress();
        Log.d(TAG, "exitGroup: " + params.toString());
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
    private void freshInfo(List<PPUserInfo> list){
        datas.clear();
        datas.addAll(list);
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
    private class UserAdapter extends BaseAdapter {

        private List<PPUserInfo> data;

        private UserAdapter(List<PPUserInfo> list) {
            this.data = list;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int i) {
            return data.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @SuppressLint("WrongViewCast")
        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            final PPUserInfo userInfo = data.get(i);
            ViewHolder holder = null;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.group_user_item, null);
                holder = new ViewHolder();
                holder.imageView = view.findViewById(R.id.icon);
                holder.name = view.findViewById(R.id.name);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            if(TextUtils.isEmpty(userInfo.getName())){
                holder.imageView.setImageResource(R.mipmap.unselect);
            }
            holder.name.setText("");
            if("add".equals(userInfo.getName())){
                holder.imageView.setImageResource(R.drawable.add_member_selector);
            }else if("remove".equals(userInfo.getName())){
                holder.imageView.setImageResource(R.drawable.remove_selector);
            }else{
                Glide.with(ChatInfoActivity.this).asBitmap().load(userInfo.getIcon()).into(holder.imageView);
                holder.name.setText(userInfo.getName());
            }
            return view;
        }
    }
    class ViewHolder{
        private RoundImageView imageView;
        private TextView name;
    }
}
