package com.ilesson.ppim.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.activity.CaptureActivity;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.Close;
import com.ilesson.ppim.entity.FriendAccept;
import com.ilesson.ppim.entity.FriendRequest;
import com.ilesson.ppim.entity.PPUserInfo;
import com.ilesson.ppim.entity.UpdateInfo;
import com.ilesson.ppim.fragment.AiFragment;
import com.ilesson.ppim.fragment.ContactFragment;
import com.ilesson.ppim.fragment.GroupFragment;
import com.ilesson.ppim.fragment.MeFragment;
import com.ilesson.ppim.fragment.PConversationListFragment;
import com.ilesson.ppim.service.MyHttpManager;
import com.ilesson.ppim.update.UpdateHelper;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.IMUtils;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.utils.StatusBarUtil;
import com.ilesson.ppim.view.IfeyVoiceWidget1;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.rong.eventbus.EventBus;
import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.IHistoryDataResultCallback;
import io.rong.imkit.manager.IUnReadMessageObserver;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;

import static com.ilesson.ppim.activity.ContactActivity.SELECT_ACTION;
import static com.ilesson.ppim.activity.LoginActivity.LOGIN_PAY;
import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;
import static com.ilesson.ppim.activity.PayScoreActivity.QR_PAY;
import static com.ilesson.ppim.activity.PayScoreActivity.TARGET_ID;

/**
 * Created by potato on 2020/3/5.
 */
@ContentView(R.layout.activity_main)
public class MainActivity extends BaseActivity {
    private static final int REQUEST_CODE = 3;
    @ViewInject(R.id.container)
    private ViewPager mViewPager;
//    @ViewInject(R.id.ai_layout)
//    private View aiLayout;
//    @ViewInject(R.id.order)
//    private View orderView;
    @ViewInject(R.id.add_layout)
    private View addLayout;
    @ViewInject(R.id.item_a)
    private View module1;
    @ViewInject(R.id.item_b)
    private View module2;
    @ViewInject(R.id.item_ai)
    private View moduleAi;
    @ViewInject(R.id.item_c)
    private View module3;
    @ViewInject(R.id.item_d)
    private View module4;
    @ViewInject(R.id.title)
    private TextView mTitle;
    @ViewInject(R.id.msg_num)
    private TextView unReadMsgView;
    @ViewInject(R.id.new_num)
    public TextView requestFriendView;
    @ViewInject(R.id.txt_a)
    private TextView mTxtA;
    @ViewInject(R.id.txt_b)
    private TextView mTxtB;
    @ViewInject(R.id.txt_ai)
    private TextView txtAI;
    @ViewInject(R.id.txt_c)
    private TextView mTxtC;
    @ViewInject(R.id.txt_d)
    private TextView mTxtD;
    @ViewInject(R.id.item_a)
    private View item_a;
    @ViewInject(R.id.item_ai)
    private View item_ai;
    @ViewInject(R.id.item_b)
    private View item_b;
    @ViewInject(R.id.item_c)
    private View item_c;
    @ViewInject(R.id.item_d)
    private View item_d;

    @ViewInject(R.id.text1)
    private TextView text1;
    @ViewInject(R.id.text2)
    private TextView text2;
    @ViewInject(R.id.apply_market)
    private TextView apply_market;
    @ViewInject(R.id.apply_marketing)
    private TextView apply_marketing;
    @ViewInject(R.id.market_layout)
    private View market_layout;
    private View[] mModules;
    private TextView[] mTVs;
    private String[] mTitleName;
    public IfeyVoiceWidget1 ifeyBtn;
    private static final String TAG = "MainActivity";
    public static final String FRESH = "fresh";
    private FragmentTransaction transaction;
    private PConversationListFragment conversationListFragment;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private List<Fragment> mFragments;
    //    private SharedPreferences mSharedPreferences;
    private List<Conversation> datas;
    public String token;
    private IMUtils imUtils;
    public static final int GROUP_TYPE = 1;
    public static final int PERSON_TYPE = 2;
    public static final int ACTIVE_SUCCESS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        StatusBarUtil.fullScreen(this);
//        setStatusBarLightMode(this,true);
        initStatusBar();
        token = SPUtils.get(LOGIN_TOKEN, "");
        String phone = SPUtils.get(LoginActivity.USER_PHONE,"");
        level = SPUtils.get(USER_MARKET_LEVEL+phone,0);
        if(level!=2){
            requestMarket();
        }
        imUtils = new IMUtils();
        imUtils.connect(this, token);
        Log.d(TAG, "onCreate: phone="+SPUtils.get(LoginActivity.USER_PHONE, ""));
        Log.d(TAG, "onCreate: token="+token);
        conversationListFragment = new PConversationListFragment();
        Uri uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon()
                .appendPath("conversationlist")
                .appendQueryParameter(Conversation.ConversationType.PRIVATE.getName(), "false")
                .appendQueryParameter(Conversation.ConversationType.GROUP.getName(), "false")
//                .appendQueryParameter(Conversation.ConversationType.PUBLIC_SERVICE.getName(), "false")
//                .appendQueryParameter(Conversation.ConversationType.APP_PUBLIC_SERVICE.getName(), "false")
//                .appendQueryParameter(Conversation.ConversationType.SYSTEM.getName(), "true")
                .build();
        conversationListFragment.setUri(uri);
        datas = new ArrayList<>();
        MyHttpManager.registerInstance();
        conversationListFragment.getConversationList(new Conversation.ConversationType[]{Conversation.ConversationType.NONE}, new IHistoryDataResultCallback<List<Conversation>>() {
            @Override
            public void onResult(List<Conversation> conversations) {
                datas.addAll(conversations);
            }

            @Override
            public void onError() {

            }
        }, true);
        mModules = new View[]{ module1, module2,moduleAi,module3, module4};
        mTVs = new TextView[]{ mTxtA, mTxtB,txtAI,mTxtC, mTxtD};
        mTitleName = getResources().getStringArray(R.array.title_array);
        setFragments();
        mViewPager.setOffscreenPageLimit(3);
        RongIM.getInstance().addUnReadMessageCountChangedObserver(observer, Conversation.ConversationType.PRIVATE, Conversation.ConversationType.GROUP);
        Conversation.ConversationType[] arr = new Conversation.ConversationType[]{Conversation.ConversationType.PRIVATE, Conversation.ConversationType.GROUP};
        RongIM.getInstance().getUnreadCount(new RongIMClient.ResultCallback<Integer>() {
            @Override
            public void onSuccess(Integer integer) {
                showUnreadMsg(integer);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

            }
        }, Conversation.ConversationType.PRIVATE, Conversation.ConversationType.GROUP);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ADD_FRIEND_MSG);
        showUnreadRequestNewFriends();
        new UpdateHelper(this).checkVersion(false);
        imUtils.setOnAddListener(new IMUtils.OnAddListener() {
            @Override
            public void onFinished() {
                hideProgress();
            }

            @Override
            public void onSuccess(int type, String id, String name) {
                if (type == GROUP_TYPE) {
                    RongIM.getInstance().startGroupChat(MainActivity.this, id, "");
                } else {
                    Toast.makeText(MainActivity.this, R.string.request_success, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFail(String msg) {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });


        EventBus.getDefault().post(new Close());
        requestSdcard();
        mViewPager.setCurrentItem(2);

        item_a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelection(0);
            }
        });
        item_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelection(1);
            }
        });
        item_ai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelection(2);
            }
        });
        item_c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelection(3);
            }
        });
        item_d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelection(4);
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        setCurrentActivity(this);
    }

    private void showUnreadMsg(int unReadMsgCount) {
        if (unReadMsgCount > 0) {
            unReadMsgView.setVisibility(View.VISIBLE);
            unReadMsgView.setText(unReadMsgCount + "");
        } else {
            unReadMsgView.setVisibility(View.GONE);
        }
        try {
//            ShortcutBadger.applyCountOrThrow(getApplicationContext(), unReadMsgCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sendToSamsumg(this, unReadMsgCount);
//        ShortcutBadger.with(getApplicationContext()).count(unReadMsgCount);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
//        if (intent.getBooleanExtra(FRESH, false)) {
//            groupFragment.requestList(false);
//        }
    }

    private static void sendToSamsumg(Context context, int count) {
        String launcherClassName = getLauncherClassName(context);
        if (launcherClassName == null) {
            return;
        }
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count", count);
        intent.putExtra("badge_count_package_name", context.getPackageName());
        intent.putExtra("badge_count_class_name", launcherClassName);
        context.sendBroadcast(intent);
    }

    private static String getLauncherClassName(Context context) {
        PackageManager packageManager = context.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        // To limit the components this Intent will resolve to, by setting an
        // explicit package name.
        intent.setPackage(context.getPackageName());
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        // All Application must have 1 Activity at least.
        // Launcher activity must be found!
        ResolveInfo info = packageManager
                .resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);

        // get a ResolveInfo containing ACTION_MAIN, CATEGORY_LAUNCHER
        // if there is no Activity which has filtered by CATEGORY_DEFAULT
        if (info == null) {
            info = packageManager.resolveActivity(intent, 0);
        }

        return info.activityInfo.name;
    }

    public static final String ADD_FRIEND_MSG = "add_friend_msg";
    public static final String FRIEND_ACCEPT = "friend_accept";

    public int unAcceptFriends = SPUtils.get(FRIEND_ACCEPT, 0);

    public void onEventMainThread(FriendRequest message) {
        if (!contactFragment.friends.containsKey(message.getId())) {
            unAcceptFriends++;
            SPUtils.put(FRIEND_ACCEPT, unAcceptFriends);
            showUnreadRequestNewFriends();
        }
    }
    public void onEventMainThread(FriendAccept friendAccept) {
        contactFragment.requestFriendsList(true);
    }

    IUnReadMessageObserver observer = new IUnReadMessageObserver() {
        @Override
        public void onCountChanged(int i) {
            showUnreadMsg(i);
        }
    };

    public void showUnreadRequestNewFriends() {
        int unAcceptFriends = SPUtils.get(FRIEND_ACCEPT, 0);
        if (unAcceptFriends > 0) {
            if (null != requestFriendView) {
                requestFriendView.setVisibility(View.VISIBLE);
                requestFriendView.setText(unAcceptFriends + "");
            }
        } else {
            requestFriendView.setVisibility(View.GONE);
        }
        contactFragment.showUnread();
    }

    private GroupFragment groupFragment = new GroupFragment();
    private MeFragment meFragment = new MeFragment();
    private ContactFragment contactFragment = new ContactFragment();
    private AiFragment aiFragment = new AiFragment();

    private void setFragments() {
        mFragments = new ArrayList<>();
        mFragments.add(conversationListFragment);
        mFragments.add(contactFragment);
        mFragments.add(aiFragment);
        mFragments.add(groupFragment);
        mFragments.add(new MeFragment());
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), mFragments);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(4);
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        mViewPager.setOnPageChangeListener(new ViewPagerListener());
//        setSelection(0);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments;
        private FragmentManager fm;
        private int curUpdatePager;

        public SectionsPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fm = fm;
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            //得到缓存的fragment
            Fragment fragment = (Fragment) super.instantiateItem(container,
                    position);
            return fragment;
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }
    }

    class ViewPagerListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrollStateChanged(int arg0) {

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageSelected(int index) {
            Log.d(TAG, "onPageSelected: " + index);
            setSelection(index);
        }
    }

    private int currentIndex;
    private int lastIndex;
    public void setSelection(int index) {
        mViewPager.setCurrentItem(index);
        Log.d(TAG, "setSelection: ");
        for (int i = 0; i < mModules.length; i++) {
            if (i != index) {
                mModules[i].setSelected(false);
                mTVs[i].setSelected(false);
                mTVs[i].setTextColor(getResources().getColor(R.color.gray_text333_color));
            }
        }
        addLayout.setVisibility(View.GONE);
//        aiLayout.setVisibility(View.GONE);
        market_layout.setVisibility(View.GONE);
        switch (index) {
            case 0:
            case 1:
                addLayout.setVisibility(View.VISIBLE);
//                hideVoice();
                break;
            case 2:
//                floatBtn.setVisibility(View.VISIBLE);
//                aiLayout.setVisibility(View.VISIBLE);
                aiFragment.request("");
                break;
            case 3:
                if(level==1){
                    requestMarket();
                }
                checkLevel();
//                hideVoice();
            case 4:
//                hideVoice();
                break;

        }
        if(index!=2){
            aiFragment.stopVoice();
        }
        mTitle.setText(mTitleName[index]);
        mModules[index].setSelected(true);
        mModules[index].setPressed(true);
        mTVs[index].setSelected(true);
        mTVs[index].setPressed(true);
        mTVs[index].setTextColor(getResources().getColor(R.color.theme_color));
        currentIndex = index;
        if(lastIndex==2){
            if(index!=2){
                initStatusBar();
            }
        }else{
            if(index==2){
                StatusBarUtil.fullScreen(this);
            }
        }
        lastIndex = index;
    }


    @Event(value = R.id.apply_market)
    private void apply_market(View v) {
        updateMarket();
    }
//    @Event(value = R.id.item_b, type = View.OnTouchListener.class)
//    public boolean touchB(View v, MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_UP) {
//            setSelection(1);
//        }
//        return true;
//    }
//    @Event(value = R.id.item_ai, type = View.OnTouchListener.class)
//    public boolean item_ai(View v, MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_UP) {
//            if(currentIndex!=2){
//                setSelection(2);
//            }
//        }
//        return true;
//    }
//
//    @Event(value = R.id.item_c, type = View.OnTouchListener.class)
//    public boolean touchC(View v, MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_UP) {
//            setSelection(3);
//        }
//        return true;
//    }
//
//    @Event(value = R.id.item_d, type = View.OnTouchListener.class)
//    public boolean touch(View v, MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_UP) {
//            setSelection(4);
//        }
//
//        return true;
//    }
//
//    @Event(value = R.id.item_a, type = View.OnTouchListener.class)
//    public boolean touchA(View v, MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_UP) {
//            setSelection(0);
//        }
//        return true;
//    }

//    @Event(value = R.id.add)
    public void add(View view) {
        showPopwindow(view);
    }

//    @Event(value = R.id.search)
public void search() {
        startActivity(new Intent(this, SearchActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQ_QR_CODE && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String result = bundle.getString(Constants.INTENT_EXTRA_KEY_QR_SCAN);
            Log.d(TAG, "onActivityResult: "+result);
            if(!result.contains(Constants.BASE_URL)){
                Toast.makeText(MainActivity.this, R.string.error_qrcode, Toast.LENGTH_LONG).show();
                return;
            }
            String targetId = result.substring(result.lastIndexOf("=") + 1);
            if (result.contains("a=g")) {
                String name = SPUtils.get(LoginActivity.USER_PHONE, "");
                imUtils.requestGroupChat(token, targetId, name, "");
                return;
            }
            if (result.contains("a=p")) {
                Intent intent = new Intent(this,FriendDetailActivity.class);
                intent.putExtra(FriendDetailActivity.USER_ID,targetId);
                startActivity(intent);
//                imUtils.addFriend(token, targetId);
                return;
            }
            boolean active = SPUtils.get(LOGIN_PAY, false);
            if (!active) {
                Toast.makeText(MainActivity.this, R.string.no_active, Toast.LENGTH_LONG).show();
                return;
            }
            Intent intent = new Intent(MainActivity.this, PayScoreActivity.class);
            intent.putExtra(TARGET_ID, targetId);
            intent.putExtra(QR_PAY, true);
            startActivity(intent);
        }
        if (resultCode == SettingActivity.LOGIN_OUT) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
//        if (resultCode == MODIFY_SUCCESS) {
//            meFragment.setUserInfo();
//            return;
//        }
//        if (resultCode == ACTIVE_SUCCESS) {
//            groupFragment.activeSuccess();
//            return;
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        RongIM.getInstance().removeUnReadMessageCountChangedObserver(observer);
//        unregisterReceiver(customMessageReceiver);
    }

    private void showPopwindow(View view) {
        //加载弹出框的布局
        View contentView = LayoutInflater.from(MainActivity.this).inflate(
                R.layout.pop_menu, null);


        final PopupWindow popupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
//        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAsDropDown(view, 0, 10);
        contentView.findViewById(R.id.add_friend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SelectAddFriendActivity.class));
                popupWindow.dismiss();
            }
        });
        contentView.findViewById(R.id.request_group).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ContactActivity.class).putExtra(SELECT_ACTION, GROUP_TYPE));
                popupWindow.dismiss();
            }
        });
        contentView.findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                boolean isPay = SPUtils.get(LOGIN_PAY, false);
//                if (!isPay) {
//                    startActivity(new Intent(MainActivity.this, PayPwdActivity.class));
//                } else {
                    toScan();
//                }
                popupWindow.dismiss();
            }
        });
    }

    public void toScan() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.CAMERA).subscribe(new Consumer<Boolean>() {
            public void accept(Boolean aBoolean) {
                if (aBoolean) {
                    Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                    startActivityForResult(intent, Constants.REQ_QR_CODE);
//                    Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
//                    startActivityForResult(intent, REQUEST_CODE);
                } else {
                    //只要有一个权限被拒绝，就会执行
                    Toast.makeText(MainActivity.this, "未授权权限，扫一扫功能不能使用", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private long mPressedTime = 0;

    @Override
    public void onBackPressed() {
        long mNowTime = System.currentTimeMillis();//获取第一次按键时间
        if ((mNowTime - mPressedTime) > 2000) {//比较两次按键时间差
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            mPressedTime = mNowTime;
        } else {//退出程序
            this.finish();
//            System.exit(0);
        }
    }


    private static final String USER_MARKET_LEVEL="user_market_level";
    public int level=0;
    public void checkLevel(){
        if(level==0){
            market_layout.setVisibility(View.VISIBLE);
            text1.setVisibility(View.VISIBLE);
            text2.setVisibility(View.VISIBLE);
            apply_market.setVisibility(View.VISIBLE);
            apply_marketing.setVisibility(View.GONE);
        }else if(level==1){
            text1.setVisibility(View.GONE);
            text2.setVisibility(View.GONE);
            apply_market.setVisibility(View.GONE);
            apply_marketing.setVisibility(View.VISIBLE);
            market_layout.setVisibility(View.VISIBLE);
        }else if(level==2){
            market_layout.setVisibility(View.GONE);
        }
    }
    public void requestMarket() {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.EXTRA);
        params.addParameter("action", "query");
        Log.d(TAG, "loadData: " + params.toString());
        x.http().post(params, new Callback.CacheCallback<String>() {
            @Override
            public boolean onCache(String result) {
                BaseCode<PPUserInfo> data = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<PPUserInfo>>() {
                        }.getType());
                if(data.getCode()==0){
                    PPUserInfo info = data.getData();
                    level = info.getLevel();
                }
                return false;
            }

            @SuppressLint("StringFormatMatches")
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: "+result);
                BaseCode<PPUserInfo> data = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<PPUserInfo>>() {
                        }.getType());
                if(data.getCode()==0){
                    PPUserInfo info = data.getData();
                    level = info.getLevel();
                    String phone = SPUtils.get(LoginActivity.USER_PHONE,"");
                    SPUtils.put(USER_MARKET_LEVEL+phone,level);
                }else {
                    showToast(data.getMessage());
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
            }
        });
    }
    public void updateMarket() {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.EXTRA);
        params.addParameter("action", "update");
        Log.d(TAG, "loadData: " + params.toString());
        showProgress();
        x.http().post(params, new Callback.CommonCallback<String>() {
            @SuppressLint("StringFormatMatches")
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: "+result);
                BaseCode data = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode>() {
                        }.getType());
                if(data.getCode()==0){
                    level = 1;
                    checkLevel();
                }else {
                    showToast(data.getMessage());
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
                level = 1;
                checkLevel();
                hideProgress();
            }
        });
    }
    public void onEventMainThread(UpdateInfo message) {
        if(null!=meFragment){
            meFragment.setUserInfo();
        }
    }
    public void requestSdcard() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Boolean aBoolean) {
                if (aBoolean) {
                } else {
                    goIntentSetting();
//只有用户拒绝开启权限，且选了不再提示时，才会走这里，否则会一直请求开启
                }
            }
            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }
    private void goIntentSetting() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
