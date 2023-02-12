package com.example.shootingmania;

import static com.example.shootingmania.GameView.dWidth;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.util.Log;
import com.google.gson.Gson;
import java.util.ArrayList;

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
    public GameScoreManager gameScoreManager;
    private Context context;
    public GameManager(GameView gameView) {
        this.gameView = gameView;
        this.context = gameView.getContext();
        //Loading game data
        gameData = new GameData(context, this);
        gameData.initializing();
        gameScoreManager = new GameScoreManager(context, this);
        gameScoreManager.initializing();
    }

    private void setActivityPage(ACTIVITY_STATE newActivityState) {
        activityState = newActivityState;
        switch (activityState) {
            case GAME_MENU: setPause(); GameActivityPage.startActivity(gameView.gameMenuActivity); break;
            case START_GAME: gameData.startGame(); setResume(); GameActivityPage.startActivity(gameView.startGameActivity); break;
            case LEADERBOARDS: GameActivityPage.startActivity(gameView.leaderboardActivity); break;
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

    public void updateKeyboardInput(char key) {
        //Single key detections
    }

    public void updateKeyboardInputString(String string) {
        //String detection
        gameView.onKeyboardInteraction(string);
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

class GameScoreManager {
    private String TAG = "GameScoreManager";
    private int supportedScoreStoringNumber = 10; //Temporarily supporting storing 10 best scores only, rest will be discard
    private SharedPreferences sharedPreferencesScoresData;
    private String sharedPreferencesScoresDataKey = "SCORE_DATA";
    private SharedPreferences mPrefs = null;
    private GameManager gameManager;
    private Context context;
    public GameScoreList gameScoreList;
    public GameScoreManager(Context context, GameManager gameManager) {
        this.context = context;
        this.gameManager = gameManager; //allow game score to communicate with game manager
    }
    public void initializing() {
        /*
            MODE_PRIVATE: File creation mode: the default mode, where the created file can only be accessed by the calling application (or all applications sharing the same user ID).
            MODE_WORLD_READABLE: File creation mode: allow all other applications to have read access to the created file.
            MODE_WORLD_WRITEABLE : File creation mode: allow all other applications to have write access to the created file.
         */
        sharedPreferencesScoresData = context.getSharedPreferences("GameSavedData", Context.MODE_PRIVATE);
        String json = sharedPreferencesScoresData.getString(sharedPreferencesScoresDataKey, "NO DATA");
        gameScoreList = new GameScoreList();
        Log.i(TAG, "Loading Score " + json);
        if (json != "NO DATA") {
            Gson gson = new Gson();
            gameScoreList = gson.fromJson(json, GameScoreList.class);
        }
    }

    public void checkLeadearboardEntryQualification(GameScore currentGameScore) {
        Log.i(TAG, "Checking qualification to enter leaderboard");
        int newHigherScoreIndex = gameScoreList.list.size();
        int lastPlaceInTheLeaderboard = gameScoreList.list.size()-1;
        for (int i=0; i<=lastPlaceInTheLeaderboard; i++) {
            if (currentGameScore.playerScore > gameScoreList.list.get(i).playerScore) {
                newHigherScoreIndex = i;
                //Found the higher scorer that qualify for i places
                Log.i(TAG, "New high score detected");
                break;
            }
        }
        /*  If the newHigherScoreIndex detect any score that is currently higher than the i index for the leaderboards
            proceed to rearrange the array with the current scores. Push the rest of the array member one space after this i index.
            If the array member exceeds supportedScoreStoringNumber, the next member from the last supportedScoreStoringNumber will be discarded.
        */
        if (newHigherScoreIndex != gameScoreList.list.size()) {
            //Store this last place score to check if we need to discard or not after the adding of the newer score
            GameScore olderLastPlaceScore = gameScoreList.list.get(lastPlaceInTheLeaderboard);
            for (int i=lastPlaceInTheLeaderboard; i>=newHigherScoreIndex; i--) {
                if (i!=newHigherScoreIndex) {
                    gameScoreList.list.set(i,gameScoreList.list.get(i-1));
                    Log.i(TAG, "Move " + Integer.toString((i-1)) + " to " + Integer.toString(i));
                } else {
                    //When reach the desired place, set the score and exit the loop
                    gameScoreList.list.set(i,currentGameScore);
                    Log.i(TAG, "Set " + Integer.toString((i)) + " to " + Integer.toString(currentGameScore.playerScore));
                }
            }
            if (gameScoreList.list.size()  < supportedScoreStoringNumber) {
                //Since there are still unfilled places in leaderboard, the olderLastPlaceScore can be stored
                gameScoreList.list.add(olderLastPlaceScore);
                Log.i(TAG, "Add new score to NO " + Integer.toString(gameScoreList.list.size()));
            }
        } else {
            //If the leaderboard is not full, add the score to the last place
            if(gameScoreList.list.size() < supportedScoreStoringNumber) {
                gameScoreList.list.add(currentGameScore);
                Log.i(TAG, "Add new score to NO " + Integer.toString(gameScoreList.list.size()));
            }
        }
        for (int i =0; i<gameScoreList.list.size(); i++) {
            Log.i(TAG, Integer.toString(gameScoreList.list.get(i).playerScore));
        }
        saveScoreToSharedPreference();
    }

    public void saveScoreToSharedPreference() {
        SharedPreferences.Editor scoreEditor = sharedPreferencesScoresData.edit();
        Gson gson = new Gson();
        String json = gson.toJson(gameScoreList);
        scoreEditor.putString(sharedPreferencesScoresDataKey, json);
        Log.i(TAG, "Saving Score " + json);
        scoreEditor.apply();
    }
}

class GameScoreList {
    public ArrayList<GameScore> list = new ArrayList<GameScore>();
    public GameScoreList() {

    }
}

class GameScore {
    private final int MAX_NAME_LENGTH = 16;
    public String playerName = "_______________";
    public int playerScore = 0;
    public GameScore(String name, int score) {
        this.playerName = name;
        this.playerScore = score;
    }

    public void setPlayerName(String name) {
        playerName = name;
        //Set limit to the length of name
        if (name.length() > MAX_NAME_LENGTH) {
            playerName = playerName.substring(0, MAX_NAME_LENGTH);
        }
        //Set default name if the string is empty
        if (name.isEmpty()) {
            playerName = "UNKNOWN";
        }
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
    private boolean userFirstShooting;

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
        gameTimer.stopCount();
    }

    public void startGame() {
        reset();
        userFirstShooting = true;
        gameTimer.setTimerTime(20000);
    }

    public void updateGameData() {
        int tempScore = 0;
        tempScore += target.returnTotalScore();
        if (userFirstShooting && tempScore != 0) {
            userFirstShooting = false;
            gameTimer.startCount();
        }
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
        if (realTimeInputControlsParameters.onReleased()) {
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
