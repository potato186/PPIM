package com.ilesson.ppim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.WaresOrder;
import com.ilesson.ppim.utils.Dateuitls;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import static com.ilesson.ppim.activity.WaresOrderDetailctivity.ORDER_DETAIL;

/**
 * Created by potato on 2020/3/11.
 */
@ContentView(R.layout.act_payresult)
public class PayResultActivity extends BaseActivity {
    public static String PAY_USER_NAME="pay_user_name";
    public static String PAY_USER_ICON="pay_user_icon";
    public static String PAY_MONEY="pay_money";
    @ViewInject(R.id.order_num)
    private TextView orderNum;
    @ViewInject(R.id.post_time)
    private TextView postTime;
    @ViewInject(R.id.consignee)
    private TextView consignee;
    @ViewInject(R.id.phone)
    private TextView phone;
    @ViewInject(R.id.address)
    private TextView address;
    private WaresOrder order;
    @Event(R.id.back_market)
    private void back(View v){
        finish();
    }
    @Event(R.id.confirm)
    private void done(View v){
        if(null==order){
            return;
        }
        Intent intent = new Intent(this, WaresOrderDetailctivity.class);
        intent.putExtra(WaresOrderDetailctivity.ORDER_DETAIL, order);
        startActivity(intent);
        finish();
    }
    private static final String TAG = "PayResultActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarLightMode(this,true);
        Intent intent = getIntent();
        order = (WaresOrder) intent.getSerializableExtra(ORDER_DETAIL);
        if(null==order){
            return;
        }
        orderNum.setText(order.getTransaction_id());
        postTime.setText(Dateuitls.getFormatOrderTime(System.currentTimeMillis()));
        consignee.setText(order.getUname());
        phone.setText(order.getUphone());
        address.setText(order.getUaddress());
    }

}
