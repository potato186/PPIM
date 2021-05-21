package com.ilesson.ppim.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ilesson.ppim.R;
import com.ilesson.ppim.activity.LoginActivity;
import com.ilesson.ppim.activity.MainActivity;
import com.ilesson.ppim.activity.MyCollectActivity;
import com.ilesson.ppim.activity.ScoreListActivity;
import com.ilesson.ppim.activity.SettingActivity;
import com.ilesson.ppim.activity.UserDetailActivity;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.utils.WxShareUtils;
import com.ilesson.ppim.view.CircleImageView;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;

import org.devio.takephoto.model.InvokeParam;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import io.rong.eventbus.EventBus;


/**
 * Created by potato on 2016/4/12.
 */
@ContentView(R.layout.frag_me)
public class MeFragment extends BaseFragment {
    private static final String TAG = "MeFragment";
    private MainActivity mainActivity;

    @ViewInject(R.id.user_icon)
    public CircleImageView userIcon;
    @ViewInject(R.id.user_nike)
    private TextView userNike;
    @ViewInject(R.id.user_phone)
    private TextView userPhone;
    private String iconPath;

//    private TakePhoto takePhoto;
    private InvokeParam invokeParam;
    private boolean needFresh;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUserInfo();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e("BaseFragment", "onCreateView");

        return x.view().inject(this, inflater, container);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void setUserInfo() {
        String icon = SPUtils.get(LoginActivity.USER_ICON, "");
        if (null == userIcon) {
            return;
        }
        if (!TextUtils.isEmpty(icon)) {
//            DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
//            builder.cacheInMemory(true).cacheOnDisk(true);
//            ImageLoader.getInstance().displayImage(icon, userIcon,
//                    builder.build());
//            userIcon.setAvatar(icon,R.mipmap.default_icon);
            Glide.with(getActivity()).load(icon).into(userIcon);
        }
        String name = SPUtils.get(LoginActivity.USER_NAME, "");
        userNike.setText(name);
        String phone = SPUtils.get(LoginActivity.USER_PHONE, "");
        userPhone.setText(phone);
        needFresh = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != userNike && needFresh) {
            setUserInfo();
        }
    }


    @Event(value = R.id.share_layout)
    private void share(View view) {
        showShareDialog();
    }
    @Event(value = R.id.collect)
    private void collect(View view) {
        startActivity(new Intent(getActivity(), MyCollectActivity.class));
    }
    @Event(value = R.id.integral_layout)
    private void integral_layout(View view) {
        startActivity(new Intent(getActivity(), ScoreListActivity.class));
    }
    @Event(value = R.id.user_info_view)
    private void userDetail(View view) {
        mainActivity.startActivityForResult(new Intent(getActivity(), UserDetailActivity.class),0);
    }

    @Event(value = R.id.more_layout)
    private void setting(View view) {
        mainActivity.startActivityForResult(new Intent(getActivity(), SettingActivity.class), 0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void showShareDialog() {
        if (mShareDialog == null) {
            initShareDialog();
        }
        mShareDialog.show();
    }
    /**
     * 初始化分享弹出框
     */
    private Dialog mShareDialog;

    private void initShareDialog() {
        mShareDialog = new Dialog(getActivity(), R.style.dialog_bottom_full);
        mShareDialog.setCanceledOnTouchOutside(true);
        mShareDialog.setCancelable(true);
        Window window = mShareDialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.share_animation);
        View view = View.inflate(getActivity(), R.layout.lay_share, null);
        view.findViewById(R.id.weixin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                share(SendMessageToWX.Req.WXSceneSession);
            }
        });
        view.findViewById(R.id.pyq).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                share(SendMessageToWX.Req.WXSceneTimeline);
            }
        });
        view.findViewById(R.id.favorite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                share(SendMessageToWX.Req.WXSceneFavorite);
            }
        });
        window.setContentView(view);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);//设置横向全屏
    }
    private void share(int type){
        dismissShareDialog();
        WxShareUtils.shareWeb(getActivity(),type,"","",mainActivity.getResources().getString(R.string.app_des));
    }
    private void dismissShareDialog() {
        if (mShareDialog != null&&mShareDialog.isShowing()) {
            mShareDialog.dismiss();
        }
    }
}
