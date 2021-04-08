package com.ilesson.ppim.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.ilesson.ppim.R;
import com.ilesson.ppim.utils.Constants;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;


/**
 * Created by potato on 2019/5/8.
 */
@ContentView(R.layout.act_private)
public class PrivateActivity extends BaseActivity {
    @ViewInject(R.id.webview)
    private WebView mWebview;
    @ViewInject(R.id.title)
    private TextView titleTV;
    public static final String USER_POLICE = "user_police";
    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setStatusBarLightMode(this,true);
        WebSettings set = mWebview.getSettings();
        set.setJavaScriptEnabled(true);
        set.setSupportZoom(true);
        set.setBuiltInZoomControls(true);
        set.setUseWideViewPort(true);
        set.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        set.setLoadWithOverviewMode(true);
        mWebview.setWebChromeClient(new WebChromeClient() {
        });
        mWebview.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return true;
            }
        });
        boolean police = getIntent().getBooleanExtra(USER_POLICE,false);
        String url = Constants.BASE_URL;
        if(police){
            url+="/user.html";
            titleTV.setText(R.string.user_police);
        }else{
            url+="/privacy.html";
            titleTV.setText(R.string.private_case);
        }
        mWebview.loadUrl(url);
    }

    @Event(R.id.back_btn)
    private void back_btn(View view){
        finish();
    }
}
