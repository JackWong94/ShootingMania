package com.example.shootingmania;

public class GameManager {
    GameView gameView;
    public GameManager(GameView gameView) {
        this.gameView = gameView;
    }

    public void run() {

    }

    public void updateTouchControls(RealTimeInputControlsParameters realTimeInputControlsParameters) {
        gameView.touchPointInteraction(realTimeInputControlsParameters.userTouchPointer);
    }

    public void updateAccelerometerControls(RealTimeInputControlsParameters realTimeInputControlsParameters) {
        gameView.userInputBySensorControlX(realTimeInputControlsParameters.accelerometerSensorValue.x);
        gameView.userInputBySensorControlY(realTimeInputControlsParameters.accelerometerSensorValue.y);
    }
}
