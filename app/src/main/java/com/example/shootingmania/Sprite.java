package com.shootingmania;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

public class Sprite {
    private static int OFFSET_DUE_TO_ASSET_ERROR = 1;
    private static int scaleUpSize = 300;
    private static int pixelRatio = 3;
    private static int spritePixelSize = 32;
    private static int spriteSizeInDisplay = spritePixelSize * pixelRatio;

    public enum NAME {
        TARGET,
        GUN, GUN_SHOOT_1, GUN_SHOOT_2, GUN_SHOOT_3,
        GUN_RELOADING_1, GUN_RELOADING_2, GUN_RELOADING_3, GUN_RELOADING_4,  GUN_RELOADING_5,
        AIM_CROSS_LOCK, AIM_CROSS_UNLOCK,
        BULLET_MARKS_1, BULLET_MARKS_2, BULLET_MARKS_3,
        BULLET,
        FLAME_0, FLAME_1, FLAME_2,
    }

    public static Bitmap createSprite(Context context, NAME name) {
        Bitmap sprite;
        sprite = BitmapFactory.decodeResource(context.getResources(),R.drawable.game_resources);
        switch (name) {
            case TARGET: {
                sprite = Bitmap.createBitmap(sprite,4*spritePixelSize*pixelRatio,(1*spritePixelSize*pixelRatio)+OFFSET_DUE_TO_ASSET_ERROR,spriteSizeInDisplay,spriteSizeInDisplay-OFFSET_DUE_TO_ASSET_ERROR);
                sprite = Bitmap.createScaledBitmap(sprite, scaleUpSize,scaleUpSize,true);
                break;
            }
            case GUN: {
                sprite = Bitmap.createBitmap(sprite,0*spritePixelSize*pixelRatio,(0*spritePixelSize*pixelRatio)+OFFSET_DUE_TO_ASSET_ERROR,spriteSizeInDisplay,spriteSizeInDisplay-OFFSET_DUE_TO_ASSET_ERROR*2);
                sprite = Bitmap.createScaledBitmap(sprite, scaleUpSize,scaleUpSize,true);
                break;
            }
            case GUN_SHOOT_1: {
                sprite = Bitmap.createBitmap(sprite,1*spritePixelSize*pixelRatio,(0*spritePixelSize*pixelRatio)+OFFSET_DUE_TO_ASSET_ERROR,spriteSizeInDisplay,spriteSizeInDisplay-OFFSET_DUE_TO_ASSET_ERROR*2);
                sprite = Bitmap.createScaledBitmap(sprite, scaleUpSize,scaleUpSize,true);
                break;
            }
            case GUN_SHOOT_2: {
                sprite = Bitmap.createBitmap(sprite,2*spritePixelSize*pixelRatio,(0*spritePixelSize*pixelRatio)+OFFSET_DUE_TO_ASSET_ERROR,spriteSizeInDisplay,spriteSizeInDisplay-OFFSET_DUE_TO_ASSET_ERROR*2);
                sprite = Bitmap.createScaledBitmap(sprite, scaleUpSize,scaleUpSize,true);
                break;
            }
            case GUN_SHOOT_3: {
                sprite = Bitmap.createBitmap(sprite,3*spritePixelSize*pixelRatio,(0*spritePixelSize*pixelRatio)+OFFSET_DUE_TO_ASSET_ERROR,spriteSizeInDisplay,spriteSizeInDisplay-OFFSET_DUE_TO_ASSET_ERROR*2);
                sprite = Bitmap.createScaledBitmap(sprite, scaleUpSize,scaleUpSize,true);
                break;
            }
            case GUN_RELOADING_1: {
                sprite = Bitmap.createBitmap(sprite,4*spritePixelSize*pixelRatio,(0*spritePixelSize*pixelRatio)+OFFSET_DUE_TO_ASSET_ERROR,spriteSizeInDisplay,spriteSizeInDisplay-OFFSET_DUE_TO_ASSET_ERROR*2);
                sprite = Bitmap.createScaledBitmap(sprite, scaleUpSize,scaleUpSize,true);
                break;
            }
            case GUN_RELOADING_2: {
                sprite = Bitmap.createBitmap(sprite,0*spritePixelSize*pixelRatio,(1*spritePixelSize*pixelRatio)+OFFSET_DUE_TO_ASSET_ERROR,spriteSizeInDisplay,spriteSizeInDisplay-OFFSET_DUE_TO_ASSET_ERROR*2);
                sprite = Bitmap.createScaledBitmap(sprite, scaleUpSize,scaleUpSize,true);
                break;
            }
            case GUN_RELOADING_3: {
                sprite = Bitmap.createBitmap(sprite,1*spritePixelSize*pixelRatio,(1*spritePixelSize*pixelRatio)+OFFSET_DUE_TO_ASSET_ERROR,spriteSizeInDisplay,spriteSizeInDisplay-OFFSET_DUE_TO_ASSET_ERROR*2);
                sprite = Bitmap.createScaledBitmap(sprite, scaleUpSize,scaleUpSize,true);
                break;
            }
            case GUN_RELOADING_4: {
                sprite = Bitmap.createBitmap(sprite,2*spritePixelSize*pixelRatio,(1*spritePixelSize*pixelRatio)+OFFSET_DUE_TO_ASSET_ERROR,spriteSizeInDisplay,spriteSizeInDisplay-OFFSET_DUE_TO_ASSET_ERROR*2);
                sprite = Bitmap.createScaledBitmap(sprite, scaleUpSize,scaleUpSize,true);
                break;
            }
            case GUN_RELOADING_5: {
                sprite = Bitmap.createBitmap(sprite,3*spritePixelSize*pixelRatio,(1*spritePixelSize*pixelRatio)+OFFSET_DUE_TO_ASSET_ERROR,spriteSizeInDisplay,spriteSizeInDisplay-OFFSET_DUE_TO_ASSET_ERROR*2);
                sprite = Bitmap.createScaledBitmap(sprite, scaleUpSize,scaleUpSize,true);
                break;
            }
            case BULLET_MARKS_1: {
                sprite = Bitmap.createBitmap(sprite,0*spritePixelSize*pixelRatio,(2*spritePixelSize*pixelRatio)+OFFSET_DUE_TO_ASSET_ERROR,spriteSizeInDisplay,spriteSizeInDisplay-OFFSET_DUE_TO_ASSET_ERROR*2);
                sprite = Bitmap.createScaledBitmap(sprite, scaleUpSize,scaleUpSize,true);
                break;
            }
            case BULLET_MARKS_2: {
                sprite = Bitmap.createBitmap(sprite,1*spritePixelSize*pixelRatio,(2*spritePixelSize*pixelRatio)+OFFSET_DUE_TO_ASSET_ERROR,spriteSizeInDisplay,spriteSizeInDisplay-OFFSET_DUE_TO_ASSET_ERROR);
                sprite = Bitmap.createScaledBitmap(sprite, scaleUpSize,scaleUpSize,true);
                break;
            }
            case BULLET_MARKS_3: {
                sprite = Bitmap.createBitmap(sprite,2*spritePixelSize*pixelRatio,(2*spritePixelSize*pixelRatio)+OFFSET_DUE_TO_ASSET_ERROR,spriteSizeInDisplay,spriteSizeInDisplay-OFFSET_DUE_TO_ASSET_ERROR);
                sprite = Bitmap.createScaledBitmap(sprite, scaleUpSize,scaleUpSize,true);
                break;
            }
            case FLAME_0: {
                sprite = Bitmap.createBitmap(sprite,0,0,1,1);
                break;
            }
            case FLAME_1: {
                sprite = Bitmap.createBitmap(sprite,3*spritePixelSize*pixelRatio,(2*spritePixelSize*pixelRatio)+OFFSET_DUE_TO_ASSET_ERROR,spriteSizeInDisplay,spriteSizeInDisplay-OFFSET_DUE_TO_ASSET_ERROR);
                sprite = Bitmap.createScaledBitmap(sprite, scaleUpSize*6/10,scaleUpSize*6/10,true);
                break;
            }
            case FLAME_2: {
                sprite = Bitmap.createBitmap(sprite,4*spritePixelSize*pixelRatio,(2*spritePixelSize*pixelRatio)+OFFSET_DUE_TO_ASSET_ERROR,spriteSizeInDisplay,spriteSizeInDisplay-OFFSET_DUE_TO_ASSET_ERROR);
                sprite = Bitmap.createScaledBitmap(sprite, scaleUpSize*6/10,scaleUpSize*6/10,true);
                break;
            }
            default: break;
        }
        return sprite;
    }

    public static Bitmap createSpriteForAimCross(Context context, NAME name) {
        Bitmap aimCrossSprite = null;
        switch (name) {
            case AIM_CROSS_LOCK: {
                aimCrossSprite = BitmapFactory.decodeResource(context.getResources(),R.drawable.aim_lock);
                aimCrossSprite = Bitmap.createScaledBitmap(aimCrossSprite, -scaleUpSize,-scaleUpSize,true);
                break;
            }
            case AIM_CROSS_UNLOCK: {
                aimCrossSprite = BitmapFactory.decodeResource(context.getResources(),R.drawable.aim_unlock);
                aimCrossSprite = Bitmap.createScaledBitmap(aimCrossSprite, -scaleUpSize,-scaleUpSize,true);
                break;
            }
            default: break;
        }
        return aimCrossSprite;
    }

    public static Bitmap createSpriteForBullets(Context context, NAME name) {
        Bitmap spriteBullet;
        spriteBullet = BitmapFactory.decodeResource(context.getResources(),R.drawable.bullet);
        switch (name) {
            case BULLET: {
                //Scale down size
                spriteBullet = Bitmap.createScaledBitmap(spriteBullet, 100,100,true);
                break;
            }

            default: break;
        }
        return spriteBullet;
    }
}
