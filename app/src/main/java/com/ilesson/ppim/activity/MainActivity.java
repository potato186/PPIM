package com.ilesson.ppim.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.activity.CaptureActivity;
import com.iflytek.cloud.SpeechUtility;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.Close;
import com.ilesson.ppim.entity.FriendAccept;
import com.ilesson.ppim.entity.FriendRequest;
import com.ilesson.ppim.entity.SmartOrder;
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
import com.ilesson.ppim.utils.PPScreenUtils;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.utils.TTSHelper;
import com.ilesson.ppim.view.DragView;
import com.ilesson.ppim.view.IfeyVoiceWidget1;
import com.tbruyelle.rxpermissions2.Permission;
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
import static com.ilesson.ppim.view.SwitchButton.PLAY_TTS;

/**
 * Created by potato on 2020/3/5.
 */
@ContentView(R.layout.activity_main)
public class MainActivity extends BaseActivity {
    private static final int REQUEST_CODE = 3;
    @ViewInject(R.id.container)
    private ViewPager mViewPager;
    @ViewInject(R.id.top_layout)
    private View topLayout;
    @ViewInject(R.id.voice_layout)
    private View voiceLayout;
    @ViewInject(R.id.ai_layout)
    private View aiLayout;
    @ViewInject(R.id.order)
    private View orderView;
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
    @ViewInject(R.id.play_tts)
    private TextView playView;
    @ViewInject(R.id.request_text)
    private TextView requestText;
    @ViewInject(R.id.tts)
    private TextView ttsTv;
    @ViewInject(R.id.result_image)
    private ImageView resultImageView;
    @ViewInject(R.id.floatBtn)
    private DragView floatBtn;
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
    private static final String FLOATX = "floatx111";
    private static final String FLOATY = "floaty111";
    public static final int GROUP_TYPE = 1;
    public static final int PERSON_TYPE = 2;
    public static final int ACTIVE_SUCCESS = 3;
    private boolean recording;
    private TTSHelper ttsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setStatusBarLightMode(this, true);
//        initStatusBar();
        token = SPUtils.get(LOGIN_TOKEN, "");
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
//        registerReceiver(customMessageReceiver, intentFilter);
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
//        Glide.with(getApplicationContext()).asGif().load(R.mipmap.assassin).into(floatBtn);
//        floatBtn.setOnTouchListener(this);
        floatBtn.setOnPressListener(new DragView.OnPressListener() {
            @Override
            public void onPressUp(boolean isDrag) {
//                if(!isDrag){
//                    overridePendingTransition(0, 0);
//                    startActivity(new Intent(MainActivity.this, VoiceTxtActivity.class));
//                }
                if (!isDrag) {
                    if (recording) {
                        stop(false);
                    } else {
                        toSpeech();
                    }
                }
            }

            @Override
            public void onPressDown() {

            }
        });
        floatBtn.setOnLocationListener(new DragView.OnLocationListener() {
            @Override
            public void onLocation(int l, int t) {
                SPUtils.put(FLOATX, l);
                SPUtils.put(FLOATY, t);
                setVoiceBtnLocation();
            }
        });
        handler.sendEmptyMessageDelayed(0, 200);
        EventBus.getDefault().post(new Close());
        requestSdcard();
        playTts = SPUtils.get(PLAY_TTS, true);
        showPlayState();
        initSpeech();
        mViewPager.setCurrentItem(2);
    }
    private void initSpeech() {
        ttsHelper = new TTSHelper(this);
        ttsHelper.setOnTTSFinish(new TTSHelper.OnTTSFinish() {
            @Override
            public void onTTSFinish(int type) {
            }

            @Override
            public void onTTSstart() {

            }
        });
        SpeechUtility.createUtility(this,
                getResources().getString(R.string.xunfei_appid));
        initIfey();
    }
    public void toSpeech() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.requestEach(Manifest.permission.RECORD_AUDIO)
                .subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(Permission permission) throws Exception {
                        if (permission.granted) {
                            // 用户已经同意该权限
                            if (recording) {
                                Log.d(TAG, "record_view: stop();");
                                stop(true);
                            } else {
                                start();
                            }
                        } else if (permission.shouldShowRequestPermissionRationale) {
                            // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时。还会提示请求权限的对话框
//                            finish();
                        } else {
                            getPermission();
                            // 用户拒绝了该权限，而且选中『不再询问』那么下次启动时，就不会提示出来了，
                        }
                    }
                });
//                .subscribe(new Consumer<Boolean>() {
//            public void accept(Boolean aBoolean) {
//                if (aBoolean) {
//                    initSpeech();
//                } else {
//                    Toast.makeText(ConversationActivity.this, R.string.no_permission, Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
    }
    public void getPermission(){
        AlertDialog.Builder alertDialog=null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alertDialog = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            alertDialog = new AlertDialog.Builder(this);
        }
        alertDialog.setTitle("权限设置")
                .setMessage("应用缺乏录音权限，是否前往手动授予该权限？")
                .setPositiveButton("前往", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();
        alertDialog.show();
    }
    private void initIfey() {
        ifeyBtn = new IfeyVoiceWidget1(this);
        ifeyBtn.initIfey(new IfeyVoiceWidget1.MessageListener() {

            @Override
            public void onReceiverMessage(String content) {
                if (null != ifeyBtn) {
                    ifeyBtn.stop();
                }
                if (TextUtils.isEmpty(content)) {
                    return;
                }
                content = content.toLowerCase();
                handleContent(content);
                stop(true);
                Log.d(TAG, "onReceiverMessage: ");
            }

            @Override
            public void onStateChanged(boolean state) {
                if (state) {
//                    start();
                } else {
                    recording = false;
                    stop(false);
                    ifeyBtn.stop();
                }
            }
        }, null, false);
        ifeyBtn.setOnVolumeChangeListener(new IfeyVoiceWidget1.OnVolumeChangeListener() {
            @Override
            public void onVolumeChanged(int progress, short[] data) {
                Log.d(TAG, "onVolumeChanged: >>"+progress);
                showVolume(progress);
            }
        });
        ifeyBtn.setOnTextReceiverListener(new IfeyVoiceWidget1.OnTextReceiverListener() {
            @Override
            public void TextReceiver(String text) {
            }
        });
        anim = AnimationUtils.loadAnimation(this, R.anim.voice_view_anim);
    }
    private void handleContent(String content) {
        if (voiceLayout.getVisibility() == View.GONE && !TextUtils.isEmpty(content)) {
            voiceLayout.setVisibility(View.VISIBLE);
        }
        content = content.replace("，", "").replace("。", "").replace("！", "").replace("？", "");
        requestText.setText(content);
        request(content);
    }
    public void clickVoice() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.RECORD_AUDIO).subscribe(new Consumer<Boolean>() {
            public void accept(Boolean aBoolean) {
                if (aBoolean) {
                    if (recording) {
                        Log.d(TAG, "record_view: stop();");
                        stop(true);
                    } else {
                        start();
                    }
                } else {
                    Toast.makeText(MainActivity.this, R.string.no_permission, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void start() {
        ifeyBtn.start();
        handler.removeMessages(2);
        LinearInterpolator lir = new LinearInterpolator();
        anim.setInterpolator(lir);
        floatBtn.startAnimation(anim);
        if (ttsHelper.isSpeaking()) {
            ttsHelper.stop();
        }
        recording = true;
    }
    private void showVolume(int volume) {
        int p = (last + volume) / 2;
        last = p;
        index = volume;
        handler.sendEmptyMessage(1);
    }
    private void stop(boolean showDialog) {
        recording = false;
        handler.removeMessages(2);
//        setVoiceBtnLocation();
        ifeyBtn.stop();
        if (showDialog) {
            floatBtn.setBackgroundResource(R.drawable.home_dialog);
            AnimationDrawable anim = (AnimationDrawable) floatBtn.getBackground();
            anim.start();
        } else {
            floatBtn.setBackgroundResource(imgs[0]);
            floatBtn.clearAnimation();
        }
    }
    private boolean stoped;
    @Override
    public void onStop() {
        super.onStop();
        stopVoice();
    }
    private int[] imgs = {R.mipmap.speak_module0, R.mipmap.speak_module1, R.mipmap.speak_module2, R.mipmap.speak_module3, R.mipmap.speak_module4, R.mipmap.speak_module5, R.mipmap.speak_module6, R.mipmap.speak_module7, R.mipmap.speak_module7};
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

    private int last = 0;
    private int index = 0;
    Animation anim;
    private int current = 0;
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
//                int x = SPUtils.get(FLOATX, PPScreenUtils.getScreenWidth(MainActivity.this) - PPScreenUtils.dip2px(MainActivity.this, 120));
//                int y = SPUtils.get(FLOATY, PPScreenUtils.getScreenHeight(MainActivity.this) - PPScreenUtils.dip2px(MainActivity.this, 120));
//                floatBtn.setLocation(x, y);
                setVoiceBtnLocation();
            } else if(msg.what==1){
                handler.removeMessages(2);
                if (!recording) {
                    return;
                }
                if (index < 0) index = 0;
                if (index > imgs.length - 1) index = imgs.length - 1;
                floatBtn.setBackgroundResource(imgs[index]);
                handler.sendEmptyMessageDelayed(2, 100);
                current = index - 1;
            } else if (msg.what == 2) {
                if (!recording) {
                    return;
                }
                if (current < index - 1) {
                    current = index;
                }
                if (current < 0) current = 0;
                if (current > imgs.length - 1) current = imgs.length - 1;
                floatBtn.setBackgroundResource(imgs[current]);
                handler.sendEmptyMessageDelayed(2, 50);
            }
        }
    };

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

    private void setFragments() {
        mFragments = new ArrayList<>();
        mFragments.add(conversationListFragment);
        mFragments.add(contactFragment);
        mFragments.add(new AiFragment());
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
        setSelection(0);
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
        orderView.setVisibility(View.GONE);
        addLayout.setVisibility(View.GONE);
        aiLayout.setVisibility(View.GONE);
        topLayout.setVisibility(View.VISIBLE);
        switch (index) {
            case 0:
            case 1:
                addLayout.setVisibility(View.VISIBLE);
                hideVoice();
                break;
            case 2:
                topLayout.setVisibility(View.GONE);
                floatBtn.setVisibility(View.VISIBLE);
                aiLayout.setVisibility(View.VISIBLE);
                break;
            case 3:
                orderView.setVisibility(View.VISIBLE);
                hideVoice();
            case 4:
                hideVoice();
                break;

        }
        mTitle.setText(mTitleName[index]);
        mModules[index].setSelected(true);
        mModules[index].setPressed(true);
        mTVs[index].setSelected(true);
        mTVs[index].setPressed(true);
        mTVs[index].setTextColor(getResources().getColor(R.color.theme_color));
        currentIndex = index;
    }
    private void hideVoice(){
        closeVoice();
        floatBtn.setVisibility(View.GONE);
    }
    private boolean playTts;
    public void playTTsEvent() {
        playTts = !playTts;
        showPlayState();
        SPUtils.put(PLAY_TTS, playTts);
        if (!playTts && ttsHelper.isSpeaking()) {
            ttsHelper.stop();
        }
    }
    private void showPlayState() {
        if (playTts) {
            playView.setBackgroundResource(R.mipmap.sy_on);
        } else {
            playView.setBackgroundResource(R.mipmap.sy_of);
        }
    }
    private void closeVoice(){
        voiceLayout.setVisibility(View.GONE);
        stopVoice();
    }

    private void stopVoice(){
        if (recording) {
            stop(false);
        }
        Log.d(TAG, "stopVoice currentActivity: "+getCurrentActivity());
        if(this==getCurrentActivity()){
            if (null!=ttsHelper&&ttsHelper.isSpeaking()) {
                ttsHelper.stop();
            }
        }
    }

    @Event(value = R.id.voice_layout)
    private void voice_layout(View v) {
        closeVoice();
    }
    @Event(value = R.id.content_layout)
    private void content_layout(View v) {
    }
    @Event(value = R.id.play_tts)
    private void play_tts(View v) {
        playTTsEvent();
    }
    @Event(value = R.id.close)
    private void close(View v) {
        closeVoice();
    }
    @Event(value = R.id.item_b, type = View.OnTouchListener.class)
    private boolean touchB(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            setSelection(1);
        }
        return true;
    }
    @Event(value = R.id.item_ai, type = View.OnTouchListener.class)
    private boolean item_ai(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            setSelection(2);
        }
        return true;
    }

    @Event(value = R.id.item_c, type = View.OnTouchListener.class)
    private boolean touchC(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            setSelection(3);
        }
        return true;
    }

    @Event(value = R.id.item_d, type = View.OnTouchListener.class)
    private boolean touch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            setSelection(4);
        }

        return true;
    }

    @Event(value = R.id.item_a, type = View.OnTouchListener.class)
    private boolean touchA(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            setSelection(0);
        }
        return true;
    }

    @Event(value = R.id.add)
    private void add(View view) {
        showPopwindow(view);
    }

    @Event(value = R.id.search)
    private void search(View view) {
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

    @Event(value = R.id.order)
    private void order(View view) {
        startActivity(new Intent(this, WareOrderListActivity.class));
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
    private void setVoiceBtnLocation() {
        int maxX = PPScreenUtils.getScreenWidth(this) - PPScreenUtils.dip2px(this, 120);
        int maxY = PPScreenUtils.getScreenHeight(this) - PPScreenUtils.dip2px(this, 150);
        int x = SPUtils.get(FLOATX,PPScreenUtils.getScreenWidth(this)/2 - PPScreenUtils.dip2px(this, 60));
        int y = SPUtils.get(FLOATY, maxY);
        if (x > maxX) {
            x = maxX;
        }
        if (y > maxY) {
            y = maxY;
        }
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) floatBtn.getLayoutParams();
        layoutParams.leftMargin = x;
        layoutParams.topMargin = y;
//        layoutParams.rightMargin = -250;
//        layoutParams.bottomMargin = -250;
        floatBtn.setLayoutParams(layoutParams);
//        floatBtn.setLocation(x, y);
    }
    public void request(String key) {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.TALK);
//        params.addParameter("pid", choicePrice.getId() + "");
//        params.addParameter("user", account);
        params.addParameter("action", "talk");
        params.addParameter("key", key);

//       showProgress();
        Log.d(TAG, "loadData: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @SuppressLint("StringFormatMatches")
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: "+result);
                BaseCode<List<SmartOrder>> data = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<List<SmartOrder>>>() {
                        }.getType());
                if(data.getCode()==0){
                    List<SmartOrder> list = data.getData();
                    if (list.isEmpty()) {
                        return;
                    }
                    SmartOrder order = list.get(0);
                    String tts = order.getTts();
                    if(!TextUtils.isEmpty(tts)){
                        ttsTv.setText(tts);
                        if(playTts){
                            ttsHelper.start(0,MainActivity.this,tts);
                        }
                    }
                    String imgUrl = order.getImgurl();
                    if(!TextUtils.isEmpty(imgUrl)){
                        Glide.with(MainActivity.this).asBitmap().load(imgUrl).into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                ViewGroup.LayoutParams params = resultImageView.getLayoutParams();
                                int width = 0;
                                int height = 0;

                                int screenWidth = PPScreenUtils.getScreenWidth(MainActivity.this);
                                int maxW = (int) (screenWidth * .85);
                                if ((double) resource.getWidth() / (double) resource.getHeight() > 1.2) {
                                    width = maxW;
                                    height = resource.getHeight() * width / resource.getWidth();
                                } else {
                                    int calW = PPScreenUtils.dip2px(MainActivity.this, resource.getWidth());
                                    if (calW < maxW) {
                                        width = calW;
                                        height = PPScreenUtils.dip2px(MainActivity.this, resource.getHeight());
                                    }
                                }
                                params.width = width;
                                params.height = height;
                                resultImageView.setLayoutParams(params);
                                resultImageView.setImageBitmap(resource);
                                resultImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                                resultImageView.setVisibility(View.VISIBLE);
                            }
                        });
                        resultImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ImagePreviewActivity.startPreview(MainActivity.this,order.getImgurl());
                            }
                        });
                    }else {
                        resultImageView.setVisibility(View.GONE);
                    }
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
                floatBtn.setBackgroundResource(imgs[0]);
                floatBtn.clearAnimation();
//                setVoiceBtnLocation();
            }
        });
    }
    public void onEventMainThread(UpdateInfo message) {
        if(null!=meFragment){
            meFragment.setUserInfo();
        }
    }
}
