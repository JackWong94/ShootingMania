package com.example.shootingmania;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

public class Target {
    private String TAG = "TARGET";
    static boolean resourcesLoaded = false;
    private static int numberOfSprites = 1;
    private Random random;
    private long spawnTime = 1000;
    private long previousSpawnTime = 0;
    public Rect targetMovingBoundary = new Rect(0,0,GameView.dWidth,GameView.dHeight);
    public volatile int posX, posY;
    public int frame = 0;
    public ArrayList<BulletMarks> bulletMarks;
    private Context context;
    private volatile boolean isVerifyingShoot = false;

    //Sound Part
    private GameSoundPool targetRelatedSound = new GameSoundPool(10, AudioManager.STREAM_MUSIC,0);
    private int hitTarget, hitTarget2;
    private int score;

    static private Bitmap target[] = new Bitmap[numberOfSprites];

    public Target(Context context) {
        this.context = context;
        if (!resourcesLoaded) {
            target[0] = Sprite.createSprite(context, Sprite.NAME.TARGET);   //1st Frame
            hitTarget = targetRelatedSound.load(context, R.raw.hit_target,1);
            hitTarget2 = targetRelatedSound.load(context, R.raw.hit_target2,1);
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
        this.posY = targetMovingBoundary.centerY();
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
        if (accuracy > getTargetWidth(currentFrame)/2 - 30) {
            Log.i(TAG,"MISSED");
        } else {
            //Target Consist Of 4 Area BULLEYES 100 points, CENTER 80, SECOND LAYER 50, OUTER LAYER 30
            Log.i(TAG,"PERFECT" + accuracy);
            if (accuracy >= 0 && accuracy < 10) {
                score += 100;
            } else if (accuracy >= 10 && accuracy < 30) {
                score += 80;
            }
            if (accuracy >= 30 && accuracy < 80) {
                score += 50;
            } else {
                score += 30;
            }
            bulletMarks.add(new BulletMarks(this.context, shotPoint.x, shotPoint.y));
            targetRelatedSound.generateSoundEffect(hitTarget2);
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
}
