package com.ilesson.ppim.activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.crop.CropActivity;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.PPUserInfo;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.SPUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.devio.takephoto.model.CropOptions;
import org.devio.takephoto.model.InvokeParam;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.File;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.rong.imageloader.core.DisplayImageOptions;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.UserInfo;
import io.rong.photoview.PhotoViewAttacher;

import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;
import static com.ilesson.ppim.activity.LoginActivity.USER_ICON;
import static com.ilesson.ppim.activity.TakePhoto.RESULT_PATH;

/**
 * Created by potato on 2020/3/11.
 */
@ContentView(R.layout.activity_avatar)
public class AvatarActivity extends BaseActivity
//        implements TakePhoto.TakeResultListener, InvokeListener
{
    @ViewInject(R.id.preview_image)
    private ImageView imageView;

    @Event(R.id.back_btn)
    private void back(View v) {
        setInfo();
    }
    @Event(R.id.menu)
    private void menu(View v) {
        showPopwindow();
        openPopWindow();
    }

    private static final String TAG = "AvatarActivity";
    private String token = "";
    private PopupWindow popupWindow;
    private View contentView;
    private boolean hasModify;
    private String userIcon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        token = SPUtils.get(LOGIN_TOKEN, "");
        userIcon = SPUtils.get(USER_ICON, "");
        showImage(userIcon);

    }

    private static final int ICON_CAMREA = 1023;
    private static final int ICON_LOCAL_PIC = 1024;
    public void startCamrea() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Boolean aBoolean) {
                if (aBoolean) {
                    Intent intent = new Intent(AvatarActivity.this, TakePhoto.class);
                    startActivityForResult(intent, ICON_CAMREA);
                } else {
                    goIntentSetting();
                    Toast.makeText(AvatarActivity.this,R.string.tip_permission_camera,Toast.LENGTH_LONG).show();
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
    public void startPICK() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Consumer<Boolean>() {
            public void accept(Boolean aBoolean) {
                if(aBoolean){
                    Intent intent = new Intent("android.intent.action.PICK");
                    intent.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(intent, ICON_LOCAL_PIC);
                }
            }
        });
    }
    private void showImage(String path) {
        final PhotoViewAttacher mAttacher = new PhotoViewAttacher(imageView);
        Glide.with(this).asBitmap().load(path).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                imageView.setImageBitmap(resource);
                mAttacher.update();
            }
        });
    }
    public void modify(String icon) {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.USER_URL);
        params.addQueryStringParameter("token", token);
        params.addQueryStringParameter("action", "modify");
        params.addQueryStringParameter("icon", "png");
        params.addQueryStringParameter("name", "");
        params.addBodyParameter("file", new File(icon), "image/jpg");
        params.setMultipart(true);
        showProgress();
        Log.d(TAG, "loadData: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: " + result);
                BaseCode<PPUserInfo> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<PPUserInfo>>() {
                        }.getType());
                if(base.getCode()==0){
                    PPUserInfo info = base.getData();
                    DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
                    builder.cacheInMemory(true).cacheOnDisk(true);
                    ImageLoader.getInstance().displayImage(info.getIcon(), imageView,
                            builder.build());
                    userIcon = info.getIcon();
                    SPUtils.put(LoginActivity.USER_ICON,info.getIcon());
                    UserInfo userInfo = new UserInfo(info.getPhone(), info.getName(), Uri.parse(info.getIcon()));
                    RongIM.getInstance().refreshUserInfoCache(userInfo);
                    showImage(info.getIcon());
                    hasModify = true;
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
                hideProgress();
            }
        });
    }
    private InvokeParam invokeParam;
    private TakePhoto takePhoto;
    public static final int MODIFY_SUCCESS = 23;

    private void setInfo() {
        finish();
    }

    public static final int RESULT_CODE=22;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(null==data){
            return;
        }
        if (requestCode == ICON_CAMREA) {
            String p = data.getStringExtra(RESULT_PATH);
            Uri selectedUri = Uri.fromFile(new File(p));
            if (selectedUri != null) {
                Intent it = new Intent(AvatarActivity.this, CropActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(CropActivity.CROP_IMG_URI, selectedUri);
                it.putExtras(bundle);
                startActivityForResult(it, 10);
            }
        } else if (requestCode == ICON_LOCAL_PIC) {
            final Uri selectedUri = data.getData();
            if (selectedUri != null) {
                Intent it = new Intent(AvatarActivity.this, CropActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(CropActivity.CROP_IMG_URI, selectedUri);
                it.putExtras(bundle);
                startActivityForResult(it, 10);
            }
        } else if (resultCode == CropActivity.REQUEST_CROP) {
            String iconPath = data.getStringExtra(CropActivity.CROP_IMG_URI);
            modify(iconPath);
        }
    }

    private CropOptions getCropOptions() {
        int height = 300;
        int width = 300;

        CropOptions.Builder builder = new CropOptions.Builder();
        builder.setAspectX(width).setAspectY(height);
        builder.setWithOwnCrop(false);
        return builder.create();
    }

    private void showPopwindow() {
        //加载弹出框的布局
        contentView = LayoutInflater.from(this).inflate(
                R.layout.select_user_icon_pop, null);


        popupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);// 取得焦点
        //注意  要是点击外部空白处弹框消息  那么必须给弹框设置一个背景色  不然是不起作用的
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        //点击外部消失
        popupWindow.setOutsideTouchable(true);
        //设置可以点击
        popupWindow.setTouchable(true);
        //进入退出的动画，指定刚才定义的style
        popupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);
        contentView.findViewById(R.id.camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                fromCamera(true);
                startCamrea();
                popupWindow.dismiss();
            }
        });
        contentView.findViewById(R.id.album).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                fromCamera(false);
                startPICK();
                popupWindow.dismiss();
            }
        });
        contentView.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    private void dissPopWindow(){
        if(null!=popupWindow){
            popupWindow.dismiss();
        }
    }
    public void openPopWindow() {
        //从底部显示
        popupWindow.showAtLocation(contentView, Gravity.BOTTOM, 0, 0);
    }

    @Override
    public void onBackPressed() {
        setInfo();
        super.onBackPressed();
        Log.d(TAG, "onBackPressed: ");
    }
}
