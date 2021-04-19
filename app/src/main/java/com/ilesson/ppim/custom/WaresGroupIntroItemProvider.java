package com.ilesson.ppim.custom;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.activity.WareDetailActivity;
import com.ilesson.ppim.entity.GroupWaresIntro;
import com.ilesson.ppim.utils.PPScreenUtils;
import com.ilesson.ppim.view.RoundImageView;

import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imlib.model.Message;

import static com.ilesson.ppim.activity.WareDetailActivity.PRODUCT_ID;
import static com.ilesson.ppim.custom.OrderNotifyItemProvider.ITEMPROVIDER_WIDTH;

/**
 * Created by potato on 2020/3/12.
 */

@ProviderTag(messageContent = WaresGroupMessage.class)
public class WaresGroupIntroItemProvider extends IContainerItemProvider.MessageProvider<WaresGroupMessage> {

    class ViewHolder {
        TextView des, name, price, num;
        RoundImageView imageView;
        ImageView imageViewContent;
        View layout;
    }

    private Context context;

    @Override
    public View newView(Context context, ViewGroup group) {
        this.context = context;
        View view = LayoutInflater.from(context).inflate(R.layout.ware_des_in_group, null);
        ViewHolder holder = new ViewHolder();
        holder.des = view.findViewById(R.id.wares_des);
        holder.name = view.findViewById(R.id.wares_name);
        holder.num = view.findViewById(R.id.wares_quantity);
        holder.price = view.findViewById(R.id.wares_price);
        holder.imageView = view.findViewById(R.id.wares_img);
        holder.imageViewContent = view.findViewById(R.id.result_image);
        holder.layout = view.findViewById(R.id.layout);
        view.setTag(holder);
        return view;
    }

    public void bindView(View v, int position, WaresGroupMessage content, Message message) {

    }

    @Override
    public void bindView(final View v, int i, WaresGroupMessage content, UIMessage message) {
        final ViewHolder holder = (ViewHolder) v.getTag();

        if (message.getMessageDirection() == Message.MessageDirection.SEND) {//消息方向，自己发送的
            holder.layout.setBackgroundResource(io.rong.imkit.R.drawable.rc_ic_bubble_right);
        } else {
            holder.layout.setBackgroundResource(io.rong.imkit.R.drawable.rc_ic_bubble_left);
        }
        final int sw = PPScreenUtils.getScreenWidth(v.getContext());
        ViewGroup.LayoutParams params = holder.layout.getLayoutParams();
        final int layoutW = (int) (sw * ITEMPROVIDER_WIDTH);
        params.width = layoutW;
        holder.layout.setLayoutParams(params);
        GroupWaresIntro waresIntro = getEntity(content.getContent());
        if (null == waresIntro) {
            return;
        }
//        if (!TextUtils.isEmpty(waresIntro.getPrice())) {
//            holder.price.setText(v.getContext().getString(R.string.rmb) + BigDecimalUtil.format(Double.valueOf(waresIntro.getPrice())/100));
//        }
        if (!TextUtils.isEmpty(waresIntro.getDetail())) {
            holder.des.setText(waresIntro.getDetail());
        }
//        if (!TextUtils.isEmpty(waresIntro.getDesc())) {
//            holder.num.setText(waresIntro.getDesc());
//        }
//        if (!TextUtils.isEmpty(waresIntro.getName())) {
//            holder.name.setText(waresIntro.getName());
//        }
//        if (!TextUtils.isEmpty(waresIntro.getIcon())) {
//            Glide.with(v.getContext()).load(waresIntro.getIcon()).into(holder.imageView);
//        }
        Glide.with(v.getContext()).asBitmap().load(waresIntro.getIcon()).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                ViewGroup.LayoutParams params = holder.imageViewContent.getLayoutParams();

                int maxW = (int) (layoutW *.9);
//                if ((double) resource.getWidth() / (double) resource.getHeight() > 1.2) {
//                    width = maxW;
//                    height = resource.getHeight() * width / resource.getWidth();
//                } else {
//                    int calW = PPScreenUtils.dip2px(ConversationActivity.this, resource.getWidth());
//                    if (calW < maxW) {
//                        width = calW;
//                        height = PPScreenUtils.dip2px(ConversationActivity.this, resource.getHeight());
//                    }
//                }
                params.width = maxW;
                params.height = resource.getHeight() * maxW / resource.getWidth();
                holder.imageViewContent.setLayoutParams(params);
                holder.imageViewContent.setImageBitmap(resource);
                holder.imageViewContent.setScaleType(ImageView.ScaleType.FIT_XY);
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
    public Spannable getContentSummary(WaresGroupMessage data) {
        GroupWaresIntro intro = getEntity(data.getContent());
        return new SpannableString(intro.getName());
    }

    @Override
    public void onItemClick(View view, int i, WaresGroupMessage content, UIMessage uiMessage) {
        GroupWaresIntro waresIntro = getEntity(content.getContent());
        Intent intent = new Intent(view.getContext(), WareDetailActivity.class);
//        intent.putExtra(GROUP_ID,uiMessage.getTargetId());
        intent.putExtra(PRODUCT_ID,waresIntro.getProduce());
        view.getContext().startActivity(intent);
    }
}