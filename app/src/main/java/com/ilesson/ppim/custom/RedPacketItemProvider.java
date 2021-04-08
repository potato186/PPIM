package com.ilesson.ppim.custom;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.activity.RedpacketDetailActivity;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.CheckRedpacketData;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.view.AwardRotateAnimation;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imlib.model.Message;

import static com.ilesson.ppim.activity.ChatInfoActivity.GROUP_ID;
import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;
import static com.ilesson.ppim.activity.RedpacketDetailActivity.CAN_SPLIT;
import static com.ilesson.ppim.activity.RedpacketDetailActivity.RED_MESSAGE;

/**
 * Created by longShun on 2017/2/24.
 * desc新建一个消息类继承 IContainerItemProvider.MessageProvider 类，实现对应接口方法，
 * 1.注意开头的注解！
 * 2.注意泛型！
 */
@ProviderTag(
        messageContent = RedPacketMessage.class,//（这里是你自定义的消息实体）
        showReadState = true
)
public class RedPacketItemProvider extends IContainerItemProvider.MessageProvider<RedPacketMessage> {

    public RedPacketItemProvider() {
    }
    private Context context;
    @Override
    public View newView(Context context, ViewGroup viewGroup) {
        this.context = context;

        //这就是展示在会话界面的自定义的消息的布局
        View view = LayoutInflater.from(context).inflate(R.layout.item_redpackage_message, null);
        ViewHolder holder = new ViewHolder();
        holder.desc = (TextView) view.findViewById(R.id.des);
        holder.state = (TextView) view.findViewById(R.id.state);
        holder.space = (TextView) view.findViewById(R.id.space);
        holder.layout = view.findViewById(R.id.layout);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, int i, RedPacketMessage redPackageMessage, UIMessage message) {

        //根据需求，适配数据
        ViewHolder holder = (ViewHolder) view.getTag();

        if (message.getMessageDirection() == Message.MessageDirection.SEND) {//消息方向，自己发送的
            holder.layout.setBackgroundResource(R.mipmap.right_redpacket_not_open);
            holder.space.setVisibility(View.GONE);
        } else {
            holder.layout.setBackgroundResource(R.mipmap.left_redpacket_not_open);
            holder.space.setVisibility(View.VISIBLE);
        }
        String state = SPUtils.get(redPackageMessage.getRedpacketId(),"");
        holder.desc.setText(redPackageMessage.getDes());
        if(TextUtils.isEmpty(state)){
            holder.state.setVisibility(View.GONE);
        }else{
            showNoRed(redPackageMessage,state,view,message);
        }
    }
    private static final String TAG = "RedPackageItemProvider";
    private void showNoRed(RedPacketMessage redPackageMessage, String state,View view,UIMessage message){
        ViewHolder holder = (ViewHolder) view.getTag();
        if (message.getMessageDirection() == Message.MessageDirection.SEND) {//消息方向，自己发送的
            holder.layout.setBackgroundResource(R.mipmap.right_redpacket_opened);
        } else {
            holder.layout.setBackgroundResource(R.mipmap.left_redpacket_open);
        }
        holder.state.setVisibility(View.VISIBLE);
        holder.state.setText(state);
    }
    @Override
    public Spannable getContentSummary(RedPacketMessage redPackageMessage) {
        return new SpannableString("[PP红包]");
    }

//    @Override
//    public Spannable getSummary(UIMessage message) {
//        Spannable spannable = null;
//        String targetId = message.getTargetId();
//        String myId = SPUtils.get(LoginActivity.USER_PHONE,"");
//        Log.d(TAG, "getSummary: targetId="+targetId);
//        if (!targetId.equals(myId)) {
//            spannable = new SpannableString("[转消费积分]朋友已收");
//        }else{
//            spannable = new SpannableString("[转消费积分]你已收");
//        }
//        return spannable;
//    }
//<string name="transfer_to">转账给</string>
//    <string name="transfer_to_you">转账给你</string>
//    <string name="transfer_summary">[转账]请你确认收款</string>
    private boolean loading;
    @Override
    public void onItemClick(View view, int i, RedPacketMessage redPackageMessage, UIMessage uiMessage) {
        if(loading){
            return;
        }
        showProgress();
        check(uiMessage.getTargetId(),redPackageMessage,view,uiMessage);
    }

    private static class ViewHolder {
        private TextView desc,state,space;
        private View layout;
    }
    @Override
    public void onItemLongClick(View view, int position, final RedPacketMessage content, final UIMessage message) {
//        String[] items1;
//        items1 = new String[]{ view.getContext().getResources().getString(io.rong.imkit.R.string.rc_dialog_item_message_delete)};
//        OptionsPopupDialog.newInstance(view.getContext(), items1).setOptionsPopupDialogListener(new OptionsPopupDialog.OnOptionsItemClickedListener() {
//            public void onOptionsItemClicked(int which) {
//                if (which == 0) {
//                    RongIM.getInstance().deleteMessages(new int[]{message.getMessageId()}, (RongIMClient.ResultCallback) null);
//                }
//            }
//        }).show();
    }

    private void check(final String groupId,final RedPacketMessage message,final View view,final UIMessage uiMessage) {
        loading = true;
        final String token = SPUtils.get(LOGIN_TOKEN, "");
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.MONEY_URL);
        params.addBodyParameter("action", "has");
        params.addBodyParameter("token", token);
        params.addBodyParameter("id", message.getRedpacketId());
        params.addBodyParameter("group", groupId);
        Log.d(TAG, "loadData: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: " + result);
                BaseCode<CheckRedpacketData> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<CheckRedpacketData>>() {
                        }.getType());
                if (base.getCode() == 0) {
                    CheckRedpacketData data = base.getData();
                    showRedDialog(groupId,data,message,view,uiMessage);
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
                loading = false;
                hideProgress();
            }
        });
    }
    private Dialog redDialog;
    private String grouId;
    private void showRedDialog(final String grouId,final CheckRedpacketData data,final RedPacketMessage message,final View redItemView,final UIMessage uiMessage) {
        this.context = redItemView.getContext();
        this.grouId = grouId;
        redDialog = new Dialog(context,R.style.MyDialog);
        redDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(context).inflate(R.layout.open_redpacket_dialog,null);
        redDialog.setContentView(view);
        TextView nameTV = view.findViewById(R.id.sender_name);
        TextView detail = view.findViewById(R.id.look_detail);
        View openLayout = view.findViewById(R.id.open_layout);
        View redpacketLayout = view.findViewById(R.id.redpacket_layout);
        Animation animationLayout = AnimationUtils.loadAnimation(context, R.anim.award_anima);
        redpacketLayout.setAnimation(animationLayout);
        final ImageView open = view.findViewById(R.id.open);
        ImageView imageView = view.findViewById(R.id.sender_icon);
        Glide.with(context).load(message.getSenderIcon()).into(imageView);
        nameTV.setText(message.getSenderName()+"的红包");
        TextView desTV = view.findViewById(R.id.des);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;// 屏幕宽度（像素）
        int height= dm.heightPixels; // 屏幕高度（像素）
        WindowManager.LayoutParams layoutParams = redDialog.getWindow().getAttributes();
        layoutParams.width = width;
        layoutParams.height = height;
        redDialog.getWindow().setAttributes(layoutParams);
        if(data.getSplitted()){
            checkRed(false,message);
        }else{
            redDialog.show();
            if(data.getHas()){
                openLayout.setVisibility(View.VISIBLE);
                desTV.setText(message.getDes());
            }else{
                String des = context.getResources().getString(R.string.no_redpacket1);
                SPUtils.put(message.getRedpacketId(),des);
                desTV.setText(R.string.no_redpacket);
                detail.setVisibility(View.VISIBLE);
                openLayout.setVisibility(View.GONE);
                showNoRed(message,des,redItemView,uiMessage);
            }
        }
        detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkRed(false,message);
                redDialog.dismiss();
            }
        });
        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AwardRotateAnimation animation = new AwardRotateAnimation();
                animation.setRepeatCount(Animation.RESTART);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        animation.cancel();
                        checkRed(true,message);
                        String des = context.getResources().getString(R.string.splited);
                        SPUtils.put(message.getRedpacketId(),des);
                        showNoRed(message,des,redItemView,uiMessage);
                        redDialog.dismiss();
                    }
                });
                open.startAnimation(animation);
            }
        });
        view.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redDialog.dismiss();
            }
        });
    }
    private void checkRed(boolean has,RedPacketMessage message){
        Intent intent = new Intent(context, RedpacketDetailActivity.class);
        intent.putExtra(RED_MESSAGE,message);
        intent.putExtra(GROUP_ID,grouId);
        intent.putExtra(CAN_SPLIT,has);
        context.startActivity(intent);
    }
    public void showProgress(){
        try{
            if(null==dialog){
                initProgressDialog();
            }
            if(null!=dialog){
                dialog.show();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void hideProgress(){
        try{
            if(null!=dialog){
                dialog.dismiss();
                dialog.cancel();
                dialog.hide();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private Dialog dialog;
    private void initProgressDialog() {
        if(null==context){
            return;
        }
        dialog = new Dialog(context, R.style.dialog_no_background);
        View view = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null);
        dialog.setContentView(view);
        dialog.setCancelable(true);
    }
}
