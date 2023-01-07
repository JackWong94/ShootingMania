package com.example.shootingmania;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Vibrator;

public class AimCross {
    static boolean resourcesLoaded = false;
    private Context context;
    private static final int RECOIL_MOVEMENT_SPEED = 50;
    static private int numberOfSprites = 2;
    static private Bitmap aimCross[] = new Bitmap[numberOfSprites];
    public int posX, posY;
    public int currentFrame = 0;
    static int frameCounter = 0;
    private STATE state = STATE.UNLOCKING;
    private enum STATE {
        LOCKING,
        UNLOCKING,
    }

    public AimCross(Context context) {
        this.context = context;
        if (!resourcesLoaded) {
            aimCross[0] = Sprite.createSpriteForAimCross(context, Sprite.NAME.AIM_CROSS_UNLOCK);   //1st Frame
            aimCross[1] = Sprite.createSpriteForAimCross(context, Sprite.NAME.AIM_CROSS_LOCK);
        }
    }

    public Bitmap animateFrame(int frame) {
        return aimCross[frame];
    }

    public void resetAimCrossPosition(int _posX, int _posY) {
        posX = _posX;
        posY = _posY;
    }

    public int getAimCrossWidth(Bitmap _aimCross) {
        return _aimCross.getWidth();
    }

    public int getAimCrossHeight(Bitmap _aimCross) {
        return _aimCross.getHeight();
    }

    public void recoil() {
        posY -= RECOIL_MOVEMENT_SPEED;
    }

    public void setAimCrossLock() {
        state = STATE.LOCKING;
    }

    public int getCurrentFrame() {
        if (state == STATE.LOCKING) {
            if (frameCounter > 5) {
                state = STATE.UNLOCKING;
                frameCounter = 0;
            } else {
                frameCounter++;
            }
            return currentFrame = 1;
        } else {
            return currentFrame = 0;
        }
    }
}
