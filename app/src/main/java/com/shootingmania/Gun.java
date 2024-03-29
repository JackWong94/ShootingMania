package com.shootingmania;

import android.content.*;
import android.graphics.*;
import android.media.*;
import android.os.*;

public class Gun {
    static boolean resourcesLoaded = false;
    private static final long VIBRATION_STRENGTH = 50;
    private Vibrator vibrator;
    static private final int numberOfSprites = 9;
    static private final Bitmap[] gun = new Bitmap[numberOfSprites];
    static private final int[] gunFrameCountControl = new int[numberOfSprites];
    static private final Bitmap[] bullets = new Bitmap[1];
    static private final Bitmap[] flames = new Bitmap[3];
    static private final int[] gunFrameCountControlForFlame = new int[3];
    private int currentFrame = 0;
    private int currentFramePlayCount = 0;
    private boolean isFirstFrame = true;

    private int currentFrameForFlame = 0;
    private int currentFramePlayCountForFlame = 0;
    private boolean isFirstFrameForFlame = true;

    private enum STATE {
        IDLE,
        SHOOTING,
        RELOADING,
        SHOOTING_FAIL
    }
    private STATE state = STATE.IDLE;

    public int posX, posY;

    //Sound Part
    private final GameSoundPool gunSoundEffect = new GameSoundPool(10, AudioManager.STREAM_MUSIC,0);
    private int shootSound;
    private int reloadSound;
    private int shootEmptySound;

    //Bullet Part
    private final int gunCartridgeSize  = 10; //Capable of holding 10 bullets max
    private int bulletsRemaining = gunCartridgeSize; //Fill cartridge during start game

    //Gun flame part
    public int flamePositionOffsetX = 80;
    public int flamePositionOffsetY = 95;

    public Gun(Context context) {
        if (!resourcesLoaded) {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            //Sprites assignment and animation timing
            gun[0] = Sprite.createSprite(context, Sprite.NAME.GUN);
            gunFrameCountControl[0] = 1;
            gun[1] = Sprite.createSprite(context, Sprite.NAME.GUN_SHOOT_1);
            gunFrameCountControl[1] = 5;
            gun[2] = Sprite.createSprite(context, Sprite.NAME.GUN_SHOOT_2);
            gunFrameCountControl[2] = 8;
            gun[3] = Sprite.createSprite(context, Sprite.NAME.GUN_SHOOT_3);
            gunFrameCountControl[3] = 10;
            gun[4] = Sprite.createSprite(context, Sprite.NAME.GUN_RELOADING_1);
            gunFrameCountControl[4] = 15;
            gun[5] = Sprite.createSprite(context, Sprite.NAME.GUN_RELOADING_2);
            gunFrameCountControl[5] = 20;
            gun[6] = Sprite.createSprite(context, Sprite.NAME.GUN_RELOADING_3);
            gunFrameCountControl[6] = 10;
            gun[7] = Sprite.createSprite(context, Sprite.NAME.GUN_RELOADING_4);
            gunFrameCountControl[7] = 10;
            gun[8] = Sprite.createSprite(context, Sprite.NAME.GUN_RELOADING_5);
            gunFrameCountControl[8] = 20;
            bullets[0] = Sprite.createSpriteForBullets(context, Sprite.NAME.BULLET);
            flames[0] = Sprite.createSprite(context, Sprite.NAME.FLAME_0);
            flames[1] = Sprite.createSprite(context, Sprite.NAME.FLAME_1);
            gunFrameCountControlForFlame[1] = 1;
            flames[2] = Sprite.createSprite(context, Sprite.NAME.FLAME_2);
            gunFrameCountControlForFlame[2] = 1;
            //Sound effect
            shootSound = gunSoundEffect.load(context, R.raw.gun_shoot,1);
            shootEmptySound = gunSoundEffect.load(context,R.raw.gun_empty,1);
            reloadSound = gunSoundEffect.load(context,R.raw.gun_reload,1);
        }
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

    public Bitmap animateFrameForShootingFlame(int frame) {
        //Animate frame passes the bitmap responding to the current frame to gameView class for draw
        return flames[frame];
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
            gunSoundEffect.generateSoundEffect(shootSound);
            Point point = new Point(aimCross.posX, aimCross.posY);
            Thread thread = new Thread(() -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_STRENGTH, VibrationEffect.EFFECT_HEAVY_CLICK));
                    }
                }
            });
            thread.start();
            //Recoil must only happen after the shoot point is taken
            aimCross.recoil();
            bulletsRemaining -= 1;
            return point;
        } else {
            gunSoundEffect.generateSoundEffect(shootEmptySound);
            state = STATE.SHOOTING_FAIL;
            return new Point(-1,-1); //Return a value that not exist on the target area
        }
    }

    public void reload() {
        state = STATE.RELOADING;
        gunSoundEffect.generateSoundEffect(reloadSound);
        bulletsRemaining = gunCartridgeSize; //Fully filled armor during each reload
    }

    public int getCurrentFrameForFlame() {
        if (currentFramePlayCountForFlame != 0) {
            currentFramePlayCountForFlame--;
        } else {
            switch (state) {
                case SHOOTING: {
                    if (isFirstFrameForFlame) {
                        currentFrameForFlame = 0;
                        currentFramePlayCountForFlame = gunFrameCountControl[currentFrameForFlame];
                        isFirstFrameForFlame = false;
                    } else {
                        if (currentFrameForFlame < 2) {
                            currentFrameForFlame++;
                            currentFramePlayCountForFlame = gunFrameCountControl[currentFrameForFlame];
                        } else {
                            isFirstFrameForFlame = true;
                        }
                    }
                    break;
                }
                case IDLE:
                default: {
                    //IDLE frame
                    currentFrameForFlame = 0;
                    currentFramePlayCountForFlame = gunFrameCountControlForFlame[currentFrameForFlame];
                    break;
                }
            }
        }
        return currentFrameForFlame;
    }

    public int getCurrentFrame() {
        if (currentFramePlayCount != 0) {
            currentFramePlayCount--;
        } else {
            switch (state) {
                case SHOOTING: {
                    if (isFirstFrame) {
                        currentFrame = 1;
                        currentFramePlayCount = gunFrameCountControl[currentFrame];
                        isFirstFrame = false;
                    } else {
                        if (currentFrame < 3) {
                            currentFrame++;
                            currentFramePlayCount = gunFrameCountControl[currentFrame];
                        } else {
                            state = STATE.IDLE;
                            isFirstFrame = true;
                        }
                    }
                    break;
                }
                case RELOADING: {
                    if (isFirstFrame) {
                        currentFrame = 4;
                        currentFramePlayCount = gunFrameCountControl[currentFrame];
                        isFirstFrame = false;
                    } else {
                        if (currentFrame < 8) {
                            currentFrame++;
                            currentFramePlayCount = gunFrameCountControl[currentFrame];
                        } else {
                            state = STATE.IDLE;
                            isFirstFrame = true;
                        }
                    }
                    break;
                }
                case SHOOTING_FAIL: {
                    if (isFirstFrame) {
                        currentFrame = 1;
                        currentFramePlayCount = gunFrameCountControl[currentFrame];
                        isFirstFrame = false;
                    } else {
                        state = STATE.IDLE;
                        isFirstFrame = true;
                    }
                    break;
                }
                default: {
                    //IDLE frame
                    currentFrame = 0;
                    currentFramePlayCount = gunFrameCountControl[currentFrame];
                    break;
                }
            }
        }
        return currentFrame;
    }
}
