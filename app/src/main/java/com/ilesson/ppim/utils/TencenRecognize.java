package com.ilesson.ppim.utils;

import android.content.Context;
import android.util.Log;

import com.tencent.aai.AAIClient;
import com.tencent.aai.audio.data.AudioRecordDataSource;
import com.tencent.aai.auth.AbsCredentialProvider;
import com.tencent.aai.auth.LocalCredentialProvider;
import com.tencent.aai.config.ClientConfiguration;
import com.tencent.aai.exception.ClientException;
import com.tencent.aai.exception.ServerException;
import com.tencent.aai.listener.AudioRecognizeResultListener;
import com.tencent.aai.listener.AudioRecognizeStateListener;
import com.tencent.aai.listener.AudioRecognizeTimeoutListener;
import com.tencent.aai.model.AudioRecognizeRequest;
import com.tencent.aai.model.AudioRecognizeResult;
import com.tencent.aai.model.type.AudioRecognizeConfiguration;
import com.tencent.aai.model.type.AudioRecognizeTemplate;
import com.tencent.aai.model.type.EngineModelType;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @ClassName: TencenRecognize
 * @Description: java类作用描述
 * @Author: potato
 * @CreateDate: 2021/7/28 17:05
 * @UpdateUser: potato
 * @UpdateDate: 2021/7/28 17:05
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class TencenRecognize {
    private static final String TAG = "TencenRecognize";
    private static TencenRecognize tencenRecognize = new TencenRecognize();
    public static TencenRecognize getInstance(){
        return tencenRecognize;
    }
    private AAIClient aaiClient;
    private AudioRecognizeRequest audioRecognizeRequest;
    private AudioRecognizeResultListener audioRecognizeResultListener;
    private AudioRecognizeConfiguration audioRecognizeConfiguration;
    private AudioRecognizeTimeoutListener audioRecognizeTimeoutListener;
    private AudioRecognizeStateListener audioRecognizeStateListener;
    boolean dontHaveResult = true;
    private long textChangeTime;
    private String textChange;
    LinkedHashMap<String, String> resMap = new LinkedHashMap<>();
    public void initTencenVoice(Context context) {
        int appid = Integer.valueOf(PPConfig.apppId);
        int projectid = 0;
        String secretId = PPConfig.secretId;
// 为了方便用户测试，sdk提供了本地签名，但是为了secretKey的安全性，正式环境下请自行在第三方服务器上生成签名。
        AbsCredentialProvider credentialProvider = new LocalCredentialProvider(PPConfig.secretKey);
        ClientConfiguration.setMaxRecognizeSliceConcurrentNumber(1);
        ClientConfiguration.setMaxAudioRecognizeConcurrentNumber(1); // 语音识别的请求的最大并发数
        try {
            // 1、初始化AAIClient对象。
            aaiClient = new AAIClient(context, appid, projectid, secretId, credentialProvider);
            // 3、初始化语音识别结果监听器。
            audioRecognizeResultListener = new AudioRecognizeResultListener() {
                @Override
                public void onSliceSuccess(AudioRecognizeRequest audioRecognizeRequest, AudioRecognizeResult result, int seq) {
                    resMap.put(String.valueOf(seq), result.getText());
                    String msg = buildMessage(resMap);
                    if(msg==null){
                        msg="";
                    }
                    if(msg.equals(textChange)){
                        if(System.currentTimeMillis()-textChangeTime>2000){
                            stop();
                        }
                    }else {
                        textChange = msg;
                        textChangeTime = System.currentTimeMillis();
                    }
                    Log.d(TAG, "onSliceSuccess: " + msg);
                    // 返回语音分片的识别结果
                }

                @Override
                public void onSegmentSuccess(AudioRecognizeRequest audioRecognizeRequest, AudioRecognizeResult result, int seq) {
                    // 返回语音流的识别结果
                    dontHaveResult = true;
                    resMap.put(String.valueOf(seq), result.getText());
                    final String msg = buildMessage(resMap);
                    if(null!=onRecognizeListener){
                        onRecognizeListener.onFinish(msg);
                    }
                    stop();
                    Log.d(TAG, "onSegmentSuccess: " + msg);
                }

                @Override
                public void onSuccess(AudioRecognizeRequest audioRecognizeRequest, String content) {
                    Log.d(TAG, "audioRecognizeResultListener onSuccess: "+content);
//                    stopTencenVoice(TextUtils.isEmpty(content) ? false : true);
                    // 返回所有的识别结果
                }

                @Override
                public void onFailure(AudioRecognizeRequest audioRecognizeRequest, ClientException e, ServerException e1) {
                    Log.d(TAG, "onFailure: "+e.toString());
                    Log.d(TAG, "onFailure: "+e1.toString());
                    // 识别失败
                }

            };
            audioRecognizeStateListener = new AudioRecognizeStateListener() {
                /**
                 * 开始录音
                 * @param request
                 */
                @Override
                public void onStartRecord(AudioRecognizeRequest request) {
                    currentRequestId = request.getRequestId();
                    textChange = null;
                    Log.d(TAG, "onStartRecord: ");
                }
                public void onNextAudioData(final short[] audioDatas, final int readBufferLength) {
                }
                /**
                 * 结束录音
                 * @param request
                 */
                @Override
                public void onStopRecord(AudioRecognizeRequest request) {
                    Log.d(TAG, "onStopRecord: ");
                    String content = buildMessage(resMap);

//                    if(TextUtils.isEmpty(content)){
//                        stopTencenVoice(false);
//                        return;
//                    }
//                    content = content.toLowerCase();
//                    Message message = Message.obtain();
//                    message.obj=content;
//                    message.what=3;
//                    handler.sendMessage(message);
                }

                /**
                 * 第seq个语音流开始识别
                 * @param request
                 * @param seq
                 */
                @Override
                public void onVoiceFlowStartRecognize(AudioRecognizeRequest request, int seq) {

                }

                /**
                 * 第seq个语音流结束识别
                 * @param request
                 * @param seq
                 */
                @Override
                public void onVoiceFlowFinishRecognize(AudioRecognizeRequest request, int seq) {
                    Log.d(TAG, "onVoiceFlowFinishRecognize: " + request);
                }

                /**
                 * 第seq个语音流开始
                 * @param request
                 * @param seq
                 */
                @Override
                public void onVoiceFlowStart(AudioRecognizeRequest request, int seq) {
                }

                /**
                 * 第seq个语音流结束
                 * @param request
                 * @param seq
                 */
                @Override
                public void onVoiceFlowFinish(AudioRecognizeRequest request, int seq) {
                }

                /**
                 * 语音音量回调
                 * @param request
                 * @param volume
                 */
                @Override
                public void onVoiceVolume(AudioRecognizeRequest request, final int volume) {
                    Log.d(TAG, "onVoiceVolume: " + volume);
//                    showVolume(volume / 3);
                    if(null!=onRecognizeListener){
                        onRecognizeListener.onVolume(volume);
                    }
                }

            };
            AudioRecognizeRequest.Builder builder = new AudioRecognizeRequest.Builder();

            boolean isSaveAudioRecordFiles = false;//默认是关的 false
            // 初始化识别请求
            audioRecognizeRequest = builder
//                        .pcmAudioDataSource(new AudioRecordDataSource()) // 设置数据源
                    .pcmAudioDataSource(new AudioRecordDataSource(false)) // 设置数据源
                    //.templateName(templateName) // 设置模板
//                    .template(new AudioRecognizeTemplate("16k_ca", 0, 0)) // 设置自定义模板
//                    .setFilterDirty(1)  // 0 ：默认状态 不过滤脏话 1：过滤脏话
//                    .setFilterModal(0) // 0 ：默认状态 不过滤语气词  1：过滤部分语气词 2:严格过滤
//                    .setFilterPunc(2) // 0 ：默认状态 不过滤句末的句号 1：滤句末的句号
//                    .setConvert_num_mode(1) //1：默认状态 根据场景智能转换为阿拉伯数字；0：全部转为中文数字。
//                    .setVadSilenceTime(2000) // 语音断句检测阈值，静音时长超过该阈值会被认为断句（多用在智能客服场景，需配合 needvad = 1 使用） 默认不传递该参数
//                    .setNeedvad(1) //0：关闭 vad，1：默认状态 开启 vad。
//                    .setHotWordId("2d36d9727d7e11eb80b3446a2eb5fd98")//热词 id。用于调用对应的热词表，如果在调用语音识别服务时，不进行单独的热词 id 设置，自动生效默认热词；如果进行了单独的热词 id 设置，那么将生效单独设置的热词 id。
//                    .build();
                    .template(new AudioRecognizeTemplate(EngineModelType.EngineModelType16K.getType(),0,0)) // 设置自定义模板
                    .setFilterDirty(0)  // 0 ：默认状态 不过滤脏话 1：过滤脏话
                    .setFilterModal(0) // 0 ：默认状态 不过滤语气词  1：过滤部分语气词 2:严格过滤
                    .setFilterPunc(0) // 0 ：默认状态 不过滤句末的句号 1：滤句末的句号
                    .setConvert_num_mode(1) //1：默认状态 根据场景智能转换为阿拉伯数字；0：全部转为中文数字。
//                        .setVadSilenceTime(1000) // 语音断句检测阈值，静音时长超过该阈值会被认为断句（多用在智能客服场景，需配合 needvad = 1 使用） 默认不传递该参数
                    .setNeedvad(1) //0：关闭 vad，1：默认状态 开启 vad。
//                        .setHotWordId("")//热词 id。用于调用对应的热词表，如果在调用语音识别服务时，不进行单独的热词 id 设置，自动生效默认热词；如果进行了单独的热词 id 设置，那么将生效单独设置的热词 id。
                    .build();

            // 自定义识别配置
            audioRecognizeConfiguration = new AudioRecognizeConfiguration.Builder()
                    .setSilentDetectTimeOut(true)// 是否使能静音检测，true表示不检查静音部分
                    .audioFlowSilenceTimeOut(500) // 静音检测超时停止录音
                    .minAudioFlowSilenceTime(1000) // 语音流识别时的间隔时间
                    .minVolumeCallbackTime(50) // 音量回调时间
                    .sensitive(3.0f)
                    .build();
            audioRecognizeTimeoutListener = new AudioRecognizeTimeoutListener() {

                /**
                 * 检测第一个语音流超时
                 * @param request
                 */
                @Override
                public void onFirstVoiceFlowTimeout(AudioRecognizeRequest request) {
                    Log.d(TAG, "onFirstVoiceFlowTimeout: ");
                }

                /**
                 * 检测下一个语音流超时
                 * @param request
                 */
                @Override
                public void onNextVoiceFlowTimeout(AudioRecognizeRequest request) {
                    Log.d(TAG, "onNextVoiceFlowTimeout: ");
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private int currentRequestId;
    private String buildMessage(Map<String, String> msg) {

        StringBuffer stringBuffer = new StringBuffer();
        Iterator<Map.Entry<String, String>> iter = msg.entrySet().iterator();
        while (iter.hasNext()) {
            String value = iter.next().getValue();
            stringBuffer.append(value);
        }
        return stringBuffer.toString();
    }

    public void start(){
        resMap.clear();
        if (aaiClient!=null) {
            boolean taskExist = aaiClient.cancelAudioRecognize(currentRequestId);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (aaiClient != null) {
                    aaiClient.startAudioRecognize(audioRecognizeRequest,
                            audioRecognizeResultListener,
                            audioRecognizeStateListener,
                            audioRecognizeTimeoutListener,
                            audioRecognizeConfiguration);
                }
            }
        }).start();
    }
    public void stop(){
        if (aaiClient != null) {
            //停止语音识别，等待当前任务结束
            boolean state = aaiClient.stopAudioRecognize(currentRequestId);
//            aaiClient.cancelAudioRecognize(currentRequestId);
//            aaiClient.release();
        }
    }
    public void release(){
        if (aaiClient != null) {
            aaiClient.cancelAudioRecognize(currentRequestId);
            aaiClient.release();
        }
    }

    public interface OnRecognizeListener{
        void onFinish(String msg);
        void onVolume(int volume);
    }
    private OnRecognizeListener onRecognizeListener;

    public void setOnRecognizeListener(OnRecognizeListener onRecognizeListener) {
        this.onRecognizeListener = onRecognizeListener;
    }
}
