package com.example.shootingmania;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

public class Gun {
    static boolean resourcesLoaded = false;
    private static final long VIBRATION_STRENGTH = 50;
    private Vibrator vibrator;
    private Context context;
    static private int numberOfSprites = 17;
    static private Bitmap gun[] = new Bitmap[numberOfSprites];
    static private Bitmap bullets[] = new Bitmap[1];
    private int currentFrame = 0;
    private boolean isFirstFrame = true;

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
    private int shootEmptySound;

    //Bullet Part
    private int gunCartridgeSize  = 10; //Capable of holding 10 bullets max
    private int bulletsRemaining = gunCartridgeSize; //Fill cartridge during start game

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
        if (!resourcesLoaded) {
            //Sprites assignment and animation timing
            gun[0] = Sprite.createSprite(context, Sprite.NAME.GUN);
            gun[1] = Sprite.createSprite(context, Sprite.NAME.GUN_SHOOT_1);
            gun[2] = Sprite.createSprite(context, Sprite.NAME.GUN_SHOOT_2);
            gun[3] = Sprite.createSprite(context, Sprite.NAME.GUN_SHOOT_3);
            gun[4] = Sprite.createSprite(context, Sprite.NAME.GUN_SHOOT_4);
            gun[5] = Sprite.createSprite(context, Sprite.NAME.GUN_RELOADING_1);
            gun[6] = Sprite.createSprite(context, Sprite.NAME.GUN_RELOADING_1);
            gun[7] = Sprite.createSprite(context, Sprite.NAME.GUN_RELOADING_1);
            gun[8] = Sprite.createSprite(context, Sprite.NAME.GUN_RELOADING_2);
            gun[9] = Sprite.createSprite(context, Sprite.NAME.GUN_RELOADING_2);
            gun[10] = Sprite.createSprite(context, Sprite.NAME.GUN_RELOADING_2);
            gun[11] = Sprite.createSprite(context, Sprite.NAME.GUN_RELOADING_3);
            gun[12] = Sprite.createSprite(context, Sprite.NAME.GUN_RELOADING_3);
            gun[13] = Sprite.createSprite(context, Sprite.NAME.GUN_RELOADING_4);
            gun[14] = Sprite.createSprite(context, Sprite.NAME.GUN_RELOADING_4);
            gun[15] = Sprite.createSprite(context, Sprite.NAME.GUN_RELOADING_4);
            gun[16] = Sprite.createSprite(context, Sprite.NAME.GUN_RELOADING_4);
            bullets[0] = Sprite.createSpriteForBullets(context, Sprite.NAME.BULLET);
        }
        //Sound effect
        shootSound = gunSoundEffect.load(context, R.raw.gun_shoot,1);
        shootEmptySound = gunSoundEffect.load(context,R.raw.gun_empty,1);
        reloadSound = gunSoundEffect.load(context,R.raw.gun_reload,1);
    }

    public int getRemainingBulletsCount() {
        return bulletsRemaining;
    }

    public Bitmap animateFrame(int frame) {
        //Animate frame passes the bitmap responding to the current frame to gameView class for draw
        return gun[frame];
    }

    public Bitmap animateFrameForBulletRemaining() {
        //Animate frame passes the bitmap responding to the current frame to gameView class for draw
        return bullets[0];
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
        if (bulletsRemaining > 0 && state != STATE.RELOADING) {
            state = STATE.SHOOTING;
            generateSoundEffect(shootSound);
            Point point = new Point(aimCross.posX, aimCross.posY);
            vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_STRENGTH, VibrationEffect.EFFECT_HEAVY_CLICK));
            //Recoil must only happen after the shoot point is taken
            aimCross.recoil();
            bulletsRemaining -= 1;
            return point;
        } else {
            generateSoundEffect(shootEmptySound);
            return new Point(-1,-1); //Return a value that not exist on the target area
        }
    }

    public void reload() {
        state = STATE.RELOADING;
        generateSoundEffect(reloadSound);
        bulletsRemaining = gunCartridgeSize; //Fully filled armor during each reload
    }

    public int getCurrentFrame() {
        if (state == STATE.SHOOTING) {
            if (isFirstFrame) {
                isFirstFrame = false;
                return currentFrame = 1; //First SHOOTING FRAME
            }
            if (currentFrame == 4) {//LAST SHOOTING FRAME
                state = STATE.IDLE;
                isFirstFrame = true;
                currentFrame = 0;
                return currentFrame;
            }
            return currentFrame++;
        }
        if (state == STATE.RELOADING) {
            if (isFirstFrame) {
                isFirstFrame = false;
                return currentFrame = 5; //First SHOOTING FRAME
            }
            if (currentFrame == 16) {//LAST Reloading Frame
                state = STATE.IDLE;
                isFirstFrame = true;
                currentFrame = 0;
                return currentFrame;
            }
            return currentFrame++;
        }
        return currentFrame;
    }
}
