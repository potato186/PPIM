package com.ilesson.ppim.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.GrammarListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.ilesson.ppim.R;
import com.ilesson.ppim.utils.BluetoothTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class IfeyVoiceWidget1 extends View implements OnClickListener {

    /**
     * 第一圈的颜色
     */
    private int mFirstColor;

    /**
     * 第二圈的颜色
     */
    private int mSecondColor;
    /**
     * 圈的宽度
     */
    private int mCircleWidth;
    /**
     * 画笔
     */
    private Paint mPaint;
    /**
     * 当前进度
     */
    private int mCurrentCount = 3;

    /**
     * 中间的图片
     */
    private Bitmap mImage;
    /**
     * 每个块块间的间隙
     */
    private int mSplitSize;
    /**
     * 个数
     */
    private int mCount;

    private Rect mRect;

    private String classType;
    public static String CURRENT_CLASS = "dcurrent_class";

    public IfeyVoiceWidget1(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IfeyVoiceWidget1(Context context) {
        this(context, null);
    }

    /**
     * 必要的初始化，获得一些自定义的值
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    public IfeyVoiceWidget1(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.IfeyVoiceWidget, defStyle, 0);
        int n = a.getIndexCount();

        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.IfeyVoiceWidget_firstColor:
                    mFirstColor = a.getColor(attr, Color.GREEN);
                    break;
                case R.styleable.IfeyVoiceWidget_secondColor:
                    mSecondColor = a.getColor(attr, Color.CYAN);
                    break;
                case R.styleable.IfeyVoiceWidget_bg:
                    mImage = BitmapFactory.decodeResource(getResources(),
                            a.getResourceId(attr, 0));
                    break;
                case R.styleable.IfeyVoiceWidget_circleWidth:
                    mCircleWidth = a.getDimensionPixelSize(attr, (int) TypedValue
                            .applyDimension(TypedValue.COMPLEX_UNIT_PX, 20,
                                    getResources().getDisplayMetrics()));
                    break;
                case R.styleable.IfeyVoiceWidget_dotCount:
                    mCount = a.getInt(attr, 20);// 默认20
                    break;
                case R.styleable.IfeyVoiceWidget_splitSize:
                    mSplitSize = a.getInt(attr, 20);
                    break;
            }
        }
        a.recycle();
        mPaint = new Paint();
        mRect = new Rect();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setAntiAlias(true); // 消除锯齿
        mPaint.setStrokeWidth(mCircleWidth); // 设置圆环的宽度
        mPaint.setStrokeCap(Paint.Cap.ROUND); // 定义线段断电形状为圆头
        mPaint.setAntiAlias(true); // 消除锯齿
        mPaint.setStyle(Paint.Style.STROKE); // 设置空心
        int centre = getWidth() / 2; // 获取圆心的x坐标
        int radius = centre - mCircleWidth / 2;// 半径
        /**
         * 画块块去
         */
        drawOval(canvas, centre, radius);

        /**
         * 计算内切正方形的位置
         */
        int relRadius = radius - mCircleWidth / 2;// 获得内圆的半径
        /**
         * 内切正方形的距离顶部 = mCircleWidth + relRadius - √2 / 2
         */
        mRect.left = (int) (relRadius - Math.sqrt(2) * 1.0f / 2 * relRadius)
                + mCircleWidth;
        /**
         * 内切正方形的距离左边 = mCircleWidth + relRadius - √2 / 2
         */
        mRect.top = (int) (relRadius - Math.sqrt(2) * 1.0f / 2 * relRadius)
                + mCircleWidth;
        mRect.bottom = (int) (mRect.left + Math.sqrt(2) * relRadius);
        mRect.right = (int) (mRect.left + Math.sqrt(2) * relRadius);

        /**
         * 如果图片比较小，那么根据图片的尺寸放置到正中心
         */
        if (mImage.getWidth() < Math.sqrt(2) * relRadius) {
            mRect.left = (int) (mRect.left + Math.sqrt(2) * relRadius * 1.0f
                    / 2 - mImage.getWidth() * 1.0f / 2);
            mRect.top = (int) (mRect.top + Math.sqrt(2) * relRadius * 1.0f / 2 - mImage
                    .getHeight() * 1.0f / 2);
            mRect.right = (int) (mRect.left + mImage.getWidth());
            mRect.bottom = (int) (mRect.top + mImage.getHeight());

        }
        // 绘图
        canvas.drawBitmap(mImage, null, mRect, mPaint);
    }

    /**
     * 根据参数画出每个小块
     *
     * @param canvas
     * @param centre
     * @param radius
     */
    private void drawOval(Canvas canvas, int centre, int radius) {
        /**
         * 根据需要画的个数以及间隙计算每个块块所占的比例*360
         */
        float itemSize = (360 * 1.0f - mCount * mSplitSize) / mCount;

        RectF oval = new RectF(centre - radius, centre - radius, centre
                + radius, centre + radius); // 用于定义的圆弧的形状和大小的界限

        mPaint.setColor(mFirstColor); // 设置圆环的颜色
        for (int i = 0; i < mCount; i++) {
            canvas.drawArc(oval, i * (itemSize + mSplitSize), itemSize, false,
                    mPaint); // 根据进度画圆弧
        }

        mPaint.setColor(mSecondColor); // 设置圆环的颜色
        for (int i = 0; i < mCurrentCount; i++) {
            canvas.drawArc(oval, i * (itemSize + mSplitSize), itemSize, false,
                    mPaint); // 根据进度画圆弧
        }
    }

    private void setProgress(int progress) {
        mCurrentCount = progress;
        postInvalidate();
    }

    public String getClassType() {
        return classType;
    }

    public void setClassType(String classType) {
    }

    private int count;

    public class KeyEventReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("intent="+intent.getAction());
            if (context.toString().contains(CURRENT_CLASS) && count == 0) {
                handler.removeMessages(0);
                handler.removeMessages(1);
                count++;
                // if (count % 2 == 0) {
                // return;
                // }
                // 获得Action
                String intentAction = intent.getAction();
                // 获得KeyEvent对象

                if ("bear.action.keydown".equals(intentAction)) {
                    Log.d("onStateChanged","bear.action.keydown=");
                    if (!flag_destroed) {
                        Log.d("onStateChanged","flag_destroed="+flag_destroed);
                        if(null!=mListener)
                            mListener.onStateChanged(true);
                        handler.sendEmptyMessageDelayed(0, 0);
                        handler.sendEmptyMessageDelayed(1, 0);
                    }
                }
            }
        }
    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            System.out.println("msg.what="+msg.what);
            switch (msg.what) {
                case 0:
//                    SoundPlayUtils.play(1);
                    break;
                case 1:
                    start();
                    break;
                case 2:
                    startRecording();
                    break;
            }
        };
    };

    public void registerKeyEventReceiver() {
//		SoundPlayUtils.init(getContext(), new int[] { R.raw.audio_initiate });
//		ComponentName rec = new ComponentName(getContext().getPackageName(),
//				MediaButtonReceiver.class.getName());
//		mAudioManager.registerMediaButtonEventReceiver(rec);
//
//		mKeyEventReceiver = new KeyEventReceiver();
//		getContext().registerReceiver(mKeyEventReceiver,
//				new IntentFilter("bear.action.keydown"));
    }

    /**
     * 0 代表示配置语音 1 代表马识图 2 代表手机
     */
    private int state = 0;
    private SharedPreferences shared;

    public boolean initIfey(MessageListener listenner, String[] keyword,
                            boolean english) {
        if (null == listenner) {
            return false;
        }
        mAudioManager = (AudioManager) getContext().getSystemService(
                Context.AUDIO_SERVICE);

        shared = getContext().getSharedPreferences(
                "config.xml", Context.MODE_PRIVATE);
        state = shared.getInt("is_speak_to_phone", 0);

        this.mListener = listenner;
        mSharedPreferences = getContext().getSharedPreferences(
                getContext().getPackageName(), Context.MODE_PRIVATE);

        StringBuffer sbf = new StringBuffer();
        if (null != keyword && keyword.length > 0) {
            for (int i = 0; i < keyword.length; i++) {
                if (i != keyword.length - 1) {
                    sbf.append(keyword[i] + " | ");
                } else {
                    sbf.append(keyword[i]);
                }
            }
        }
        initIfeyRecognizer(sbf.toString(), english);

//        setOnClickListener(this);

        flag_stoped = false;
        flag_destroed = false;
//        postInvalidate();
        return true;
    }

    private void stopListening() {
        flag_stoped = true;
        if (mAsr.isListening()) {
            mAsr.stopListening();

            postInvalidate();
        }
    }

    public void destroy() {
        if (null == mAsr) {
            return;
        }
        flag_destroed = true;
        stopListening();

        // if (null != mKeyEventReceiver) {
        // getContext().unregisterReceiver(mKeyEventReceiver);
        // mAudioManager.unregisterMediaButtonEventReceiver(new ComponentName(
        // getContext().getPackageName(), MediaButtonReceiver.class
        // .getName()));
        // }
    }
    public void unregist(){
        if (null != mKeyEventReceiver) {
            getContext().unregisterReceiver(mKeyEventReceiver);
//				 mAudioManager.unregisterMediaButtonEventReceiver(new ComponentName(
//				 getContext().getPackageName(), MediaButtonReceiver.class
//				 .getName()));
        }
    }
    public interface MessageListener {
        void onReceiverMessage(String msg);

        void onStateChanged(boolean recording);
    }

    private SpeechRecognizer mAsr;
    private SharedPreferences mSharedPreferences;
    private static final String KEY_GRAMMAR_ABNF_ID = "grammar_abnf_id";
    private static final String TAG = "IfeyVoiceWidget";
    private MessageListener mListener;
    private boolean flag_destroed, flag_stoped;
    private KeyEventReceiver mKeyEventReceiver;
    private AudioManager mAudioManager;

    private void initIfeyRecognizer(String keyword, boolean longVoice) {
        // 云端语法识别：如需本地识别请参照本地识别
        // 1.创建SpeechRecognizer对象
        mAsr = SpeechRecognizer.createRecognizer(getContext(), null);
        // ABNF语法示例，可以说”北京到上海”
        String mCloudGrammar = "#ABNF 1.0 gb2312;\n\tlanguage zh-CN;\n\tmode voice;\n\troot $main;\n\t$main = $place;\n\t$place="
                + keyword + ";";
        // 2.构建语法文件
        if(null==mAsr)return;
        mAsr.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
        mAsr.updateLexicon("contact", "成语", null);
        int ret = mAsr.buildGrammar("abnf", mCloudGrammar, grammarListener);
        if (ret != ErrorCode.SUCCESS) {
            Log.d(TAG, "语法构建失败,错误码：" + ret);
        } else {
            Log.d(TAG, "语法构建成功");
        }
        // 3.开始识别,设置引擎类型为云端
        mAsr.setParameter(SpeechConstant.ENGINE_TYPE, "cloud");
        if (longVoice) {
            mAsr.setParameter(SpeechConstant.ASR_PTT, "1");
//            mAsr.setParameter(SpeechConstant.VAD_BOS, "10000");
//            mAsr.setParameter(SpeechConstant.VAD_EOS, "10000");
        }else {
            mAsr.setParameter(SpeechConstant.ASR_PTT, "0");
        }
        mAsr.setParameter(SpeechConstant.VAD_BOS, "5000");
        mAsr.setParameter(SpeechConstant.VAD_EOS, "1000");
        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
//        mAsr.setParameter(SpeechConstant.VAD_BOS, "5000");
//        mAsr.setParameter(SpeechConstant.ASR_PTT, "0");
//        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
//        mAsr.setParameter(SpeechConstant.VAD_EOS, "1000");
//        mAsr.setParameter(SpeechConstant.ACCENT, "cn_cantonese");

//        }
        // else{
        // mAsr.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // System.out.println("SpeechConstant.LANGUAGE, zh_cn");
        // System.out.println("english="+english);
        // }

        if (ret != ErrorCode.SUCCESS) {
            Log.d(TAG, "识别失败,错误码: " + ret);
        }
    }

    private GrammarListener grammarListener = new GrammarListener() {
        @Override
        public void onBuildFinish(String grammarId, SpeechError error) {
            if (error == null) {
                if (null != grammarId && !"".equals(grammarId)) {
                    // 构建语法成功，请保存grammarId用于识别
                    Editor editor = mSharedPreferences.edit();
                    editor.putString(KEY_GRAMMAR_ABNF_ID, grammarId);
                    editor.commit();
                } else {
                    Log.d(TAG, "语法构建失败,错误码：" + error.getErrorCode());
                }
            }
        }
    };

    public interface OnVolumeChangeListener {
        void onVolumeChanged(int progress, short[] data);
    }

    private OnVolumeChangeListener onVolumeChangeListener;

    public void setOnVolumeChangeListener(
            OnVolumeChangeListener onVolumeChangeListener) {
        this.onVolumeChangeListener = onVolumeChangeListener;
    }

    public static short[] toShortArray(byte[] src) {

        int count = src.length >> 1;
        short[] dest = new short[count];
        for (int i = 0; i < count; i++) {
            dest[i] = (short) (src[i * 2] << 8 | src[2 * i + 1] & 0xff);
        }
        return dest;
    }
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onVolumeChanged(int progress, byte[] arg1) {
            setProgress((int) Math.rint(progress / 2.0));
            if (null != onVolumeChangeListener){
                onVolumeChangeListener.onVolumeChanged((int) Math
                        .rint(progress / 2.0),toShortArray(arg1));
            }
        }
        private StringBuffer sbf = new StringBuffer();

        @Override
        public void onResult(RecognizerResult result, boolean isLast) {
            if (null != result) {
                StringBuilder sb = new StringBuilder();
                try {
                    JSONObject x = new JSONObject(result.getResultString());
                    JSONArray jsonArray = new JSONArray(x.getString("ws"));
                    for (int j = 0; j < jsonArray.length(); j++) {
                        JSONObject j2 = jsonArray.getJSONObject(j);
                        JSONArray ja2 = j2.getJSONArray("cw");
                        for (int i = 0; i < ja2.length(); i++) {
                            JSONObject jo = ja2.getJSONObject(i);
                            String word = jo.getString("w");
                            if(null!=onTextReceiverListener){
                                onTextReceiverListener.TextReceiver(word);
                            }
                            sb.append(word);
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String keyWord = sb.toString();

                String key = null != keyWord ? keyWord.trim() : "";
                sbf.append(key);
                System.out.println("===========message=>>====" + key);
            }
            if (isLast) {
                String word = sbf.toString();
                Log.d(TAG, "message="+word);
                System.out.println("===========message==========" + word);

                if (null != word && !"".equals(word)) {
//                    if (!flag_stoped) {
                        if (word.length() > 0) {
//                            stop(word);
//                            mAsr.stopListening();
                            if (null != word) {
                                if(null!=mListener)
                                    mListener.onReceiverMessage(word);
                            }
                        }
//                    }
                    sbf = new StringBuffer();
                }

            }

        }

        @Override
        public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
            Log.d(TAG, "onEvent: "+arg3);
        }

        @Override
        public void onError(SpeechError error) {
        }

        @Override
        public void onEndOfSpeech() {
            if(null!=mListener)
                mListener.onStateChanged(false);
        }

        @Override
        public void onBeginOfSpeech() {

        }
    };

    @Override
    public void onClick(View v) {
        if (null != mAsr) {
            if (mAsr.isListening()) {
                stop();
            } else {
                start();
            }
        }
    }

    public void stop() {
        stop(null);
    }

    private void stop(final String word) {
        count = 0;
        if (mAsr == null) {
            return;
        }
//        if (flag_stoped) {
//            return;
//        }

        flag_stoped = true;
        mAsr.stopListening();
//        postInvalidate();
//        if (null != word) {
//            if(null!=mListener)
//                mListener.onReceiverMessage(word);
//        }
        if(true)return;
        if (state == 2) {

        } else {
            // 此版本先使用手机录音
            if (mAudioManager.isBluetoothScoAvailableOffCall()
                    && BluetoothTools.bluetoothState(getContext()) > 0) {

                mAudioManager.setBluetoothScoOn(false);
                mAudioManager.stopBluetoothSco();
                getContext().registerReceiver(
                        new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                int state = intent.getIntExtra(
                                        AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
                                if (AudioManager.SCO_AUDIO_STATE_DISCONNECTED == state) {
                                    if (null != word) {
                                        if(null!=mListener)
                                            mListener.onReceiverMessage(word);
                                    }
                                    getContext().unregisterReceiver(this); // 别遗漏
                                }
                            }
                        },
                        new IntentFilter(
                                AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED));
            } else {
                if (null != word) {
                    if(null!=mListener)
                        mListener.onReceiverMessage(word);
                }
            }
        }
    }

    public void start() {
        Log.d(TAG, "start: ");
        if(null!=mListener)
            mListener.onStateChanged(true);
        flag_stoped = false;
        handler.removeMessages(1);


        state = shared.getInt("is_speak_to_phone", 0);

        if (state == 2) {
            handler.sendEmptyMessage(2);
        } else {
            // 此版本先使用手机录音
            if (mAudioManager.isBluetoothScoAvailableOffCall()
                    && BluetoothTools.bluetoothState(getContext()) > 0) {
                if (mAudioManager.isBluetoothScoOn()) {
                    handler.sendEmptyMessage(2);
                } else {
                    mAudioManager.setBluetoothScoOn(true);
                    mAudioManager.startBluetoothSco();
                    getContext().registerReceiver(
                            new BroadcastReceiver() {
                                @Override
                                public void onReceive(Context context,
                                                      Intent intent) {
                                    int state = intent.getIntExtra(
                                            AudioManager.EXTRA_SCO_AUDIO_STATE,
                                            -1);
                                    count = 0;
                                    if (AudioManager.SCO_AUDIO_STATE_CONNECTED == state) {
                                        handler.sendEmptyMessageDelayed(2, 0);
                                        getContext().unregisterReceiver(this);
                                    }
                                }
                            },
                            new IntentFilter(
                                    AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED));
                }
            } else {
                handler.sendEmptyMessageDelayed(2, 0);
            }
        }
    }

    private void startRecording() {
        Log.d(TAG, "startRecording: ");
        if (null != onStartRecordingListener)
            onStartRecordingListener.onStartRecord();
		if(null==mAsr)return;
        mAsr.startListening(mRecognizerListener);
        // postInvalidate();
    }

    public interface OnStartRecordingListener {
        void onStartRecord();
    }

    private OnStartRecordingListener onStartRecordingListener;

    public void setOnStartRecordingListener(
            OnStartRecordingListener onStartRecordingListener) {
        this.onStartRecordingListener = onStartRecordingListener;
    }
    public interface OnTextReceiverListener {
        void TextReceiver(String text);
    }

    private OnTextReceiverListener onTextReceiverListener;

    public void setOnTextReceiverListener(OnTextReceiverListener onTextReceiverListener) {
        this.onTextReceiverListener = onTextReceiverListener;
    }
}
