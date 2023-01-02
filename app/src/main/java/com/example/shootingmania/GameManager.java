package com.example.shootingmania;

import static com.example.shootingmania.GameView.dWidth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.util.Log;

public class GameManager {
    public enum ACTIVITY_STATE {
        GAME_MENU,
        START_GAME,
        LEADERBOARDS,
        GAME_OVER,
    }
    public ACTIVITY_STATE activityState = ACTIVITY_STATE.GAME_MENU;
    public boolean isInitialized = false;
    private boolean isPause = false;
    public GameView gameView;
    public GameData gameData;
    private Context context;
    public GameManager(GameView gameView) {
        this.gameView = gameView;
        this.context = gameView.getContext();
        //Loading game data
        gameData = new GameData(context, this);
        gameData.initializing();

    }

    private void setActivityPage(ACTIVITY_STATE newActivityState) {
        activityState = newActivityState;
        switch (activityState) {
            case GAME_MENU: setPause(); GameActivityPage.startActivity(gameView.gameMenuActivity); break;
            case START_GAME: gameData.startGame(); setResume(); GameActivityPage.startActivity(gameView.startGameActivity); break;
            case LEADERBOARDS: break;
            case GAME_OVER: setPause(); GameActivityPage.startActivity(gameView.gameOverActivity); break;
            default: break;
        }
    }

    public void run() {
        if (!isInitialized) {
            setActivityPage(activityState);
            isInitialized = true;
            return;
        }
        if (isPause) {
            //Skip the game update for pausing moment
            return;
        }
        //Start game loop
        gameData.updateGameData();
    }

    public void setPause() {
        isPause = true;
        GameRunnable.pauseAllGameRunnable();
    }

    public void setResume() {
        isPause = false;
        GameRunnable.resumeAllGameRunnable();
    }

    public void setGameOver() {
        activityState = ACTIVITY_STATE.GAME_OVER;
        isInitialized = false;
        //setActivityPage(ACTIVITY_STATE.GAME_OVER);
    }

    public void backToMainMenu() {
        activityState = ACTIVITY_STATE.GAME_MENU;
        isInitialized = false;
        //setActivityPage(ACTIVITY_STATE.GAME_MENU);
    }

    public void exitGame() {
        ((Activity)context).finish();
    }

    public void updateTouchControls(RealTimeInputControlsParameters realTimeInputControlsParameters) {
        if (isPause) {
            //Set menu responsiveness during pausing game
            gameView.onTouchPointInteraction(realTimeInputControlsParameters);
            return;
            //Pause all game related touch controls
        }
        gameView.onTouchPointInteraction(realTimeInputControlsParameters);
        gameData.touchControlsDetected(realTimeInputControlsParameters);
    }

    public void updateSwipeMotionControls(RealTimeInputControlsParameters realTimeInputControlsParameters) {
        if (isPause) {
            return;
            //Pause all game related touch controls
        }
        gameData.swipeControlsDetected(realTimeInputControlsParameters);
    }

    public void updateAccelerometerControls(RealTimeInputControlsParameters realTimeInputControlsParameters) {
        if (isPause) {
            return;
            //Pause all game related accelerometer controls
        }
        gameData.accelerometerControlsDetected(realTimeInputControlsParameters);
    }
}

class GameData {
    private Context context;
    private GameManager gameManager;
    public Rect targetMoveArea;
    public Gun gun;
    public Target target;
    public AimCross aimCross;
    public int scorePoints;
    public GameTimer gameTimer;

    public GameData(Context context, GameManager gameManager) {
        this.context = context;
        this.gameManager = gameManager; //allow game data to communicate with game manager
        gameTimer = new GameTimer() {
            @Override
            public void onTimesUp() {
                gameManager.setGameOver();
            }
        };
    }

    public void initializing() {
        reset();
    }

    public void reset() {
        //Reset and create all game object for the game
        targetMoveArea = new Rect(0, 500, dWidth, 900);//Place target move area at the top section of game screen
        scorePoints = 0;
        gun = new Gun(context);
        gun.resetGunPosition(dWidth/2, GameView.dHeight*4/5); //Place gun at the lower center of game screen
        aimCross = new AimCross(context);
        aimCross.resetAimCrossPosition(targetMoveArea.centerX(), targetMoveArea.centerY());
        //Set single target only in the game, future levels implementation can consider add more targets
        target = new Target(context);
        target.setMovingArea(targetMoveArea);
    }

    public void startGame() {
        reset();
        gameTimer.setTimerTime(20000);
        gameTimer.startCount();
    }

    public void updateGameData() {
        int tempScore = 0;
        tempScore += target.returnTotalScore();
        scorePoints = tempScore;
    }

    public void controlGunMovement(float x_dir_movement, float y_dir_movement){
        aimCross.posY += y_dir_movement;
        aimCross.posX -= x_dir_movement;
        gun.posX -= x_dir_movement;
        //Aim cross and gun out of screen prevention
        if (aimCross.posY < targetMoveArea.top){
            aimCross.posY = targetMoveArea.top;
        }
        if (aimCross.posY > targetMoveArea.bottom){
            aimCross.posY = targetMoveArea.bottom;
        }
        if (aimCross.posX < targetMoveArea.left + aimCross.getAimCrossWidth((aimCross.animateFrame(0)))/2) {
            aimCross.posX = targetMoveArea.left + aimCross.getAimCrossWidth((aimCross.animateFrame(0)))/2;
            gun.posX = aimCross.posX;
        }
        if (aimCross.posX > targetMoveArea.right - aimCross.getAimCrossWidth((aimCross.animateFrame(0)))/2) {
            aimCross.posX = targetMoveArea.right - aimCross.getAimCrossWidth((aimCross.animateFrame(0)))/2;
            gun.posX = aimCross.posX;
        }
    }

    public void touchControlsDetected(RealTimeInputControlsParameters realTimeInputControlsParameters) {
        if (realTimeInputControlsParameters.onPressed()) {
            target.verifyShoot(gun.shoot(aimCross), target.animateFrame(target.frame));
            aimCross.setAimCrossLock(); //Set state of AimCross for animation
        }
    }

    public void swipeControlsDetected(RealTimeInputControlsParameters realTimeInputControlsParameters) {
        if (realTimeInputControlsParameters.swipeDirection == RealTimeInputControlsParameters.SWIPE_DIR.SWIPE_DOWN) {
            gun.reload();
        }
    }

    public void accelerometerControlsDetected(RealTimeInputControlsParameters realTimeInputControlsParameters) {
        controlGunMovement(realTimeInputControlsParameters.accelerometerSensorValue.x, realTimeInputControlsParameters.accelerometerSensorValue.y);
    }
}

abstract class GameRunnable implements Runnable{
    private static boolean gameIsPause = false;
    private boolean resumeFromPause;
    private boolean run = true;
    public GameRunnable() {
        resumeFromPause = false;
    }

    @Override
    public void run() {
        while(run) {
            if (gameIsPause) {
                //Continue keep thread alive but skipping gameRun
                resumeFromPause = true;
                continue;
            }
            if (resumeFromPause) {
                gameResume();
                resumeFromPause = false;
            }
            gameRun();
        }
    }

    public void killRunnable() {
        run = false;
    }

    public void gameResume() {
        //Override this function in the gameRunnable implementation
        //This function will run in a separate thread and is control under GameManager for game resume state
    }

    public void gameRun() {
        //Override this function in the gameRunnable implementation
        //This function will run in a separate thread and is control under GameManager for game pausing state
    }

    public static void pauseAllGameRunnable() {
        gameIsPause = true;
    }

    public static void resumeAllGameRunnable() {
        gameIsPause = false;
    }
}
