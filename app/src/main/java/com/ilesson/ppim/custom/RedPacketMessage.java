package com.ilesson.ppim.custom;


import android.os.Parcel;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import io.rong.common.ParcelUtils;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;


@MessageTag(value = "custom:redpacket", flag = MessageTag.ISCOUNTED | MessageTag.ISPERSISTED)
public class RedPacketMessage extends MessageContent {

    private String redpacketId;
    private String senderName;
    private String senderIcon;
    private String des;
    /*
    *
    * 实现 encode() 方法，该方法的功能是将消息属性封装成 json 串，
    * 再将 json 串转成 byte 数组，该方法会在发消息时调用，如下面示例代码：
    * */
    @Override
    public byte[] encode() {
        JSONObject jsonObj = new JSONObject();

        try {
            jsonObj.put("redpacketId", this.getRedpacketId());
            jsonObj.put("des", this.getDes());
            jsonObj.put("senderName", this.getSenderName());
            jsonObj.put("senderIcon", this.getSenderIcon());

        } catch (JSONException e) {
            Log.e("JSONException", e.getMessage());
        }

        try {
            return jsonObj.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
    public RedPacketMessage(){

    }
    /*
    * 覆盖父类的 MessageContent(byte[] data) 构造方法，该方法将对收到的消息进行解析，
    * 先由 byte 转成 json 字符串，再将 json 中内容取出赋值给消息属性。
    * */
    public RedPacketMessage(byte[] data) {
        String jsonStr = null;

        try {
            jsonStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        try {
            JSONObject jsonObj = new JSONObject(jsonStr);


            if (jsonObj.has("des"))
                setDes(jsonObj.optString("des"));

            if (jsonObj.has("redpacketId"))
                setRedpacketId(jsonObj.optString("redpacketId"));
            if (jsonObj.has("senderName"))
                setSenderName(jsonObj.optString("senderName"));
            if (jsonObj.has("senderIcon"))
                setSenderIcon(jsonObj.optString("senderIcon"));

        } catch (JSONException e) {
            Log.d("JSONException", e.getMessage());
        }
    }

    //给消息赋值。
    public RedPacketMessage(Parcel in) {

        setRedpacketId(ParcelUtils.readFromParcel(in));//该类为工具类，消息属性
        //这里可继续增加你消息的属性
        setDes(ParcelUtils.readFromParcel(in));//该类为工具类，消息属性
        setSenderName(ParcelUtils.readFromParcel(in));//该类为工具类，消息属性
        setSenderIcon(ParcelUtils.readFromParcel(in));//该类为工具类，消息属性
    }

    /**
     * 读取接口，目的是要从Parcel中构造一个实现了Parcelable的类的实例处理。
     */
    public static final Creator<RedPacketMessage> CREATOR = new Creator<RedPacketMessage>() {

        @Override
        public RedPacketMessage createFromParcel(Parcel source) {
            return new RedPacketMessage(source);
        }

        @Override
        public RedPacketMessage[] newArray(int size) {
            return new RedPacketMessage[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * 将类的数据写入外部提供的 Parcel 中。
     *
     * @param dest  对象被写入的 Parcel。
     * @param flags 对象如何被写入的附加标志。
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeToParcel(dest, getRedpacketId());
        ParcelUtils.writeToParcel(dest, getDes());
        ParcelUtils.writeToParcel(dest, getSenderName());
        ParcelUtils.writeToParcel(dest, getSenderIcon());
    }

    public String getRedpacketId() {
        return redpacketId;
    }

    public void setRedpacketId(String redpacketId) {
        this.redpacketId = redpacketId;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderIcon() {
        return senderIcon;
    }

    public void setSenderIcon(String senderIcon) {
        this.senderIcon = senderIcon;
    }
}
