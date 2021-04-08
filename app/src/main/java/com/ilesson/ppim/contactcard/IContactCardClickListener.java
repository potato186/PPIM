package com.ilesson.ppim.contactcard;

import android.view.View;

import com.ilesson.ppim.contactcard.message.ContactMessage;


/**
 * 单击名片消息，展示联系人详情信息的接口
 * Created by Beyond on 05/01/2017.
 */

public interface IContactCardClickListener {
    void onContactCardClick(View view, ContactMessage content);
}
