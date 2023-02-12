package com.example.shootingmania;

public class GameTimer {
    private long timePerGame = 10000; //10 seconds default game time
    private volatile long timeLeft = timePerGame;
    private long prevTime = timeLeft;
    private long countDownResolution = 100; //count down each 100 ms
    private boolean timerStatus = false;
    public GameTimer() {

    }

    public void setTimerTime(long duration) {
        timeLeft = duration;
    }
    public void startCount() {
        prevTime = System.currentTimeMillis();
        timerStatus = true;
        //Create a new game runnable and track for time decrement
        Thread thread = new Thread(new GameRunnable() {
            @Override
            public void gameRun() {
                if (timerStatus) {
                    if (System.currentTimeMillis() - prevTime > countDownResolution) {
                        timeLeft -= countDownResolution;
                        prevTime = System.currentTimeMillis();
                    }
                    if (timeLeft < 0) {
                        timesUp();
                        this.killRunnable();
                        timeLeft = timePerGame;
                    }
                }
            }
        });
        thread.start();
    }
    public void timesUp() {
        //Override this method for game implementation
        onTimesUp();
    }
    public void onTimesUp() {
        //Override this method for game implementation
        stopCount();
    }

    public long getTimeLeft() {
        return timeLeft;
    }

    public void stopCount() {
        timerStatus = false;
    }

    public void addTimeAccumulate(long bonusTimeAccumulate) {
        timeLeft += bonusTimeAccumulate*1000;
    }
}
