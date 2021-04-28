package com.ilesson.ppim.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import com.google.zxing.activity.CaptureActivity;
import com.ilesson.ppim.IlessonApp;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.Express;
import com.ilesson.ppim.entity.PostState;
import com.ilesson.ppim.entity.WaresOrder;
import com.ilesson.ppim.utils.BigDecimalUtil;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.SPUtils;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import io.rong.eventbus.EventBus;

import static com.ilesson.ppim.activity.InvoiceActivity.INVOICE_COMPANY;
import static com.ilesson.ppim.activity.InvoiceActivity.INVOICE_ELECT;
import static com.ilesson.ppim.activity.InvoiceActivity.INVOICE_PERSON;
import static com.ilesson.ppim.activity.WaresOrderDetailctivity.ORDER_DETAIL;
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
    @ViewInject(R.id.tax_num)
    public View taxLayout;
    @ViewInject(R.id.company_num)
    public TextView companyNum;
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
    @ViewInject(R.id.product_name)
    public TextView productName;
    @ViewInject(R.id.num)
    public TextView quantityView;
    @ViewInject(R.id.order_num)
    public TextView orderNum;
    @ViewInject(R.id.phone)
    public TextView phoneView;
    @ViewInject(R.id.address)
    public TextView addressView;
    @ViewInject(R.id.consignee)
    public TextView consignee;
    private EditText[] homes;
    private EditText[] companys;
    public static final int MODIFY_ORDER_SUCCESS=1;
    private WaresOrder order;
    private boolean confirm;
    public static final String POST_NAME="post_name";
    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setStatusBarLightMode(this,true);
        order = (WaresOrder) getIntent().getSerializableExtra(ORDER_DETAIL);
        if(null==order){
            return;
        }
        homes = new EditText[]{postName, postNo};
        if(!TextUtils.isEmpty(order.getPostname())){
            postName.setText(order.getPostname());
        }else {
            postName.setText(SPUtils.get(POST_NAME,""));
        }
        if(!TextUtils.isEmpty(order.getPostno())){
            postNo.setText(order.getPostno());
        }
        productName.setText(order.getName());
        quantityView.setText(order.getNum());
        orderNum.setText(order.getTransaction_id());
        phoneView.setText(order.getUphone());
        addressView.setText(order.getUaddress());
        consignee.setText(order.getUname());
        if(!TextUtils.isEmpty(order.getInvoice_name())){
            invoiceLayout.setVisibility(View.VISIBLE);
            String title = order.getInvoice_type().equals(INVOICE_PERSON)?getResources().getString(R.string.personal):getResources().getString(R.string.enterprise);
            invoiceType.setText(order.getInvoice_mediumName());
            invoiceType1.setText(order.getInvoice_mediumName());
            invoiceTitleType.setText(title);
            invoiceTitleName.setText(order.getInvoice_name());
            double price = Double.valueOf(order.getNum())*Double.valueOf(order.getPerPrice());
            invoicePrice.setText(String.format(getResources().getString(R.string.format_yuan_s), BigDecimalUtil.format(Double.valueOf((double)price/100))));
            if(order.getInvoice_type().equals(INVOICE_COMPANY)){
                taxLayout.setVisibility(View.VISIBLE);
                companyNum.setText(order.getInvoice_number());
            }else{
                taxLayout.setVisibility(View.GONE);
            }
            if(order.getInvoice_medium().equals(INVOICE_ELECT)){
                invoiceEmail.setText(order.getInvoice_email());
                emailLayout.setVisibility(View.VISIBLE);
            }else{
                emailLayout.setVisibility(View.GONE);
            }
        }else{
            invoiceLayout.setVisibility(View.GONE);
        }
        if(!TextUtils.isEmpty(order.getPostno())){
            checkPostState();
        }
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
//        if(TextUtils.isEmpty(postName.getText().toString())){
//            showToast(R.string.hint_logist_name_tip);
//            return;
//        }
        if(TextUtils.isEmpty(postNo.getText().toString())){
            showToast(R.string.hint_logist_num_tip);
            return;
        }
        if(!TextUtils.isEmpty(order.getInvoice_name())&&!confirm){
            showToast(R.string.has_send_invoice_tip);
            return;
        }
        String postname = postName.getText().toString();
        if(!TextUtils.isEmpty(postname)){
            SPUtils.put(POST_NAME,postname);
        }
        postWares();
    }
    @Event(R.id.check_layout)
    private void check(View view){
        checkImage.setImageResource(R.mipmap.checked);
        confirm = true;
    }

    @Event(value = R.id.invoice_layout,type=View.OnLongClickListener.class)
    private boolean  invoice_layout(View view) {
        StringBuilder stringBuilder = new StringBuilder(order.getInvoice_name());
        if(INVOICE_ELECT.equals(order.getInvoice_medium())){
            stringBuilder.append(order.getInvoice_email());
        }
        if(INVOICE_COMPANY.equals(order.getInvoice_type())){
            stringBuilder.append(order.getInvoice_number());
        }
        copy(stringBuilder.toString());
        return true;
    }
    @Event(value = R.id.logistics_layout,type=View.OnLongClickListener.class)
    private boolean  logistics_layout(View view) {
        copy(order.getUname()+order.getUphone()+order.getUaddress());
        return true;
    }
    private void copy(String content){
        //获取剪贴板管理器：
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        // 创建普通字符型ClipData
        ClipData mClipData = ClipData.newPlainText("Label", content);
        // 将ClipData内容放到系统剪贴板里。
        cm.setPrimaryClip(mClipData);
        showToast(R.string.clipboard_tip);
    }
    @Event(R.id.scan)
    private void scan(View view){
        Intent intent = new Intent(this, CaptureActivity.class);
        startActivityForResult(intent, Constants.REQ_QR_CODE);
    }
    @Event(R.id.back)
    private void back(View view){
        finish();
    }
    private void postWares() {
        save.setEnabled(false);
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.SHOPKEEPER);
        params.addParameter("action", "modify");
        params.addParameter("trade_no", order.getTrade_no());
        params.addParameter("phone", order.getUphone());
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
                    PostState postState = new PostState();
                    postState.setOrder(order);
                    EventBus.getDefault().post(postState);
//                    PostSuccess postSuccess = new PostSuccess();
//                    postSuccess.setOid(order.getId());
                    checkPostState();
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
    private void checkPostState(){
        TextView textView = IlessonApp.getInstance().getTextView();
        SPUtils.put(order.getTransaction_id(),"true");
        if(null!=textView){
            textView.setEnabled(false);
            textView.setText(R.string.has_post);
            textView.setTextColor(textView.getContext().getResources().getColor(R.color.gray_text333_color));
            textView.setBackgroundResource(R.drawable.general_gray_btn_corner20_selector);
            IlessonApp.getInstance().setTextView(null);
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQ_QR_CODE && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String result = bundle.getString(Constants.INTENT_EXTRA_KEY_QR_SCAN);
            postNo.setText(result);
//            loadData(result);
            Log.d(TAG, "onActivityResult: "+result);
        }
    }
    private void loadData(String no) {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.EXPRESS);
        params.addParameter("action", "query");
        params.addParameter("no", no);
//        params.addParameter("test", "true");
        Log.d(TAG, "loadData: " + params.toString());
        showProgress();
        x.http().post(params, new Callback.CacheCallback<String>() {
            @Override
            public boolean onCache(String result) {
                return false;
            }

            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: " + result);
                try {
                    BaseCode<Express> base = new Gson().fromJson(
                            result,
                            new TypeToken<BaseCode<Express>>() {
                            }.getType());
                    if (base.getCode() == 0) {
                        Express data = base.getData();
                        if (null == data) {
                            return;
                        }
                        if (!TextUtils.isEmpty(data.getTypename())) {
                            postName.setText(data.getTypename());
                        }
                        if (!TextUtils.isEmpty(data.getNumber())) {
                            postNo.setText(data.getNumber());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
                hideProgress();
            }
        });
    }
}
