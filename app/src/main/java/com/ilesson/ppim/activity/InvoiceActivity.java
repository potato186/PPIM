package com.ilesson.ppim.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.InvoiceInfo;
import com.ilesson.ppim.utils.SPUtils;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ilesson.ppim.activity.LoginActivity.USER_PHONE;


/**
 * Created by potato on 2019/4/9.
 */
@ContentView(R.layout.act_invoice)
public class InvoiceActivity extends BaseActivity {
    @ViewInject(R.id.elect_invoice)
    private TextView electInvoice;
    @ViewInject(R.id.paper_invoice)
    private TextView paperInvoice;
    @ViewInject(R.id.personal_invoice)
    private TextView personalInvoice;
    @ViewInject(R.id.company_invoice)
    private TextView companyInvoice;
    @ViewInject(R.id.company_name)
    private EditText companyName;
    @ViewInject(R.id.company_num)
    private EditText companyNum;
    @ViewInject(R.id.person_name)
    private EditText personName;
    @ViewInject(R.id.email)
    private EditText emailText;
    @ViewInject(R.id.company_layout)
    private View companyLayout;
    @ViewInject(R.id.tax_layout)
    private View taxLayout;
    @ViewInject(R.id.person_name_layout)
    private View personLayout;
    @ViewInject(R.id.email_layout)
    private View emailLayout;
    public static final String INVOICE_DATA = "invoice_data";
    public static final String INVOICE_MODIFY = "invoice_modify";
    public static final String PERSON_NAME = "person_name";
    public static final String COMPANY_NAME = "company_name";
    public static final String EMAIL_NAME = "email_name";
    public static final String COMPANY_NUM = "company_num";
    public static final String PERSON_MEDIUM = "person_medium";
    public static final String COMPANY_MEDIUM = "company_medium";
    public static final String INVOICE_PERSON = "0";
    public static final String INVOICE_COMPANY = "1";
    public static final String INVOICE_ELECT = "0";
    public static final String INVOICE_PAPER = "1";
    public static final int INVOICE_MODIFY_SUCCESS = 101;
    public static final int INVOICE_CANCEL = 102;

    private List<InvoiceInfo> invoiceInfos;
    private InvoiceInfo modifyInfo;
    private InvoiceInfo editInfo;
    private String phone;
    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setStatusBarLightMode(this, true);
        Intent intent = getIntent();
        invoiceInfos = (List<InvoiceInfo>) intent.getSerializableExtra(INVOICE_DATA);
        modifyInfo = (InvoiceInfo) intent.getSerializableExtra(INVOICE_MODIFY);
        phone = SPUtils.get(USER_PHONE, "");
        String pName = SPUtils.get(PERSON_NAME+phone, "");
        String cName = SPUtils.get(COMPANY_NAME+phone, "");
        String eName = SPUtils.get(EMAIL_NAME+phone, "");
        String cNum = SPUtils.get(COMPANY_NUM+phone, "");
        String pMedium = SPUtils.get(PERSON_MEDIUM+phone, "");
        String cMedium = SPUtils.get(COMPANY_MEDIUM+phone, "");
        if (invoiceInfos != null && invoiceInfos.size() > 0) {
            for (int i = 0; i < invoiceInfos.size(); i++) {
                InvoiceInfo info = invoiceInfos.get(i);
                if (TextUtils.isEmpty(emailText.getText().toString())) {
                    emailText.setText(info.getEmail());
                }
                if (null == modifyInfo) {
                    modifyInfo = info;
                }
                if (info.getType().equals(INVOICE_PERSON)) {
                    personName.setText(info.getName());
                    if (info.getMedium().equals(INVOICE_ELECT)) {
//                        electInvoice(electInvoice);
                    } else {
//                        paperInvoice(paperInvoice);
                    }

                } else {
                    companyName.setText(info.getName());
                    companyNum.setText(info.getNumber());
                }
            }
        }
        if (null != modifyInfo) {
            if (INVOICE_COMPANY.equals(modifyInfo.getType())) {
                companyInvoice(companyInvoice);
            } else {
                personalInvoice(personalInvoice);
            }
            if (INVOICE_PAPER.equals(modifyInfo.getMedium())) {
                paperInvoice(paperInvoice);
            } else {
                electInvoice(electInvoice);
            }
        } else {
            modifyInfo = new InvoiceInfo();
            electInvoice(electInvoice);
            personalInvoice(personalInvoice);
        }

        if (!TextUtils.isEmpty(pName)) {
            personName.setText(pName);
        }
        if (!TextUtils.isEmpty(cName)) {
            companyName.setText(cName);
        }
        if (!TextUtils.isEmpty(eName)) {
            emailText.setText(eName);
        }
        if (!TextUtils.isEmpty(cNum)) {
            companyNum.setText(cNum);
        }
    }

    @Event(R.id.close)
    private void close(View view) {
        finish();
        overridePendingTransition(0, 0);
    }

    @Event(R.id.elect_invoice)
    private void electInvoice(TextView view) {
        setSelectedStyle(electInvoice);
        setUnSelectedStyle(paperInvoice);
        emailLayout.setVisibility(View.VISIBLE);
        modifyInfo.setMedium(INVOICE_ELECT);
        modifyInfo.setMediumName(getResources().getString(R.string.elec_invoice));
    }

    @Event(R.id.paper_invoice)
    private void paperInvoice(TextView view) {
        setSelectedStyle(paperInvoice);
        setUnSelectedStyle(electInvoice);
        emailLayout.setVisibility(View.GONE);
        modifyInfo.setMedium(INVOICE_PAPER);
        modifyInfo.setMediumName(getResources().getString(R.string.paper_invoice));
    }

    @Event(R.id.personal_invoice)
    private void personalInvoice(View view) {
        setSelectedStyle(personalInvoice);
        setUnSelectedStyle(companyInvoice);
        personLayout.setVisibility(View.VISIBLE);
        companyLayout.setVisibility(View.GONE);
        taxLayout.setVisibility(View.GONE);
        modifyInfo.setType(INVOICE_PERSON);
        modifyInfo.setTypeName(getResources().getString(R.string.personal));
    }

    @Event(R.id.company_invoice)
    private void companyInvoice(View view) {
        setSelectedStyle(companyInvoice);
        setUnSelectedStyle(personalInvoice);
        personLayout.setVisibility(View.GONE);
        companyLayout.setVisibility(View.VISIBLE);
        taxLayout.setVisibility(View.VISIBLE);
        modifyInfo.setType(INVOICE_COMPANY);
        modifyInfo.setTypeName(getResources().getString(R.string.enterprise));
    }

    @Event(R.id.save)
    private void save(View view) {
        String pName = personName.getText().toString();
        String cName = companyName.getText().toString();
        String cNum = companyNum.getText().toString();
        String email = emailText.getText().toString().replace(" ","");
        if (!TextUtils.isEmpty(pName)) {
            SPUtils.put(PERSON_NAME+phone, pName);
        } else {
            if (modifyInfo.getType().equals(INVOICE_PERSON)) {
                showToast(R.string.hint_invoice_personname);
                return;
            }
        }
        if (!TextUtils.isEmpty(cName)) {
            SPUtils.put(COMPANY_NAME+phone, cName);
        } else {
            if (modifyInfo.getType().equals(INVOICE_COMPANY)) {
                showToast(R.string.hint_enterprise_name);
                return;
            }
        }
        if (!TextUtils.isEmpty(cNum)) {
            modifyInfo.setNumber(cNum);
            SPUtils.put(COMPANY_NUM+phone, cNum);
        } else {
            if (modifyInfo.getType().equals(INVOICE_COMPANY)) {
                showToast(R.string.hint_tax_num);
                return;
            }
        }
        if (!TextUtils.isEmpty(email)) {
            Pattern p = Pattern.compile("^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+(\\.([a-zA-Z0-9_-])+)+$");  //正则表达式
            Matcher m = p.matcher(email);
            if(!m.matches()){
                showToast(R.string.error_email);
                return;
            }
            modifyInfo.setEmail(email);
            SPUtils.put(EMAIL_NAME+phone, email);
        } else {
            if (modifyInfo.getMedium().equals(INVOICE_ELECT)) {
                showToast(R.string.hint_invoice_email);
                return;
            }
        }
        if (modifyInfo.getType().equals(INVOICE_PERSON)) {
            modifyInfo.setName(pName);
            SPUtils.put(PERSON_MEDIUM+phone,modifyInfo.getMedium());
        }else {
            modifyInfo.setName(cName);
            SPUtils.put(COMPANY_MEDIUM+phone,modifyInfo.getMedium());
        }
        setInvoiceResult(INVOICE_MODIFY_SUCCESS);
    }

    @Event(R.id.no_invoice)
    private void noInvoice(View view) {
        setInvoiceResult(INVOICE_CANCEL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideInput();
    }

    private void setSelectedStyle(TextView textView) {
        textView.setBackgroundResource(R.drawable.background_theme_corner20);
        textView.setTextColor(getResources().getColor(R.color.white));
    }

    private void setUnSelectedStyle(TextView textView) {
        textView.setBackgroundResource(R.drawable.background_gray_corner20);
        textView.setTextColor(getResources().getColor(R.color.gray_text333_color));
    }

    private static final String TAG = "InvoiceActivity";

    private void setInvoiceResult(int code) {
        Intent intent = new Intent();
        intent.putExtra(INVOICE_MODIFY, modifyInfo);
        Log.d(TAG, "setInvoiceResult: " + modifyInfo);
        setResult(code, intent);
        finish();
    }

    private void hideInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(paperInvoice.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

}
