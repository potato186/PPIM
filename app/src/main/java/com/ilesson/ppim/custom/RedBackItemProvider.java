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
        messageContent = RedBackMessage.class,
        showPortrait = false,
        showReadState = true
)
public class RedBackItemProvider extends IContainerItemProvider.MessageProvider<RedBackMessage> {

    public RedBackItemProvider() {
    }

    private Context context;

    @Override
    public View newView(Context context, ViewGroup viewGroup) {
        this.context = context;
        //这就是展示在会话界面的自定义的消息的布局
        View view = LayoutInflater.from(context).inflate(R.layout.red_back_item, null);
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
    public void bindView(View view, int i, RedBackMessage payMessage, UIMessage message) {

        //根据需求，适配数据
        ViewHolder holder = (ViewHolder) view.getTag();
        String content = payMessage.getContent();
//        String money = content.substring(0, content.indexOf("|"));
        RedBack redBack = new Gson().fromJson(
                content,
                new TypeToken<RedBack>() {
                }.getType());

        holder.money.setText("+" + redBack.getMoney());
        holder.number.setText(redBack.getId());
    }

    @Override
    public Spannable getContentSummary(RedBackMessage redPackageMessage) {
        return new SpannableString("PP支付消费积分凭证");
    }

    //<string name="transfer_to">转账给</string>
//    <string name="transfer_to_you">转账给你</string>
//    <string name="transfer_summary">[转账]请你确认收款</string>
    @Override
    public void onItemClick(View view, int i, RedBackMessage redPackageMessage, UIMessage uiMessage) {

    }

    @Override
    public void onItemLongClick(final View view, int i, RedBackMessage redPackageMessage, UIMessage uiMessage) {
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
        private TextView money, target, state, from, time, number, payTitle, formUser;
        private View payLayout;
    }
    class RedBack{
        private String id;
        private String sender;
        private int money;
        private int count;
        private long date;
        private boolean finished;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSender() {
            return sender;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }

        public int getMoney() {
            return money;
        }

        public void setMoney(int money) {
            this.money = money;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public long getDate() {
            return date;
        }

        public void setDate(long date) {
            this.date = date;
        }

        public boolean isFinished() {
            return finished;
        }

        public void setFinished(boolean finished) {
            this.finished = finished;
        }
    }
}
