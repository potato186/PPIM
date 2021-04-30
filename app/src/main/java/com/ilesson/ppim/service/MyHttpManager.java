package com.ilesson.ppim.service;

import android.util.Log;
import android.widget.Toast;

import com.ilesson.ppim.IlessonApp;
import com.ilesson.ppim.R;
import com.ilesson.ppim.utils.NetCheckUtil;
import com.ilesson.ppim.utils.SPUtils;

import org.xutils.HttpManager;
import org.xutils.common.Callback;
import org.xutils.http.HttpMethod;
import org.xutils.http.HttpTask;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.lang.reflect.Type;

import static com.ilesson.ppim.activity.LoginActivity.TOKEN;
import static com.ilesson.ppim.crop.ProgressDialogFragment.TAG;

public class MyHttpManager implements HttpManager {
    private static final Object lock = new Object();
    private static volatile MyHttpManager instance;

    private MyHttpManager() {
    }

    public static void registerInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new MyHttpManager();
                }
            }
        }
        x.Ext.setHttpManager(instance);
    }

    @Override
    public <T> Callback.Cancelable get(RequestParams entity, Callback.CommonCallback<T> callback) {


        return request(HttpMethod.GET, entity, callback);
    }

    @Override
    public <T> Callback.Cancelable post(RequestParams entity, Callback.CommonCallback<T> callback) {
        return request(HttpMethod.POST, entity, callback);
    }
    private String token;
    private long lastTip;
    @Override
    public <T> Callback.Cancelable request(HttpMethod method, RequestParams entity, Callback.CommonCallback<T> callback) {
        Log.d(TAG, "request: "+entity.getUri());
        if(NetCheckUtil.checkNet(IlessonApp.getInstance())){
        }else{
            if(Math.abs(System.currentTimeMillis()-lastTip)>2000){
                Toast.makeText(IlessonApp.getInstance(), R.string.no_net,Toast.LENGTH_SHORT).show();
            }
        }
        lastTip = System.currentTimeMillis();
        setRequestHeader(entity);
        entity.setMethod(method);
        Callback.Cancelable cancelable = null;
        if (callback instanceof Callback.Cancelable) {
            cancelable = (Callback.Cancelable) callback;
        }
        HttpTask<T> task = new HttpTask<T>(entity, cancelable, callback);
        return x.task().start(task);
    }

    @Override
    public <T> T getSync(RequestParams entity, Class<T> resultType) throws Throwable {
        return requestSync(HttpMethod.GET, entity, resultType);
    }

    @Override
    public <T> T postSync(RequestParams entity, Class<T> resultType) throws Throwable {
        return requestSync(HttpMethod.POST, entity, resultType);
    }

    @Override
    public <T> T requestSync(HttpMethod method, RequestParams entity, Class<T> resultType) throws Throwable {

        MyHttpManager.DefaultSyncCallback<T> callback = new MyHttpManager.DefaultSyncCallback<T>(resultType);
        return requestSync(method, entity, callback);
    }

    private void setRequestHeader(RequestParams entity){
        if(null != entity){
            String token = SPUtils.get(TOKEN, "");
            entity.addHeader("produce","pp");
            entity.addHeader("channel","1001");
            entity.addHeader("token",token);
            entity.addHeader("authorization",token);
            entity.addHeader("version","2");
            entity.setConnectTimeout(60*1000);
        }
    }

    @Override
    public <T> T requestSync(HttpMethod method, RequestParams entity, Callback.TypedCallback<T> callback) throws Throwable {
        setRequestHeader(entity);
        entity.setMethod(method);
        HttpTask<T> task = new HttpTask<T>(entity, null, callback);
        return x.task().startSync(task);
    }

    private class DefaultSyncCallback<T> implements Callback.TypedCallback<T> {

        private final Class<T> resultType;

        public DefaultSyncCallback(Class<T> resultType) {
            this.resultType = resultType;
        }

        @Override
        public Type getLoadType() {
            return resultType;
        }

        @Override
        public void onSuccess(T result) {

        }

        @Override
        public void onError(Throwable ex, boolean isOnCallback) {
//            Toast.makeText(IlessonApp.getIlessonApp(),ex.getMessage(),Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCancelled(CancelledException cex) {

        }

        @Override
        public void onFinished() {

        }
    }
}
