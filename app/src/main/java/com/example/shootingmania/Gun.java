package com.example.shootingmania;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

public class Gun {
    private static final long VIBRATION_STRENGTH = 50;
    private Vibrator vibrator;
    private Context context;
    private int numberOfSprites = 5;
    private Bitmap gun[] = new Bitmap[numberOfSprites];
    private int currentFrame = 0;
    private enum STATE {
        IDLE,
        SHOOTING,
        RELOADING,
    }
    private STATE state = STATE.IDLE;

    public int posX, posY;

    //Sound Part
    private SoundPool gunSoundEffect = new SoundPool(10, AudioManager.STREAM_MUSIC,0);
    private int shootSound;
    private int reloadSound;

    public void generateSoundEffect(int id) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                gunSoundEffect.play(id,1.0f, 1.0f,1,0,1.0f);
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Gun(Context context) {
        this.context = context;
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        gun[0] = Sprite.createSprite(context, Sprite.NAME.GUN);   //1st Frame
        gun[1] = Sprite.createSprite(context, Sprite.NAME.GUN_SHOOT_1);
        gun[2] = Sprite.createSprite(context, Sprite.NAME.GUN_SHOOT_2);
        gun[3] = Sprite.createSprite(context, Sprite.NAME.GUN_SHOOT_3);
        gun[4] = Sprite.createSprite(context, Sprite.NAME.GUN_SHOOT_4);
        shootSound = gunSoundEffect.load(context, R.raw.gun_shoot,1);
        reloadSound = gunSoundEffect.load(context,R.raw.gun_reload,1);
    }

    public Bitmap animateFrame(int frame) {
        return gun[frame];
    }

    public void resetGunPosition(int _posX, int _posY) {
        posX = _posX;
        posY = _posY;
    }

    public int getGunWidth(Bitmap _gun) {
        return _gun.getWidth();
    }

    public int getGunHeight(Bitmap _gun) {
        return _gun.getHeight();
    }

    public Point shoot(AimCross aimCross){
        state = STATE.SHOOTING;
        generateSoundEffect(shootSound);
        Point point = new Point(aimCross.posX, aimCross.posY);
        vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_STRENGTH, VibrationEffect.EFFECT_HEAVY_CLICK));
        //vibrator.cancel();
        //vibrator.vibrate(10);
        //Recoil must only happen after the shoot point is taken
        aimCross.recoil();
        return point;
    }

    public int getCurrentFrame() {
        if (state == STATE.SHOOTING) {
            if (currentFrame < 1 && currentFrame > 4) { //
                currentFrame = 2;
                return currentFrame;
            }
            if (currentFrame == 4) {//LAST SHOOTING FRAME
                state = STATE.IDLE;
                currentFrame = 0;
                return currentFrame;
            }
            return currentFrame++;
        }
        return currentFrame;
    }
}
