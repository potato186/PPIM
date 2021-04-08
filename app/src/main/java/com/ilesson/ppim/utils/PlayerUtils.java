package com.ilesson.ppim.utils;

import android.content.Context;
import android.media.MediaPlayer;

import com.ilesson.ppim.R;

public class PlayerUtils {
    private MediaPlayer mediaPlayer;

    public void initPlayer(Context context){
        mediaPlayer = MediaPlayer.create(context,
                R.raw.uds_ret);
    }

    public void play(){
        mediaPlayer.setLooping(false);//音乐的循环播放
        mediaPlayer.start();
    }
}
