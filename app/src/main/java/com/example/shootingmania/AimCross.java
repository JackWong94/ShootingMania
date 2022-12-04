package com.example.shootingmania;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Vibrator;

public class AimCross {
    private Context context;
    private static final int RECOIL_MOVEMENT_SPEED = 50;
    private int numberOfSprites = 2;
    private Bitmap aimCross[] = new Bitmap[numberOfSprites];
    public int posX, posY;

    public AimCross(Context context) {
        this.context = context;
        aimCross[0] = Sprite.createSprite(context, Sprite.NAME.AIM_CROSS_UNLOCK);   //1st Frame
        aimCross[1] = Sprite.createSprite(context, Sprite.NAME.AIM_CROSS_LOCK);
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
}
