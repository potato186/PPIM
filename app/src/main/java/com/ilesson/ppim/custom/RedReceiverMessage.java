package com.ilesson.ppim.custom;

import android.annotation.SuppressLint;
import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import io.rong.common.ParcelUtils;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;

/**
 * Created by potato on 2020/3/12.
 */
@SuppressLint("ParcelCreator")
@MessageTag(value = "custom:red_receiver", flag = MessageTag.ISCOUNTED | MessageTag.ISPERSISTED)
public class RedReceiverMessage extends MessageContent {

    private String content;
    public RedReceiverMessage(Parcel in) {
        content = ParcelUtils.readFromParcel(in);
    }
    public RedReceiverMessage(){

    }
    public RedReceiverMessage(byte[] data) {
        String jsonStr = null;

        try {
            jsonStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e1) {

        }

        try {
            JSONObject jsonObj = new JSONObject(jsonStr);

            if (jsonObj.has("content"))
                content = jsonObj.optString("content");

        } catch (JSONException e) {
        }

    }
    @Override
    public byte[] encode() {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("content", this.content);
            return jsonObj.toString().getBytes(StandardCharsets.UTF_8);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getContent() {
        return content;
    }
    @Override
    public int describeContents() {
        return 0;
    }
    public static RedReceiverMessage obtain(String content) {
        RedReceiverMessage customizeMessage = new RedReceiverMessage();
        customizeMessage.content = content;
        return customizeMessage;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeToParcel(dest, this.content);
    }

    public static final Creator<RedReceiverMessage> CREATOR = new Creator<RedReceiverMessage>() {
        public RedReceiverMessage createFromParcel(Parcel source) {
            return new RedReceiverMessage(source);
        }

        public RedReceiverMessage[] newArray(int size) {
            return new RedReceiverMessage[size];
        }
    };
}
