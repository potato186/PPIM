package com.ilesson.ppim.fragment;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import com.ilesson.ppim.R;
import com.ilesson.ppim.activity.LoginActivity;
import com.ilesson.ppim.activity.MainActivity;
import com.ilesson.ppim.activity.ResetPwdActivity;
import com.ilesson.ppim.utils.SPUtils;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;

import static com.ilesson.ppim.activity.BootActivity.GUIDE_STATE;
import static com.ilesson.ppim.activity.ResetPwdActivity.RESET_FROM_BOOT;
import static com.ilesson.ppim.activity.ResetPwdActivity.RESET_LOGIN_PWD;

/**
 * Created by potato on 2016/4/12.
 */
@ContentView(R.layout.fragment_guide2)
public class GuideFragment2 extends BaseFragment {
    private static final String TAG = "GuideFragment2";

    @Event(R.id.experience_now)
    private void experience(View view){
        SPUtils.put(GUIDE_STATE,"true");
        String token = SPUtils.get("token", "");
        String btoken = SPUtils.get("bToken", "");
        if (!TextUtils.isEmpty(token)&&!TextUtils.isEmpty(btoken)) {
            Intent intent = new Intent(getActivity(), ResetPwdActivity.class);
            intent.putExtra(RESET_LOGIN_PWD,true);
            intent.putExtra(RESET_FROM_BOOT,true);
            startActivity(intent);
            getActivity().finish();
            return;
        }
        if (TextUtils.isEmpty(token)) {
            startActivity(new Intent(getActivity(), LoginActivity.class));
        } else {
            startActivity(new Intent(getActivity(),MainActivity.class));
        }
        getActivity().finish();
    }
}
