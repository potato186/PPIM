package com.ilesson.ppim.custom;


import android.os.Parcel;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import io.rong.common.ParcelUtils;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;


@MessageTag(value = "custom:transaction", flag = MessageTag.ISCOUNTED | MessageTag.ISPERSISTED)
public class TransactionMessage extends MessageContent{

    private String hasName;//用什么东西
    private String needName;//需要换什么东西
    private String hasNum;//用多少个
    private String needNum;//换多少个
    private String des;//描述
    private String userId;//用户id
    private String userName;//用户名

    public TransactionMessage(String hasName, String needName, String hasNum, String needNum, String des, String userId, String userName) {
        this.hasName = hasName;
        this.needName = needName;
        this.hasNum = hasNum;
        this.needNum = needNum;
        this.des = des;
        this.userId = userId;
        this.userName = userName;
    }

    /*
    *
    * 实现 encode() 方法，该方法的功能是将消息属性封装成 json 串，
    * 再将 json 串转成 byte 数组，该方法会在发消息时调用，如下面示例代码：
    * */
    @Override
    public byte[] encode() {
        JSONObject jsonObj = new JSONObject();

        try {
            jsonObj.put("needName", this.getNeedName());
            jsonObj.put("hasName", this.getHasName());
            jsonObj.put("hasNum", this.getHasNum());
            jsonObj.put("needNum", this.getNeedNum());
            jsonObj.put("des", this.getDes());
            jsonObj.put("userId", this.getUserId());
            jsonObj.put("userName", this.getUserName());

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
    public TransactionMessage(){

    }
    /*
    * 覆盖父类的 MessageContent(byte[] data) 构造方法，该方法将对收到的消息进行解析，
    * 先由 byte 转成 json 字符串，再将 json 中内容取出赋值给消息属性。
    * */
    public TransactionMessage(byte[] data) {
        String jsonStr = null;

        try {
            jsonStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        try {
            JSONObject jsonObj = new JSONObject(jsonStr);
            if (jsonObj.has("hasName"))
                setHasName(jsonObj.optString("hasName"));

            if (jsonObj.has("des"))
                setDes(jsonObj.optString("des"));
            if (jsonObj.has("needName"))
                setNeedName(jsonObj.optString("needName"));
            if (jsonObj.has("hasNum"))
                setHasNum(jsonObj.optString("hasNum"));
            if (jsonObj.has("needNum"))
                setNeedNum(jsonObj.optString("needNum"));
            if (jsonObj.has("userId"))
                setUserId(jsonObj.optString("userId"));
            if (jsonObj.has("userName"))
                setUserName(jsonObj.optString("userName"));

        } catch (JSONException e) {
            Log.d("JSONException", e.getMessage());
        }
    }

    //给消息赋值。
    public TransactionMessage(Parcel in) {

        setNeedName(ParcelUtils.readFromParcel(in));//该类为工具类，消息属性
        //这里可继续增加你消息的属性
        setHasName(ParcelUtils.readFromParcel(in));//该类为工具类，消息属性
        setDes(ParcelUtils.readFromParcel(in));
        setHasNum(ParcelUtils.readFromParcel(in));
        setNeedNum(ParcelUtils.readFromParcel(in));
        setUserId(ParcelUtils.readFromParcel(in));
        setUserName(ParcelUtils.readFromParcel(in));
    }

    /**
     * 读取接口，目的是要从Parcel中构造一个实现了Parcelable的类的实例处理。
     */
    public static final Creator<TransactionMessage> CREATOR = new Creator<TransactionMessage>() {

        @Override
        public TransactionMessage createFromParcel(Parcel source) {
            return new TransactionMessage(source);
        }

        @Override
        public TransactionMessage[] newArray(int size) {
            return new TransactionMessage[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * 将类的数据写入外部提供的 Parcel 中。
     * @param dest  对象被写入的 Parcel。
     * @param flags 对象如何被写入的附加标志。
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeToParcel(dest, getNeedName());
        ParcelUtils.writeToParcel(dest, getHasName());
        ParcelUtils.writeToParcel(dest, getDes());
        ParcelUtils.writeToParcel(dest, getHasNum());
        ParcelUtils.writeToParcel(dest, getNeedNum());
        ParcelUtils.writeToParcel(dest, getUserId());
        ParcelUtils.writeToParcel(dest, getUserName());
    }

    public String getHasName() {
        return hasName;
    }

    public void setHasName(String hasName) {
        this.hasName = hasName;
    }

    public String getNeedName() {
        return needName;
    }

    public void setNeedName(String needName) {
        this.needName = needName;
    }

    public String getHasNum() {
        return hasNum;
    }

    public void setHasNum(String hasNum) {
        this.hasNum = hasNum;
    }

    public String getNeedNum() {
        return needNum;
    }

    public void setNeedNum(String needNum) {
        this.needNum = needNum;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "TransactionMessage{" +
                "hasName='" + hasName + '\'' +
                ", needName='" + needName + '\'' +
                ", hasNum='" + hasNum + '\'' +
                ", needNum='" + needNum + '\'' +
                ", des='" + des + '\'' +
                ", userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }
}
