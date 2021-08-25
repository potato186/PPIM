package com.ilesson.ppim.utils;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.sunflower.FlowerCollector;
import com.ilesson.ppim.activity.SettingActivity;
import com.tencent.qcloudtts.LongTextTTS.LongTextTtsController;
import com.tencent.qcloudtts.LongTextTTS.audio.QCloudMediaService;
import com.tencent.qcloudtts.callback.QCloudPlayerCallback;
import com.tencent.qcloudtts.callback.TtsExceptionHandler;
import com.tencent.qcloudtts.exception.TtsException;
import com.tencent.qcloudtts.exception.TtsNotInitializedException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ilesson.ppim.activity.SettingActivity.XUNFEI;

public class TTSHelper {
	private SpeechSynthesizer mTts;
	private static LongTextTtsController mTtsController;
	private boolean playing;
	public TTSHelper(Context context) {
		if(null==mTtsController){
			mTtsController = new LongTextTtsController();
			mTtsController.init(context,Long.valueOf(PPConfig.apppId),PPConfig.secretId,PPConfig.secretKey);
			//设置语速
			mTtsController.setVoiceSpeed(0);
//设置音色
			mTtsController.setVoiceType(101010);
//设置音量
//			mTtsController.setVoiceVolume(volume);
////设置语言
			mTtsController.setVoiceLanguage(2);
//设置ProjectId
			mTtsController.setProjectId(1217813);
		}
//			mTts = SpeechSynthesizer.createSynthesizer(context, mTtsInitListener);

	}
	private InitListener mTtsInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
		}
	};
	private SynthesizerListener mTtsListener = new SynthesizerListener() {

		@Override
		public void onSpeakBegin() {
			System.out.println("onSpeakBegin");
			Log.d("tts", "text="+text1+"   >>>>讯飞用时："+(System.currentTimeMillis()-start));
			if (null != mOnTTSFinish)
				mOnTTSFinish.onTTSstart();
			QCloudMediaService s;
		}

		@Override
		public void onSpeakPaused() {
			System.out.println("onSpeakPaused");
		}

		@Override
		public void onSpeakResumed() {
		}

		@Override
		public void onBufferProgress(int percent, int beginPos, int endPos,
				String info) {
		}

		@Override
		public void onSpeakProgress(int percent, int beginPos, int endPos) {
//			if (percent == 92) {
//				System.out.println("  if (null != mOnTTSFinish)mOnTTSFinish.onTTSFinish(type); ");
//				if (null != mOnTTSFinish)
//					mOnTTSFinish.onTTSFinish(type);
//			}
//			System.out.println("percent="+percent+"    endPos="+endPos);
		}

		@Override
		public void onCompleted(SpeechError error) {
			if (null != mOnTTSFinish)
				mOnTTSFinish.onTTSFinish(type);
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			// 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
			// 若使用本地能力，会话id为null
			// if (SpeechEvent.EVENT_SESSION_ID == eventType) {
			// String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
			// Log.d(TAG, "session id =" + sid);
			// }
		}
	};
	private int type;

	public interface OnTTSFinish {
		void onTTSFinish(int type);

		void onTTSstart();
	}

	private OnTTSFinish mOnTTSFinish;

	public void setOnTTSFinish(OnTTSFinish mOnTTSFinish) {
		this.mOnTTSFinish = mOnTTSFinish;
	}
	public boolean isSpeaking(){
		if(null!=mTts){
			return mTts.isSpeaking();
		}
		if(null!=mTtsController&&playing){
			return true;
		}
		return false;
	}
	private long start;
	//发起tts请求
	public void start(int type,final String ttsText){
		playing = true;
		if(TextUtils.isEmpty(ttsText)){
			return;
		}
		if(isChinese(ttsText)){//读中文
			mTtsController.setVoiceLanguage(1);
			mTtsController.setVoiceType(101010);
		}else {//读英文
			mTtsController.setVoiceLanguage(2);
			mTtsController.setVoiceType(101050);
		}
		start = System.currentTimeMillis();
		try {
			mTtsController.startTts(ttsText, mTtsExceptionHandler, new QCloudPlayerCallback() {

				//播放开始
				@Override
				public void onTTSPlayStart() {
					playing = true;
				}

				//音频缓冲中
				@Override
				public void onTTSPlayWait() {
				}

				//缓冲完成，继续播放
				@Override
				public void onTTSPlayResume() {
				}

				//连续播放下一句
				@Override
				public void onTTSPlayNext() {
				}

				//播放中止
				@Override
				public void onTTSPlayStop() {
				}

				//播放结束
				@Override
				public void onTTSPlayEnd() {
					playing = false;
					if (null != mOnTTSFinish)
						mOnTTSFinish.onTTSFinish(type);
				}

				//当前播放的字符,当前播放的字符在所在的句子中的下标.
				@Override
				public void onTTSPlayProgress(String currentWord, int currentIndex) {
//					Log.d("tts", "onTTSPlayProgress" + currentWord + currentIndex);
				}
				public void onTTSPlayAudioCachePath(String path) {

				}
			});
		} catch (TtsNotInitializedException e) {
			Log.e("tts", e.getMessage());
		}
	}

	private final TtsExceptionHandler mTtsExceptionHandler = new TtsExceptionHandler() {
		@Override
		public void onRequestException(TtsException e) {
			Log.e(TAG, "tts onRequestException");
			//网络出错的时候
			mTtsController.pause();
		}

	};
	private static final String TAG = "TTSHelper";
	private String text1;
	public void start(final int type, Context context, String text) {
//		start(type,text);
//		if(true)return;
		Log.d(TAG, "start: ");
		text1 = text;
		start = System.currentTimeMillis();
		if(null==mTts){
			mTts = SpeechSynthesizer.createSynthesizer(context, mTtsInitListener);
		}
		if (mTts.isSpeaking())
			return;
		this.type = type;
		FlowerCollector.onEvent(context, "tts_play");
		// 设置参数
		setParam();
//		try {
//			text = new String(text.getBytes("UnicodeLittleUnmarked"))+ "  .";
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}
		String content = text.replace(" ","");
		int code = mTts.startSpeaking(content, mTtsListener);
	}
	private TtsExceptionHandler handler = new TtsExceptionHandler() {
		@Override
		public void onRequestException(TtsException e) {
			Log.d(TAG, "onRequestException: "+e.getErrMsg());
			Log.d(TAG, "onRequestException: "+e.getMessage());
			e.printStackTrace();
		}
	};
//	//接收接口异常
//	private final TtsController.TtsExceptionHandler mTtsExceptionHandler = new TtsController.TtsExceptionHandler() {
//		@Override
//		public void onRequestException(TtsController.TtsException e) {
//			Log.e(TAG, "tts onRequestException");
//			Toast.makeText(TtsActivity.this, e.getErrMsg(), Toast.LENGTH_SHORT).show();
//		}
//	};
	private void setParam() {
		// 清空参数
		mTts.setParameter(SpeechConstant.PARAMS, null);
		// 根据合成引擎设置相应参数
		mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
		// 设置合成语速
//		mTts.setParameter(SpeechConstant.SPEED, "75");
		// 设置合成音调
		mTts.setParameter(SpeechConstant.PITCH, "50");
		mTts.setParameter("rdn", "1");
		// 设置合成音量
		mTts.setParameter(SpeechConstant.VOLUME, "50");
		// 设置播放器音频流类型
		mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
//		mTts.setParameter(SpeechConstant.LANGUAGE, "en_us");
		// 设置播放合成音频打断音乐播放，默认为true
		mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "false");
		mTts.setParameter(SpeechConstant.ENGINE_MODE, "false");
		boolean xunfei = SPUtils.get(SettingActivity.VOICE_NAME, XUNFEI).equals(XUNFEI) ? true : false;
//		if(xunfei){
//			// 设置在线合成发音人
			mTts.setParameter(SpeechConstant.VOICE_NAME, "vixf");
			mTts.setParameter(SpeechConstant.SPEED, "75");
//		}else {
			// 设置在线合成发音人
//			mTts.setParameter(SpeechConstant.VOICE_NAME, "x_xiaomei");
//			mTts.setParameter(SpeechConstant.SPEED, "45");
//		}
		// 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
		// 注：AUDIO_FORMAT参数语记需要更新版本才能生效
		mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
		mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH,
				Environment.getExternalStorageDirectory() + "/msc/tts.wav");
	}

	public void destroy() {
		if (mTts != null) {
			mTts.stopSpeaking();
			mTts.destroy();
		}
	}

	public void stop() {
		Log.d(TAG, "stop: ");
		if (mTts != null) {
			mTts.stopSpeaking();
		}
		if (mTtsController != null) {
			mTtsController.stop();
		}
	}
	public boolean isChinese(String str)
	{
		Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
		Matcher m = p.matcher(str);
		if (m.find()) {
			return true;
		}
		return false;
	}
}
