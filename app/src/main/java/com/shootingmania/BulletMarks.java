package com.shootingmania;

import android.content.*;
import android.graphics.*;

import java.util.*;

public class BulletMarks {
    static boolean resourcesLoaded = false;
    static private final int numberOfSprites = 3;
    public int posX, posY;
    private int frame;


    static private final Bitmap[] bulletMarks = new Bitmap[numberOfSprites];

    public BulletMarks(Context context, int posX, int posY) {
        this.posX = posX;
        this.posY = posY;

        if (!resourcesLoaded) {
            bulletMarks[0] = Sprite.createSprite(context, Sprite.NAME.BULLET_MARKS_1);
            bulletMarks[1] = Sprite.createSprite(context, Sprite.NAME.BULLET_MARKS_2);
            bulletMarks[2] = Sprite.createSprite(context, Sprite.NAME.BULLET_MARKS_3);
            resourcesLoaded = true;
        }
        Random random = new Random();
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
