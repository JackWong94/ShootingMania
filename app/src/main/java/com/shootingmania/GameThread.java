package com.shootingmania;

import android.graphics.*;
import android.util.*;
import android.view.*;

public class GameThread extends Thread {

    private static final long TARGET_FPS = 60; // target frames per second
    private static final long TARGET_FRAME_TIME = 1000 / TARGET_FPS; // target time per frame in milliseconds
    private final SurfaceHolder surfaceHolder;
    private final GameView gameView;
    private boolean running;

    //Stabilize elapsed time
    private static final int MOVING_AVERAGE_WINDOW_SIZE = 15;
    private final double[] elapsedTimes = new double[MOVING_AVERAGE_WINDOW_SIZE];
    private int elapsedTimesIndex = 0;

    public GameThread(SurfaceHolder surfaceHolder, GameView gameView) {
        this.surfaceHolder = surfaceHolder;
        this.gameView = gameView;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public void run() {
        long startTime;
        long timeMillis;
        long waitTime;
        long frameCount = 0;
        long totalTime = 0;
        long averageFPS;
        Canvas canvas;

        long previousTime = System.nanoTime();
        double elapsedTime = 0;

        while (running) {
            startTime = System.nanoTime();
            canvas = null;
            elapsedTimes[elapsedTimesIndex] = elapsedTime;
            elapsedTimesIndex = (elapsedTimesIndex + 1) % MOVING_AVERAGE_WINDOW_SIZE;

            double weightedAverageElapsedTime = 0;
            double weightSum = 0;
            for (int i = 0; i < MOVING_AVERAGE_WINDOW_SIZE; i++) {
                double weight = 1.0 / (i + 1);
                weightedAverageElapsedTime += elapsedTimes[(elapsedTimesIndex + i) % MOVING_AVERAGE_WINDOW_SIZE] * weight;
                weightSum += weight;
            }
            double stableElapsedTime = weightedAverageElapsedTime / weightSum;

            try {
                canvas = surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {
                    gameView.update(stableElapsedTime); // Update the game state using elapsed time
                    gameView.draw(canvas);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            timeMillis = (System.nanoTime() - startTime) / 1000000;
            waitTime = TARGET_FRAME_TIME - timeMillis;

            try {
                if (waitTime > 0) {
                    sleep(waitTime);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Calculate elapsed time in seconds
            long currentTime = System.nanoTime();
            elapsedTime = (currentTime - previousTime) / 1000000000.0;
            previousTime = currentTime;

            totalTime += System.nanoTime() - startTime;
            frameCount++;
            if (frameCount == TARGET_FPS) {
                averageFPS = 1000 / ((totalTime / frameCount) / 1000000);
                frameCount = 0;
                totalTime = 0;
                Log.d("FPS", "Average FPS: " + averageFPS);
            }
        }
    }
}
