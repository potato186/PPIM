package com.ilesson.ppim.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ilesson.ppim.R;
import com.ilesson.ppim.db.DatabaseManager;
import com.ilesson.ppim.utils.IMUtils;
import com.ilesson.ppim.utils.PPScreenUtils;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.utils.SetFont;

import org.xutils.ex.DbException;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.RongIM;


/**
 * Created by potato on 2019/4/9.
 */
@ContentView(R.layout.act_setting)
public class SettingActivity extends BaseActivity{
    @ViewInject(R.id.voice_name)
    private TextView voiceName;
    public static final int LOGIN_OUT = 38923;
    public static final String VOICE_NAME = "voice_name";
    public static final String XUNFEI = "讯飞";
    public static final String TENCEN = "腾讯";
    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setStatusBarLightMode(this,true);
        String type = SPUtils.get(VOICE_NAME,getResources().getString(R.string.xunfei));
        voiceName.setText(type);
//        mVisionTextView = findViewById(R.id.version);
//        mSharedPreferences = getSharedPreferences(getPackageName(),
//                MODE_PRIVATE);
//        View newVersion = findViewById(R.id.new_version_tip);
//        String name = getAppVersionName(this);
//        mVisionTextView.setText(name);
//        if(UpdateHelper.needUpdate){
//            newVersion.setVisibility(View.VISIBLE);
//        }
    }

    @Event(R.id.loginout_view)
    private void loginout(View view){
        showQuitDialog();
    }
    @Event(R.id.blacklist)
    private void blacklist(View view){
        startActivity(new Intent(this,BlackListActivity.class));
    }
    @Event(R.id.back_btn)
    private void back_btn(View view){
        finish();
    }
    @Event(R.id.user_police)
    private void user_police(View view){
        Intent intent = new Intent(this, PrivateActivity.class);
        intent.putExtra(PrivateActivity.USER_POLICE,true);
        startActivity(intent);
    }
    @Event(R.id.private_case)
    private void private_case(View view){
        Intent intent = new Intent(this, PrivateActivity.class);
        startActivity(intent);
    }
    @Event(R.id.account_scurity)
    private void account_scurity(View view){
        Intent intent = new Intent(this,SecurityActivity.class);
        startActivity(intent);
    }
    @Event(R.id.about_us)
    private void about_us(View view){
        Intent intent = new Intent(this,AboutActivity.class);
        startActivity(intent);
    }
    @Event(R.id.font)
    private void font(View view){
        Intent intent = new Intent(this,TextSizeShowActivity.class);
        startActivity(intent);
    }
    @Event(R.id.feedback)
    private void feedback(View view){
        Intent intent = new Intent(this,FeedBackActivity.class);
        startActivity(intent);
    }

    @Event(R.id.check_voice)
    private void check_voice(View view){
        showDialog();
    }

    private void showQuitDialog(){
        View view = getLayoutInflater().inflate(R.layout.practice_dialog,null);
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        android.view.WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        p.width = (int) (PPScreenUtils.getScreenWidth(this) * 0.85);
        p.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setAttributes(p);
        TextView scoreTv = (TextView) view.findViewById(R.id.content);
        scoreTv.setText(R.string.ask_out);
        view.findViewById(R.id.left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.right_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IMUtils().resetUserInfo();
                dialog.dismiss();
                RongIM.getInstance().disconnect();
                try {
                    DatabaseManager.getInstance().dropDb();
                } catch (DbException e) {
                    e.printStackTrace();
                }
                setResult(LOGIN_OUT,new Intent());
                finish();
            }
        });
        dialog.show();
    }

    public void onEventMainThread(SetFont event) {
        finish();
    }
    private class CountryAdapter extends BaseAdapter {

        private List<String> data;

        private CountryAdapter(List<String> data) {
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int i) {
            return data.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            final String content = data.get(i);
            ViewHolder holder = null;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.voice_type_item, null);
                holder = new ViewHolder();
                holder.textView = view.findViewById(R.id.textView);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            holder.textView.setText(content);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    voiceName.setText(content);
                    SPUtils.put(VOICE_NAME,content);
                    mDialog.dismiss();
                }
            });
            return view;
        }
    }

    class ViewHolder {
        private TextView textView;
    }
    private Dialog mDialog;

    private void showDialog() {
        mDialog = new Dialog(this);
        mDialog.setCanceledOnTouchOutside(false);
        Window window = mDialog.getWindow();
        window.setGravity(Gravity.CENTER);
        View view = View.inflate(this, R.layout.country_layout, null);
        ListView listView = view.findViewById(R.id.country_list);
        List<String> list = new ArrayList<>();
        list.add(XUNFEI);
        list.add(TENCEN);
        listView.setAdapter(new CountryAdapter(list));
        window.setContentView(view);
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);//设置横向全屏
        window.setBackgroundDrawableResource(android.R.color.transparent);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.show();
    }
}
