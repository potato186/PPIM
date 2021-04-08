package com.ilesson.ppim.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.PaySuccess;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelpay.PayResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import io.rong.eventbus.EventBus;

import static com.ilesson.ppim.activity.LoginActivity.USER_PHONE;


public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {


    private IWXAPI api;
    private String account;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(),
                MODE_PRIVATE);
        account = sharedPreferences.getString(USER_PHONE, "");
        api = WXAPIFactory.createWXAPI(this, getString(R.string.wx_key));
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {
    }

    private static final String TAG = "WXPayEntryActivity";
    public static final String PREPAYID = "prepayId";
    @Override
    public void onResp(BaseResp resp) {
        Log.d(TAG, "onPayFinish, errCode = " + resp.errCode);
        Log.d(TAG, "onPayFinish, errCode = " + resp.errStr);
        if (resp.errCode == 0) {
            PayResp pr = (PayResp) resp;
            EventBus.getDefault().post(new PaySuccess());
        }
        finish();
    }
}