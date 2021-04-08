package com.ilesson.ppim.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.ilesson.ppim.R;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.utils.WxShareUtils;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

/**
 * Created by potato on 2019/5/8.
 */

public class PWebActivity extends BaseActivity {

    private WebView mWebview;
    private String phone;
    private String url;
    public static final String URL = "url";
    public static final String TITLE = "title";
    private static final String TAG = "PWebActivity";
    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.act_history_result);
        setStatusBarLightMode(this,true);
        phone = SPUtils.get(LoginActivity.USER_PHONE,"");
        mWebview = findViewById(R.id.webview);
        findViewById(R.id.menu).setVisibility(View.GONE);
        Intent intent = getIntent();
        WebSettings set = mWebview.getSettings();
        set.setJavaScriptEnabled(true);
        set.setSupportZoom(true);
        set.setBuiltInZoomControls(true);
        set.setUseWideViewPort(true);
        showProgress();
        set.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        set.setLoadWithOverviewMode(true);
        mWebview.setWebChromeClient(new WebChromeClient() {

        });
        final TextView titleTv = findViewById(R.id.title);
        mWebview.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(WxShareUtils.SHARE_URL);
                intent.setData(content_url);
                startActivity(intent);
                return true;
            }

            @Override
            public void onPageFinished(WebView webView, String s) {
                super.onPageFinished(webView, s);
                hideProgress();
                String title = webView.getTitle();
                if(!TextUtils.isEmpty(title)){
                    titleTv.setText(title);
                }
            }
        });
        url = intent.getStringExtra(URL);
        if(!TextUtils.isEmpty(url)){
            mWebview.loadUrl(url);
        }
        String title = intent.getStringExtra(TITLE);
        if(!TextUtils.isEmpty(title)){
            titleTv.setText(title);
        }
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
