package com.ilesson.ppim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.PostState;
import com.ilesson.ppim.entity.WaresOrder;
import com.ilesson.ppim.utils.Constants;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import io.rong.eventbus.EventBus;

import static com.ilesson.ppim.activity.InvoiceActivity.INVOICE_ELECT;
import static com.ilesson.ppim.activity.InvoiceActivity.INVOICE_PERSON;
import static com.ilesson.ppim.activity.WaresOrderManagerListActivity.WARESORDER;


/**
 * Created by potato on 2019/4/9.
 */
@ContentView(R.layout.act_manager_order)
public class ModifyLogisticActivity extends BaseActivity{
    @ViewInject(R.id.post_name)
    private EditText postName;
    @ViewInject(R.id.post_no)
    private EditText postNo;
    @ViewInject(R.id.save)
    private TextView save;
    @ViewInject(R.id.check_image)
    public ImageView checkImage;
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
    private EditText[] homes;
    private EditText[] companys;
    public static final int MODIFY_ORDER_SUCCESS=1;
    private WaresOrder order;
    private boolean confirm;
    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setStatusBarLightMode(this,true);
        order = (WaresOrder) getIntent().getSerializableExtra(WARESORDER);
        if(null==order){
            return;
        }
        homes = new EditText[]{postName, postNo};
        if(!TextUtils.isEmpty(order.getPostname())){
            postName.setText(order.getPostname());
        }
        if(!TextUtils.isEmpty(order.getPostno())){
            postNo.setText(order.getPostno());
        }
        if(!TextUtils.isEmpty(order.getInvoice_name())){
            invoiceLayout.setVisibility(View.VISIBLE);
            String title = order.getInvoice_type().equals(INVOICE_PERSON)?getResources().getString(R.string.personal):getResources().getString(R.string.enterprise);
            invoiceType.setText(order.getInvoice_mediumName());
            invoiceType1.setText(order.getInvoice_mediumName());
            invoiceTitleType.setText(title);
            invoiceTitleName.setText(order.getInvoice_name());
            double price = Double.valueOf(order.getNum())*Double.valueOf(order.getPerPrice());
            invoicePrice.setText(String.format(getResources().getString(R.string.format_yuan_s),price+""));
            if(order.getInvoice_medium().equals(INVOICE_ELECT)){
                invoiceEmail.setText(order.getInvoice_email());
                emailLayout.setVisibility(View.VISIBLE);
            }else{
                emailLayout.setVisibility(View.GONE);
            }
        }else{
            invoiceLayout.setVisibility(View.GONE);
        }
//        postName.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                checkState();
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });
//        postNo.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                checkState();
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });
//        save.setEnabled(false);
//        addListener();
    }
    private void checkState(){
        if(!TextUtils.isEmpty(postName.getText().toString().trim())&&!TextUtils.isEmpty(postNo.getText().toString().trim())){
            setEnableSate();
        }else{
            setDisEnablestate();
        }
    }
    private void setEnableSate(){
        save.setEnabled(true);
        save.setBackgroundResource(R.drawable.background_theme_corner20);
        save.setTextColor(getResources().getColor(R.color.white));
    }
    private void setDisEnablestate(){
        save.setEnabled(false);
        save.setBackgroundResource(R.drawable.background_gray_corner20);
        save.setTextColor(getResources().getColor(R.color.gray_text_color));
    }
    private static final String TAG = "AdressActivity";
    @Event(R.id.save)
    private void save(View view){
        if(TextUtils.isEmpty(postName.getText().toString())){
            showToast(R.string.hint_logist_name_tip);
            return;
        }
        if(TextUtils.isEmpty(postNo.getText().toString())){
            showToast(R.string.hint_logist_num_tip);
            return;
        }
        if(!TextUtils.isEmpty(order.getInvoice_name())&&!confirm){
            showToast("请确定已开发票");
            return;
        }
        modify();
    }
    @Event(R.id.check_layout)
    private void check(View view){
        checkImage.setImageResource(R.mipmap.checked);
        confirm = true;
    }
    @Event(R.id.back)
    private void back(View view){
        finish();
    }
    private void modify() {
        save.setEnabled(false);
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.SHOPKEEPER);
        params.addParameter("action", "modify");
        params.addParameter("trade_no", order.getTrade_no());
        params.addParameter("post_name", postName.getText().toString().trim());
        params.addParameter("post_no", postNo.getText().toString().trim());
        Log.d(TAG, "search: " + params.toString());
        showProgress();
        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "add: "+result);
                BaseCode base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode>() {
                        }.getType());
                if(base.getCode()==0){
                    Intent intent = new Intent();
                    order.setPostname(postName.getText().toString().trim());
                    order.setPostno(postNo.getText().toString().trim());
                    intent.putExtra(WARESORDER, order);
                    setResult(MODIFY_ORDER_SUCCESS,intent);
                    finish();
                    EventBus.getDefault().post(new PostState());
                }else {
                    showToast(base.getMessage());
                }

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
                hideProgress();
                save.setEnabled(true);
            }
        });
    }
    private int homeItems;
    private void checkHome(){
        homeEdited = true;
        homeItems=0;
        for(EditText editText:homes){
            if(!TextUtils.isEmpty(editText.getText().toString())){
                homeItems++;
            }
        }
    }
    private int companyItems;
    private void checkCompany(){
        companyEdited = true;
        companyItems=0;
        for(EditText editText:companys){
            if(!TextUtils.isEmpty(editText.getText().toString())){
                companyItems++;
            }
        }
    }
    private boolean checkSave(){
        boolean changed = false;
        if(homeItems==0&&companyItems==3){
            changed = true;
        }else if(homeItems==3&&companyItems==3){
            changed = true;
        }else if(homeItems==3&&companyItems==0){
            changed = true;
        }else if(homeItems==0&&companyItems==0){
//            if(homeAddress!=null||companyAddress!=null){
//                changed = true;
//            }
        }

        if(changed){
            save.setTextColor(getResources().getColor(R.color.white));
            save.setBackgroundResource(R.drawable.theme_gray_corer5_btn_selector);
            save.setEnabled(true);
        }else{
            save.setTextColor(getResources().getColor(R.color.color_999999));
            save.setBackgroundResource(R.drawable.background_deepgray_corner5);
            save.setEnabled(false);
        }
        return false;
    }
    private boolean homeEdited;
    private boolean companyEdited;
    private void addListener(){
        for(EditText editText:homes){
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    checkHome();
                    checkSave();
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
    }
}
