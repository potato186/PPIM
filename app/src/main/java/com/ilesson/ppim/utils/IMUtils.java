package com.ilesson.ppim.utils;

import android.content.Context;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.IlessonApp;
import com.ilesson.ppim.R;
import com.ilesson.ppim.custom.RedPacketMessage;
import com.ilesson.ppim.custom.TransactionMessage;
import com.ilesson.ppim.custom.TransferMessage;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.GroupInfo;
import com.ilesson.ppim.entity.PPUserInfo;
import com.ilesson.ppim.entity.RongUserInfo;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import io.rong.imkit.RongIM;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;
import io.rong.message.TextMessage;

import static com.ilesson.ppim.activity.LoginActivity.LOGIN_PAY;
import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;
import static com.ilesson.ppim.activity.LoginActivity.TOKEN;
import static com.ilesson.ppim.activity.LoginActivity.USER_ICON;
import static com.ilesson.ppim.activity.LoginActivity.USER_MONEY;
import static com.ilesson.ppim.activity.LoginActivity.USER_NAME;
import static com.ilesson.ppim.activity.LoginActivity.USER_PHONE;
import static com.ilesson.ppim.activity.MainActivity.GROUP_TYPE;
import static com.ilesson.ppim.activity.MainActivity.PERSON_TYPE;
import static com.ilesson.ppim.fragment.FundFragment.FUND_HAD_LOAD;
import static com.ilesson.ppim.fragment.FundFragment.FUND_NOT_ACTIVED;

/**
 * Created by potato on 2020/3/10.
 */

public class IMUtils {
    private static final String TAG = "IMUtils";

    public void connect(Context context, final String token) {
        login(context, token);
//        refreshUserInfo(token);
        RongIM.connect(token, new RongIMClient.ConnectCallback() {

            @Override
            public void onSuccess(String s) {
                Log.d(TAG, "onSuccess: "+s);
            }

            @Override
            public void onError(RongIMClient.ConnectionErrorCode connectionErrorCode) {
                Log.d(TAG, "onError: "+connectionErrorCode);
            }

            @Override
            public void onDatabaseOpened(RongIMClient.DatabaseOpenStatus databaseOpenStatus) {

            }

        });
        RongIM.setUserInfoProvider(new RongIM.UserInfoProvider() {

            @Override

            public UserInfo getUserInfo(String userId) {
                searchUserInfo(token, userId);
                return null;

            }

        }, true);

        RongIM.setGroupInfoProvider(new RongIM.GroupInfoProvider() {
            @Override
            public Group getGroupInfo(String s) {
                searchGroupInfo(token, s);
                return null;
            }
        },true);
    }

    public void searchGroupInfo(String token, final String userId) {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.RONG_URL);
        params.addBodyParameter("action", "group");
        params.addBodyParameter("token", token);
        params.addBodyParameter("target", userId);
        Log.d(TAG, "searchUserInfo: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: =" + result);
                BaseCode<GroupInfo> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<GroupInfo>>() {
                        }.getType());
                if (base.getCode() == 0) {
                    GroupInfo info = base.getData();
//                    String test = "https://pp.fangnaokeji.com:9443/pp/images/demo/shop_01.png";
//                    Group group = new Group(userId, info.getName(), Uri.parse(test));
                    Group group = new Group(userId, info.getName(), Uri.parse(info.getIcon()));
                    RongIM.getInstance().refreshGroupInfoCache(group);
                    if(null!=onQueryInfoListener){
                        onQueryInfoListener.onResult(info.getSize(),info.getName());
                    }
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
            }
        });
    }
    public void searchUserInfo(String token, final String userId) {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.RONG_URL);
        params.addBodyParameter("action", "info");
        params.addBodyParameter("token", token);
        params.addBodyParameter("target", userId);
        Log.d(TAG, "searchUserInfo: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: =" + result);
                BaseCode<RongUserInfo> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<RongUserInfo>>() {
                        }.getType());
                if (base.getCode() == 0) {
                    RongUserInfo info = base.getData();
                    UserInfo userInfo = new UserInfo(userId, info.getName(), Uri.parse(info.getIcon()));
                    RongIM.getInstance().refreshUserInfoCache(userInfo);
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
            }
        });
    }

    public static void login(final Context context, String token) {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.USER_URL);
        params.addBodyParameter("action", "login");
        params.addBodyParameter("token", token);
        Log.d(TAG, "loadData: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                BaseCode<PPUserInfo> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<PPUserInfo>>() {
                        }.getType());
                if (base.getCode() == 0) {
//                    SPUtils.put(LOGIN_PAY, base.getData().isPay());
//                    SPUtils.put(USER_MONEY, base.getData().getMoney());
//                    SPUtils.put(USER_ICON, base.getData().getIcon());
//                    SPUtils.put(USER_PHONE, base.getData().getPhone());
//                    SPUtils.put(USER_NAME, base.getData().getName());
//                    context.sendBroadcast(new Intent(ACTION_PAY));
                } else {
                }

                Log.d(TAG, "onSuccess: +" + result);
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

    public void sendTransfer(String tartgetId, String money, String desc) {
        TransferMessage message = new TransferMessage();
        message.setExtra(desc);
        message.setContent(money+"//"+tartgetId);
        Message msg = Message.obtain(tartgetId, Conversation.ConversationType.PRIVATE, message);
        RongIM.getInstance().sendMessage(msg,
                "转账", null, new IRongCallback.ISendMessageCallback() {
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
    public void sendRedPacket(String tartgetId, String id, String desc) {
        RedPacketMessage message = new RedPacketMessage();
        message.setRedpacketId(id);
        message.setDes(desc);
        message.setSenderIcon(SPUtils.get(USER_ICON,""));
        message.setSenderName(SPUtils.get(USER_NAME,""));
        Message msg = Message.obtain(tartgetId, Conversation.ConversationType.GROUP, message);
        RongIM.getInstance().sendMessage(msg,
                "转账", null, new IRongCallback.ISendMessageCallback() {
                    @Override
                    public void onAttached(Message message) {
                        Log.d(TAG, "onAttached: "+message);
                    }

                    @Override
                    public void onSuccess(Message message) {
                        Log.d(TAG, "onSuccess: "+message);
                    }

                    @Override
                    public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                        Log.d(TAG, "onError: "+errorCode);
                    }
                });
    }
    public void sendTransaction(String tartgetId, TransactionMessage transactionMessage) {
        Message msg = Message.obtain(tartgetId, Conversation.ConversationType.GROUP, transactionMessage);

        RongIM.getInstance().sendMessage(msg,
                "transaction", null, new IRongCallback.ISendMessageCallback() {
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
    public void addFriend(String token,final String id) {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.USER_URL);
        params.addBodyParameter("action", "friend_add");
        params.addBodyParameter("token", token);
        params.addBodyParameter("method", "1");
        params.addBodyParameter("content", "hello");
        params.addBodyParameter("target", id);
        Log.d(TAG, "loadData: "+params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: "+result);
                BaseCode base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode>() {
                        }.getType());
                if (base.getCode() == 0) {
                    if(null!=onAddListener){
                        onAddListener.onSuccess(PERSON_TYPE,id,"");
                    }
                } else {
                    if(null!=onAddListener){
                        onAddListener.onFail(base.getMessage());
                    }
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
                if(null!=onAddListener){
                    onAddListener.onFinished();
                }
            }
        });
    }
    public void requestGroupChat(String token, final String groupId, String member,final String names) {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.GROUP_URL);
        params.addBodyParameter("action", "request");
        params.addBodyParameter("token", token);
        params.addBodyParameter("group", groupId);
        params.addBodyParameter("member", member);
        Log.d(TAG, "loadData: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: " + result);
                BaseCode base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode>() {
                        }.getType());
                if (base.getCode() == 0) {
                    if(null!=onAddListener){
                        onAddListener.onSuccess(GROUP_TYPE,groupId,names);
                    }
                } else if(base.getCode() == 1){
                    if(null!=onAddListener){
                        onAddListener.onSuccess(GROUP_TYPE,groupId,names);
                    }
                }
                else {
                    if(null!=onAddListener){
                        onAddListener.onFail(base.getMessage());
                    }
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
                if(null!=onAddListener){
                    onAddListener.onFinished();
                }
            }
        });
    }
    public void requestShopServer(String groupId,String oid) {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.SHOP);
        params.addBodyParameter("action", "shop_server");
        if(TextUtils.isEmpty(groupId)){
            params.addBodyParameter("oid", oid);
        }else{
            params.addBodyParameter("group", groupId);
        }
        Log.d(TAG, "requestShopServer: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "requestShopServer: " + result);
                BaseCode base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode>() {
                        }.getType());
                if (base.getCode() == 0) {

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
    public void confirmOrder(final View view, final String oid) {
        //确认订单：https://pp.fangnaokeji.com:9443/pp/order?action=confirm&oid=689
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.ORDER);
        params.addBodyParameter("action", "confirm");
        params.addBodyParameter("oid", oid);
        final String phone = SPUtils.get(USER_PHONE,"");
        Log.d(TAG, "confirmOrder: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "requestShopServer: " + result);
                BaseCode base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode>() {
                        }.getType());
                if (base.getCode() == 0) {
                    SPUtils.put(phone+oid,"true");
                    view.setBackgroundResource(R.drawable.background_gray_corner20);
                    view.setEnabled(false);
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
    public void sendTextMsg(String groupId,String text){
        TextMessage textMessage = TextMessage.obtain(text);
        RongIMClient.getInstance().sendMessage(Conversation.ConversationType.GROUP, groupId, textMessage, null, null, new IRongCallback.ISendMessageCallback() {
            @Override
            public void onAttached(Message message) {
                // 消息成功存到本地数据库的回调
                Log.d(TAG, "onAttached: "+message);
            }

            @Override
            public void onSuccess(Message message) {
                Log.d(TAG, "onSuccess: "+message);
                // 消息发送成功的回调
            }

            @Override
            public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                // 消息发送失败的回调
                Log.d(TAG, "onError: "+message);
            }
        });
    }
    private OnAddListener onAddListener;
    public void setOnAddListener(OnAddListener onAddListener){
        this.onAddListener = onAddListener;
    }
    public interface OnAddListener{
        void onFinished();
        void onSuccess(int type,String id,String names);
        void onFail(String msg);
    }
    private OnQueryInfoListener onQueryInfoListener;
    public void setOnAddListener(OnQueryInfoListener onQueryInfoListener){
        this.onQueryInfoListener = onQueryInfoListener;
    }
    public interface OnQueryInfoListener{
        void onResult(int num,String name);
    }
    public static String getDate(String date){
        SimpleDateFormat format0 = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.CHINA);
        String time = format0.format(Long.valueOf(date));
        return time;
    }

    public void upUser(Context context) {
        String device = Settings.System.getString(context.getContentResolver(), Settings.System.ANDROID_ID);
        String tag = ",7-1|";
        List<PPUserInfo> list = IlessonApp.getInstance().getDatas();
        StringBuilder stringBuilder = new StringBuilder();
        for(PPUserInfo info:list){
            stringBuilder.append(info.getName()).append(tag);
        }
        RequestParams params = new RequestParams(Constants.AI_URL);
        params.addParameter("app", "JeJblZH5");
        params.addParameter("ak", "c48a0cd52ddfce1582c722b4a0c6cf023517e54c");
        params.addParameter("token", "f9ee3925fadf67cf363bf415448f4c8e33e6f00b");
        params.addParameter("mode", "phnnam");
        params.addParameter("submode", "20");
        params.addParameter("devid", device);
        params.addParameter("userid", SPUtils.get(USER_PHONE,""));
        params.addParameter("text", stringBuilder.toString());
        Log.d(TAG, "loadData: "+params.toString());
        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "upnames: "+result);
                if(result.contains("\"code\":\"0\"")){
//                    SPUtils.put(UPUSER,true);
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
    public void resetUserInfo(){
        SPUtils.put(LOGIN_PAY, "");
        SPUtils.put(USER_MONEY, "");
        SPUtils.put(USER_ICON, "");
        SPUtils.put(USER_PHONE, "");
        SPUtils.put(USER_NAME, "");
        SPUtils.put(LOGIN_TOKEN, "");
        SPUtils.put(FUND_NOT_ACTIVED, false);
        SPUtils.put(FUND_HAD_LOAD, false);
        SPUtils.put(TOKEN, "");
        SPUtils.put("bToken", "");
    }
}
