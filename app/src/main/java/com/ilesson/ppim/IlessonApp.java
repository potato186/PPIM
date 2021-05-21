package com.ilesson.ppim;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ilesson.ppim.activity.FriendDetailActivity;
import com.ilesson.ppim.activity.ModifyFontActivity;
import com.ilesson.ppim.contactcard.IContactCardClickListener;
import com.ilesson.ppim.contactcard.message.ContactMessage;
import com.ilesson.ppim.contactcard.message.ContactMessageItemProvider;
import com.ilesson.ppim.custom.ComposeItemProvider;
import com.ilesson.ppim.custom.ComposeMessage;
import com.ilesson.ppim.custom.CustomizeMessage;
import com.ilesson.ppim.custom.ExpressItemProvider;
import com.ilesson.ppim.custom.ExpressMessage;
import com.ilesson.ppim.custom.MyExtensionModule;
import com.ilesson.ppim.custom.NewOrderItemProvider;
import com.ilesson.ppim.custom.NewOrderMessage;
import com.ilesson.ppim.custom.OrderConfirmMessage;
import com.ilesson.ppim.custom.OrderNotifyItemProvider;
import com.ilesson.ppim.custom.PPFileItemProvider;
import com.ilesson.ppim.custom.PPImageItemProvider;
import com.ilesson.ppim.custom.PPayItemProvider;
import com.ilesson.ppim.custom.PPayMessage;
import com.ilesson.ppim.custom.RedBackItemProvider;
import com.ilesson.ppim.custom.RedBackMessage;
import com.ilesson.ppim.custom.RedNotifyItemProvider;
import com.ilesson.ppim.custom.RedNotifyMessage;
import com.ilesson.ppim.custom.RedPacketItemProvider;
import com.ilesson.ppim.custom.RedPacketMessage;
import com.ilesson.ppim.custom.RedReceiverItemProvider;
import com.ilesson.ppim.custom.RedReceiverMessage;
import com.ilesson.ppim.custom.TransactionMessage;
import com.ilesson.ppim.custom.TransactionProvider;
import com.ilesson.ppim.custom.TransferItemProvider;
import com.ilesson.ppim.custom.TransferMessage;
import com.ilesson.ppim.custom.WaresGroupIntroItemProvider;
import com.ilesson.ppim.custom.WaresGroupMessage;
import com.ilesson.ppim.entity.Currency;
import com.ilesson.ppim.entity.FriendAccept;
import com.ilesson.ppim.entity.FriendRequest;
import com.ilesson.ppim.entity.PPUserInfo;
import com.ilesson.ppim.service.MyHttpManager;
import com.ilesson.ppim.utils.SPUtils;
import com.tencent.smtt.sdk.QbSdk;

import org.xutils.x;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.rong.eventbus.EventBus;
import io.rong.imkit.DefaultExtensionModule;
import io.rong.imkit.IExtensionModule;
import io.rong.imkit.RongExtensionManager;
import io.rong.imkit.RongIM;

/**
 * Created by potato on 2020/3/5.
 */

public class IlessonApp extends MultiDexApplication implements Application.ActivityLifecycleCallbacks{
    private static final String TAG = "IlessonApp";
    private static IlessonApp ilessonApp;
    public static final String FONT_SCALE="font_scale";
    @Override
    public void onCreate() {
        super.onCreate();


        init();
        changeFontSize();
//        closeAndroidPDialog();
        fontScale = getFontScale();
//        SDKInitializer.initialize(this);
        //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
//        SDKInitializer.setCoordType(CoordType.BD09LL);
    }
    public static Context getContext() {
        return ilessonApp.getApplicationContext();
    }

    public void init(){
        RongIM.init(this);
//        ZXingLibrary.initDisplayOpinion(this);
        x.Ext.init(this);
        x.Ext.setDebug(true);
        MyHttpManager.registerInstance();
        MultiDex.install(this);
        ilessonApp = this;
        RongIM.getInstance().setOnReceiveMessageListener((message, left) -> {
            Log.d("message", "IlessonApp onReceived: ="+message.getContent());
            if(message.getObjectName().equals("custom:pay_to_me")){
                EventBus.getDefault().post(new TransferMessage());
            }
            else if(message.getObjectName().equals("custom:pay")){
                PPayMessage payMessage = (PPayMessage) message.getContent();
                String content = payMessage.getContent();
                String[] arr = content.split("\\|");
                String currency = arr[2];
                TransferMessage transferMessage = new TransferMessage();
                transferMessage.setExtra(currency);
                EventBus.getDefault().post(transferMessage);

            }
            else if(message.getObjectName().equals("custom:user_pay")){
                TransferMessage transferMessage = (TransferMessage) message.getContent();
                EventBus.getDefault().post(transferMessage);
            }
            else if(message.getObjectName().equals("custom:friend_request")){
                EventBus.getDefault().post(new FriendRequest(message.getSenderUserId()));
            }else if(message.getObjectName().equals("custom:friend_accept")){
                EventBus.getDefault().post(new FriendAccept());
            }
            return false;
        });
        QbSdk.initX5Environment(this, new QbSdk.PreInitCallback() {
            @Override
            public void onCoreInitFinished() {
                Log.e("snow", "========onCoreInitFinished===");
            }

            @Override
            public void onViewInitFinished(boolean b) {
                Log.e("snow", "x5初始化结果====" + b);
            }
        });
        setMyExtensionModule();
        RongIM.setLocationProvider((context, locationCallback) -> {

        });
        RongIM.registerMessageType(CustomizeMessage.class);
//        RongIM.getInstance().registerMessageTemplate(new CustomizeMessageItemProvider());
        RongIM.registerMessageType(TransferMessage.class);
        RongIM.registerMessageTemplate(new TransferItemProvider());
        RongIM.registerMessageType(PPayMessage.class);
        RongIM.registerMessageTemplate(new PPayItemProvider());
        RongIM.registerMessageType(TransactionMessage.class);
        RongIM.registerMessageType(RedPacketMessage.class);
        RongIM.registerMessageTemplate(new TransactionProvider());
        RongIM.registerMessageType(RedNotifyMessage.class);
        RongIM.registerMessageTemplate(new RedNotifyItemProvider());
        RongIM.registerMessageType(RedReceiverMessage.class);
        RongIM.registerMessageTemplate(new RedReceiverItemProvider());
        RongIM.registerMessageTemplate(new WaresGroupIntroItemProvider());
        RongIM.registerMessageTemplate(new OrderNotifyItemProvider());
        RongIM.registerMessageTemplate(new ExpressItemProvider());
        RongIM.registerMessageTemplate(new NewOrderItemProvider());
        RongIM.registerMessageType(NewOrderMessage.class);
        RongIM.registerMessageType(RedBackMessage.class);
        RongIM.registerMessageType(ComposeMessage.class);
        RongIM.registerMessageType(ContactMessage.class);
        RongIM.registerMessageType(WaresGroupMessage.class);
        RongIM.registerMessageType(OrderConfirmMessage.class);
        RongIM.registerMessageType(ExpressMessage.class);
        RongIM.registerMessageTemplate(new ContactMessageItemProvider(new IContactCardClickListener() {
            @Override
            public void onContactCardClick(View view, ContactMessage content) {
                PPUserInfo userInfo = new PPUserInfo();
                userInfo.setPhone(content.getId());
                userInfo.setName(content.getName());
                userInfo.setIcon(content.getImgUrl());
                Intent intent = new Intent(getContext(), FriendDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(FriendDetailActivity.USER_INFO, userInfo);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }));
        RongIM.registerMessageTemplate(new RedBackItemProvider());
        RongIM.registerMessageTemplate(new PPFileItemProvider());
        RongIM.registerMessageTemplate(new ComposeItemProvider());
        RongIM.registerMessageTemplate(new PPImageItemProvider());
        RongIM.registerMessageTemplate(new RedPacketItemProvider());
//        ImageLoader.getInstance().clearDiskCache();
    }
    public static IlessonApp getInstance(){
        return ilessonApp;
    }

    private List<PPUserInfo> datas = new ArrayList<>();
    private List<Currency> currecys = new ArrayList<>();

    public List<Currency> getCurrecys() {
        return currecys;
    }

    public void setCurrecys(List<Currency> currecys) {
        this.currecys = currecys;
    }

    public List<PPUserInfo> getDatas() {
        return datas;
    }

    public PPUserInfo getUser(String name){
        for (PPUserInfo ppUserInfo:datas){
            if(name.equals(ppUserInfo.getName())){
                return ppUserInfo;
            }
        }
        return null;
    }
    public String getUserByPhone(String phone){
        return map.get(phone);
    }
    private Map<String,String> map=new HashMap<>();
    public void setDatas(List<PPUserInfo> datas) {
        this.datas = datas;
        map.clear();
        for(PPUserInfo info:datas){
            map.put(info.getPhone(),info.getPhone());
        }
    }

    public static Context getIlessonApp(){
        return ilessonApp;
    }
    public static String getCurProcessName(Context context) {

        int pid = android.os.Process.myPid();

        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager
                .getRunningAppProcesses()) {

            if (appProcess.pid == pid) {

                return appProcess.processName;
            }
        }
        return null;
    }
    public void setMyExtensionModule() {
        List<IExtensionModule> moduleList = RongExtensionManager.getInstance().getExtensionModules();
        IExtensionModule defaultModule = null;
        if (moduleList != null) {
            for (IExtensionModule module : moduleList) {
                if (module instanceof DefaultExtensionModule) {
                    defaultModule = module;
                    break;
                }
            }
            if (defaultModule != null) {
                RongExtensionManager.getInstance().unregisterExtensionModule(defaultModule);
            }
        }
        RongExtensionManager.getInstance().registerExtensionModule(new MyExtensionModule());
    }

    /**
     * 重写 getResource 方法，防止系统字体影响
     */
//    @Override
//    public Resources getResources() {//禁止app字体大小跟随系统字体大小调节
//        Resources resources = super.getResources();
//        ilessonApp = this;
//        if (resources != null && resources.getConfiguration().fontScale != 1.0f) {
//            android.content.res.Configuration configuration = resources.getConfiguration();
//            float scale = SPUtils.get(FONT_SCALE, 1.0f);
//            configuration.fontScale = 1.3f;
//            Log.d(TAG, "getResources: fontScale="+scale);
//            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
//        }
//        return resources;
//    }
    public void changeFontSize() {
        Configuration c = getResources().getConfiguration();
        c.fontScale = SPUtils.get(FONT_SCALE, 1.0f);
//        DisplayMetrics metrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(metrics);
//        metrics.scaledDensity = c.fontScale * metrics.density;
        getResources().updateConfiguration(c, getResources().getDisplayMetrics());
    }
    /**
     * 重启app
     * @param context
     */
    public static void restartApp(Context context) {
        PackageManager packageManager = context.getPackageManager();
        if (null == packageManager) {
            return;
        }
        final Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        }
    }

    private void closeAndroidPDialog(){
        try {
            Class aClass = Class.forName("android.content.pm.PackageParser$Package");
            Constructor declaredConstructor = aClass.getDeclaredConstructor(String.class);
            declaredConstructor.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Class cls = Class.forName("android.app.ActivityThread");
            Method declaredMethod = cls.getDeclaredMethod("currentActivityThread");
            declaredMethod.setAccessible(true);
            Object activityThread = declaredMethod.invoke(null);
            Field mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown");
            mHiddenApiWarningShown.setAccessible(true);
            mHiddenApiWarningShown.setBoolean(activityThread, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private List<Activity> activityList;
    private float fontScale;

    public static float getFontScale() {
        float fontScale = 1.0f;
        if (ilessonApp != null) {
            fontScale = SPUtils.get(FONT_SCALE, 1.0f);
        }
        return fontScale;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        if(activityList == null){
            activityList = new ArrayList<>();
        }
        // 禁止字体大小随系统设置变化
        Resources resources = activity.getResources();
        if (resources != null && resources.getConfiguration().fontScale != fontScale) {
            android.content.res.Configuration configuration = resources.getConfiguration();
            configuration.fontScale = fontScale;
            //设置大小
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        }
        activityList.add(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if(activityList != null){
            activityList.remove(activity);
        }
    }

    public static String getStringById(int res){
        return getContext().getResources().getString(res);
    }
    //调用该方法即可
    public static void setAppFontSize(float fontScale,Activity context) {
        if (ilessonApp != null) {
            List<Activity> activityList = ilessonApp.activityList;
            if (activityList != null) {
                for (Activity activity : activityList) {
                    //当前页面设置重启会闪黑屏
                    if (activity instanceof ModifyFontActivity) {
                        continue;
                    }
                    Resources resources = activity.getResources();
                    if (resources != null) {
                        android.content.res.Configuration configuration = resources.getConfiguration();
                        configuration.fontScale = fontScale;
                        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
                        activity.recreate();
                        if (fontScale != ilessonApp.fontScale) {
                            ilessonApp.fontScale = fontScale;
                            //保存设置后的字体大小
                            SPUtils.put(FONT_SCALE, fontScale);
                        }
                    }
                }
            }
        }
    }
    private TextView textView;

    public TextView getTextView() {
        return textView;
    }

    public void setTextView(TextView textView) {
        this.textView = textView;
    }
    private Map<Integer,String> itemMap;

    public Map<Integer, String> getItemMap() {
        return itemMap;
    }

    public void setItemMap(Map<Integer, String> map) {
        this.itemMap = map;
    }
    private boolean commonBuy;

    public boolean isCommonBuy() {
        return commonBuy;
    }

    public void setCommonBuy(boolean commonBuy) {
        this.commonBuy = commonBuy;
    }

    public boolean isActivityTop(Class cls){
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        String name = manager.getRunningTasks(1).get(0).topActivity.getClassName();
        return name.equals(cls.getName());
    }
}
