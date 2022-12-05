package com.example.shootingmania;

import android.content.Context;

public class GameManager {
    private boolean isPause = false;
    GameView gameView;
    GameData gameData;
    public GameManager(GameView gameView) {
        this.gameView = gameView;
        gameData = new GameData(gameView.getContext());
    }

    public void run() {
        if (isPause) {
            //Skip the game update for pausing moment
            return;
        }
    }

    public void updateTouchControls(RealTimeInputControlsParameters realTimeInputControlsParameters) {
        gameView.touchPointInteraction(realTimeInputControlsParameters.userTouchPointer);
    }

    public void updateAccelerometerControls(RealTimeInputControlsParameters realTimeInputControlsParameters) {
        gameView.userInputBySensorControlX(realTimeInputControlsParameters.accelerometerSensorValue.x);
        gameView.userInputBySensorControlY(realTimeInputControlsParameters.accelerometerSensorValue.y);
    }
}

class GameData {

    public GameData(Context context) {

    }
}
