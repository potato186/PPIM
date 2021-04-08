package com.ilesson.ppim.update;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.UpdateInfo;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.IMUtils;
import com.ilesson.ppim.utils.PPScreenUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;


public class UpdateHelper {

	private static final String UPDATE_URL = "/config/update-znpp.json";
	private Thread mThread;
	private Context mContext;
	private static final String TAG = "UpdateHelper";
	public UpdateHelper(Context context){
		this.mContext = context;
	}
	
	public void checkForUpdates(final boolean isClick) {
		mThread = new Thread() {
			@Override
			public void run() {
				String json = sendPost(UPDATE_URL);
				if (json != null) {
					parseJson(json,isClick);
				} else {
					Log.e(TAG, "can't get app update json");
				}
			}

		};
		mThread.start();
	}
	private String descript;
	public void checkVersion(final boolean isClick) {//pp/update?version=1
		int versionCode=1;
		try {
			versionCode = mContext.getPackageManager().getPackageInfo(
                    mContext.getPackageName(), 0).versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		final int version = versionCode;
		if(version<=1170){
			new IMUtils().upUser(mContext);
		}
		RequestParams params = new RequestParams(Constants.BASE_URL + Constants.UPDATE_URL);
		params.addParameter("version", ""+versionCode);
		Log.d(TAG, "loadData: " + params.toString());
		x.http().post(params, new Callback.CommonCallback<String>() {
			@Override
			public void onSuccess(String result) {
				Log.d(TAG, "onSuccess: " + result);
				BaseCode<UpdateInfo> base = new Gson().fromJson(
						result,
						new TypeToken<BaseCode<UpdateInfo>>() {
						}.getType());
				if(base.getCode()==0){
					UpdateInfo info = base.getData();
					if (version<Integer.valueOf(info.getVersion())) {
						needUpdate = true;
						if(null!=updateLinstener){
							updateLinstener.update(info.getVersion(),apkUrl,updateMessage);
						}
						descript = info.getMessage();
						apkUrl = info.getUrl();
//						version = info.getVersion();
						handler.sendEmptyMessage(0);
					}else{
						if(isClick){
							handler.sendEmptyMessage(1);
						}
					}
				}
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
	protected String sendPost(String urlStr) {
		HttpURLConnection uRLConnection = null;
		InputStream is = null;
		BufferedReader buffer = null;
		String result = null;
		try {
			URL url = new URL(urlStr);
			uRLConnection = (HttpURLConnection) url.openConnection();
			uRLConnection.setDoInput(true);
			uRLConnection.setDoOutput(true);
			uRLConnection.setRequestMethod("POST");
			uRLConnection.setUseCaches(false);
			uRLConnection.setConnectTimeout(10 * 1000);
			uRLConnection.setReadTimeout(10 * 1000);
			uRLConnection.setInstanceFollowRedirects(false);
			uRLConnection.setRequestProperty("Connection", "Keep-Alive");
			uRLConnection.setRequestProperty("Charset", "UTF-8");
			uRLConnection
					.setRequestProperty("Accept-Encoding", "gzip, deflate");
			uRLConnection
					.setRequestProperty("Content-Type", "application/json");

			uRLConnection.connect();

			is = uRLConnection.getInputStream();

			String content_encode = uRLConnection.getContentEncoding();

			if (null != content_encode && !"".equals(content_encode)
					&& content_encode.equals("gzip")) {
				is = new GZIPInputStream(is);
			}

			buffer = new BufferedReader(new InputStreamReader(is));
			StringBuilder strBuilder = new StringBuilder();
			String line;
			while ((line = buffer.readLine()) != null) {
				strBuilder.append(line);
			}
			result = strBuilder.toString();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "http post error", e);
		} finally {
			if (buffer != null) {
				try {
					buffer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (uRLConnection != null) {
				uRLConnection.disconnect();
			}
		}
		return result;
	}

	public String updateMessage="";
	public String apkUrl="";
	public String version="";
	public static boolean needUpdate=false;
	private void parseJson(String json,boolean isClick) {
		try {

			JSONObject obj = new JSONObject(json);
			updateMessage = obj.getString(Consts.APK_UPDATE_CONTENT);
			apkUrl = obj.getString(Consts.APK_DOWNLOAD_URL);
			version = obj.getString(Consts.APK_VERSION_NAME);
			int apkCode = obj.getInt(Consts.APK_VERSION_CODE);

			int versionCode = mContext.getPackageManager().getPackageInfo(
					mContext.getPackageName(), 0).versionCode;

			if (apkCode > versionCode) {
				needUpdate = true;
				if(null!=updateLinstener){
					updateLinstener.update(version,apkUrl,updateMessage);
				}
					handler.sendEmptyMessage(0);
			}else{
				if(isClick){
					handler.sendEmptyMessage(1);
				}
			}

		} catch (PackageManager.NameNotFoundException ignored) {
		} catch (JSONException e) {
			Log.e(TAG, "parse json error", e);
		}
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if(msg.what==0){
				showD(descript, apkUrl);
			}else {
				Toast.makeText(mContext, R.string.lastest_version,Toast.LENGTH_LONG).show();
			}
		};
	};

	@SuppressLint("NewApi") private void showD(String msg, final String url) {
		
		LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View v = layoutInflater.inflate(R.layout.update_dialog, null);
		final Dialog dialog = new Dialog(mContext);
		dialog.setContentView(v);
		dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		android.view.WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
		p.width = (int) (PPScreenUtils.getScreenWidth(mContext) * 0.85);
		p.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		dialog.setCanceledOnTouchOutside(false);
		dialog.getWindow().setAttributes(p);
        TextView contentTextView = (TextView) v.findViewById(R.id.update_content);
        contentTextView.setText(msg);
        v.findViewById(R.id.update_confim_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	goToDownload(url);
                dialog.dismiss();
            }
        });
        v.findViewById(R.id.update_confim_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
//        try{

			dialog.show();
//			dialog.setView(v, 0, 0, 0, 0);
			dialog.setCancelable(true);
//		}catch (Exception e){
//			e.printStackTrace();
//		}
	}

	public void goToDownload(String url) {
		Intent intent = new Intent(mContext, ApkUpdateService.class);
		intent.putExtra(Consts.APK_DOWNLOAD_URL, url);
		mContext.startService(intent);
	}
	private UpdateLinstener updateLinstener;
	public void setUpdateLinstener(UpdateLinstener updateLinstener){
		this.updateLinstener = updateLinstener;
	}
	public interface UpdateLinstener{
		void update(String name, String url, String msg);
	}
}
