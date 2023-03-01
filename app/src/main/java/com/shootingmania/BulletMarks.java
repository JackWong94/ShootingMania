package com.shootingmania;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.util.ConcurrentModificationException;
import java.util.Random;

public class BulletMarks {
    static boolean resourcesLoaded = false;
    static private int numberOfSprites = 3;
    private Random random;
    public int posX, posY;
    private int frame = 0;


    static private Bitmap bulletMarks[] = new Bitmap[numberOfSprites];

    public BulletMarks(Context context, int posX, int posY) {
        this.posX = posX;
        this.posY = posY;

        if (!resourcesLoaded) {
            bulletMarks[0] = Sprite.createSprite(context, Sprite.NAME.BULLET_MARKS_1);
            bulletMarks[1] = Sprite.createSprite(context, Sprite.NAME.BULLET_MARKS_2);
            bulletMarks[2] = Sprite.createSprite(context, Sprite.NAME.BULLET_MARKS_3);
            resourcesLoaded = true;
        }
        random = new Random();
        frame = random.nextInt(numberOfSprites);
    }

    public Bitmap animateFrame() {
        return bulletMarks[frame];
    }

    public int getTargetWidth() {
        return bulletMarks[0].getWidth();
    }

    public int getTargetHeight() {
        return bulletMarks[0].getHeight();
    }
}
