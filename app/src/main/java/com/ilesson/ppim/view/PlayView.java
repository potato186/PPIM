package com.ilesson.ppim.view;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.ilesson.ppim.R;
import com.ilesson.ppim.utils.Dateuitls;

import java.util.List;


public class PlayView extends RelativeLayout implements OnCompletionListener,
        OnPreparedListener {

    private boolean flag_prepared = false;
    private boolean playState = false;
    private ImageView mPlayBtn;
    private SeekBar seekBar;
    public boolean flag_play;
    private TextView mTotalTime;
    private TextView mCurrentTime;
    private Player player;
    private List<String> urls;
    private int mIndex;
    private String url;

    public PlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(
                R.layout.xueban_player_control, this);
        mPlayBtn = (ImageView) view.findViewById(R.id.xueban_play_btn);
        seekBar = (SeekBar) view.findViewById(R.id.xueban_seekbar);
        mTotalTime = (TextView) view.findViewById(R.id.xueban_total_time);
        mCurrentTime = (TextView) view.findViewById(R.id.xueban_play_time);
        Click click = new Click();
        mPlayBtn.setOnClickListener(click);
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // if(!flag_prepared){
                // seekBar.setProgress(0);
                // }
                // if (!flag_play) {
                // seekBar.setProgress(0);
                // }
                if (fromUser & player != null) {
                    player.seekTo(progress);
                }
            }
        });
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void playList(List<String> list) {
        urls = list;
        mIndex = 0;
//		playVoice(urls.get(mIndex));
    }

    public void playVoice() {
        if(TextUtils.isEmpty(url)){
            return;
        }
        mPlayBtn.setImageResource(R.mipmap.voice_pause);
        new Thread(new Runnable() {

            @Override
            public void run() {
                player = new Player(url);
                player.prepare();
            }
        }).start();
    }


    class Click implements OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.xueban_play_btn:
                    if(!flag_prepared){
                        playVoice();
                        return;
                    }
                    playState = !playState;
                    if (playState) {
                        mPlayBtn.setImageResource(R.mipmap.voice_pause);
                        if (null != player && !player.getIsPlaying()) {
                            player.start();
                            flag_play = true;
                        }
                    } else {
                        if (null != player && player.getIsPlaying()) {
                            player.pause();
                            flag_play = false;
                        }
                        mPlayBtn.setImageResource(R.mipmap.voice_play);
                    }
                    break;

                default:
                    break;
            }
            if (null != mOnEnventListener) {
                mOnEnventListener.onEnvent();
            }
        }

    }

    public void pause() {
        if (playState) {
            mPlayBtn.performClick();
        }
    }

    public boolean isPlaying() {
        return player.getIsPlaying();
    }

    private String voice;

    class Player {
        MediaPlayer mp;

        Player(String path) {
            voice = path;
            mp = new MediaPlayer();
        }

        private void prepare() {
            if(null==mp)
                mp = new MediaPlayer();
            mp.setOnCompletionListener(PlayView.this);
            mp.setOnPreparedListener(PlayView.this);
            try {
                mp.reset();
                mp.setDataSource(voice);
                flag_play = true;
                mp.prepare();
            } catch (IllegalStateException e) {
                mp = null;
                mp = new MediaPlayer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void seekTo(int seek) {
            if (null != mp) {
                mp.seekTo(seek);
            }
        }

        private int getDuration() {
            if (null != mp) {
                return mp.getDuration();
            }
            return 0;
        }

        private boolean getIsPlaying() {
            if (null != mp) {
                return mp.isPlaying();
            }
            return false;
        }

        private void pause() {
            if (null != mp) {
                mp.pause();
            }
        }

        private void start() {
            if (null != mp) {
                mp.start();
            }
            playState = true;
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        while (flag_play) {
                            if (null != mp) {
                                handler.obtainMessage(update_progress,
                                        mp.getCurrentPosition(), 0)
                                        .sendToTarget();
                            }
                            Thread.sleep(20);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        private void stop() {
            if (null != mp && player != null) {
                mp.stop();
                mp.reset();
                mp.release();
                mp = null;
            }
        }

        ;
    }

    private static final String TAG = "PlayView";
    private static final int update_progress = 0x01;
    private static final int completion = 0x02;
    private static final int prepared = 0x03;
    private static final int next = 0x04;
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case update_progress:
                    if (flag_play) {
                        seekBar.setProgress(msg.arg1);
                        int s = msg.arg1 / 1000;
                        mCurrentTime.setText(Dateuitls.formatSeconds(s));
                    }
                    break;
                case completion:
//				playVoice(voice);
                    break;
                case next:
//				playVoice(urls.get(mIndex));
                    break;
                case prepared:
                    if (null != player) {
                        play();
                        int length = player.getDuration();
                        int s = length / 1000;
                        mTotalTime.setText(Dateuitls.formatSeconds(s));
//                        mTotalTime.setText("" + s / 60 + ":"
//                                + (s % 60 < 10 ? ("0" + (s % 60)) : s % 60));
                        seekBar.setMax(length);
                    }
                    break;
            }
        }
    };

    public void play() {
        if (null != player && flag_prepared) {
            player.start();
        }
    }

    public void setVoiceCmdFinish(boolean voiceCmdFinish) {
        this.voiceCmdFinish = voiceCmdFinish;
    }

    public void init() {
        mCurrentTime.setText("00:00");
        mTotalTime.setText("00:00");
        seekBar.setProgress(0);
        mPlayBtn.setImageResource(R.mipmap.voice_play);
        flag_play = false;
    }

    private boolean voiceCmdFinish = false;
    private boolean sigle = false;

    public void stopPlay() {
        flag_play = false;
        // if (null!=seekBar) {
        // seekBar.setProgress(0);
        // }
        if (null != player) {
            player.stop();
        }
    }

    public void setSigle(boolean sigle) {
        this.sigle = sigle;
    }


    public void setState(boolean state) {
        this.state = state;
    }

    private boolean state = false;

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (flag_play) {
            flag_prepared = true;
            handler.sendEmptyMessage(prepared);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        init();
        stopPlay();
    }

    private OnPlayListener mOnPlayListener;

    public void setOnPlayListener(OnPlayListener mOnPlayListener) {
        this.mOnPlayListener = mOnPlayListener;
    }

    public interface OnPlayListener {
        void onPrevious();

        void onNext();
    }

    private OnEnventListener mOnEnventListener;

    public void setmOnEnventListener(OnEnventListener mOnEnventListener) {
        this.mOnEnventListener = mOnEnventListener;
    }

    public interface OnEnventListener {
        void onEnvent();

        void onStartPlay();
    }
}
