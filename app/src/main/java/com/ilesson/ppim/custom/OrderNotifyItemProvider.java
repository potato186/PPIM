package com.ilesson.ppim.custom;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.IlessonApp;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.GroupWaresIntro;
import com.ilesson.ppim.entity.OrderConfirmInfo;
import com.ilesson.ppim.utils.BigDecimalUtil;
import com.ilesson.ppim.utils.IMUtils;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.view.RoundImageView;

import io.rong.imkit.RongIM;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;

import static com.ilesson.ppim.activity.LoginActivity.USER_PHONE;

/**
 * Created by potato on 2020/3/12.
 */

@ProviderTag(messageContent = OrderConfirmMessage.class)
public class OrderNotifyItemProvider extends IContainerItemProvider.MessageProvider<OrderConfirmMessage> {

    class ViewHolder {
        TextView uName,address,phone, name, price, num;
        RoundImageView imageView;
        View layout,callServer,confirm;
    }

    private Context context;

    @Override
    public View newView(Context context, ViewGroup group) {
        this.context = context;
        View view = LayoutInflater.from(context).inflate(R.layout.order_confirm_item, null);
        ViewHolder holder = new ViewHolder();
        holder.uName = view.findViewById(R.id.username);
        holder.name = view.findViewById(R.id.wares_name);
        holder.num = view.findViewById(R.id.wares_quantity);
        holder.price = view.findViewById(R.id.wares_price);
        holder.address = view.findViewById(R.id.address_view);
        holder.phone = view.findViewById(R.id.phone_view);
        holder.imageView = view.findViewById(R.id.wares_img);
        holder.layout = view.findViewById(R.id.layout);
        holder.callServer = view.findViewById(R.id.call_server);
        holder.confirm = view.findViewById(R.id.confirm);
        view.setTag(holder);
        return view;
    }

    public void bindView(View v, int position, OrderConfirmMessage content, Message message) {

    }

    @Override
    public void bindView(View v, int i, OrderConfirmMessage content, UIMessage message) {
        final ViewHolder holder = (ViewHolder) v.getTag();

        if (message.getMessageDirection() == Message.MessageDirection.SEND) {//消息方向，自己发送的
            holder.layout.setBackgroundResource(io.rong.imkit.R.drawable.rc_ic_bubble_right);
        } else {
            holder.layout.setBackgroundResource(io.rong.imkit.R.drawable.rc_ic_bubble_left);
        }
        final OrderConfirmInfo waresIntro = new Gson().fromJson(content.getContent(),OrderConfirmInfo.class);

//        GroupWaresIntro waresIntro = getEntity(content.getContent());
        if (null == waresIntro) {
            return;
        }
        if (!TextUtils.isEmpty(waresIntro.getMoney())) {
            String p = String.format(v.getContext().getResources().getString(R.string.order_confirm_num),waresIntro.getNum()+"", BigDecimalUtil.format(Double.valueOf(waresIntro.getMoney())/100));
            holder.num.setText(p);
        }
        if (!TextUtils.isEmpty(waresIntro.getpName())) {
            holder.uName.setText(waresIntro.getpName());
        }
        if (!TextUtils.isEmpty(waresIntro.getpAddress())) {
            holder.address.setText(waresIntro.getpAddress());
        }
        if (!TextUtils.isEmpty(waresIntro.getpPhone())) {
            holder.phone.setText(waresIntro.getpPhone());
        }
        if (!TextUtils.isEmpty(waresIntro.getName())) {
            holder.name.setText(waresIntro.getName());
        }
        if (!TextUtils.isEmpty(waresIntro.getIcon())) {
            Glide.with(v.getContext()).load(waresIntro.getIcon()).into(holder.imageView);
        }
        holder.callServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IMUtils().requestShopServer(null, waresIntro.getShopkeeper());
                RongIM.getInstance().startConversation(v.getContext(), Conversation.ConversationType.PRIVATE, waresIntro.getShopkeeper(), waresIntro.getName() + v.getContext().getResources().getString(R.string.custom_server));
            }
        });
        String state = SPUtils.get(SPUtils.get(USER_PHONE,"")+waresIntro.getOid(),"");
        if(TextUtils.isEmpty(state)){
            holder.confirm.setEnabled(true);
            holder.confirm.setBackgroundResource(R.drawable.general_red_theme_corner20_selector);
        }else{
            holder.confirm.setBackgroundResource(R.drawable.background_gray_corner20);
            holder.confirm.setEnabled(false);
        }
        holder.confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(SPUtils.get(SPUtils.get(USER_PHONE,"")+waresIntro.getOid(),""))){
                    return;
                }
                new IMUtils().confirmOrder(holder.confirm,waresIntro.getOid());
            }
        });
    }

    private GroupWaresIntro getEntity(String json) {
        try {
            return new Gson().fromJson(
                    json,
                    new TypeToken<GroupWaresIntro>() {
                    }.getType());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Spannable getContentSummary(OrderConfirmMessage data) {
        GroupWaresIntro intro = getEntity(data.getContent());
        return new SpannableString(IlessonApp.getStringById(R.string.order_tips));
    }

    @Override
    public void onItemClick(View view, int i, OrderConfirmMessage OrderConfirmMessage, UIMessage uiMessage) {
//        Intent intent = new Intent(view.getContext(), ShopSearchActivity.class);
//        intent.putExtra(GROUP_ID,uiMessage.getTargetId());
//        view.getContext().startActivity(intent);
    }
}