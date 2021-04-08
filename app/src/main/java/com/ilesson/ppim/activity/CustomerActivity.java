package com.ilesson.ppim.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ilesson.ppim.R;
import com.ilesson.ppim.update.UpdateHelper;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.SPUtils;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import static com.ilesson.ppim.utils.Constants.SHARE_LINK;


/**
 * Created by potato on 2019/4/9.
 */
//@ContentView(R.layout.act_fund_server)
public class CustomerActivity extends BaseActivity{
    private WebView mWebview;
    //    private History mHistory;
    private String phone;
    private String url;
    private ImageView collectImg;
    public static final String URL = "url";
    public static final String TITLE = "title";
    public static final String SCORE = "score";
    public static final String WORD_NUM = "word_num";
    public static final String UUID = "uuid";
    public static final String GRADE = "grade";
    private String title;
    private String score;
    private String wordNum;
    private String uuid;
    private String grade;

    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.act_fund_server);
        setStatusBarLightMode(this,true);
        phone = SPUtils.get(LoginActivity.USER_PHONE,"");
        mWebview = findViewById(R.id.webview);
        Intent intent = getIntent();
        collectImg = findViewById(R.id.collect);
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

            @Override
            public void onPageFinished(WebView webView, String s) {
                super.onPageFinished(webView, s);
                hideProgress();
            }
        });
        TextView titleTv = findViewById(R.id.title);
        titleTv.setText(R.string.contact_server);
        mWebview.loadUrl(Constants.BASE_URL+"/contact.html");
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
