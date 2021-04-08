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
@MessageTag(value = "custom:friend_request", flag = MessageTag.ISCOUNTED | MessageTag.ISPERSISTED)
public class CustomizeMessage extends MessageContent {

    private String content;
    public CustomizeMessage(Parcel in) {
        content = ParcelUtils.readFromParcel(in);
    }
    public CustomizeMessage(){

    }
    public CustomizeMessage(byte[] data) {
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
    public static CustomizeMessage obtain(String content) {
        CustomizeMessage customizeMessage = new CustomizeMessage();
        customizeMessage.content = content;
        return customizeMessage;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeToParcel(dest, this.content);
    }

    public static final Creator<CustomizeMessage> CREATOR = new Creator<CustomizeMessage>() {
        public CustomizeMessage createFromParcel(Parcel source) {
            return new CustomizeMessage(source);
        }

        public CustomizeMessage[] newArray(int size) {
            return new CustomizeMessage[size];
        }
    };
}
