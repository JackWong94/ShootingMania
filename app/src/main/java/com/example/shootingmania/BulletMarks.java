package com.example.shootingmania;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.ConcurrentModificationException;
import java.util.Random;

public class BulletMarks {
    private int numberOfSprites = 3;
    private Random random;
    public int posX, posY;
    private int frame = 0;


    private Bitmap bulletMarks[] = new Bitmap[numberOfSprites];

    public BulletMarks(Context context, int posX, int posY) {
        this.posX = posX;
        this.posY = posY;
        bulletMarks[0] = Sprite.createSprite(context, Sprite.NAME.BULLET_MARKS_1);
        bulletMarks[1] = Sprite.createSprite(context, Sprite.NAME.BULLET_MARKS_2);
        bulletMarks[2] = Sprite.createSprite(context, Sprite.NAME.BULLET_MARKS_3);
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
