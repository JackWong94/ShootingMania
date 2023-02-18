package com.shootingmania;

import android.media.AudioManager;
import android.media.SoundPool;

public class SoundManager {
    private boolean muted;
    public void setAllGameSoundToMute() {
        GameSoundPool.setMute();
    }
    public void setAllGameSoundToUnmute() {
        GameSoundPool.setUnmute();
    }
}

class GameSoundPool extends SoundPool {
    private static boolean muted = false;
    public GameSoundPool(int maxStreams, int streamType, int srcQuality) {
        super(maxStreams, streamType, srcQuality);
    }

    public static void setMute() {
        muted = true;
    }

    public static void setUnmute() {
        muted = false;
    }

    public void generateSoundEffect(int id) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (!muted) {
                    play(id, 1.0f, 1.0f, 1, 0, 1.0f);
                }
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}