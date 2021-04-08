package com.ilesson.ppim.custom;


import android.os.Parcel;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import io.rong.common.ParcelUtils;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;


@MessageTag(value = "custom:compose", flag = MessageTag.ISCOUNTED | MessageTag.ISPERSISTED)
public class ComposeMessage extends MessageContent {

    private String senderName;
    private String senderIcon;
    private String title;
    private String score;
    private String count;
    private String grade;
    private String uuid;
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
            jsonObj.put("title", this.getTitle());
            jsonObj.put("score", this.getScore());
            jsonObj.put("count", this.getCount());
            jsonObj.put("grade", this.getGrade());
            jsonObj.put("des", this.getDes());
            jsonObj.put("uuid", this.getUuid());
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
    public ComposeMessage(){

    }
    /*
    * 覆盖父类的 MessageContent(byte[] data) 构造方法，该方法将对收到的消息进行解析，
    * 先由 byte 转成 json 字符串，再将 json 中内容取出赋值给消息属性。
    * */
    public ComposeMessage(byte[] data) {
        String jsonStr = null;

        try {
            jsonStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        try {
            JSONObject jsonObj = new JSONObject(jsonStr);

            if (jsonObj.has("uuid")){
                setUuid(jsonObj.optString("uuid"));
            }
            if (jsonObj.has("count")){
                setCount(jsonObj.optString("count"));
            }
            if (jsonObj.has("grade")){
                setGrade(jsonObj.optString("grade"));
            }
            if (jsonObj.has("des")){
                setDes(jsonObj.optString("des"));
            }
            if (jsonObj.has("score")){
                setScore(jsonObj.optString("score"));
            }
            if (jsonObj.has("title")){
                setTitle(jsonObj.optString("title"));
            }
            if (jsonObj.has("senderName")){
                setSenderName(jsonObj.optString("senderName"));
            }
            if (jsonObj.has("senderIcon")){
                setSenderIcon(jsonObj.optString("senderIcon"));
            }

        } catch (JSONException e) {
            Log.d("JSONException", e.getMessage());
        }
    }

    //给消息赋值。
    public ComposeMessage(Parcel in) {

        setTitle(ParcelUtils.readFromParcel(in));//该类为工具类，消息属性
        //这里可继续增加你消息的属性
        setDes(ParcelUtils.readFromParcel(in));//该类为工具类，消息属性
        setUuid(ParcelUtils.readFromParcel(in));//该类为工具类，消息属性
        setCount(ParcelUtils.readFromParcel(in));//该类为工具类，消息属性
        setGrade(ParcelUtils.readFromParcel(in));//该类为工具类，消息属性
        setScore(ParcelUtils.readFromParcel(in));//该类为工具类，消息属性
        setSenderName(ParcelUtils.readFromParcel(in));//该类为工具类，消息属性
        setSenderIcon(ParcelUtils.readFromParcel(in));//该类为工具类，消息属性
    }

    /**
     * 读取接口，目的是要从Parcel中构造一个实现了Parcelable的类的实例处理。
     */
    public static final Creator<ComposeMessage> CREATOR = new Creator<ComposeMessage>() {

        @Override
        public ComposeMessage createFromParcel(Parcel source) {
            return new ComposeMessage(source);
        }

        @Override
        public ComposeMessage[] newArray(int size) {
            return new ComposeMessage[size];
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
        ParcelUtils.writeToParcel(dest, getTitle());
        ParcelUtils.writeToParcel(dest, getDes());
        ParcelUtils.writeToParcel(dest, getUuid());
        ParcelUtils.writeToParcel(dest, getCount());
        ParcelUtils.writeToParcel(dest, getGrade());
        ParcelUtils.writeToParcel(dest, getScore());
        ParcelUtils.writeToParcel(dest, getSenderName());
        ParcelUtils.writeToParcel(dest, getSenderIcon());
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }


    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }
}
