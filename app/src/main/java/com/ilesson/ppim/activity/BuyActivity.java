package com.ilesson.ppim.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.AddressInfo;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.ExchangeAddress;
import com.ilesson.ppim.entity.InvoiceInfo;
import com.ilesson.ppim.entity.Options;
import com.ilesson.ppim.entity.PayOrder;
import com.ilesson.ppim.entity.PaySuccess;
import com.ilesson.ppim.entity.Produce;
import com.ilesson.ppim.entity.Produces;
import com.ilesson.ppim.entity.Shop;
import com.ilesson.ppim.entity.WaresDetialData;
import com.ilesson.ppim.utils.BigDecimalUtil;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.view.RoundImageView;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.rong.eventbus.EventBus;

import static com.ilesson.ppim.activity.InvoiceActivity.COMPANY_MEDIUM;
import static com.ilesson.ppim.activity.InvoiceActivity.COMPANY_NAME;
import static com.ilesson.ppim.activity.InvoiceActivity.COMPANY_NUM;
import static com.ilesson.ppim.activity.InvoiceActivity.EMAIL_NAME;
import static com.ilesson.ppim.activity.InvoiceActivity.INVOICE_CANCEL;
import static com.ilesson.ppim.activity.InvoiceActivity.INVOICE_DATA;
import static com.ilesson.ppim.activity.InvoiceActivity.INVOICE_ELECT;
import static com.ilesson.ppim.activity.InvoiceActivity.INVOICE_MODIFY;
import static com.ilesson.ppim.activity.InvoiceActivity.INVOICE_MODIFY_SUCCESS;
import static com.ilesson.ppim.activity.InvoiceActivity.INVOICE_PERSON;
import static com.ilesson.ppim.activity.InvoiceActivity.PERSON_MEDIUM;
import static com.ilesson.ppim.activity.InvoiceActivity.PERSON_NAME;
import static com.ilesson.ppim.activity.LoginActivity.USER_PHONE;
import static com.ilesson.ppim.activity.WareDetailActivity.PRODUCT_ID;

/**
 * Created by potato on 2020/3/10.
 */
@ContentView(R.layout.activity_buy)
public class BuyActivity extends BaseActivity {

    @ViewInject(R.id.no_address_layout)
    private View noAddress;
    @ViewInject(R.id.address_layout)
    private View addressLayout;
    @ViewInject(R.id.tag)
    private TextView tag;
    @ViewInject(R.id.user_name)
    private TextView userName;
    @ViewInject(R.id.phone)
    private TextView phoneView;
    @ViewInject(R.id.address)
    private TextView addressView;
    @ViewInject(R.id.wares_name)
    private TextView waresName;
    @ViewInject(R.id.wares_quantity)
    private TextView quantity;
    @ViewInject(R.id.wares_score_price)
    private TextView unitPrice;
    @ViewInject(R.id.wares_price)
    private TextView waresPrice;
    @ViewInject(R.id.fei_price)
    private TextView feiPrice;
    @ViewInject(R.id.wares_img)
    private RoundImageView imageView;
    @ViewInject(R.id.minus)
    private View minus;
    @ViewInject(R.id.add)
    private View add;
    @ViewInject(R.id.num)
    private TextView selectNum;
    @ViewInject(R.id.exchange)
    private TextView exchangeBtn;
    @ViewInject(R.id.exchange_score_all)
    private TextView exchangeAll;
    @ViewInject(R.id.recylerview)
    private RecyclerView recyclerView;
    @ViewInject(R.id.invoice_layout)
    public View invoiceLayout;
    @ViewInject(R.id.no_invoice)
    public View noInvoiceLayout;
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
    public static final String ADDRESS_INFO = "address_info";
    public static final int SET_ADDRESS_SUCCESS_TO_USE = 2;
//    private ScoreInfo scoreInfo=new ScoreInfo();
    private int num = 1;
    private WaresDetialData waresDetialData;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarLightMode(this, true);
        EventBus.getDefault().register(this);
//        token = SPUtils.get(LOGIN_TOKEN,"");
        int pid = getIntent().getIntExtra(PRODUCT_ID,0);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        phone = SPUtils.get(USER_PHONE, "");
//        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
//            @Override
//            public int getSpanSize(int position) {
//                String content = selection.getName();
//
//                return content.length();
//            }
//        });
        recyclerView.setLayoutManager(layoutManager);
            loadData(pid);
    }

    private static final String TAG = "BuyActivity";

    @Event(value = R.id.back_btn)
    private void back_btn(View view) {
        finish();
    }

    @Event(value = R.id.no_invoice)
    private void no_invoice(View view) {
        setInvoice(null);
    }
    @Event(value = R.id.modify)
    private void modify(View view) {
        setInvoice(invoiceInfo);
    }

    @Event(value = R.id.exchange)
    private void exchange(View view) {
        if(addressInfo==null){
            showToast(R.string.no_address);
            return;
        }
        exchange();
    }

    @Event(value = R.id.minus)
    private void minus(View view) {
        if (num <= selection.getMin()||num<=1) {
            return;
        }
        num--;
        selectNum.setText(num + "");
        setAllScore();
    }

    @Event(value = R.id.add)
    private void add(View view) {
//        if (num >= mostNum) {
//            return;
//        }
        num++;
        Log.d(TAG, "add: " + num + "");
        selectNum.setText(num + "");
        setAllScore();
    }

    private void showInvoiceView(InvoiceInfo invoice){
        String pName = SPUtils.get(PERSON_NAME+phone, "");
        String cName = SPUtils.get(COMPANY_NAME+phone, "");
        String eName = SPUtils.get(EMAIL_NAME+phone, "");
        String cNum = SPUtils.get(COMPANY_NUM+phone, "");
        String pMedium = SPUtils.get(PERSON_MEDIUM+phone, "");
        String cMedium = SPUtils.get(COMPANY_MEDIUM+phone, "");
        if(null!=invoiceInfos&&!invoiceInfos.isEmpty()){
            for(InvoiceInfo invoiceInfo:invoiceInfos){
                if(INVOICE_PERSON.equals(invoiceInfo.getType())){
                    if(!TextUtils.isEmpty(pName)){
                        invoiceInfo.setName(pName);
                    }
                    if(!TextUtils.isEmpty(pMedium)){
                        invoiceInfo.setMedium(pMedium);
                    }
                }else{
                    if(!TextUtils.isEmpty(cName)){
                        invoiceInfo.setName(cName);
                    }
                    if(!TextUtils.isEmpty(cNum)){
                        invoiceInfo.setNumber(cNum);
                    }
                    if(!TextUtils.isEmpty(cMedium)){
                        invoiceInfo.setMedium(cMedium);
                    }
                }
                if(!TextUtils.isEmpty(eName)){
                    invoiceInfo.setEmail(eName);
                }
            }
        }
        invoiceLayout.setVisibility(View.GONE);
        noInvoiceLayout.setVisibility(View.GONE);
        if(null==invoice){
            noInvoiceLayout.setVisibility(View.VISIBLE);
        }else{
            invoiceLayout.setVisibility(View.VISIBLE);
            String title = invoice.getType().equals(INVOICE_PERSON)?getResources().getString(R.string.personal):getResources().getString(R.string.enterprise);
            invoiceType.setText(invoice.getMediumName());
            invoiceType1.setText(invoice.getMediumName());
            invoiceTitleType.setText(title);
            invoiceTitleName.setText(invoice.getName());
            invoicePrice.setText(total+"");
//            invoicePrice.setText(String.format(getResources().getString(R.string.format_yuan),smartOrder.getAllPrice()));
            if(invoice.getMedium().equals(INVOICE_ELECT)){
                invoiceEmail.setText(invoice.getEmail());
                emailLayout.setVisibility(View.VISIBLE);
            }else{
                emailLayout.setVisibility(View.GONE);
            }
        }
    }

    private void setInvoice(InvoiceInfo invoice){
        Intent invoiceIntent = new Intent(this,InvoiceActivity.class);
        invoiceIntent.putExtra(INVOICE_DATA,(Serializable)invoiceInfos);
        invoiceIntent.putExtra(INVOICE_MODIFY,invoice);
        startActivityForResult(invoiceIntent,0);
        overridePendingTransition(0, 0);
    }
    private double total;
    private double fei;

    public void setAllScore() {
        double waresp = Double.valueOf(num * selection.getPrice())/ 100;
        total = waresp+fei;
        String feiText = String.format(getResources().getString(R.string.rmb_format), BigDecimalUtil.format(Double.valueOf(total) / 100));
        String price = String.format(getResources().getString(R.string.rmb_format), BigDecimalUtil.format(waresp));
        String text = String.format(getResources().getString(R.string.all_format_rmb), total + "");
        int length = String.valueOf(total).length();
        waresPrice.setText(price);
        feiPrice.setText(feiText);
        SpannableStringBuilder style = new SpannableStringBuilder(text);
        style.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.helptext_color)), 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        style.setSpan(new ForegroundColorSpan(Color.RED), 3, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        style.setSpan(new RelativeSizeSpan(1.2f), 3, 3 + length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        exchangeAll.setText(style);
//        if (scoreInfo.getValue() < total) {
//            exchangeBtn.setBackgroundColor(getResources().getColor(R.color.btn_disenable));
//            exchangeBtn.setText(R.string.score_not_enough);
//            exchangeBtn.setEnabled(false);
//        } else {
//            exchangeBtn.setBackgroundResource(R.drawable.general_red_theme_selector);
//            exchangeBtn.setEnabled(true);
//            exchangeBtn.setText(R.string.exchange_now);
//        }
    }

    @Event(value = R.id.address_view)
    private void address_view(View view) {
        Intent intent = new Intent();
        if (addressInfo == null) {
            intent.setClass(this, AddressActivity.class);
        } else {
            intent.setClass(this, AddressListActivity.class);
        }
        intent.putExtra(BuyActivity.ADDRESS_INFO, true);
        startActivityForResult(intent, 0);
    }

    private void loadData(int id) {
        //https://pp.fangnaokeji.com:9443/pp/produce?action=selection&id=6
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.PRODUCE);
        params.addParameter("action", "selection");
        params.addParameter("id", id);
        String phone = SPUtils.get(USER_PHONE, "");
        params.addParameter("phone", phone);
        Log.d(TAG, "loadData: " + params.toString());
        showProgress();
        x.http().post(params, new Callback.CacheCallback<String>() {
            @Override
            public boolean onCache(String result) {
                readJson(result);
                return false;
            }

            @Override
            public void onSuccess(String result) {
                Log.d(TAG, " onSuccess: " + result);
                readJson(result);
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

    private void exchange() {
        //action=order&subid=12&num=1&aid=88&money=30000&invoice_type=0&invoice_name=陈茂&invoice_medium=0&invoice_email=jiwanger@outlook.com&invoice_number=431122198903
        //action=order&subid=11&num=1&money=0.03&aid=503&invoice_type=0&invoice_name=potato&invoice_medium=0&invoice_email=47523521990@qq.com&invoice_number=123654897x
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.PRODUCE);
        params.addParameter("action", "order");
        params.addParameter("subid", selection.getId());
        params.addParameter("num", num);
        params.addParameter("money", (int)(total*100));
        params.addParameter("aid", addressInfo.getId());
        if(null!=invoiceInfo){
            params.addParameter("invoice_type", invoiceInfo.getType());
            params.addParameter("invoice_medium", invoiceInfo.getMedium());
            params.addParameter("invoice_name", invoiceInfo.getName());
            params.addParameter("invoice_email", invoiceInfo.getEmail());
            params.addParameter("invoice_number", invoiceInfo.getNumber());
        }
        Log.d(TAG, "loadData: " + params.toString());
        showProgress();
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "exchange onSuccess: " + result);
                pay(result);
//                BaseCode base = new Gson().fromJson(
//                        result,
//                        new TypeToken<BaseCode>() {
//                        }.getType());
//                if (base.getCode() == 0) {
////                    scoreInfo.setValue(scoreInfo.getValue()-total);
////                    EventBus.getDefault().post(scoreInfo);
////                    showSuccessDialog();
//                } else {
//                    showToast(base.getMessage());
//                }
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
    private IWXAPI api;
    private PayReq req;

    private void pay(String result) {
        if (null == api) {
            api = WXAPIFactory.createWXAPI(this, getString(R.string.wx_key));
            api.registerApp(getString(R.string.wx_key));
        }
        req = new PayReq();
        try {
            BaseCode<PayOrder> base = new Gson().fromJson(
                    result,
                    new TypeToken<BaseCode<PayOrder>>() {
                    }.getType());
            if (base.getCode() == 0) {
                PayOrder order = base.getData();
                req.appId = order.getAppid();
                req.partnerId = order.getPartnerid();
                req.prepayId = order.getPrepayid();
                req.nonceStr = order.getNoncestr();
                req.timeStamp = order.getTimestamp();
                req.packageValue = "Sign=WXPay";
                req.sign = order.getSign();
                req.extData = "app data";
                api.sendReq(req);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private List<AddressInfo> addressInfos;
    private AddressInfo addressInfo;
    private Produce produce;
    private List<Produces> produces;
    private List<Options> selections;
    private Options selection;
    private Shop shop;
    private List<InvoiceInfo> invoiceInfos;
    private void readJson(String json) {
        try {
            BaseCode<WaresDetialData> base = new Gson().fromJson(
                    json,
                    new TypeToken<BaseCode<WaresDetialData>>() {
                    }.getType());
            if (base.getCode() == 0) {
                waresDetialData = base.getData();
                addressInfos = waresDetialData.getAddress();
                shop = waresDetialData.getShop();
                if (addressInfos == null || addressInfos.isEmpty()) {
                    return;
                }
                addressInfo = addressInfos.get(0);
                showAdress();
//                List<Produces> produces = scoreData.getProduces();
//                produce = waresDetialData.getProduce();
                selections = waresDetialData.getOptions();
                produce = waresDetialData.getProduce();
                invoiceInfos = waresDetialData.getInvoice();
//                List<Selections> data = new ArrayList<>();
//                data.addAll(selections);
//                data.addAll(selections);
//                data.addAll(selections);
//                for (int i = 0; i < data.size(); i++) {
//                    Random random = new Random();
//                    String name = arr[random.nextInt(arr.length)];
//                    data.get(i).setName(name);
//                }
                RefreshAdapter adapter = new RefreshAdapter(selections);
                recyclerView.setAdapter(adapter);
                showProduce(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private int mostNum;

    private void showProduce(int index) {
        total = 0;
        selection = selections.get(index);
//        mostNum = scoreInfo.getValue() / selection.getScoreget();
        Glide.with(getApplicationContext()).load(selection.getImage()).into(imageView);
        waresName.setText(produce.getName());
        quantity.setText(selection.getName() + "/" + selection.getUnit());
        String price = String.format(getResources().getString(R.string.rmb_format), BigDecimalUtil.format(Double.valueOf(num * selection.getPrice()) / 100));
//        String text = String.format(getResources().getString(R.string.rmb_format), selection.getPrice()+"");
        int length = String.valueOf(selection.getPrice()).length();
        SpannableStringBuilder style = new SpannableStringBuilder(price);
//        style.setSpan(new RelativeSizeSpan(1.2f), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        unitPrice.setText(style);
//        scorePrice.setText(String.format(getResources().getString(R.string.score_price),selection.getScoreget()));
        num = selection.getMin();
        selectNum.setText(num + "");
        setAllScore();
    }

    private InvoiceInfo invoiceInfo;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: resultCode=" + resultCode);
        if (resultCode == INVOICE_MODIFY_SUCCESS) {
            invoiceInfo = (InvoiceInfo) data.getSerializableExtra(INVOICE_MODIFY);
            for(InvoiceInfo info:invoiceInfos){
                if(info.getType().equals(invoiceInfo.getType())){
                    info.setMedium(invoiceInfo.getMedium());
                    info.setTypeName(invoiceInfo.getTypeName());
                    info.setType(invoiceInfo.getType());
                    info.setName(invoiceInfo.getName());
                    info.setMediumName(invoiceInfo.getMediumName());
                    if(!TextUtils.isEmpty(invoiceInfo.getEmail())) {
                        info.setEmail(invoiceInfo.getEmail());
                    }
                    if(!TextUtils.isEmpty(invoiceInfo.getNumber())) {
                        info.setNumber(invoiceInfo.getNumber());
                    }
                    if(!TextUtils.isEmpty(invoiceInfo.getEmail())) {
                        info.setEmail(invoiceInfo.getEmail());
                    }
                }
            }
            if(invoiceInfos==null||invoiceInfos.isEmpty()){
                invoiceInfos = new ArrayList<>();
                invoiceInfos.add(invoiceInfo);
            }
            showInvoiceView(invoiceInfo);
            return;
        }
        if (resultCode == INVOICE_CANCEL) {
//            invoiceTag=null;
            showInvoiceView(null);
            return;
        }
        if (resultCode == SET_ADDRESS_SUCCESS_TO_USE) {
            if (data != null) {
                addressInfo = (AddressInfo) data.getSerializableExtra(ADDRESS_INFO);
                showAdress();
            }
        }
    }

    private void showAdress() {
        if(null==noAddress){
            return;
        }
        if (null == addressInfo) {
            noAddress.setVisibility(View.VISIBLE);
            addressLayout.setVisibility(View.GONE);
        } else {
            noAddress.setVisibility(View.GONE);
            addressLayout.setVisibility(View.VISIBLE);
            tag.setText(addressInfo.getTag());
            userName.setText(addressInfo.getName());
            phoneView.setText(addressInfo.getPhone());
            addressView.setText(addressInfo.getAddress());
        }
    }

    public void onEventMainThread(ExchangeAddress var) {
        addressInfo = var.getAddressInfo();
        showAdress();
    }
    public void onEventMainThread(PaySuccess var) {
        startActivity(new Intent(this,WareOrderListActivity.class));
        finish();
    }
    class RefreshAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        List<Options> datas = new ArrayList<>();

        private static final int TYPE_ITEM = 1;
        private int mLoadMoreStatus = 0;

        public RefreshAdapter(List<Options> data) {
            this.datas = data;
        }


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = getLayoutInflater().inflate(R.layout.score_section, parent, false);
            return new ItemViewHolder(itemView);
        }


        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            //绑定数据
            if (holder instanceof ItemViewHolder) {
                ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
                Options scoreInfo = datas.get(position);
                if (0 == position) {
                    lastItem = itemViewHolder.textView;
                    itemViewHolder.textView.setBackgroundResource(R.drawable.score_selection_bg);
                    itemViewHolder.textView.setTextColor(getResources().getColor(R.color.theme_color));
                }
                itemViewHolder.textView.setText(scoreInfo.getName());
            }
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }

        @Override
        public int getItemViewType(int position) {
            return TYPE_ITEM;
        }

        private void setDefaultView(TextView textView) {
            if (null == textView) {
                return;
            }
            textView.setBackgroundResource(R.drawable.background_gray_corner20);
            textView.setTextColor(getResources().getColor(R.color.gray_text333_color));
        }

        private void setSelectView(TextView textView) {
            textView.setBackgroundResource(R.drawable.score_selection_bg);
            textView.setTextColor(getResources().getColor(R.color.theme_color));
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {

            TextView textView;

            public ItemViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.selecion_item);
                Log.d(TAG, "ItemViewHolder:getLayoutPosition= " + getLayoutPosition());

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (lastItem == textView) {
                            return;
                        }
                        setDefaultView(lastItem);
                        setSelectView(textView);
                        lastItem = textView;
                        showProduce(getLayoutPosition());
                    }
                });
            }
        }

        private TextView lastItem;
    }

//    private void showSuccessDialog(){
//        View view = getLayoutInflater().inflate(R.layout.privacy_dialog,null);
//        final AlertDialog dialog = new AlertDialog.Builder(this)
//                .setView(view).create();
//        TextView title = (TextView) view.findViewById(R.id.title);
//        title.setText(R.string.exchange_success);
//        TextView scoreTv = (TextView) view.findViewById(R.id.content);
//        String tips = String.format(getResources().getString(R.string.exchange_detail),shop.getName(),selection.getName(),num,selection.getUnit());
//        scoreTv.setText(tips);
//        TextView left = (TextView) view.findViewById(R.id.left_btn);
//        TextView right = (TextView) view.findViewById(R.id.right_btn);
//        left.setText(R.string.scan_ordre);
//        right.setText(R.string.continue_exchange);
//        dialog.setCanceledOnTouchOutside(false);
//        scoreTv.setMovementMethod(LinkMovementMethod.getInstance());
//        left.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//                startActivity(new Intent(BuyActivity.this,WareOrderListActivity.class));
//                finish();
//            }
//        });
//        right.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//                finish();
//            }
//        });
//        dialog.show();
//    }
}
