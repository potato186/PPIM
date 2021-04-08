package com.ilesson.ppim.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextUtils;
import android.widget.Toast;

import com.ilesson.ppim.R;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.ByteArrayOutputStream;

/**
 * Created by potato on 2019/4/2.
 */
public class WxShareUtils {
    public static final String SHARE_URL = "https://pp.aiibt.net/pp.html";
    public static void shareWeb(Context context,int type,String url,String title,String des) {
        // 通过appId得到IWXAPI这个对象
        String  wxKey = context.getResources().getString(R.string.wx_key);
        IWXAPI wxapi = WXAPIFactory.createWXAPI(context, wxKey);
        // 检查手机或者模拟器是否安装了微信
        if (!wxapi.isWXAppInstalled()) {
            Toast.makeText(context,"您还没有安装微信",Toast.LENGTH_LONG).show();
            return;
        }

        // 初始化一个WXWebpageObject对象
        WXWebpageObject webpageObject = new WXWebpageObject();
        // 填写网页的url

        // 用WXWebpageObject对象初始化一个WXMediaMessage对象
        WXMediaMessage msg = new WXMediaMessage(webpageObject);
        // 填写网页标题、描述、位图
        if(TextUtils.isEmpty(url)){
            url = SHARE_URL;
        }
        webpageObject.webpageUrl = url;
        if(TextUtils.isEmpty(title)){
            title = context.getResources().getString(R.string.app_label_name);
        }
        if(TextUtils.isEmpty(des)){
            des = context.getResources().getString(R.string.app_label_name);
        }
        msg.title = title;
        msg.description = des;
        // 如果没有位图，可以传null，会显示默认的图片
//        Bitmap bitmap = drawableBitmapOnWhiteBg(context,BitmapFactory.decodeResource(context.getResources(), R.drawable.ico));
        msg.setThumbImage(BitmapFactory.decodeResource(context.getResources(), R.mipmap.share_icon));

        // 构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        // transaction用于唯一标识一个请求（可自定义）
        req.transaction = "webpage";
        // 上文的WXMediaMessage对象
        req.message = msg;
        req.scene = type;

        // 向微信发送请求
        wxapi.sendReq(req);
    }
    public static void WXsharePic(Context context, Bitmap bitmap,int type) {
        //初始化WXImageObject和WXMediaMessage对象
        String  wxKey = context.getResources().getString(R.string.wx_key);
        IWXAPI wxapi = WXAPIFactory.createWXAPI(context, wxKey);
        // 检查手机或者模拟器是否安装了微信
        if (!wxapi.isWXAppInstalled()) {
            Toast.makeText(context,"您还没有安装微信",Toast.LENGTH_LONG).show();
            return;
        }
        WXImageObject imageObject = new WXImageObject(bitmap);
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imageObject;
        //设置缩略图
//        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, true);
//        bitmap.recycle();
        msg.thumbData = bmpToByteArray(bitmap,32);
        //构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = Long.toString(System.currentTimeMillis());
        req.message = msg;
        //表示发送给朋友圈  WXSceneTimeline  表示发送给朋友  WXSceneSession
        req.scene = type;
        //调用api接口发送数据到微信
        wxapi.sendReq(req);
    }
    public static void sharePic(Context context,Bitmap bitmap,String url) {
        // 通过appId得到IWXAPI这个对象
        String  wxKey = context.getResources().getString(R.string.wx_key);
        IWXAPI wxapi = WXAPIFactory.createWXAPI(context, wxKey);
        // 检查手机或者模拟器是否安装了微信
        if (!wxapi.isWXAppInstalled()) {
            Toast.makeText(context,"您还没有安装微信",Toast.LENGTH_LONG).show();
            return;
        }
        WXImageObject wxImageObject = new WXImageObject(bitmap);
        // 初始化一个WXWebpageObject对象
        WXWebpageObject webpageObject = new WXWebpageObject();
        // 填写网页的url
        webpageObject.webpageUrl = url;
        // 用WXWebpageObject对象初始化一个WXMediaMessage对象
        WXMediaMessage msg = new WXMediaMessage(webpageObject);
        // 填写网页标题、描述、位图
        msg.title = context.getResources().getString(R.string.app_label_name);
        msg.description = "记忆力提升&英语高效学习";
//        msg.mediaObject = WXMediaMessage.IMediaObject.TYPE_IMAGE;
        // 如果没有位图，可以传null，会显示默认的图片
//        Bitmap bitmap = drawableBitmapOnWhiteBg(context,BitmapFactory.decodeResource(context.getResources(), R.drawable.ico));
        msg.setThumbImage(BitmapFactory.decodeResource(context.getResources(), R.mipmap.icon));
        msg.thumbData = bmpToByteArray(bitmap,32);
        // 构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        // transaction用于唯一标识一个请求（可自定义）
        req.transaction = "webpage";
        // 上文的WXMediaMessage对象
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneTimeline;

        // 向微信发送请求
        wxapi.sendReq(req);
    }
    // 图片转 byte[] 数组
    public static byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
    /**
     * Bitmap转换成byte[]并且进行压缩,压缩到不大于maxkb
     *
     * @param bitmap
     * @param maxkb
     * @return
     */
    public static byte[] bmpToByteArray(Bitmap bitmap, int maxkb) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
        int options = 100;
        while (output.toByteArray().length > maxkb && options != 10) {
            output.reset(); //清空output
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, output);//这里压缩options%，把压缩后的数据存放到output中
            options -= 10;
        }
        return output.toByteArray();
    }
    public static Bitmap drawableBitmapOnWhiteBg(Context context,Bitmap bitmap){
        Bitmap newBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(context.getResources().getColor(android.R.color.white));
        Paint paint=new Paint();
        canvas.drawBitmap(bitmap, 0, 0, paint); //将原图使用给定的画笔画到画布上
        return newBitmap;
    }
}