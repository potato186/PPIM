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
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.Express;
import com.ilesson.ppim.entity.LogistBase;
import com.ilesson.ppim.entity.LogistReg;
import com.ilesson.ppim.entity.PostState;
import com.ilesson.ppim.entity.WaresOrder;
import com.ilesson.ppim.utils.BigDecimalUtil;
import com.ilesson.ppim.utils.Constants;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.rong.eventbus.EventBus;

import static com.ilesson.ppim.activity.InvoiceActivity.INVOICE_COMPANY;
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
    private LogistBase logistBase;
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
        logistBase = new Gson().fromJson(json,LogistBase.class);
        Log.d(TAG, "onCreate: "+logistBase);
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
            showToast(R.string.has_send_invoice_tip);
            return;
        }
        modify();
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
    private void modify() {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQ_QR_CODE && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String result = bundle.getString(Constants.INTENT_EXTRA_KEY_QR_SCAN);
            postNo.setText(result);

            loadData(result);
            Log.d(TAG, "onActivityResult: "+result);
        }
    }
    private void checkLogist(String result){
        List<LogistReg> companyReturnList = logistBase.getCompanyReturnList();
        for (LogistReg logistReg : companyReturnList) {
            Pattern p = Pattern.compile(logistReg.getReg_mail_no());  //正则表达式
            Matcher m = p.matcher(result);
            if(m.matches()){
                postName.setText(logistReg.getName());
                break;
            }
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
    public static String json = "{\n" +
            "  \"companyReturnList\": [\n" +
            "    {\n" +
            "      \"code\": \"ZJS\",\n" +
            "      \"id\": \"103\",\n" +
            "      \"name\": \"宅急送\",\n" +
            "      \"reg_mail_no\": \"^[a-zA-Z0-9]{10}$|^(42|16)[0-9]{8}$|^A[0-9]{12}\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"SF\",\n" +
            "      \"id\": \"505\",\n" +
            "      \"name\": \"顺丰速运\",\n" +
            "      \"reg_mail_no\": \"^[A-Za-z0-9-]{4,35}$\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"STO\",\n" +
            "      \"id\": \"100\",\n" +
            "      \"name\": \"申通快递\",\n" +
            "      \"reg_mail_no\": \"^(888|588|688|468|568|668|768|868|968)[0-9]{9}$|^(11|22)[0-9]{10}$|^(STO)[0-9]{10}$|^(37|33|11|22|44|55|66|77|88|99)[0-9]{11}$|^(4)[0-9]{11}$\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"EMS\",\n" +
            "      \"id\": \"2\",\n" +
            "      \"name\": \"EMS\",\n" +
            "      \"reg_mail_no\": \"^[A-Z]{2}[0-9]{9}[A-Z]{2}$|^(10|11)[0-9]{11}$|^(50|51)[0-9]{11}$|^(95|97)[0-9]{11}$\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"YUNDA\",\n" +
            "      \"id\": \"102\",\n" +
            "      \"name\": \"韵达快递\",\n" +
            "      \"reg_mail_no\": \"^(10|11|12|13|14|15|16|17|19|18|50|55|58|80|88|66|31|77|39)[0-9]{11}$|^[0-9]{13}$\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"ZTO\",\n" +
            "      \"id\": \"500\",\n" +
            "      \"name\": \"中通快递\",\n" +
            "      \"reg_mail_no\": \"^((768|765|778|828|618|680|518|528|688|010|880|660|805|988|628|205|717|718|728|761|762|763|701|757|719|751|358|100|200|118|128|689|738|359|779|852)[0-9]{9})$|^((5711|2008|7380|1180|2009|2013|2010|1000|1010)[0-9]{8})$|^((8010|8021|8831|8013)[0-9]{6})$|^((1111|90|36|11|50|53|37|39|91|93|94|95|96|98)[0-9]{10})$|^((a|b|h)[0-9]{13})$|^((90|80|60)[0-9]{7})$|^((80|81)[0-9]{6})$|^((21|23|24|25|93|94|95|96|97|110|111|112|113|114|115|116|117|118|119|121|122|123|124|125|126|127|128|129|130|131)[0-9]{8})$|^(100|101|102|103|104|105|106|107|503|504|505|506|507)[0-9]{10}$|^(4)[0-9]{11}$|^(120)[0-9]{9}$|^(780)[0-9]{9}$|^(881)[0-9]{9}$|^(882|885)[0-9]{9}$|^(91|92)[0-9]{10}$|^(54|55|56)[0-9]{10}$|^(63)[0-9]{10}$|^(7)[0-9]{9}$|^(64)[0-9]{10}$|^(72)[0-9]{10}$|^(220|221|223|224|225|226|227|228|229)[0-9]{7}$|^(21|22|23|24|25|26|27|28|29)[0-9]{10}$|^3[0-9]{9}$|^2710[0-9]{11}$|^731[0-9]{11}$|^751[0-9]{11}$|^7320[0-9]{10}$\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"HTKY\",\n" +
            "      \"id\": \"502\",\n" +
            "      \"name\": \"百世快递\",\n" +
            "      \"reg_mail_no\": \"^((A|B|D|E)[0-9]{12})$|^(BXA[0-9]{10})$|^(K8[0-9]{11})$|^(02[0-9]{11})$|^(000[0-9]{10})$|^(C0000[0-9]{8})$|^((21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|36|37|38|39|61|63)[0-9]{10})$|^((50|51)[0-9]{12})$|^7[0-9]{13}$|^6[0-9]{13}$|^58[0-9]{14}$\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"YTO\",\n" +
            "      \"id\": \"101\",\n" +
            "      \"name\": \"圆通速递\",\n" +
            "      \"reg_mail_no\": \"^[A-Za-z0-9]{2}[0-9]{10}$|^[A-Za-z0-9]{2}[0-9]{8}$|^[6-9][0-9]{17}$|^[DD]{2}[8-9][0-9]{15}$|^[Y][0-9]{12}$\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"QFKD\",\n" +
            "      \"id\": \"1216\",\n" +
            "      \"name\": \"全峰快递\",\n" +
            "      \"reg_mail_no\": \"^[0-6|9][0-9]{11}$|^[7][0-8][0-9]{10}$|^[0-9]{15}$|^[S][0-9]{9,11}(-|)P[0-9]{1,2}$|^[0-9]{13}$|^[8][0,2-9][0,2-9][0-9]{9}$|^[8][1][0,2-9][0-9]{9}$|^[8][0,2-9][0-9]{10}$|^[8][1][1][0][8][9][0-9]{6}$\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"TTKDEX\",\n" +
            "      \"id\": \"504\",\n" +
            "      \"name\": \"天天快递\",\n" +
            "      \"reg_mail_no\": \"^[0-9]{12}$\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"EYB\",\n" +
            "      \"id\": \"3\",\n" +
            "      \"name\": \"EMS经济快递\",\n" +
            "      \"reg_mail_no\": \"^[A-Z]{2}[0-9]{9}[A-Z]{2}$|^(10|11)[0-9]{11}$|^(50|51)[0-9]{11}$|^(95|97)[0-9]{11}$\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"UC\",\n" +
            "      \"id\": \"1207\",\n" +
            "      \"name\": \"优速快递\",\n" +
            "      \"reg_mail_no\": \"^VIP[0-9]{9}|V[0-9]{11}|[0-9]{12}$|^LBX[0-9]{15}-[2-9AZ]{1}-[1-9A-Z]{1}$|^(9001)[0-9]{8}$\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"DBKD\",\n" +
            "      \"id\": \"5000000110730\",\n" +
            "      \"name\": \"德邦快递\",\n" +
            "      \"reg_mail_no\": \"^[5789]\\\\d{9}$\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"GTO\",\n" +
            "      \"id\": \"200143\",\n" +
            "      \"name\": \"国通快递\",\n" +
            "      \"reg_mail_no\": \"^(3(([0-6]|[8-9])\\\\d{8})|((2|4|5|6)\\\\d{9})|(7(?![0|1|2|3|4|5|7|8|9])\\\\d{9})|(8(?![2-9])\\\\d{9})|(2|4)\\\\d{11})$\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"SURE\",\n" +
            "      \"id\": \"201174\",\n" +
            "      \"name\": \"速尔快递\",\n" +
            "      \"reg_mail_no\": \"^(SUR)[0-9]{12}$|^[0-9]{12}$\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"FEDEX\",\n" +
            "      \"id\": \"106\",\n" +
            "      \"name\": \"联邦快递\",\n" +
            "      \"reg_mail_no\": \"^[0-9]{12}$\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"SHQ\",\n" +
            "      \"id\": \"108\",\n" +
            "      \"name\": \"华强物流\",\n" +
            "      \"reg_mail_no\": \"^[A-Za-z0-9]*[0|2|4|6|8]$\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"UAPEX\",\n" +
            "      \"id\": \"1259\",\n" +
            "      \"name\": \"全一快递\",\n" +
            "      \"reg_mail_no\": \"^\\\\d{12}|\\\\d{11}$\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"HOAU\",\n" +
            "      \"id\": \"1191\",\n" +
            "      \"name\": \"天地华宇\",\n" +
            "      \"reg_mail_no\": \"^[A-Za-z0-9]{8,9}$\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"BEST\",\n" +
            "      \"id\": \"105\",\n" +
            "      \"name\": \"百世物流\",\n" +
            "      \"reg_mail_no\": \"^[0-9]{11,12}$\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"LB\",\n" +
            "      \"id\": \"1195\",\n" +
            "      \"name\": \"龙邦速递\",\n" +
            "      \"reg_mail_no\": \"^[0-9]{12}$|^LBX[0-9]{15}-[2-9AZ]{1}-[1-9A-Z]{1}$|^[0-9]{15}$|^[0-9]{15}-[1-9A-Z]{1}-[1-9A-Z]{1}$\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"XB\",\n" +
            "      \"id\": \"1186\",\n" +
            "      \"name\": \"新邦物流\",\n" +
            "      \"reg_mail_no\": \"^[0-9]{8}$|^[0-9]{10}$\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"FAST\",\n" +
            "      \"id\": \"1204\",\n" +
            "      \"name\": \"快捷快递\",\n" +
            "      \"reg_mail_no\": \"^(?!440)(?!510)(?!520)(?!5231)([0-9]{9,13})$|^(P330[0-9]{8})$|^(D[0-9]{11})$|^(319)[0-9]{11}$|^(56)[0-9]{10}$|^(536)[0-9]{9}$\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"POSTB\",\n" +
            "      \"id\": \"200734\",\n" +
            "      \"name\": \"邮政快递包裹\",\n" +
            "      \"reg_mail_no\": \"^([GA]|[KQ]|[PH]){2}[0-9]{9}([2-5][0-9]|[1][1-9]|[6][0-5])$|^[99]{2}[0-9]{11}$|^[96]{2}[0-9]{11}$|^[98]{2}[0-9]{11}$\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"NEDA\",\n" +
            "      \"id\": \"1192\",\n" +
            "      \"name\": \"能达速递\",\n" +
            "      \"reg_mail_no\": \"^((88|)[0-9]{10})$|^((1|2|3|5|)[0-9]{9})$|^(90000[0-9]{7})$\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"BJRFD-001\",\n" +
            "      \"id\": \"100034107\",\n" +
            "      \"name\": \"如风达配送\",\n" +
            "      \"reg_mail_no\": \"^[\\\\x21-\\\\x7e]{1,100}$\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"DBL\",\n" +
            "      \"id\": \"107\",\n" +
            "      \"name\": \"德邦物流\",\n" +
            "      \"reg_mail_no\": \"^[5789]\\\\d{9}$\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"YCT\",\n" +
            "      \"id\": \"1185\",\n" +
            "      \"name\": \"黑猫宅急便\",\n" +
            "      \"reg_mail_no\": \"^[0-9]{12}$\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"LTS\",\n" +
            "      \"id\": \"1214\",\n" +
            "      \"name\": \"联昊通\",\n" +
            "      \"reg_mail_no\": \"^[0-9]{9,12}$\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"CNEX\",\n" +
            "      \"id\": \"1056\",\n" +
            "      \"name\": \"佳吉快递\",\n" +
            "      \"reg_mail_no\": \"^[7,1,9][0-9]{9}$\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"HZABC\",\n" +
            "      \"id\": \"1121\",\n" +
            "      \"name\": \"飞远(爱彼西)配送\",\n" +
            "      \"reg_mail_no\": \"^[0-9]{10,11}$|^T[0-9]{10}$|^FYPS[0-9]{12}$|^LBX[0-9]{15}-[2-9AZ]{1}-[1-9A-Z]{1}$\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"XFWL\",\n" +
            "      \"id\": \"202855\",\n" +
            "      \"name\": \"信丰物流\",\n" +
            "      \"reg_mail_no\": \"^130[0-9]{9}|13[7-9]{1}[0-9]{9}|18[8-9]{1}[0-9]{9}$\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"ESB\",\n" +
            "      \"id\": \"200740\",\n" +
            "      \"name\": \"E速宝\",\n" +
            "      \"reg_mail_no\": \"[0-9a-zA-Z-]{5,20}\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"GDEMS\",\n" +
            "      \"id\": \"1269\",\n" +
            "      \"name\": \"广东EMS\",\n" +
            "      \"reg_mail_no\": \"^[a-zA-Z]{2}[0-9]{9}[a-zA-Z]{2}$\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"QRT\",\n" +
            "      \"id\": \"1208\",\n" +
            "      \"name\": \"增益速递\",\n" +
            "      \"reg_mail_no\": \"^[0-9]{12,13}$\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";
}
