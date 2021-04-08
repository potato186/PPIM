package com.ilesson.ppim.activity;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.RongUserInfo;
import com.ilesson.ppim.utils.BitmapUtils;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.view.RoundImageView;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import io.rong.imageloader.core.DisplayImageOptions;
import io.rong.imageloader.core.ImageLoader;

import static com.ilesson.ppim.activity.ChatInfoActivity.GROUP_ID;
import static com.ilesson.ppim.activity.LoginActivity.USER_ICON;
import static com.ilesson.ppim.activity.LoginActivity.USER_NAME;

/**
 * Created by potato on 2020/3/11.
 */
@ContentView(R.layout.activity_user_code)
public class UserCodeActivity extends BaseActivity {
    @ViewInject(R.id.code)
    private ImageView imageView;
    @ViewInject(R.id.user_icon)
    private RoundImageView userIcon;
    @ViewInject(R.id.user_name)
    private TextView userName;
    @ViewInject(R.id.title)
    private TextView title;
    @ViewInject(R.id.tips)
    private TextView tips;
    @Event(R.id.back_btn)
    private void back(View v){
        finish();
    }
    private static final String TAG = "UserCodeActivity";
    private static final String GROUP_INFO = "group_info";
    private DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarLightMode(this, true);
        String groupId = getIntent().getStringExtra(GROUP_ID);
        String token = SPUtils.get("token","");
        String result = Constants.BASE_URL + Constants.CODE_URL;
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.CODE_URL);
        if(!TextUtils.isEmpty(groupId)){
            title.setText(R.string.code_group);
            result+="?action=group&token="+token+"&group="+groupId;
            searchGroupInfo(token,groupId);
            tips.setText(R.string.scan_add_group);
        }else{
            result+="?action=private&token="+token;
            String icon = SPUtils.get(USER_ICON,"");
            String name = SPUtils.get(USER_NAME,"");
            showGourpInfo(icon,name);
        }
//        Glide.with(this).asBitmap().load(result).into(new SimpleTarget<Bitmap>() {
//            @Override
//            public void onResourceReady(@NonNull final Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
//                imageView.setImageBitmap(resource);
//                imageView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        BitmapUtils.saveImage(UserCodeActivity.this,resource);
//                    }
//                });
//            }
//
//            @Override
//            public void onLoadFailed(@Nullable Drawable errorDrawable) {
//                super.onLoadFailed(errorDrawable);
//            }
//        });
        /*Glide.with(this)
                .load(result)
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        final Bitmap bitmap = ((BitmapDrawable)resource).getBitmap();
                        imageView.setImageBitmap(bitmap);
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BitmapUtils.saveImage(UserCodeActivity.this,bitmap);
                            }
                        });
                    }


                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {


                    }


                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        Log.d(TAG, "onLoadFailed: "+errorDrawable);

                    }
                });*/


        x.image().bind(imageView,result, new Callback.CommonCallback<Drawable>() {
            @Override
            public void onSuccess(Drawable drawable) {
                final Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
                imageView.setImageBitmap(bitmap);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BitmapUtils.saveImage(UserCodeActivity.this,bitmap);
                    }
                });
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
    public void searchGroupInfo(String token, final String userId) {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.RONG_URL);
        params.addParameter("action", "group");
        params.addParameter("token", token);
        params.addParameter("target", userId);
        Log.d(TAG, "searchUserInfo: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: =" + result);
                BaseCode<RongUserInfo> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<RongUserInfo>>() {
                        }.getType());
                if (base.getCode() == 0) {
                    RongUserInfo info = base.getData();
                    showGourpInfo(info.getIcon(),info.getName());
                } else {
                }
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
    private void showGourpInfo(String icon,String name){
        userName.setText(name);
        builder.cacheInMemory(true).cacheOnDisk(true);
        ImageLoader.getInstance().displayImage(icon, userIcon,
                builder.build());
    }
}
