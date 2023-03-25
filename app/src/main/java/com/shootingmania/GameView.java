package com.shootingmania;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.text.TextPaint;
import android.util.*;
import android.view.*;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    public GameThread gameThread;
    private final SoundManager gameSoundManager;
    private GameManager gameManager;
    private InputControlsManager gameInputControlManager;
    private Display display;
    private String ThemeColorString = "#DEEBF7";
    public static int dHeight;
    public static int dWidth;
    private String TAG = "GameView";
    public GameActivityPage gameMenuActivity;
    public GameActivityPage startGameActivity;
    public GameActivityPage gameOverActivity;
    public GameActivityPage leaderboardActivity;
    private Context context;
    private Handler handler;
    public static boolean isShowingAdvertisement;
    private Rect advertisement;
    private TextDisplay systemUpsFpsDisplay;
    private long systemUPS, systemFPS;

    public GameView(Context context) {
        super(context);
        this.context = context;
        this.handler = new Handler();
        display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
        //Display background settings
        Point displaySize = new Point();
        display.getRealSize(displaySize);
        dWidth = displaySize.x;
        dHeight = displaySize.y;

        //Implement game thread
        getHolder().addCallback(this);
        gameThread = new GameThread(getHolder(), this);
        setFocusable(true);

        //Dependency among managers are unavoidable, manager instantiation sequence is critical in this section.
        gameSoundManager = new SoundManager();
        gameManager = new GameManager(this);
        gameInputControlManager = new InputControlsManager(context, display, gameManager, this);

        //Advertisement coming soon
        isShowingAdvertisement = true;
        advertisement = new Rect(0, dHeight - 150, dWidth, dHeight); //728 x 90

        systemUpsFpsDisplay = new TextDisplay(context, "fps=          ups=         ", new Point(dWidth*82/100,dHeight*2/100));
        systemUpsFpsDisplay.setFontSize(40);
        systemUpsFpsDisplay.setDefaultTypeFace();
        systemUpsFpsDisplay.setColor("#00FF00");
        systemUPS = 0;
        systemFPS = 0;

        designNewActivity();
    }
    //Design new activity here
    private void designNewActivity() {

        gameMenuActivity =new GameActivityPage() {
            private Rect gameBackground;
            private Paint gameBackgroundColor;
            private TextDisplay displayGameTitle1stLine;
            private TextDisplay displayGameTitle2ndLine;
            private TextButton displayStartGameButton;
            private TextButton displayLeaderboardsGameButton;
            private TextButton displayExitGameButton;
            @Override
            public void initialize() {
                gameBackground = new Rect(0, 0, dWidth, dHeight);
                gameBackgroundColor = new Paint();
                gameBackgroundColor.setColor(Color.parseColor("#DEEBF7"));

                int fontSize = 150;
                displayGameTitle1stLine = new TextDisplay(context, "SHOOTING", new Point(dWidth/2,dHeight/5));
                displayGameTitle1stLine.setFontSize(fontSize);
                displayGameTitle2ndLine = new TextDisplay(context, "MANIA", new Point(dWidth/2,dHeight/5 + fontSize*2));
                displayGameTitle2ndLine.setFontSize(fontSize);
                int offset = 300;
                Point displayStartButtonPosition = new Point(dWidth/2,dHeight/2);
                Point displayLeaderboardsGameButtonPosition = new Point(displayStartButtonPosition.x,displayStartButtonPosition.y + offset);
                Point displayExitGameButtonPosition = new Point(displayStartButtonPosition.x,displayLeaderboardsGameButtonPosition.y + offset);
                displayStartGameButton = new TextButton(context, "Start Game", displayStartButtonPosition);
                displayStartGameButton.setButtonBoxVisibility(true);
                displayLeaderboardsGameButton = new TextButton(context, "Leaderboards", displayLeaderboardsGameButtonPosition);
                displayLeaderboardsGameButton.setButtonBoxVisibility(true);
                displayExitGameButton = new TextButton(context, "Exit", displayExitGameButtonPosition);
                displayExitGameButton.setButtonBoxVisibility(true);
            }

            @Override
            public void onDraw(Canvas canvas) {
                canvas.drawRect(gameBackground, gameBackgroundColor);
                displayGameTitle1stLine.draw(canvas);
                displayGameTitle2ndLine.draw(canvas);
                displayStartGameButton.draw(canvas);
                displayLeaderboardsGameButton.draw(canvas);
                displayExitGameButton.draw(canvas);
            }

            @Override
            public void onTouchInteraction(Rect _userTouchPointer) {
                if (displayStartGameButton.isClicked(_userTouchPointer)) {
                    //Start game
                    gameManager.activityState = GameManager.ACTIVITY_STATE.START_GAME;
                    gameManager.isInitialized = false;
                }
                if (displayLeaderboardsGameButton.isClicked(_userTouchPointer)) {
                    //Show leaderboards
                    gameManager.activityState = GameManager.ACTIVITY_STATE.LEADERBOARDS;
                    gameManager.isInitialized = false;
                }
                if (displayExitGameButton.isClicked(_userTouchPointer)) {
                    //Exit Game
                    gameManager.exitGame();
                }
            }

            @Override
            public void onKeyboardInteraction(String _string) {

            }

        };

        startGameActivity = new GameActivityPage() {
            private Rect gameBackground;
            private Paint gameBackgroundColor;
            private int displayScorePoints;
            private Point displayScorePointsPosition;
            private Paint displayScorePointsTextPaint;
            private TextButton displayMenuButton;
            private Point displayMenuButtonPosition;
            private DialogBox displayMenuDialogBox;
            private int displayGameLeftTime;
            private Point displayGameLeftTimePosition;
            private Paint displayGameLeftTimeTextPaint;
            private int displayGameLeftTimeTextPaintSize = 160;
            private Rect displayTargetMoveArea;
            private Paint displayTargetMoveAreaColor;
            private Gun displayGun;
            private AimCross displayAimCross;
            private Target displayTarget;
            private Point displayBulletRemainsPosition;
            private TextDisplay reloadingInstructionsPopUp;
            private Point reloadingInstructionsPopUpPosition;
            private int TEXT_SIZE = 80;
            private ArrayList<FontEffects> displayFontEffectsList;
            @Override
            public void initialize() {
                //Game activity page initialize
                gameBackground = new Rect(0, 0, dWidth, dHeight);
                gameBackgroundColor = new Paint();
                gameBackgroundColor.setColor(Color.parseColor(ThemeColorString));

                //Get all display object from gameManager.gameData
                displayScorePoints = gameManager.gameData.scorePoints;
                displayScorePointsPosition = new Point(50,150);
                displayScorePointsTextPaint = new TextPaint();
                displayScorePointsTextPaint.setTextAlign(TextPaint.Align.LEFT);   //For Text That Updates It Self CENTER Align may cause unwanted swift in display if Text become longer
                displayScorePointsTextPaint.setTextSize(TEXT_SIZE);
                displayScorePointsTextPaint.setColor(Color.parseColor("#EF8F3F"));
                displayScorePointsTextPaint.setTypeface(ResourcesCompat.getFont(context,R.font.kenney_blocks));
                displayGameLeftTime = (int) gameManager.gameData.gameTimer.getTimeLeft();
                displayGameLeftTimePosition = new Point(dWidth/2-25 + displayGameLeftTimeTextPaintSize/4,380);
                displayGameLeftTimeTextPaint = new TextPaint();
                displayGameLeftTimeTextPaint.setTextAlign(TextPaint.Align.CENTER);   //For Text That Updates It Self CENTER Align may cause unwanted swift in display if Text become longer
                displayGameLeftTimeTextPaint.setTextSize(displayGameLeftTimeTextPaintSize);
                displayGameLeftTimeTextPaint.setColor(Color.parseColor("#EF8F3F"));
                displayGameLeftTimeTextPaint.setTypeface(ResourcesCompat.getFont(context,R.font.kenney_blocks));
                displayMenuButtonPosition = new Point(displayScorePointsPosition.x + 850,150);
                displayMenuButton = new TextButton(context, "MENU",  displayMenuButtonPosition);
                displayMenuDialogBox = new DialogBox(context, new Point(dWidth/2,dHeight/2), "BACK TO MENU ?");
                displayTargetMoveArea = gameManager.gameData.targetMoveArea;
                displayTargetMoveAreaColor = new Paint();
                displayTargetMoveAreaColor.setColor(Color.parseColor("#0AAAFF"));
                displayGun = gameManager.gameData.gun;
                displayAimCross = gameManager.gameData.aimCross;
                displayTarget = gameManager.gameData.target;
                if (isShowingAdvertisement) {
                    gameManager.gameData.gun.posY -= advertisement.height();
                    displayBulletRemainsPosition = new Point(120, dHeight - 220 - advertisement.height());
                    reloadingInstructionsPopUpPosition = new Point( dWidth/2, dHeight - 200 - advertisement.height());
                } else {
                    displayBulletRemainsPosition = new Point(120, dHeight - 220);
                    reloadingInstructionsPopUpPosition = new Point( dWidth/2, dHeight - 200);
                }
                reloadingInstructionsPopUp = new TextDisplay(context,"Slide Down To Reload !", reloadingInstructionsPopUpPosition);
                reloadingInstructionsPopUp.setFontSize(50);
                reloadingInstructionsPopUp.setBlinkCapability(500);
                displayFontEffectsList = FontEffects.activeFontEffectsList;
            }

            @Override
            public void onDraw(Canvas canvas) {
                //Game activity page onDraw
                Paint paint = new Paint();
                paint.setStyle(Paint.Style.FILL);

                //Drawing sequence affecting the overlay sequence, be cautious during changing the sequence at these draw method
                //Game ui rendering
                canvas.drawRect(gameBackground, gameBackgroundColor);
                displayScorePoints = gameManager.gameData.scorePoints;
                canvas.drawText("SCORE: " + Integer.toString(displayScorePoints),displayScorePointsPosition.x,displayScorePointsPosition.y, displayScorePointsTextPaint);
                displayGameLeftTime = (int) gameManager.gameData.gameTimer.getTimeLeft()/1000; //Convert Millis To Seconds
                canvas.drawText(Integer.toString(displayGameLeftTime), displayGameLeftTimePosition.x,displayGameLeftTimePosition.y, displayGameLeftTimeTextPaint);
                displayMenuButton.draw(canvas);
                displayMenuDialogBox.draw(canvas);
                canvas.drawRect(displayTargetMoveArea, displayTargetMoveAreaColor);

                //Game object rendering
                if (displayTarget.animateFrame(displayTarget.frame)!=null) {
                    canvas.drawBitmap(displayTarget.animateFrame(displayTarget.frame), displayTarget.posX - displayTarget.getTargetWidth(displayTarget.animateFrame(displayTarget.frame))/2, displayTarget.posY - displayTarget.getTargetHeight(displayTarget.animateFrame(displayTarget.frame))/2, null);
                }
                for (BulletMarks b : displayTarget.bulletMarks) {
                    if (b!=null) {
                        canvas.drawBitmap(b.animateFrame(), b.posX - b.getTargetWidth() / 2, b.posY - b.getTargetHeight() / 2, null);
                    }
                }
                if (displayGun.animateFrame(displayGun.getCurrentFrame()) != null) {
                    canvas.drawBitmap(displayGun.animateFrame(displayGun.getCurrentFrame()), displayGun.posX - displayGun.getGunWidth((displayGun.animateFrame(displayGun.getCurrentFrame()))) / 4, displayGun.posY - displayGun.getGunHeight((displayGun.animateFrame(displayGun.getCurrentFrame()))) / 2, null);
                    if (displayGun.animateFrameForShootingFlame(displayGun.getCurrentFrameForFlame()) != null) {
                        canvas.drawBitmap(displayGun.animateFrameForShootingFlame(displayGun.getCurrentFrameForFlame()), displayGun.posX - displayGun.getGunWidth((displayGun.animateFrame(displayGun.getCurrentFrameForFlame()))) / 4 - displayGun.flamePositionOffsetX, displayGun.posY - displayGun.getGunHeight((displayGun.animateFrame(displayGun.getCurrentFrameForFlame()))) / 2 - displayGun.flamePositionOffsetY, null);
                    }
                }
                if (displayAimCross.animateFrame(displayAimCross.getCurrentFrame()) != null) {
                    canvas.drawBitmap(displayAimCross.animateFrame(displayAimCross.getCurrentFrame()), displayAimCross.posX - displayAimCross.getAimCrossWidth((displayAimCross.animateFrame(0))) / 2, displayAimCross.posY - displayAimCross.getAimCrossHeight((displayAimCross.animateFrame(0))) / 2, null);
                }
                for (int i=0; i<displayGun.getRemainingBulletsCount(); i++) {
                    if (displayGun.animateFrameForBulletRemaining() != null) {
                        canvas.drawBitmap(displayGun.animateFrameForBulletRemaining(), displayBulletRemainsPosition.x * i, displayBulletRemainsPosition.y, null);
                    }
                }
                if (displayGun != null) {
                    if (displayGun.getRemainingBulletsCount() == 0) {
                        reloadingInstructionsPopUp.draw(canvas);
                    }
                }
                if (displayFontEffectsList != null) {
                    for (FontEffects f:displayFontEffectsList) {
                        if (f!=null) {
                            canvas.drawText(f.fontsContent, f.x, f.y, f.paint);
                        }
                    }
                }
            }

            @Override
            public void onTouchInteraction(Rect _userTouchPointer) {
                //Game activity on touch interaction
                //Detecting click on menuButton
                if (displayMenuButton.isClicked(_userTouchPointer)) {
                    //Toggle Back To Menu Dialog Box
                    if (displayMenuDialogBox.popUp) {
                        //Out of the menu, resume game
                        gameManager.setResume();
                        displayMenuDialogBox.hide();
                    } else {
                        //Enter the menu, resume game
                        gameManager.setPause();
                        displayMenuDialogBox.show();
                    }
                }

                //Detecting click on menuDialogBox when it is pop up
                switch (displayMenuDialogBox.isInteracted(_userTouchPointer)) {
                    case NO: gameManager.setResume(); displayMenuDialogBox.hide();break;
                    case YES: gameManager.backToMainMenu(); break;
                    default: break;
                }
            }

            @Override
            public void onKeyboardInteraction(String _string) {

            }
        };

        gameOverActivity = new GameActivityPage() {
            private Rect gameBackground;
            private Paint gameBackgroundColor;
            private long activityUpTime;
            private long minimumGameOverShowingTime = 1500;         //1.5 seconds
            private boolean allowToSwitchActivity = false;
            private TextDisplay displayGameOverText;
            private TextDisplay displayYourScoreText;
            private TextDisplay displayScoreTextDisplay;
            private TextDisplay displayPressToContinueTextDisplay;
            private TextDisplay displayLeaderboardPlayerNameUnderline;
            private TextInput displayLeaderboardPlayerName;
            private TextButton displayLeaderboardPlayerNameClickToEditArea;
            private TextButton displayPressToContinueTextDisplayButtonArea;
            private Point displayGameOverTextPosition = new Point(dWidth/2, dHeight/8*2);
            private Point displayYourScoreTextPosition = new Point(dWidth/2, dHeight/8*4);
            private Point displayScoreTextDisplayPosition = new Point(dWidth/2, dHeight/8*5);
            private Point displayPressToContinueTextDisplayPosition = new Point(dWidth/2, dHeight/8*6);
            private Point displayPressToContinueTextDisplayButtonAreaPosition = new Point(dWidth/2, dHeight/8*6);
            private Point displayLeaderboardPlayerNamePosition = new Point(dWidth/2, dHeight/16*6);
            private Point displayLeaderboardPlayerNameUnderlinePosition = new Point(dWidth/2, dHeight/16*7 - 100);
            //Activity data related
            private GameScore thisSessionGameScore;

            @Override
            public void initialize() {
                //Managing score and leaderboard
                thisSessionGameScore = new GameScore("UNKNOWN", gameManager.gameData.scorePoints);
                gameBackground = new Rect(0, 0, dWidth, dHeight);
                gameBackgroundColor = new Paint();
                gameBackgroundColor.setColor(Color.parseColor(ThemeColorString));
                allowToSwitchActivity = false;
                displayGameOverText = new TextDisplay(context, "GAME OVER", displayGameOverTextPosition);
                displayYourScoreText = new TextDisplay(context, "YOUR SCORES : ", displayYourScoreTextPosition);
                displayScoreTextDisplay = new TextDisplay(context, Integer.toString(gameManager.gameData.scorePoints),displayScoreTextDisplayPosition);
                displayPressToContinueTextDisplay = new TextDisplay(context, "Press Here To Continue !",displayPressToContinueTextDisplayPosition);
                displayPressToContinueTextDisplay.setFontSize(50);
                displayPressToContinueTextDisplay.setBlinkCapability(1000);
                displayPressToContinueTextDisplayButtonArea = new TextButton(context, "", displayPressToContinueTextDisplayButtonAreaPosition);
                displayPressToContinueTextDisplayButtonArea.setButtonArea(dWidth, 500);
                displayLeaderboardPlayerName = new TextInput(context, "Enter Your Name !", displayLeaderboardPlayerNamePosition);
                displayLeaderboardPlayerNameClickToEditArea = new TextButton(context, "", displayLeaderboardPlayerNamePosition);
                displayLeaderboardPlayerNameClickToEditArea.setButtonArea(800, 200);
                displayLeaderboardPlayerNameUnderline = new TextDisplay(context, "_ _ _ _ _ _ _ _ _", displayLeaderboardPlayerNameUnderlinePosition);
                activityUpTime = System.currentTimeMillis();
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (!allowToSwitchActivity) {
                            if (System.currentTimeMillis() - activityUpTime > minimumGameOverShowingTime) {
                                allowToSwitchActivity = true;
                            }
                        }
                    }
                });
                thread.start();
            }

            @Override
            public void onDraw(Canvas canvas) {
                canvas.drawRect(gameBackground, gameBackgroundColor);
                displayGameOverText.draw(canvas);
                displayYourScoreText.draw(canvas);
                displayScoreTextDisplay.draw(canvas);
                displayLeaderboardPlayerNameClickToEditArea.draw(canvas);
                displayLeaderboardPlayerName.draw(canvas);
                displayLeaderboardPlayerNameUnderline.draw(canvas);
                if (allowToSwitchActivity) {
                    displayPressToContinueTextDisplayButtonArea.draw(canvas);
                    displayPressToContinueTextDisplay.draw(canvas);
                }
            }

            @Override
            public void onTouchInteraction(Rect _userTouchPointer) {
                if (displayLeaderboardPlayerNameClickToEditArea.isClicked(_userTouchPointer)) {
                    gameInputControlManager.keyboardControl.showKeyboard();
                } else {
                    //Any other click location beside the typing box, the keboard will be hidden
                    gameInputControlManager.keyboardControl.hideKeyboard();
                }

                //This activity must stay for the minimumGameOverShowingTime, so that user will not miss the score display
                if (allowToSwitchActivity) {
                    //When touch screen, trigger back to main activity
                    if (displayPressToContinueTextDisplayButtonArea.isClicked(_userTouchPointer)) {
                        gameManager.gameScoreManager.checkLeadearboardEntryQualification(thisSessionGameScore);
                        gameManager.backToMainMenu();
                    }
                }
            }

            @Override
            public void onKeyboardInteraction(String _string) {
                thisSessionGameScore.setPlayerName(_string);
                displayLeaderboardPlayerName.setBlinkCapability(0);
                displayLeaderboardPlayerName.setText(thisSessionGameScore.playerName);
            }
        };

        leaderboardActivity = new GameActivityPage() {
            private Rect gameBackground;
            private Paint gameBackgroundColor;
            private TextDisplay displayLeaderboardTitle;
            private Point displayLeaderboardTitlePosition;
            private TextButton displayMenuButton;
            private Point displayMenuButtonPosition;
            private DialogBox displayMenuDialogBox;
            //Leaderboard UI Design
            private int displayLeaderboardListSupportedSize = 10;   //Only show top 10 scores
            private int displayLeaderboardListOffsetYFromTitle = 225;
            private int displayLeaderboardListOffsetYBetweenList = 160;
            private Point displayLeaderboardListPosition;
            private ArrayList<TextDisplay> displayLeaderboardListNo;
            private ArrayList<TextDisplay> displayLeaderboardListName;
            private ArrayList<TextDisplay> displayLeaderboardListScore;

            @Override
            public void initialize() {
                gameBackground = new Rect(0, 0, dWidth, dHeight);
                gameBackgroundColor = new Paint();
                gameBackgroundColor.setColor(Color.parseColor(ThemeColorString));
                displayLeaderboardTitlePosition = new Point(dWidth/2, dHeight/6);
                displayLeaderboardTitle = new TextDisplay(context, "Leaderboards", displayLeaderboardTitlePosition);
                displayLeaderboardTitle.setFontSize(120);
                displayMenuButtonPosition = new Point(900,150);
                displayMenuButton = new TextButton(context, "MENU",  displayMenuButtonPosition);
                displayMenuDialogBox = new DialogBox(context, new Point(dWidth/2,dHeight/2), "BACK TO MENU ?");
                //Initialize leaderboard list
                displayLeaderboardListPosition = new Point(dWidth/9, displayLeaderboardTitlePosition.y + displayLeaderboardListOffsetYFromTitle);
                displayLeaderboardListNo = new ArrayList<>();
                displayLeaderboardListName = new ArrayList<>();
                displayLeaderboardListName = new ArrayList<>();
                displayLeaderboardListScore = new ArrayList<>();
                for (int i=0; i<displayLeaderboardListSupportedSize; i++) {
                    displayLeaderboardListNo.add( new TextDisplay(context, Integer.toString(i+1), new Point(displayLeaderboardListPosition.x - 10, displayLeaderboardListPosition.y + displayLeaderboardListOffsetYBetweenList*i)));
                    displayLeaderboardListName.add( new TextDisplay(context, "__________", new Point(displayLeaderboardListPosition.x + 350, displayLeaderboardListPosition.y + displayLeaderboardListOffsetYBetweenList*i)));
                    displayLeaderboardListName.get(i).setFontSize(50);
                    displayLeaderboardListScore.add( new TextDisplay(context, "_", new Point(displayLeaderboardListPosition.x + 750, displayLeaderboardListPosition.y + displayLeaderboardListOffsetYBetweenList*i)));
                    displayLeaderboardListScore.get(i).setFontSize(80);
                }
                for (int i=0; i<gameManager.gameScoreManager.gameScoreList.list.size(); i++) {
                    displayLeaderboardListName.get(i).setText(gameManager.gameScoreManager.gameScoreList.list.get(i).playerName);
                    displayLeaderboardListScore.get(i).setText(Integer.toString(gameManager.gameScoreManager.gameScoreList.list.get(i).playerScore));
                }
            }

            @Override
            public void onDraw(Canvas canvas) {
                canvas.drawRect(gameBackground, gameBackgroundColor);
                displayLeaderboardTitle.draw(canvas);
                displayMenuButton.draw(canvas);
                //Drawing leaderboard list
                for (int i=0; i<displayLeaderboardListSupportedSize; i++) {
                    displayLeaderboardListNo.get(i).draw(canvas);
                    displayLeaderboardListName.get(i).draw(canvas);
                    displayLeaderboardListScore.get(i).draw(canvas);
                }
                //Menu dialog should not be obscured by other elements drawn
                displayMenuDialogBox.draw(canvas);
            }

            @Override
            public void onTouchInteraction(Rect _userTouchPointer) {
                //Detecting click on menuButton
                if (displayMenuButton.isClicked(_userTouchPointer)) {
                    //Toggle Back To Menu Dialog Box
                    if (displayMenuDialogBox.popUp) {
                        displayMenuDialogBox.hide();
                    } else {
                        displayMenuDialogBox.show();
                    }
                }

                //Detecting click on menuDialogBox when it is pop up
                switch (displayMenuDialogBox.isInteracted(_userTouchPointer)) {
                    case NO: displayMenuDialogBox.hide();break;
                    case YES: gameManager.backToMainMenu(); break;
                    default: break;
                }
            }

            @Override
            public void onKeyboardInteraction(String _string) {

            }
        };
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (gameThread.isAlive()) {
            System.out.println("Thread is still running.");
        } else {
            System.out.println("Thread has stopped.");
            gameThread = new GameThread(getHolder(), this);
        }
        gameThread.setRunning(true);
        gameThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Not used
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        while (retry) {
            try {
                gameThread.setRunning(false);
                gameThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retry = false;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        super.onKeyDown(keyCode, event);
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event){
        super.onKeyUp(keyCode, event);
        gameInputControlManager.keyboardControl.retrieveKeyboardInput(keyCode, event);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e){
        //GameView Class Inform gameInputControlManager for touch event on the view
        //gameInputControlManager will process events and pass to GameManager to decide the actions to be done
        return gameInputControlManager.gameScreenPressedDetected(e);
    }

    @Override
    public void draw(Canvas canvas) {
        long previousMillis = System.currentTimeMillis();
        gameManager.run();
        super.draw(canvas);
        gameMenuActivity.draw(canvas);
        startGameActivity.draw(canvas);
        gameOverActivity.draw(canvas);
        leaderboardActivity.draw(canvas);
        systemUpsFpsDisplay.setText(new String("FPS= " + Long.toString(systemFPS) + " UPS= " + Long.toString(systemUPS)));
        //systemUpsFpsDisplay.draw(canvas);
    }

    public void onTouchPointInteraction(RealTimeInputControlsParameters realTimeInputControlsParameters) {
        //GameManager inform GameView for all touch related user input
        //GameView decide on the function that response to the touch surface on GameView
        if (realTimeInputControlsParameters.onReleased()) {
            gameMenuActivity.touchInteraction(realTimeInputControlsParameters.userTouchPointer);
            startGameActivity.touchInteraction(realTimeInputControlsParameters.userTouchPointer);
            gameOverActivity.touchInteraction(realTimeInputControlsParameters.userTouchPointer);
            leaderboardActivity.touchInteraction(realTimeInputControlsParameters.userTouchPointer);
        }
    }

    public void onKeyboardInteraction(String string) {
        gameMenuActivity.keyboardInteraction(string);
        startGameActivity.keyboardInteraction(string);
        gameOverActivity.keyboardInteraction(string);
        leaderboardActivity.keyboardInteraction(string);
    }
}

class DialogBox {
    public enum INTERACTION {YES, NO_INTERACTION, NO};
    private String dialogString;
    public Rect dialogBox;
    TextButton yesButton, noButton;
    private Point centerXY;
    private Point yesButtonCenterXY, noButtonCenterXY;
    private Paint dialogBoxPaint;
    private TextPaint textPaint;
    private Paint yesNoButtonPaint;
    private int TEXT_SIZE = 80;
    private int dialogBoxWidth = 800;
    private int dialogBoxHeight = 500;
    public boolean popUp = false;

    public DialogBox(Context context, Point _centerXY, String _dialogString) {
        //Center position of dialog box
        this.dialogString = _dialogString;
        this.centerXY = _centerXY;
        yesButtonCenterXY = new Point(centerXY.x - centerXY.x/3, centerXY.y + centerXY.y/10);
        noButtonCenterXY = new Point(centerXY.x + centerXY.x/3, centerXY.y + centerXY.y/10);

        dialogBox = new Rect(centerXY.x - dialogBoxWidth/2, centerXY.y - dialogBoxHeight/2, centerXY.x + dialogBoxWidth/2, centerXY.y + dialogBoxHeight/2);

        yesButton = new TextButton(context,"YES", new Point(yesButtonCenterXY.x, yesButtonCenterXY.y));
        noButton = new TextButton(context,"NO", new Point(noButtonCenterXY.x, noButtonCenterXY.y));

        dialogBoxPaint = new Paint();

        textPaint = new TextPaint();
        textPaint.setTextAlign(TextPaint.Align.CENTER);
        textPaint.setTextSize(this.TEXT_SIZE);
        textPaint.setColor(Color.parseColor("#EF8F3F"));
        textPaint.setTypeface(ResourcesCompat.getFont(context,R.font.kenney_blocks));

        yesNoButtonPaint = new Paint();
        yesNoButtonPaint.setColor(Color.parseColor("#EF8F3F"));
        yesNoButtonPaint.setAlpha(255);    //Set Transparent
    }

    public void draw(Canvas canvas) {
        if (popUp) {
            //Dialog box UI
            dialogBoxPaint.setColor(Color.parseColor("#FFFFFF"));
            canvas.drawRect(dialogBox, dialogBoxPaint);
            yesButton.draw(canvas);
            noButton.draw(canvas);

            canvas.drawText(this.dialogString, centerXY.x, centerXY.y - dialogBoxHeight / 4, textPaint);
        }
    }

    public void show() {
        popUp = true;
    }

    public void hide() {
        popUp = false;
    }

    public INTERACTION isInteracted(Rect userTouchPointer) {
        if (popUp) {
            if (yesButton.isClicked(userTouchPointer)) {
                return INTERACTION.YES;
            }
            if (noButton.isClicked(userTouchPointer)) {
                return  INTERACTION.NO;
            }
        }
        return INTERACTION.NO_INTERACTION;
    }
}

class TextInput extends TextDisplay {

    public TextInput(Context _context, String _text, Point _position) {
        super(_context, _text, _position);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
    }

}

class TextButton {
    private Context context;
    private Rect area;
    private int width = 50;
    private int height = 100;
    private int offsetButtonToMatchTextDisplay_height_height = 28;
    private int offsetButtonToMatchTextDisplay_height_width = 2;
    private String text;
    private Point position;
    private int TEXT_SIZE = 80;
    private TextPaint textPaint = new TextPaint();
    private boolean showButtonBox = false;
    private Paint buttonBoxPaint = new Paint(Color.parseColor("#00003F"));

    public TextButton(Context _context, String _text, Point _position) {
        this.context = _context;
        this.text = _text;
        this.position = _position;
        this.width = this.text.length() * this.width;
        this.area = new Rect(position.x - width/2 + offsetButtonToMatchTextDisplay_height_width ,position.y - height/2 - offsetButtonToMatchTextDisplay_height_height,position.x + width/2 - offsetButtonToMatchTextDisplay_height_width,position.y + height/2 - offsetButtonToMatchTextDisplay_height_height);
        textPaint.setTextAlign(TextPaint.Align.CENTER);
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setColor(Color.parseColor("#EF8F3F"));
        textPaint.setTypeface(ResourcesCompat.getFont(context,R.font.kenney_blocks));
    }

    public void setButtonBoxVisibility(boolean visible) {
        showButtonBox = visible;
    }

    public void setButtonArea(int sizeX, int sizeY) {
        width = sizeX;
        height = sizeY;
        area = new Rect(position.x - width/2 + offsetButtonToMatchTextDisplay_height_width ,position.y - height/2 - offsetButtonToMatchTextDisplay_height_height,position.x + width/2 - offsetButtonToMatchTextDisplay_height_width,position.y + height/2 - offsetButtonToMatchTextDisplay_height_height);
    }

    public boolean isClicked(Rect _userTouchPointer) {
        if (Rect.intersects(area, _userTouchPointer)) {
            return true;
        } else {
            return false;
        }
    }

    public void draw(Canvas canvas) {
        if (!showButtonBox) {
            buttonBoxPaint.setAlpha(0);
        } else {
            buttonBoxPaint.setAlpha(255);
        }
        canvas.drawRect(area, buttonBoxPaint);
        canvas.drawText(text ,position.x,position.y, textPaint);
    }
}

class TextDisplay {
    private Context context;
    private String text;
    private Point position;
    private int TEXT_SIZE = 80;
    private TextPaint textPaint = new TextPaint();
    //Animation
    private long blinkTiming = 1000; //Default blinking timing is 1 second
    private boolean visible = true; //Default visibility true
    private long previousBlinkTime = System.currentTimeMillis();

    public TextDisplay(Context _context, String _text, Point _position) {
        //This class is created defaulted to game style text
        this.context = _context;
        this.text = _text;
        this.position = _position;
        textPaint.setTextAlign(TextPaint.Align.CENTER);
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setColor(Color.parseColor("#EF8F3F"));
        textPaint.setTypeface(ResourcesCompat.getFont(context, R.font.kenney_blocks));
    }

    public void setDefaultTypeFace() {
        textPaint.setTypeface(Typeface.create("Arial",Typeface.NORMAL));
    }

    public void setColor(String  colorString) {
        textPaint.setColor(Color.parseColor(colorString));
    }

    public void draw(Canvas canvas) {
        if (visible) {
            Paint paint = new Paint(R.color.black);
            paint.setAlpha(0);
            canvas.drawText(text, position.x, position.y, textPaint);
        }
    }

    public void setFontSize(int fontSize) {
        textPaint.setTextSize(fontSize);
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setBlinkCapability(long _blinkTiming) {
        blinkTiming = _blinkTiming;
        if (_blinkTiming == 0) {
            //Make sure that this view is visible after the blinking stop
            visible = true;
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (blinkTiming != 0) {
                    if (System.currentTimeMillis() - previousBlinkTime > blinkTiming) {
                        visible = !visible;
                        previousBlinkTime = System.currentTimeMillis();
                    }
                }
            }
        });
        thread.start();
    }
}

abstract class GameActivityPage {
    /*
    Game activity page initialize
    if (isActive) run (onDraw) and (touchInteraction)
        Game activity page onDraw
        Game activity on touch interaction
    */

    public static GameActivityPage previousActiveActivity = null;

    protected boolean isActive = false;
    GameActivityPage() {

    }

    public void draw(Canvas canvas) {
        //Do not draw activity if the activity is not currently active
        if (isActive) {
            onDraw(canvas);
        }
    }

    public void touchInteraction(Rect _userTouchPointer) {
        //Do not detect touch interaction if the activity is not currently active
        if (isActive) {
            onTouchInteraction(_userTouchPointer);
        }
    }

    public void keyboardInteraction(String _string) {
        //Do not detect keyboard interaction if the activity is not currently active
        if (isActive) {
            onKeyboardInteraction(_string);
        }
    }

    abstract public void initialize();
    abstract public void onDraw(Canvas canvas);
    abstract public void onTouchInteraction(Rect _userTouchPointer);
    abstract public void onKeyboardInteraction(String _string);

    public static void startActivity(GameActivityPage activityPage) {
        if (previousActiveActivity != null) {
            stopActivity(previousActiveActivity);
        }
        activityPage.initialize();
        activityPage.isActive = true;
        previousActiveActivity = activityPage;
    }

    public static void stopActivity(GameActivityPage activityPage) {
        activityPage.isActive = false;
    }

}

