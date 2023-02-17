package com.example.shootingmania;

import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;

import java.util.ArrayList;

public class FontEffects {
    public String fontsContent;
    public int x;
    public int y;
    public long spawnTime = 1000;
    public long prevTime;
    public Paint paint;
    public boolean active;

    static ArrayList<FontEffects> activeFontEffectsList = new ArrayList<FontEffects>();
    public FontEffects(String fontsContent, int x, int y) {
        this.fontsContent = fontsContent;
        this.x =  x;
        this.y =  y;
        this.paint = new TextPaint();
        this.paint.setColor(Color.parseColor("#00FF00"));
        this.paint.setTextSize(50);
        this.prevTime = System.currentTimeMillis();
        this.active = true;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (active)
                if (System.currentTimeMillis() - prevTime > spawnTime) {
                    effectEnded();
                    active = false;
                }
            }
        });
        thread.start();
        activeFontEffectsList.add(this);
    }

    public void effectMovementUpdate() {
        y-=4;
    }

    public void effectEnded() {
        activeFontEffectsList.remove(this);
    }

    public static void updateAll() {
        for (FontEffects f:activeFontEffectsList) {
            if (f!=null) {
                f.effectMovementUpdate();
            }
        }
    }

    public static void reset() {
        activeFontEffectsList.clear();
    }
}
