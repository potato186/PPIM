package com.ilesson.ppim.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ilesson.ppim.R;
import com.ilesson.ppim.utils.SPUtils;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;

import io.rong.imkit.RongIM;

import static com.ilesson.ppim.activity.InputPhoneActivity.TYPE_VERIFY;
import static com.ilesson.ppim.activity.LoginActivity.LOGIN_PAY;
import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;
import static com.ilesson.ppim.activity.LoginActivity.USER_ICON;
import static com.ilesson.ppim.activity.LoginActivity.USER_MONEY;
import static com.ilesson.ppim.activity.LoginActivity.USER_NAME;
import static com.ilesson.ppim.activity.LoginActivity.USER_PHONE;
import static com.ilesson.ppim.activity.ResetPwdActivity.RESET_LOGIN_PWD;
import static com.ilesson.ppim.fragment.FundFragment.FUND_NOT_ACTIVED;


/**
 * Created by potato on 2019/4/9.
 */
@ContentView(R.layout.act_security)
public class SecurityActivity extends BaseActivity{
    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setStatusBarLightMode(this,true);
    }

    @Event(R.id.back_btn)
    private void back_btn(View view){
        finish();
    }
    @Event(R.id.reset_login_pwd)
    private void reset_login_pwd(View view){
//        Intent intent = new Intent(this,ResetPwdActivity1.class);
//        intent.putExtra(RESET_LOGIN_PWD,true);
//        startActivity(intent);
        Intent intent = new Intent(SecurityActivity.this,InputPhoneActivity.class);
        intent.putExtra(VerifyActivity.VERIFY_ACTION,VerifyActivity.FORGET_PWD_TYPE);
        startActivityForResult(intent,0);
    }
    @Event(R.id.reset_pay_pwd)
    private void reset_pay_pwd(View view){
        boolean active = SPUtils.get(LOGIN_PAY, false);
        if(!active){
            Toast.makeText(this,R.string.no_active,Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(this,ResetPwdActivity.class);
        startActivity(intent);
    }

}
