package com.ilesson.ppim.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.ilesson.ppim.R;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.utils.UrlUtil;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import io.rong.imageloader.core.DisplayImageOptions;
import io.rong.imageloader.core.ImageLoader;

import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;

/**
 * Created by potato on 2020/3/11.
 */
@ContentView(R.layout.activity_pay_code)
public class ScoreCodeActivity extends BaseActivity {
    @ViewInject(R.id.code)
    private ImageView imageView;

    @Event(R.id.back_btn)
    private void back(View v){
        finish();
    }
    private static final String TAG = "ContactActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestCode();
        String token = SPUtils.get("token","");
//        String t = UrlUtil.getURLEncoderString(token);
        String result = Constants.BASE_URL + Constants.CODE_URL+"?action=money&token="+token;
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.CODE_URL);
        params.addParameter("action", "money");
        params.addParameter("token", token);
        x.image().bind(imageView,result);
    }

    private void requestCode() {
        //action=friend_pre&token=q6TqTmCJVngz3lt%2bloDqEfKjlVc0nPCvkB
        String token = SPUtils.get(LOGIN_TOKEN, "");
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.CODE_URL);
        params.addBodyParameter("action", "money");
        params.addBodyParameter("token", token);
        Log.d(TAG, "loadData: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
//               ;
//                imageView.setImageBitmap(convertStringToIcon(result));
                Log.d(TAG, "onSuccess: " + result);
//                SearchUser base = new Gson().fromJson(
//                        result,
//                        new TypeToken<SearchUser>() {
//                        }.getType());
//                if (base.getCode() == 0) {
//                    List<PPUserInfo> data = base.getData();
//                    if (null == data || data.isEmpty()) {
//                        return;
//                    }
//                } else {
//                }
            }


            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ex.printStackTrace();
            }


            @Override
            public void onCancelled(CancelledException cex) {
                cex.printStackTrace();
            }


            @Override
            public void onFinished() {
            }
        });
    }
    public static Bitmap convertStringToIcon(String str) {
        // OutputStream out;
        Bitmap bitmap = null;
        try {
            // out = new FileOutputStream("/sdcard/aa.jpg");
            byte[] bitmapArray;
            bitmapArray = Base64.decode(str, Base64.DEFAULT);
            bitmap =
                    BitmapFactory.decodeByteArray(bitmapArray, 0,
                            bitmapArray.length);
            // bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }
}
