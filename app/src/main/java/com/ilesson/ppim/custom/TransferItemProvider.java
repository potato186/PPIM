package com.ilesson.ppim.custom;


import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.ilesson.ppim.R;
import com.ilesson.ppim.activity.ConversationActivity;
import com.ilesson.ppim.activity.LoginActivity;
import com.ilesson.ppim.utils.SPUtils;

import io.rong.imkit.RongIM;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.utilities.OptionsPopupDialog;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;

/**
 * Created by longShun on 2017/2/24.
 * desc新建一个消息类继承 IContainerItemProvider.MessageProvider 类，实现对应接口方法，
 * 1.注意开头的注解！
 * 2.注意泛型！
 */
@ProviderTag(
        messageContent = TransferMessage.class,//（这里是你自定义的消息实体）
        showReadState = true
)
public class TransferItemProvider extends IContainerItemProvider.MessageProvider<TransferMessage> {

    public TransferItemProvider() {
    }
    private Context context;
    @Override
    public View newView(Context context, ViewGroup viewGroup) {
        this.context = context;

        //这就是展示在会话界面的自定义的消息的布局
        View view = LayoutInflater.from(context).inflate(R.layout.item_transfer_message, null);
        ViewHolder holder = new ViewHolder();
        holder.money = (TextView) view.findViewById(R.id.money);
        holder.desc = (TextView) view.findViewById(R.id.description);
        holder.layout = view.findViewById(R.id.layout);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, int i, TransferMessage redPackageMessage, UIMessage message) {

        //根据需求，适配数据
        ViewHolder holder = (ViewHolder) view.getTag();
        StringBuilder stringBuilder = new StringBuilder("转");
        String currency = redPackageMessage.getExtra();
        if(TextUtils.isEmpty(currency)){
            currency = view.getContext().getString(R.string.score);
        }else{
            currency = redPackageMessage.getExtra();
        }
        stringBuilder.append(currency).append("给");
        if (message.getMessageDirection() == Message.MessageDirection.SEND) {//消息方向，自己发送的
            holder.layout.setBackgroundResource(R.mipmap.red_pack_right);
            stringBuilder = new StringBuilder("向好友转出");
            stringBuilder.append(currency);
        } else {
            holder.layout.setBackgroundResource(R.mipmap.red_pack_left);
            stringBuilder.append("你");
        }
//        if(!TextUtils.isEmpty(redPackageMessage.getExtra())){
//            stringBuilder.append("-").append(redPackageMessage.getExtra());
//        }
        //AndroidEmoji.ensure((Spannable) holder.message.getText());//显示消息中的 Emoji 表情。
        //holder.tvTitle.setText(redPackageMessage.getTitle());
        holder.desc.setText(stringBuilder.toString());
        String content = redPackageMessage.getContent();
        if(content.contains(TRANSFER_SPLIT)){
            String[] arr = content.split("//");
            holder.money.setText(arr[0]);
        }else{
            holder.money.setText(content.replace("|null",""));
        }
        //holder.tvDesc1.setText(redPackageMessage.getDesc1());
        //holder.tvDesc2.setText(redPackageMessage.getDesc2());
    }
    private static final String TAG = "RedPackageItemProvider";
    public static final String TRANSFER_SPLIT = "//";

    @Override
    public Spannable getContentSummary(TransferMessage redPackageMessage) {
        Spannable spannable = null;
        String content = redPackageMessage.getContent();
        if(!content.contains(TRANSFER_SPLIT)){
            return null;
        }
        String[] arr = content.split(TRANSFER_SPLIT);
        String targetId = arr[1];
        String myId = SPUtils.get(LoginActivity.USER_PHONE,"");
        if (!targetId.equals(myId)) {
            spannable = new SpannableString("[转消费积分]朋友已收");
        }else{
            spannable = new SpannableString("[转消费积分]你已收");
        }
        return spannable;
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
    @Override
    public void onItemClick(View view, int i, TransferMessage redPackageMessage, UIMessage uiMessage) {

    }

    private static class ViewHolder {
        private TextView money, desc;
        private View layout;
    }
    @Override
    public void onItemLongClick(View view, int position, final TransferMessage content, final UIMessage message) {
        String[] items1;
        items1 = new String[]{ view.getContext().getResources().getString(io.rong.imkit.R.string.rc_dialog_item_message_delete)};
        OptionsPopupDialog.newInstance(view.getContext(), items1).setOptionsPopupDialogListener(new OptionsPopupDialog.OnOptionsItemClickedListener() {
            public void onOptionsItemClicked(int which) {
                if (which == 0) {
                    RongIM.getInstance().deleteMessages(new int[]{message.getMessageId()}, (RongIMClient.ResultCallback) null);
                }
            }
        }).show();
    }
}
