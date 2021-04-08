package com.ilesson.ppim.custom;


import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.PPayInfo;
import com.ilesson.ppim.utils.BigDecimalUtil;
import com.ilesson.ppim.utils.IMUtils;

import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.provider.IContainerItemProvider;

/**
 * Created by longShun on 2017/2/24.
 * desc新建一个消息类继承 IContainerItemProvider.MessageProvider 类，实现对应接口方法，
 * 1.注意开头的注解！
 * 2.注意泛型！
 */
@ProviderTag(
        messageContent = PPayMessage.class,
        showPortrait = false,
        showReadState = true
)
public class PPayItemProvider extends IContainerItemProvider.MessageProvider<PPayMessage> {

    public PPayItemProvider() {
    }
    private Context context;
    @Override
    public View newView(Context context, ViewGroup viewGroup) {
        this.context = context;
        //这就是展示在会话界面的自定义的消息的布局
        View view = LayoutInflater.from(context).inflate(R.layout.ppay_item, null);
        ViewHolder holder = new ViewHolder();
        holder.money = view.findViewById(R.id.money);
        holder.target = view.findViewById(R.id.target);
        holder.state = view.findViewById(R.id.pay_state);
        holder.from = view.findViewById(R.id.pay_from);
        holder.formUser = view.findViewById(R.id.from_user);
        holder.time = view.findViewById(R.id.pay_time);
        holder.number = view.findViewById(R.id.pay_no);
        holder.payTitle = view.findViewById(R.id.pay_title);
        holder.payLayout = view.findViewById(R.id.ppay_layout);
//        holder.time = view.findViewById(R.id.time);
        view.setTag(holder);
        return view;
    }

    private static final String TAG = "PPayItemProvider";
    @Override
    public void bindView(View view, int i, PPayMessage payMessage, UIMessage message) {

        //根据需求，适配数据
        ViewHolder holder = (ViewHolder) view.getTag();
        String content = payMessage.getContent();
        String[] arr = content.split("\\|");
        double score = Double.valueOf(arr[0]);
        String currency = arr[2];
        String money = BigDecimalUtil.format(score);
        PPayInfo payInfo = new Gson().fromJson(
                payMessage.getExtra(),
                new TypeToken<PPayInfo>() {
                }.getType());
        if(null!=payInfo.getDate()){
            holder.time.setText(IMUtils.getDate(payInfo.getDate()));
        }
        holder.number.setText(payInfo.getUuid());
        holder.target.setText(payInfo.getName());

        if(score>0){
            holder.state.setText("收消费积分成功，已收对方消费积分");
            holder.money.setText("+"+money);
            holder.from.setText("收"+currency+"消费积分");
            holder.formUser.setText("付消费积分方");
            holder.payTitle.setText("消费积分到账通知");
        }else{
            holder.target.setText(payInfo.getName());
            holder.money.setText(money);
            holder.state.setText("支付消费积分成功，对方已收消费积分");
            holder.from.setText("支付"+currency+"消费积分");
            holder.formUser.setText("收消费积分方");
            holder.payTitle.setText("消费积分支付凭证");
        }
//        holder.time.setText(pp)
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        Log.d(TAG, "bindView: "+payMessage.getContent());
    }
    @Override
    public Spannable getContentSummary(PPayMessage redPackageMessage) {
        return new SpannableString("PP支付消费积分凭证");
    }
//<string name="transfer_to">转账给</string>
//    <string name="transfer_to_you">转账给你</string>
//    <string name="transfer_summary">[转账]请你确认收款</string>
    @Override
    public void onItemClick(View view, int i, PPayMessage redPackageMessage, UIMessage uiMessage) {

    }

    @Override
    public void onItemLongClick(final View view, int i, PPayMessage redPackageMessage, UIMessage uiMessage) {
//        //实现长按删除等功能，咱们直接复制融云其他provider的实现
//        String[] items1;//复制，删除
//        items1 = new String[]{view.getContext().getResources().getString(io.rong.imkit.R.string.rc_dialog_item_message_copy), view.getContext().getResources().getString(io.rong.imkit.R.string.rc_dialog_item_message_delete)};
//
//        OptionsPopupDialog.newInstance(view.getContext(), items1).setOptionsPopupDialogListener(new OptionsPopupDialog.OnOptionsItemClickedListener() {
//            public void onOptionsItemClicked(int which) {
//                if (which == 0) {
//                    ClipboardManager clipboard = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
////                    clipboard.setText(content.getContent());//这里是自定义消息的消息属性
//                } else if (which == 1) {
////                    RongIM.getInstance().deleteMessages(new int[]{redPackageMessage.getMessageId()}, (RongIMClient.ResultCallback) null);
//                }
//            }
//        }).show();

    }

    private static class ViewHolder {
        private TextView money, target,state,from,time,number,payTitle,formUser;
        private View payLayout;
    }
}
