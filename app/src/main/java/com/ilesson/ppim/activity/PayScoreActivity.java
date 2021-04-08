package com.ilesson.ppim.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.IlessonApp;
import com.ilesson.ppim.R;
import com.ilesson.ppim.custom.TransferMessage;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.Currency;
import com.ilesson.ppim.entity.PPUserInfo;
import com.ilesson.ppim.entity.RongUserInfo;
import com.ilesson.ppim.utils.BigDecimalUtil;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.IMUtils;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.view.RoundImageView;
import com.ilesson.ppim.view.VirtualKeyboardView;

import org.xutils.common.Callback;
import org.xutils.common.util.MD5;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import io.rong.eventbus.EventBus;
import io.rong.imageloader.core.DisplayImageOptions;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.Conversation;

import static com.ilesson.ppim.activity.ConversationActivity.PAY_FIAL;
import static com.ilesson.ppim.activity.ConversationActivity.PAY_SUCCESS;
import static com.ilesson.ppim.activity.FriendDetailActivity.USER_INFO;
import static com.ilesson.ppim.activity.LoginActivity.LOGIN_PAY;
import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;
import static com.ilesson.ppim.activity.LoginActivity.USER_PHONE;
import static com.ilesson.ppim.fragment.FundFragment.CURRENCY;

@ContentView(R.layout.activity_pay_score)
public class PayScoreActivity extends BaseActivity {

    @ViewInject(R.id.virtualKeyboardView)
    private VirtualKeyboardView virtualKeyboardView;
    private GridView gridView;

    private ArrayList<Map<String, String>> valueList;

    @ViewInject(R.id.textAmount)
    private EditText textAmount;
    @ViewInject(R.id.pay_layout)
    private View payLayout;
    @ViewInject(R.id.pay_for_score)
    private TextView payScore;
    @ViewInject(R.id.trans)
    private TextView transBtn;
    @ViewInject(R.id.content)
    private TextView allScore;
    @ViewInject(R.id.select_currency)
    private TextView selectCurrency;
    @ViewInject(R.id.user_icon)
    private RoundImageView iconImageView;

    private Animation enterAnim;

    private Animation exitAnim;
    @ViewInject(R.id.name)
    private TextView nameTextView;
    @ViewInject(R.id.pay_for_user)
    private TextView payForUser;
    @ViewInject(R.id.code1)
    private EditText mCode1;
    @ViewInject(R.id.code2)
    private EditText mCode2;
    @ViewInject(R.id.code3)
    private EditText mCode3;
    @ViewInject(R.id.code4)
    private EditText mCode4;
    @ViewInject(R.id.code5)
    private EditText mCode5;
    @ViewInject(R.id.code6)
    private EditText mCode6;
    private EditText[] mCodes;
    private PPUserInfo ppUserInfo;
    public static final String TARGET_ID = "target_id";
    public static final String PAY_MONEY = "pay_money";
    public static final String PAY_DECS = "pay_decs";
    private String targetId;
    public static final String QR_PAY = "qr_pay";
    public static final String PAY_TYPE = "pay_type";
    public static final int TRANSFER_TYPE = 0;
    public static final int REDPACKET_TYPE = 1;
    private boolean isPQPay;
    private int payType;
    private List<Currency> datas = new ArrayList<>();
    private Currency currency = new Currency();
    private String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarLightMode(this, true);
        token = SPUtils.get("token","");
        Intent intent = getIntent();
        targetId = intent.getStringExtra(TARGET_ID);
        isPQPay = intent.getBooleanExtra(QR_PAY, false);
        payType = intent.getIntExtra(PAY_TYPE,0);
        Bundle bundle = intent.getExtras();
        if (TextUtils.isEmpty(targetId)) {
            ppUserInfo = (PPUserInfo) bundle.getSerializable(USER_INFO);
            targetId = ppUserInfo.getPhone();

            setUserInfo();
        } else {
            search(targetId);
        }
        currency = (Currency) bundle.getSerializable(CURRENCY);
        datas = IlessonApp.getInstance().getCurrecys();
//        datas = (List<Currency>) intent.getSerializableExtra(CURRENCY_LIST);
        if(null==currency&&datas!=null){
//            requestList();
            for(Currency c:datas){
                if(c.isDefault()){
                    currency = c;
                    break;
                }
            }
            if(null==currency){
                currency = datas.get(0);
            }
        }
        if(null==currency){
            currency = new Currency();
        }
        setBalance();
//        int score = SPUtils.get(USER_MONEY, 0);
//        allScore.setText(String.format(getResources().getString(R.string.all_score), score));
        mCodes = new EditText[]{mCode1, mCode2, mCode3, mCode4, mCode5, mCode6};
        for (int i = 0; i < mCodes.length; i++) {
            EditText editText = mCodes[i];
            editText.setCursorVisible(false);
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
        }
        initAnim();
        initView();
        valueList = virtualKeyboardView.getValueList();
        boolean isPay = SPUtils.get(LOGIN_PAY, false);
        if(!isPay){
            startActivity(new Intent(this, PayPwdActivity.class));
        }
        textAmount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                textAmount.setSelection(textAmount.getText().toString().length());
            }
        });
    }

    private void setBalance(){
        if(null!=currency){
            selectCurrency.setText(currency.getCurrency()+"("+"剩余:"+BigDecimalUtil.format(currency.getBalance())+")");
        }
    }

    private void setUserInfo() {
        nameTextView.setText(ppUserInfo.getName());
        payForUser.setText(getResources().getString(R.string.trans_score_to) + ppUserInfo.getName());
        DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
        builder.cacheInMemory(true).cacheOnDisk(true);
        ImageLoader.getInstance().displayImage(ppUserInfo.getIcon(), iconImageView,
                builder.build());
//        Glide.with(getApplicationContext()).load(ppUserInfo.getIcon()).into(iconImageView);
    }

    private void addChar(String str) {
        if (!TextUtils.isEmpty(mCodes[mCodes.length - 1].getText().toString())) {
            return;
        }
        for (int i = 0; i < mCodes.length; i++) {
            if (TextUtils.isEmpty(mCodes[i].getText().toString())) {
                mCodes[i].setText(str);
                break;
            }
        }
        if (!TextUtils.isEmpty(mCodes[mCodes.length - 1].getText().toString())) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < mCodes.length; i++) {
                stringBuilder.append(mCodes[i].getText().toString());
            }
            trans(stringBuilder.toString());
        }
    }

    private void clearPwd() {
        for (int i = 0; i < mCodes.length; i++) {
            mCodes[i].setText("");
        }
    }

    private void deleteChar() {
        for (int i = mCodes.length - 1; i >= 0; i--) {
            if (!TextUtils.isEmpty(mCodes[i].getText().toString())) {
                mCodes[i].setText("");
                break;
            }
        }
    }

    private void initAnim() {

        enterAnim = AnimationUtils.loadAnimation(this, R.anim.push_bottom_in);
        exitAnim = AnimationUtils.loadAnimation(this, R.anim.push_bottom_out);
    }

    private void initView() {
        // 设置不调用系统键盘
        if (android.os.Build.VERSION.SDK_INT <= 10) {
            textAmount.setInputType(InputType.TYPE_NULL);
        } else {
            this.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            try {
                Class<EditText> cls = EditText.class;
                Method setShowSoftInputOnFocus;
                setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus",
                        boolean.class);
                setShowSoftInputOnFocus.setAccessible(true);
                setShowSoftInputOnFocus.invoke(textAmount, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        virtualKeyboardView.getLayoutBack().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                virtualKeyboardView.startAnimation(exitAnim);
                virtualKeyboardView.setVisibility(View.GONE);
            }
        });

        gridView = virtualKeyboardView.getGridView();
        gridView.setOnItemClickListener(onItemClickListener);

        textAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                virtualKeyboardView.setFocusable(true);
                virtualKeyboardView.setFocusableInTouchMode(true);

                virtualKeyboardView.startAnimation(enterAnim);
                virtualKeyboardView.setVisibility(View.VISIBLE);
            }
        });

    }

    @Event(value = R.id.trans)
    private void confrim(View view) {
        score = textAmount.getText().toString();
        if(TextUtils.isEmpty(score)){
            return;
        }
        DecimalFormat decimalFormat = new DecimalFormat("###################.###");
        score = decimalFormat.format(Double.valueOf(score));
        if(Double.valueOf(score)>currency.getBalance()){
            showDialog(currency.getCurrency());
            return;
        }
        if (TextUtils.isEmpty(score)) {
            Toast.makeText(this, R.string.input_score_hint, Toast.LENGTH_LONG).show();
            return;
        }
        if (Double.valueOf(score)<=0) {
            Toast.makeText(this, R.string.input_score_error, Toast.LENGTH_LONG).show();
            return;
        }
        showPay();
    }

    private void showPay(){
        clearPwd();
        payScore.setText(score);
        payLayout.setVisibility(View.VISIBLE);
    }
    private void add(View view) {
        TextView textView = (TextView) view;
        String score = textAmount.getText().toString();
        if(score.contains(".")&&score.substring(score.indexOf(".")).length()==4){
            showToast("小数点后不能超过3位");
            return;
        }
        String content = score + textView.getText();
        textAmount.setText(content);
        textAmount.setSelection(content.length());
    }
    private void checkFirst(View view){
        String score = textAmount.getText().toString();
        if(score.equals("0")){
            textAmount.setText("");
        }
        add(view);
    }
    @Event(R.id.one)
    private void one(View view) {
        checkFirst(view);
    }

    @Event(R.id.two)
    private void two(View view) {
        checkFirst(view);
    }

    @Event(R.id.three)
    private void three(View view) {
        checkFirst(view);
    }

    @Event(R.id.four)
    private void four(View view) {
        checkFirst(view);
    }

    @Event(R.id.five)
    private void five(View view) {
        checkFirst(view);
    }

    @Event(R.id.six)
    private void six(View view) {
        checkFirst(view);
    }

    @Event(R.id.seven)
    private void seven(View view) {
        checkFirst(view);
    }

    @Event(R.id.eight)
    private void eight(View view) {
        checkFirst(view);
    }

    @Event(R.id.nine)
    private void nine(View view) {
        checkFirst(view);
    }
    @Event(R.id.dot)
    private void dot(View view) {
        String score = textAmount.getText().toString();
        if(TextUtils.isEmpty(score)){
            textAmount.setText("0");
        }
        if(score.contains(".")){
            return;
        }
        add(view);
    }

    @Event(R.id.zero)
    private void zero(View view) {
        String score = textAmount.getText().toString();
        if(score.equals("0")){
            return;
        }
        add(view);
    }

    @Event(R.id.back_btn)
    private void back(View v) {
        finish();
    }
    @Event(R.id.currency_view)
    private void select_currency(View v) {
        showSelectCurrencyDialog();
    }

    @Event(R.id.delete)
    private void delete(View v) {
        String text = textAmount.getText().toString();
        if (!TextUtils.isEmpty(text)) {
            String currentText = text.substring(0, text.length() - 1);
            textAmount.setText(currentText);
            textAmount.setSelection(currentText.length());
        }
    }

    @Event(R.id.close)
    private void close(View v) {
        payLayout.setVisibility(View.GONE);
    }

    private static final String TAG = "PayScoreActivity";
    private String score;
    private void trans(String pwd) {
        score = textAmount.getText().toString();
        if(TextUtils.isEmpty(score)){
            return;
        }
        if(Double.valueOf(score)>currency.getBalance()){
            showToast(R.string.no_balance);
            return;
        }
        showProgress();
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.BUSER_ASSET);
        params.addParameter("action", "transfer_v3");
        params.addParameter("token", token);
//        params.addParameter("authorization", SPUtils.get("bToken",""));
        params.addParameter("amount", score);
        params.addParameter("sender", SPUtils.get(USER_PHONE,""));
        params.addParameter("receiver", targetId);
        if (isPQPay) {
            params.addParameter("method", "1");
        } else {
            params.addParameter("method", "2");
        }
        params.addParameter("pwd", MD5.md5(pwd));
        params.addParameter("bak", "CN");
        params.addParameter("currency", currency.getCurrency());
        Log.d(TAG, "loadData: " + params.toString());
        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: " + result);
                BaseCode<String> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<String>>() {
                        }.getType());
                if (base.getCode() == 0) {
                    SPUtils.put("currency",currency.getCurrency());
//                    new IMUtils().connect(PayScoreActivity.this, token);
                    String name = "";
                    String icon = "";
                    if (null != ppUserInfo) {
                        name = ppUserInfo.getName();
                        icon = ppUserInfo.getIcon();
                    }
                    Intent intent = new Intent(PayScoreActivity.this, PayResultActivity.class);
                    intent.putExtra(PayResultActivity.PAY_USER_NAME, name);
                    intent.putExtra(PayResultActivity.PAY_USER_ICON, icon);
                    intent.putExtra(PayResultActivity.PAY_MONEY, score);
                    if (!isPQPay) {
                        new IMUtils().sendTransfer(targetId, score, currency.getCurrency());
                    }
                    double balance = BigDecimalUtil.sub(currency.getBalance(),Double.valueOf(score)).doubleValue();
                    currency.setBalance(balance);
                    TransferMessage transferMessage = new TransferMessage();
                    transferMessage.setExtra(currency.getCurrency());
                    EventBus.getDefault().post(transferMessage);
                    startActivityForResult(intent, 0);
                } else if(base.getCode()==6){
                    showPwdDialog();
                    payLayout.setVisibility(View.GONE);
                }else{
                    payLayout.setVisibility(View.GONE);
                    Toast.makeText(PayScoreActivity.this, base.getMessage(), Toast.LENGTH_LONG).show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(isPQPay){
            startActivity(new Intent(this,MainActivity.class));
        }else{
            EventBus.getDefault().post(new Conversation());
            RongIM.getInstance().startConversation(this, Conversation.ConversationType.PRIVATE,targetId,ppUserInfo.getName());
        }
        setSuccessInfo();
    }

    private void setSuccessInfo() {
        Intent intent = new Intent();
        intent.putExtra(PAY_MONEY, score);
        setResult(PAY_SUCCESS, intent);
        finish();
    }

    private void setFailInfo() {
        Intent intent = new Intent();
//        intent.putExtra(PAY_DECS,score);
        setResult(PAY_FIAL, intent);
    }
    private void search(final String userId) {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.RONG_URL);
        params.addBodyParameter("action", "info");
        String token = SPUtils.get(LOGIN_TOKEN, "");
        params.addBodyParameter("token", token);
        params.addBodyParameter("target", userId);
        Log.d(TAG, "searchUserInfo: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: =" + result);
                BaseCode<RongUserInfo> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<RongUserInfo>>() {
                        }.getType());
                if (base.getCode() == 0) {
                    RongUserInfo info = base.getData();
                    ppUserInfo = new PPUserInfo();
                    ppUserInfo.setIcon(info.getIcon());
                    ppUserInfo.setName(info.getName());
                    setUserInfo();
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
            }
        });
    }

    private Dialog mCountryDialog;

    private void showSelectCurrencyDialog() {
        mCountryDialog = new Dialog(this);
        mCountryDialog.setCanceledOnTouchOutside(false);
        Window window = mCountryDialog.getWindow();
        window.setGravity(Gravity.CENTER);
        View view = View.inflate(this, R.layout.currency_item_layout, null);
        view.findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissDialog();
            }
        });
        ListView listView = view.findViewById(R.id.country_list);
        listView.setAdapter(new CountryAdapter(datas));
        window.setContentView(view);
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);//设置横向全屏
        window.setBackgroundDrawableResource(android.R.color.transparent);
        mCountryDialog.setCanceledOnTouchOutside(false);
        mCountryDialog.show();
    }

    private void dismissDialog(){
        if(null!=mCountryDialog&&mCountryDialog.isShowing()){
            mCountryDialog.dismiss();
        }
    }
    private class CountryAdapter extends BaseAdapter {

        private List<Currency> data = new ArrayList<>();

        private CountryAdapter(List<Currency> data) {
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int i) {
            return data.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }
        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            final Currency content = data.get(i);
            ViewHolder holder = null;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.balance_item, null);
                holder = new ViewHolder();
                holder.name = view.findViewById(R.id.name);
                holder.banlance = view.findViewById(R.id.banlance);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            holder.name.setText(content.getCurrency());
            holder.banlance.setText("("+"剩余:"+BigDecimalUtil.format(content.getBalance())+")");
            if(content.getBalance()>0){
                holder.banlance.setTextColor(getResources().getColor(R.color.gray_text333_color));
                holder.name.setTextColor(getResources().getColor(R.color.gray_text333_color));
            }else{
                holder.banlance.setTextColor(getResources().getColor(R.color.color_999999));
                holder.name.setTextColor(getResources().getColor(R.color.color_999999));
            }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currency = content;
                    setBalance();
                    dismissDialog();
                }
            });
            return view;
        }
    }
    class ViewHolder {
        private TextView name;
        private TextView banlance;
    }
    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

            if (position < 11 && position != 9) {    //点击0~9按钮
                String amount = valueList.get(position).get("name");
                addChar(amount);
            } else {

                if (position == 9) {      //点击退格键
                }

                if (position == 11) {      //点击退格键
                    deleteChar();
                }
            }
        }
    };

    private void requestList() {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.BUSER_ASSET);
        params.addParameter("action", "all_v3");
        params.addParameter("token",SPUtils.get("token",""));
        showProgress();
        Log.d(TAG, "loadData: " + params.toString());
        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: " + result);
                BaseCode<List<Currency>> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<List<Currency>>>() {
                        }.getType());
                if (base.getCode() == 0) {
                    List<Currency> list = base.getData();
                    datas = list;
                    String name = SPUtils.get("currency","");
                    currency = list.get(0);
                    for(Currency c:list){
                        if(c.getCurrency().equals(name)){
                            currency = c;
                            break;
                        }
                    }
                    setBalance();
                }else if(base.getCode() == 1){
                    SPUtils.put("token", "");
                    startActivity(new Intent(PayScoreActivity.this, LoginActivity.class));
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

    private void showPwdDialog(){
        View view = getLayoutInflater().inflate(R.layout.practice_dialog,null);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view).create();
        TextView scoreTv =  view.findViewById(R.id.content);
        scoreTv.setText(R.string.paypwd_error);

        TextView left_btn =  view.findViewById(R.id.left_btn);
        left_btn.setText(R.string.forget_pwd);
        TextView right_btn =  view.findViewById(R.id.right_btn);
        right_btn.setText(R.string.retry);
        left_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PayScoreActivity.this,ResetPwdActivity.class);
                startActivity(intent);
                dialog.dismiss();
            }
        });
        right_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPay();
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    private void showDialog(String currency){
        View view = getLayoutInflater().inflate(R.layout.practice_dialog,null);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view).create();
        TextView scoreTv =  view.findViewById(R.id.content);
        scoreTv.setText(String.format(getResources().getString(R.string.balance_low_tips),currency));
        TextView left_btn =  view.findViewById(R.id.left_btn);
        left_btn.setText(R.string.cancel);
        TextView right_btn =  view.findViewById(R.id.right_btn);
        right_btn.setText(R.string.pay_other);
        left_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        right_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectCurrencyDialog();
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
