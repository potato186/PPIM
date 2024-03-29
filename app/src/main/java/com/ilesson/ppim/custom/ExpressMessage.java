package com.ilesson.ppim.custom;


import android.os.Parcel;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import io.rong.common.ParcelUtils;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;


@MessageTag(value = "custom:express", flag = MessageTag.ISCOUNTED | MessageTag.ISPERSISTED)
public class ExpressMessage extends MessageContent {

    private String extra;
    private String content;
    /*
    *
    * 实现 encode() 方法，该方法的功能是将消息属性封装成 json 串，
    * 再将 json 串转成 byte 数组，该方法会在发消息时调用，如下面示例代码：
    * */
    @Override
    public byte[] encode() {
        JSONObject jsonObj = new JSONObject();

        try {
            jsonObj.put("content", this.getContent());
            jsonObj.put("extra", this.getExtra());

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
    public ExpressMessage(){

    }
    /*
    * 覆盖父类的 MessageContent(byte[] data) 构造方法，该方法将对收到的消息进行解析，
    * 先由 byte 转成 json 字符串，再将 json 中内容取出赋值给消息属性。
    * */
    public ExpressMessage(byte[] data) {
        String jsonStr = null;

        try {
            jsonStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        try {
            JSONObject jsonObj = new JSONObject(jsonStr);


            if (jsonObj.has("extra"))
                setExtra(jsonObj.optString("extra"));

            if (jsonObj.has("content"))
                setContent(jsonObj.optString("content"));

        } catch (JSONException e) {
            Log.d("JSONException", e.getMessage());
        }
    }

    //给消息赋值。
    public ExpressMessage(Parcel in) {

        setContent(ParcelUtils.readFromParcel(in));//该类为工具类，消息属性
        //这里可继续增加你消息的属性
        setExtra(ParcelUtils.readFromParcel(in));//该类为工具类，消息属性
    }

    /**
     * 读取接口，目的是要从Parcel中构造一个实现了Parcelable的类的实例处理。
     */
    public static final Creator<ExpressMessage> CREATOR = new Creator<ExpressMessage>() {

        @Override
        public ExpressMessage createFromParcel(Parcel source) {
            return new ExpressMessage(source);
        }

        @Override
        public ExpressMessage[] newArray(int size) {
            return new ExpressMessage[size];
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
        ParcelUtils.writeToParcel(dest, getContent());
        ParcelUtils.writeToParcel(dest, getExtra());
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
