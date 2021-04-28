package com.ilesson.ppim.custom;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.IlessonApp;
import com.ilesson.ppim.R;
import com.ilesson.ppim.activity.WaresLogistcDetailctivity;
import com.ilesson.ppim.activity.WaresOrderDetailctivity;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.ExpressNotify;
import com.ilesson.ppim.entity.HideProgress;
import com.ilesson.ppim.entity.ShowProgress;
import com.ilesson.ppim.entity.WaresOrder;
import com.ilesson.ppim.utils.BigDecimalUtil;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.Dateuitls;
import com.ilesson.ppim.utils.PPScreenUtils;
import com.ilesson.ppim.view.RoundImageView;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import io.rong.eventbus.EventBus;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imlib.model.Message;

/**
 * Created by potato on 2020/3/12.
 */

@ProviderTag(messageContent = ExpressMessage.class)
public class ExpressItemProvider extends IContainerItemProvider.MessageProvider<ExpressMessage> {
    public static final double ITEMPROVIDER_WIDTH = .736;
    class ViewHolder {
        TextView uName,address,phone, name, price, num,orderNo,postTime,logisticsNo,des;
        RoundImageView imageView;
        View layout,detailView,confirm;
    }

    private Context context;

    @Override
    public View newView(Context context, ViewGroup group) {
        this.context = context;
        View view = LayoutInflater.from(context).inflate(R.layout.express_notify_item, null);
        ViewHolder holder = new ViewHolder();
        holder.orderNo = view.findViewById(R.id.order_num);
        holder.postTime = view.findViewById(R.id.post_time);
        holder.logisticsNo = view.findViewById(R.id.logistics_no);
        holder.uName = view.findViewById(R.id.consignee);
        holder.name = view.findViewById(R.id.wares_name);
        holder.num = view.findViewById(R.id.num);
        holder.des = view.findViewById(R.id.wares_quantity);
        holder.price = view.findViewById(R.id.all_price);
        holder.address = view.findViewById(R.id.address);
        holder.phone = view.findViewById(R.id.phone);
        holder.imageView = view.findViewById(R.id.wares_img);
        holder.layout = view.findViewById(R.id.layout);
        holder.detailView = view.findViewById(R.id.call_server);
        holder.confirm = view.findViewById(R.id.confirm);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View v, int i, ExpressMessage content, UIMessage message) {
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
        ExpressNotify waresIntro = new Gson().fromJson(content.getContent(),ExpressNotify.class);

        if (!TextUtils.isEmpty(waresIntro.getSubdesc())) {
            String p = String.format(v.getContext().getResources().getString(R.string.order_confirm_num),waresIntro.getNum()+"", BigDecimalUtil.format(Double.valueOf(waresIntro.getMoney())/100));
            holder.des.setText(waresIntro.getSubdesc());
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
        if (!TextUtils.isEmpty(waresIntro.getSubname())) {
            holder.name.setText(waresIntro.getSubname());
        }
        if (!TextUtils.isEmpty(waresIntro.getPostNumber())) {
            holder.logisticsNo.setText(waresIntro.getPostNumber());
        }
        if (!TextUtils.isEmpty(waresIntro.getIcon())) {
            Glide.with(v.getContext()).load(waresIntro.getIcon()).into(holder.imageView);
        }

        String unit = null==waresIntro.getSubunit()?"":waresIntro.getSubunit();
        holder.num.setText(String.format(v.getContext().getResources().getString(R.string.quantity_),waresIntro.getNum(),unit));
        if (!TextUtils.isEmpty(waresIntro.getTransationId())) {
            holder.orderNo.setText(waresIntro.getTransationId());
        }
        if (!TextUtils.isEmpty(waresIntro.getPayDate())) {
            holder.postTime.setText(Dateuitls.getOrderPayTime(waresIntro.getPayDate()));
        }
        if (!TextUtils.isEmpty(waresIntro.getMoney())) {
            String text = String.format(v.getContext().getResources().getString(R.string.all_fee_),BigDecimalUtil.format(Double.valueOf(waresIntro.getMoney()) / 100));
            SpannableStringBuilder style = new SpannableStringBuilder(text);
            style.setSpan(new ForegroundColorSpan(v.getContext().getResources().getColor(R.color.helptext_color)), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            style.setSpan(new ForegroundColorSpan(Color.RED), 4, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            style.setSpan(new RelativeSizeSpan(1.2f), 4, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.price.setText(style);
        }
        holder.detailView.setOnClickListener(v1 -> loadData(v.getContext(),waresIntro.getOid(),true));
        holder.confirm.setOnClickListener(v12 -> loadData(v.getContext(),waresIntro.getOid(),false));
        Log.d(TAG, "bindView: "+holder.confirm+" tranid="+waresIntro.getOid());
    }

    private void loadData(Context cotnext,String id,boolean detail) {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.ORDER);
        params.addParameter("action", "info");
        params.addParameter("oid", id);
        Log.d(TAG, "loadData: " + params.toString());
        EventBus.getDefault().post(new ShowProgress());
        x.http().post(params, new Callback.CacheCallback<String>() {
            @Override
            public boolean onCache(String result) {
                WaresOrder order = readJson(cotnext,result,detail);
                if(null==order||TextUtils.isEmpty(order.getName())){
                    return false;
                }
                return true;
            }

            @Override
            public void onSuccess(String result) {
                Log.d(TAG, " onSuccess: " + result);
                readJson(cotnext,result,detail);
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
                EventBus.getDefault().post(new HideProgress());
            }
        });
    }
    private WaresOrder readJson(Context cotnext,String json,boolean detail) {
        BaseCode<WaresOrder> base = new Gson().fromJson(
                json,
                new TypeToken<BaseCode<WaresOrder>>() {
                }.getType());
        if(base==null||base.getCode()!=0){
            return null;
        }
        WaresOrder order = base.getData();
        Intent intent = new Intent(cotnext, WaresOrderDetailctivity.class);
        intent.putExtra(WaresOrderDetailctivity.ORDER_DETAIL, order);
        if(!detail){
            intent.setClass(cotnext, WaresLogistcDetailctivity.class);
        }
        cotnext.startActivity(intent);
        return order;
    }
    @Override
    public Spannable getContentSummary(ExpressMessage data) {
        return new SpannableString(IlessonApp.getStringById(R.string.order_tips));
    }

    @Override
    public void onItemClick(View view, int i, ExpressMessage ExpressMessage, UIMessage uiMessage) {
//        Intent intent = new Intent(view.getContext(), ShopSearchActivity.class);
//        intent.putExtra(GROUP_ID,uiMessage.getTargetId());
//        view.getContext().startActivity(intent);
    }

    private static final String TAG = "ExpressItemProvider";
}