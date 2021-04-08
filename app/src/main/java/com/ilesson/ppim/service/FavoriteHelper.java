package com.ilesson.ppim.service;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.SPUtils;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.util.List;

import io.rong.imkit.RongIM;
import io.rong.imlib.model.UserInfo;

public class FavoriteHelper {
    public static final String FILE_TAG = "{@file}";
    private static final String TAG = "FavoriteHelper";
    public static final int TYPE_IMAGE=1;
    public static final int TYPE_LINK=2;
    public static final int TYPE_FILE=3;
    public static final int TYPE_LOCATION=4;
    public static final int TYPE_CHAT_RECORD=5;
    public static final int TYPE_VOICE=6;
    public static final int TYPE_NOTE=7;
    public static final int TYPE_TEXT=8;
    public void post(String json, List<File> files) {
        RequestParams params = new RequestParams(Constants.FAV_URL + Constants.FAV);
        params.addParameter("json", json);
        if(null!=files&&files.size()>0){
            for(File file : files){
                params.addBodyParameter("files", file, "multipart/form-data");
            }
            params.setMultipart(true);
        }
        Log.d(TAG, "loadData: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: " + result);
//                BaseCode<PPUserInfo> base = new Gson().fromJson(
//                        result,
//                        new TypeToken<BaseCode<PPUserInfo>>() {
//                        }.getType());
//                if(base.getCode()==0){file:/storage/emulated/0/Pictures/WeiXin/wx_camera_1610975738217.jpg (No such file or directory)
//                }
            }


            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ex.printStackTrace();
            }


            @Override
            public void onCancelled(CancelledException cex) {
                cex.printStackTrace();
            }


            @Override
            public void onFinished() {
            }
        });
    }
}
