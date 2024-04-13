package com.shootingmania;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.AudioManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

public class Target {
    static boolean resourcesLoaded = false;
    private static final int numberOfSprites = 1;
    private final Random random;
    private long spawnTime = 1800;
    private long previousSpawnTime;
    public Rect targetMovingBoundary = new Rect(0,0,GameView.dWidth,GameView.dHeight);
    public volatile int posX, posY;
    public int frame = 0;
    public ArrayList<BulletMarks> bulletMarks;
    private final Context context;
    private volatile boolean isVerifyingShoot = false;
    private long bonusTimeAccumulate;

    //Sound Part
    private final GameSoundPool targetRelatedSound = new GameSoundPool(10, AudioManager.STREAM_MUSIC,0);
    private int hitTarget;
    private int score;

    static private final Bitmap[] target = new Bitmap[numberOfSprites];

    public Target(Context context) {
        this.bonusTimeAccumulate = 0;
        this.context = context;
        if (!resourcesLoaded) {
            target[0] = Sprite.createSprite(context, Sprite.NAME.TARGET);   //1st Frame
            hitTarget = targetRelatedSound.load(context, R.raw.hit_target2,1);
        }
        random = new Random();
        bulletMarks = new ArrayList<>();
        spawnTimesUp();
        previousSpawnTime = System.currentTimeMillis();
        Thread thread = new Thread(new GameRunnable() {
            @Override
            public void gameRun() {
                if ((System.currentTimeMillis() - previousSpawnTime) > spawnTime) {
                    if (!isVerifyingShoot) {
                        //wait for the last verifying shoot to be finish
                        spawnTimesUp();
                    }
                }
            }
        });
        thread.start();
    }

    public Bitmap animateFrame(int frame) {
        return target[frame];
    }

    public void spawnTimesUp() {
        previousSpawnTime = System.currentTimeMillis();
        resetPosition();
    }

    private void resetPosition() {
        //Clear bullet mark first to prevent UI thread to draw bullet marks on old location of the target
        bulletMarks.clear();
        this.posX = random.nextInt(targetMovingBoundary.right - getTargetWidth(target[0])) + getTargetWidth(target[0])/2;      //Get size from 1st frame
        //this.posX = targetMovingBoundary.centerX();
        //this.posY = targetMovingBoundary.centerY();
        this.posY = random.nextInt((targetMovingBoundary.bottom - targetMovingBoundary.top) - getTargetHeight(target[0])) + getTargetHeight(target[0])/2 + targetMovingBoundary.top;      //Get size from 1st frame
    }

    public int getTargetWidth(Bitmap _target) {
        return _target.getWidth();
    }

    public int getTargetHeight(Bitmap _target) {
        return _target.getHeight();
    }

    public void setMovingArea(Rect targetMoveArea) {
        targetMovingBoundary = targetMoveArea;
        resetPosition();
    }

    public void verifyShoot(Point shotPoint, Bitmap currentFrame) {
        isVerifyingShoot = true;
        Point targetLocation = new Point(this.posX,this.posY);
        int accuracy = calculateDistance(shotPoint,targetLocation);
        String TAG = "TARGET";
        if (accuracy > getTargetWidth(currentFrame)/2 - 30) {
            Log.i(TAG,"MISSED");
        } else {
            //Target Consist Of 4 Area BULLSEYE 100 points, CENTER 80, SECOND LAYER 50, OUTER LAYER 30
            Log.i(TAG,"PERFECT" + accuracy);
            if (accuracy >= 0 && accuracy < 15) {
                score += 100;
                new FontEffects("PERFECT + 3s", targetLocation.x, targetLocation.y);
                bonusTimeAccumulate = 3;
            } else if (accuracy >= 15 && accuracy < 35) {
                score += 80;
                new FontEffects("NICE + 1s", targetLocation.x, targetLocation.y);
                bonusTimeAccumulate = 1;
            } else if (accuracy >= 35 && accuracy < 80) {
                score += 50;
                new FontEffects("GOOD", targetLocation.x, targetLocation.y);
            } else {
                score += 30;
                new FontEffects("GOOD", targetLocation.x, targetLocation.y);
            }
            bulletMarks.add(new BulletMarks(this.context, shotPoint.x, shotPoint.y));
            targetRelatedSound.generateSoundEffect(hitTarget);
        }
        isVerifyingShoot = false;
    }

    public int returnTotalScore() {
        return score;
    }

    private int calculateDistance(Point point1, Point point2) {
        double distance = Math.sqrt(Math.pow((point1.x-point2.x),2) + Math.pow((point1.y-point2.y),2));
        return (int)distance;
    }

    public long updateBonusTimeAccumulate() {
        long temp = bonusTimeAccumulate;
        bonusTimeAccumulate = 0;
        return temp;
    }

    public void getLatestScore(int scorePoints) {
        //The higher score player get, the faster the target refresh
        if (scorePoints < 250) {
            spawnTime = 1800;
        } else if (scorePoints < 500) {
            spawnTime = 1600;
        } else if (scorePoints < 700) {
            spawnTime = 1400;
        } else if (scorePoints < 1000) {
            spawnTime = 1300;
        } else if (scorePoints < 1300) {
            spawnTime = 1200;
        }  else if (scorePoints < 1500) {
            spawnTime = 1000;
        } else if (scorePoints < 2000) {
            spawnTime = 900;
        } else {
            spawnTime = 800;
        }
    }
}
