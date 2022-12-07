package com.example.shootingmania;

import static com.example.shootingmania.GameView.dWidth;

import android.content.Context;
import android.graphics.Rect;

public class GameManager {
    private boolean isPause = false;
    GameView gameView;
    GameData gameData;
    public GameManager(GameView gameView) {
        this.gameView = gameView;
        gameData = new GameData(gameView.getContext());
        gameData.reset();
    }

    public void run() {
        if (isPause) {
            //Skip the game update for pausing moment
            return;
        }
        //Start game loop
        gameData.updateGameData();
    }

    public void updateTouchControls(RealTimeInputControlsParameters realTimeInputControlsParameters) {
        gameView.touchPointInteraction(realTimeInputControlsParameters.userTouchPointer);
        gameData.touchControlsDetected();
    }

    public void updateAccelerometerControls(RealTimeInputControlsParameters realTimeInputControlsParameters) {
        gameData.accelerometerControlsDetected(realTimeInputControlsParameters);
    }
}

class GameData {


    private Context context;
    public Rect targetMoveArea;
    public Gun gun;
    public Target target;
    public AimCross aimCross;
    public int scorePoints;

    public GameData(Context context) {
        this.context = context;
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

    public void touchControlsDetected() {
        target.verifyShoot(gun.shoot(aimCross), target.animateFrame(target.frame));
        aimCross.setAimCrossLock(); //Set state of AimCross for animation
    }

    public void accelerometerControlsDetected(RealTimeInputControlsParameters realTimeInputControlsParameters) {
        controlGunMovement(realTimeInputControlsParameters.accelerometerSensorValue.x, realTimeInputControlsParameters.accelerometerSensorValue.y);
    }
}
