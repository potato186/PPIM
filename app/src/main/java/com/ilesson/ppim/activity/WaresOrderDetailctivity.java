package com.ilesson.ppim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.WaresOrder;
import com.ilesson.ppim.utils.BigDecimalUtil;
import com.ilesson.ppim.utils.Dateuitls;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import java.util.HashMap;

import io.rong.imkit.RongIM;
import io.rong.imlib.model.Conversation;

import static com.ilesson.ppim.activity.InvoiceActivity.INVOICE_ELECT;
import static com.ilesson.ppim.activity.InvoiceActivity.INVOICE_PERSON;


/**
 * Created by potato on 2020/3/10.
 */
@ContentView(R.layout.act_order_detail)
public class WaresOrderDetailctivity extends BaseActivity {


    @ViewInject(R.id.keeper_icon)
    private ImageView iconView;
    @ViewInject(R.id.wares_img)
    private ImageView waresImg;
    @ViewInject(R.id.wares_name)
    private TextView waresName;
    @ViewInject(R.id.keeper_name)
    private TextView keeperName;
    @ViewInject(R.id.consignee)
    private TextView nameView;
    @ViewInject(R.id.wares_price)
    private TextView waresPrice;
    @ViewInject(R.id.wares_quantity)
    private TextView waresQuantity;
    @ViewInject(R.id.express_fee_price)
    private TextView express;
    @ViewInject(R.id.num)
    public TextView waresNum;
    @ViewInject(R.id.unit_price)
    public TextView unitPrice;
    @ViewInject(R.id.wares_info)
    public TextView waresInfo;
    @ViewInject(R.id.all_price)
    public TextView allPrice;
    @ViewInject(R.id.order_num)
    public TextView orderNum;
    @ViewInject(R.id.pay_time)
    public TextView payTime;
    @ViewInject(R.id.phone)
    public TextView phoneView;
    @ViewInject(R.id.address)
    public TextView addressView;
    @ViewInject(R.id.logistics_name)
    public TextView logisticsName;
    @ViewInject(R.id.logistics_no)
    public TextView logisticsNo;
    @ViewInject(R.id.post_time)
    public TextView postTime;
    @ViewInject(R.id.check_logistc)
    public TextView checkLogistc;
    @ViewInject(R.id.state)
    public TextView state;

    @ViewInject(R.id.post_time_view)
    public View postTimeView;
    @ViewInject(R.id.logistics_no_view)
    public View logisticsNoView;
    @ViewInject(R.id.logistics_name_view)
    public View logisticsNameView;
    @ViewInject(R.id.invoice_layout)
    public View invoiceLayout;
    @ViewInject(R.id.email_layout)
    public View emailLayout;
    @ViewInject(R.id.invoice_type)
    public TextView invoiceType;
    @ViewInject(R.id.voice_type)
    public TextView invoiceType1;
    @ViewInject(R.id.title_type)
    public TextView invoiceTitleType;
    @ViewInject(R.id.title_type_name)
    public TextView invoiceTitleName;
    @ViewInject(R.id.invoice_price)
    public TextView invoicePrice;
    @ViewInject(R.id.invoice_email)
    public TextView invoiceEmail;
    @ViewInject(R.id.call_server)
    public TextView callServer;
    public static final String ORDER_DETAIL = "order_detail";
    public static final String SHOP_ORDER = "shop_order";
    private WaresOrder order;
    private boolean shopOrder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarLightMode(this,true);
        order = (WaresOrder) getIntent().getSerializableExtra(ORDER_DETAIL);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        shopOrder = getIntent().getBooleanExtra(SHOP_ORDER,false);
        if(shopOrder){
            callServer.setText(R.string.call_user);
        }
        HashMap<String, Integer> stringIntegerHashMap = new HashMap<>();
        waresName.setText(order.getName());
        waresPrice.setText(getString(R.string.rmb) + BigDecimalUtil.format(Double.valueOf(order.getPrice()) / 100));
//        waresQuantity.setText(order.getInfo());
        waresInfo.setText(order.getSubDesc());
        if(TextUtils.isEmpty(order.getPostdate())){
            postTimeView.setVisibility(View.GONE);
        }else{
            postTime.setText(Dateuitls.getFormatOrderTime(Long.valueOf(order.getPostdate())));
            postTimeView.setVisibility(View.VISIBLE);
        }
        if(TextUtils.isEmpty(order.getPostno())){
            logisticsNoView.setVisibility(View.GONE);
            state.setText(R.string.no_post);
        }else{
            logisticsNo.setText(order.getPostno());
            logisticsNoView.setVisibility(View.VISIBLE);
        }
        if(TextUtils.isEmpty(order.getPostname())){
            logisticsNameView.setVisibility(View.GONE);
        }else{
            logisticsName.setText(order.getPostname());
            logisticsNameView.setVisibility(View.VISIBLE);
        }
        orderNum.setText(order.getTransaction_id());
        payTime.setText(Dateuitls.getOrderPayTime(order.getPay_date()));
        phoneView.setText(order.getUphone());
        addressView.setText(order.getUaddress());
        nameView.setText(order.getUname());

        Glide.with(WaresOrderDetailctivity.this).load(order.getIcon()).into(waresImg);
        Glide.with(WaresOrderDetailctivity.this).load(order.getShoplogo()).into(iconView);
        keeperName.setText(order.getShopname());
        waresName.setText(order.getName());
        waresNum.setText(String.format(getResources().getString(R.string.num_format),order.getNum()));
        if (!TextUtils.isEmpty(order.getFei())) {
            express.setText(String.format(getResources().getString(R.string.express_fee), BigDecimalUtil.format(Double.valueOf(order.getFei()) / 100)));
        }
        if(order.getTrade_no().startsWith("ex")){
            String text = String.format(getResources().getString(R.string.score_price), Integer.valueOf(order.getPerPrice()));
            int length = order.getPrice().length();
            SpannableStringBuilder style = new SpannableStringBuilder(text);
            style.setSpan(new RelativeSizeSpan(1.2f), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                    price.setText(style);
            unitPrice.setText(text);
            waresPrice.setVisibility(View.GONE);
            allPrice.setText(String.format(getResources().getString(R.string.all_pay_score), Integer.valueOf(order.getPrice())));
        }else{
            waresPrice.setVisibility(View.VISIBLE);
            double price = Double.valueOf(order.getNum())*Double.valueOf(order.getPerPrice());
            waresPrice.setText(String.format(getResources().getString(R.string.wares_price), BigDecimalUtil.format(price / 100)));
            unitPrice.setText(String.format(getResources().getString(R.string.rmb_format),BigDecimalUtil.format(Double.valueOf(order.getPerPrice()) / 100)));
            String allp = String.format(getResources().getString(R.string.all_fee),BigDecimalUtil.format(Double.valueOf((double)price/100))+"");
            allPrice.setText(allp);
        }
        allPrice.setTextColor(getResources().getColor(R.color.gray_text333_color));
        if(TextUtils.isEmpty(order.getPostdate())){
            checkLogistc.setVisibility(View.GONE);
        }else{
            checkLogistc.setVisibility(View.VISIBLE);
        }

        if(!TextUtils.isEmpty(order.getInvoice_name())){
            invoiceLayout.setVisibility(View.VISIBLE);
            String title = order.getInvoice_type().equals(INVOICE_PERSON)?getResources().getString(R.string.personal):getResources().getString(R.string.enterprise);
            invoiceType.setText(order.getInvoice_mediumName());
            invoiceType1.setText(order.getInvoice_mediumName());
            invoiceTitleType.setText(title);
            invoiceTitleName.setText(order.getInvoice_name());
            double price = Double.valueOf(order.getNum())*Double.valueOf(order.getPerPrice());
            invoicePrice.setText(String.format(getResources().getString(R.string.format_yuan_s),BigDecimalUtil.format(Double.valueOf((double)price/100))));
            if(order.getInvoice_medium().equals(INVOICE_ELECT)){
                invoiceEmail.setText(order.getInvoice_email());
                emailLayout.setVisibility(View.VISIBLE);
            }else{
                emailLayout.setVisibility(View.GONE);
            }
        }else{
            invoiceLayout.setVisibility(View.GONE);
        }
    }

    private static final String TAG = "WaresOrderDetailctivity";
    @Event(value = R.id.back_btn)
    private void back_btn(View view) {
        finish();
    }
    @Event(value = R.id.check_logistc)
    private void check_logistc(View view) {
        Intent intent = new Intent(this,WaresLogistcDetailctivity.class);
        intent.putExtra(WaresOrderDetailctivity.ORDER_DETAIL,order);
        startActivity(intent);
    }
    @Event(value = R.id.call_server)
    private void call_server(View view) {
        String targetId = null;
        String name = null;
        if(shopOrder){
            targetId = order.getUser();
            name = order.getUname();
        }else{
            targetId = order.getShopkeeper();
            name = order.getShopname()+getResources().getString(R.string.custom_server);
        }
        if(!TextUtils.isEmpty(targetId)){
            RongIM.getInstance().startConversation(this, Conversation.ConversationType.PRIVATE,targetId,name);
        }
    }
}
