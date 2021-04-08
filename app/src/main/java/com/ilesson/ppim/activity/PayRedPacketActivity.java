package com.ilesson.ppim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.IMUtils;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.view.VirtualKeyboardView;

import org.xutils.common.Callback;
import org.xutils.common.util.MD5;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.Map;

import static com.ilesson.ppim.activity.ConversationActivity.PAY_FIAL;
import static com.ilesson.ppim.activity.ConversationActivity.PAY_SUCCESS;
import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;

@ContentView(R.layout.activity_pay_redpacket)
public class PayRedPacketActivity extends BaseActivity {
    @ViewInject(R.id.virtualKeyboardView)
    private VirtualKeyboardView virtualKeyboardView;
    @ViewInject(R.id.content)
    private EditText scoreEdit;
    @ViewInject(R.id.num)
    private EditText numEdit;
    @ViewInject(R.id.des)
    private EditText desEdit;
    @ViewInject(R.id.redpacket_score)
    private TextView redpacketView;
    @ViewInject(R.id.confirm)
    private TextView transBtn;
    @ViewInject(R.id.pay_for_score)
    private TextView payScore;
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
    @ViewInject(R.id.pay_layout)
    private View payLayout;
    private GridView gridView;
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

    private ArrayList<Map<String, String>> valueList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarLightMode(this, true);
        Intent intent = getIntent();
        targetId = intent.getStringExtra(TARGET_ID);
        isPQPay = intent.getBooleanExtra(QR_PAY, false);
        payType = intent.getIntExtra(PAY_TYPE, 0);
        scoreEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String num = scoreEdit.getText().toString();
                if(!TextUtils.isEmpty(num)){
                    scoreEdit.setText(Integer.valueOf(num)+"");
                }
            }
        });
        numEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String num = numEdit.getText().toString();
                if(!TextUtils.isEmpty(num)){
                    numEdit.setText(Integer.valueOf(num)+"");
                }
            }
        });
        mCodes = new EditText[]{mCode1, mCode2, mCode3, mCode4, mCode5, mCode6};
        for (int i = 0; i < mCodes.length; i++) {
            EditText editText = mCodes[i];
            editText.setCursorVisible(false);
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
        }
        valueList = virtualKeyboardView.getValueList();
        gridView = virtualKeyboardView.getGridView();
        gridView.setOnItemClickListener(onItemClickListener);
        virtualKeyboardView.getLayoutBack().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                virtualKeyboardView.startAnimation(exitAnim);
                virtualKeyboardView.setVisibility(View.GONE);
            }
        });
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

    private Animation enterAnim;
    private Animation exitAnim;

    private void initAnim() {

        enterAnim = AnimationUtils.loadAnimation(this, R.anim.push_bottom_in);
        exitAnim = AnimationUtils.loadAnimation(this, R.anim.push_bottom_out);
    }

    private String score;
    private String count;
    private String des;

    @Event(value = R.id.confirm)
    private void confrim(View view) {
        score = scoreEdit.getText().toString();
        clearPwd();
        if (TextUtils.isEmpty(score)) {
            Toast.makeText(this, R.string.hint_redpacket, Toast.LENGTH_LONG).show();
            return;
        }
        count = numEdit.getText().toString();
        if (TextUtils.isEmpty(count)) {
            Toast.makeText(this, R.string.hint_redpacket_num, Toast.LENGTH_LONG).show();
            return;
        }
        if (Integer.valueOf(count)==0||Integer.valueOf(score)==0||Integer.valueOf(score)<Integer.valueOf(count)) {
            Toast.makeText(this, R.string.wrong_redpacket_num, Toast.LENGTH_LONG).show();
            return;
        }
        des = desEdit.getText().toString();
        if(TextUtils.isEmpty(des)){
            des = getResources().getString(R.string.default_des);
        }
        hideInput();
        payScore.setText(score);
        payLayout.setVisibility(View.VISIBLE);
    }

    protected void hideInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View v = getWindow().peekDecorView();
        if (null != v) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }
    @Event(R.id.back_btn)
    private void back_btn(View v) {
        finish();
    }

    private static final String TAG = "PayScoreActivity";

    private void trans(String pwd) {
        showProgress();
        transBtn.setEnabled(false);
        final String token = SPUtils.get(LOGIN_TOKEN, "");
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.MONEY_URL);
        params.addBodyParameter("action", "send");
        params.addBodyParameter("token", token);
        params.addBodyParameter("money", score);
        params.addBodyParameter("group", targetId);
        params.addBodyParameter("count", count);
        params.addBodyParameter("pwd", MD5.md5(pwd));
        Log.d(TAG, "loadData: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: " + result);
                BaseCode<String> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<String>>() {
                        }.getType());
                if (base.getCode() == 0) {
                    String id = base.getData();
                    new IMUtils().sendRedPacket(targetId, id, des);
                    IMUtils.login(PayRedPacketActivity.this,SPUtils.get(LOGIN_TOKEN,""));
                    finish();
                } else {
                    payLayout.setVisibility(View.GONE);
                    setFailInfo();
                    Toast.makeText(PayRedPacketActivity.this, base.getMessage(), Toast.LENGTH_LONG).show();
                }
            }


            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Toast.makeText(PayRedPacketActivity.this, "支付失败", Toast.LENGTH_LONG).show();
                clearPwd();
                payLayout.setVisibility(View.GONE);
                ex.printStackTrace();
            }


            @Override
            public void onCancelled(CancelledException cex) {
                cex.printStackTrace();
            }


            @Override
            public void onFinished() {
                transBtn.setEnabled(true);
                hideProgress();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setSuccessInfo() {
        Intent intent = new Intent();
        intent.putExtra(PAY_MONEY, score);
        setResult(PAY_SUCCESS, intent);
        finish();
    }
    @Event(R.id.close)
    private void close(View v) {
        payLayout.setVisibility(View.GONE);
    }
    private void setFailInfo() {
        Intent intent = new Intent();
//        intent.putExtra(PAY_DECS,score);
        setResult(PAY_FIAL, intent);
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
}
