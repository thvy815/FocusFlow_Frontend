package com.example.focusflow_frontend.presentation.pomo;

import android.content.Context;
import android.media.MediaPlayer;
import android.widget.Toast;

public class WhiteNoisePlayer {

    private MediaPlayer mediaPlayer;

    public void startWhiteNoise(Context context, int id) {
        stopWhiteNoise();
        mediaPlayer = MediaPlayer.create(context, id);
        if (mediaPlayer != null) {
            Toast.makeText(context, "Playing white noise", Toast.LENGTH_SHORT).show(); // ← Thêm dòng này
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(1.0f, 1.0f);
            mediaPlayer.start();
        } else {
            Toast.makeText(context, "MediaPlayer is null!", Toast.LENGTH_SHORT).show(); // ← Báo lỗi
        }
    }


    public void stopWhiteNoise() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
