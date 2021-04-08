package com.ilesson.ppim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.ilesson.ppim.R;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;

/**
 * Created by potato on 2020/3/10.
 */
@ContentView(R.layout.activity_add_friend_select)
public class SelectAddFriendActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarLightMode(this,true);
    }

    @Event(value = R.id.back_btn)
    private void back_btn(View view) {
        finish();
    }
    @Event(value = R.id.search_edit)
    private void search_edit(View view) {
        startActivity(new Intent(this,SearchFriendActivity.class));
    }
}
