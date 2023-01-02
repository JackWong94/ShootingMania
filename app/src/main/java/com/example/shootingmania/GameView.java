package com.example.shootingmania;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.text.TextPaint;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayDeque;
import java.util.ArrayList;

public class GameView extends View {
    private GameManager gameManager;
    private InputControlsManager inputControlsManager;
    private Display display;
    final long UPDATE_MILLIS = 60;
    public static int dHeight;
    public static int dWidth;
    private String TAG = "GameView";
    public GameActivityPage gameMenuActivity;
    public GameActivityPage startGameActivity;
    public GameActivityPage gameOverActivity;
    private Context context;
    private Handler handler;
    private Runnable runnable;


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

        gameManager = new GameManager(this);
        inputControlsManager = new InputControlsManager(context, display, gameManager, this);

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
                }
                if (displayExitGameButton.isClicked(_userTouchPointer)) {
                    //Exit Game
                    gameManager.exitGame();
                }
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
            private int TEXT_SIZE = 80;
            @Override
            public void initialize() {
                //Game activity page initialize
                gameBackground = new Rect(0, 0, dWidth, dHeight);
                gameBackgroundColor = new Paint();
                gameBackgroundColor.setColor(Color.parseColor("#DEEBF7"));

                //Get all display object from gameManager.gameData
                displayScorePoints = gameManager.gameData.scorePoints;
                displayScorePointsPosition = new Point(50,150);
                displayScorePointsTextPaint = new TextPaint();
                displayScorePointsTextPaint.setTextAlign(TextPaint.Align.LEFT);   //For Text That Updates It Self CENTER Align may cause unwanted swift in display if Text become longer
                displayScorePointsTextPaint.setTextSize(TEXT_SIZE);
                displayScorePointsTextPaint.setColor(Color.parseColor("#EF8F3F"));
                displayScorePointsTextPaint.setTypeface(ResourcesCompat.getFont(context,R.font.kenney_blocks));
                displayGameLeftTime = (int) gameManager.gameData.gameTimer.getTimeLeft();
                displayGameLeftTimePosition = new Point(dWidth/2 + displayGameLeftTimeTextPaintSize/4,380);
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
                canvas.drawBitmap(displayTarget.animateFrame(displayTarget.frame), displayTarget.posX - displayTarget.getTargetWidth(displayTarget.animateFrame(displayTarget.frame))/2, displayTarget.posY - displayTarget.getTargetHeight(displayTarget.animateFrame(displayTarget.frame))/2, null);
                for (BulletMarks b : displayTarget.bulletMarks) {
                    canvas.drawBitmap(b.animateFrame(), b.posX - b.getTargetWidth() / 2, b.posY - b.getTargetHeight() / 2, null);
                }
                canvas.drawBitmap(displayGun.animateFrame(displayGun.getCurrentFrame()),displayGun.posX - displayGun.getGunWidth((displayGun.animateFrame(displayGun.getCurrentFrame())))/4, displayGun.posY - displayGun.getGunHeight((displayGun.animateFrame(displayGun.getCurrentFrame())))/2,null);
                canvas.drawBitmap(displayAimCross.animateFrame(displayAimCross.getCurrentFrame()),displayAimCross.posX - displayAimCross.getAimCrossWidth((displayAimCross.animateFrame(0)))/2, displayAimCross.posY - displayAimCross.getAimCrossHeight((displayAimCross.animateFrame(0)))/2,null);
                for (int i=0; i<displayGun.getRemainingBulletsCount(); i++) {
                    canvas.drawBitmap(displayGun.animateFrameForBulletRemaining(),120 * i, dHeight - 220,null);
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
        };

        gameOverActivity = new GameActivityPage() {
            private long activityUpTime;
            private long minimumGameOverShowingTime = 1500;         //2 seconds
            private boolean allowToSwitchActivity = false;
            private TextDisplay gameOverTextDisplay;
            private TextDisplay yourScoreTextDisplay;
            private TextDisplay scoreTextDisplay;
            private TextDisplay pressToContinueTextDisplay;
            private Point gameOverTextDisplayPosition = new Point(dWidth/2, dHeight/8*2);
            private Point yourScoreTextDisplayPosition = new Point(dWidth/2, dHeight/8*4);
            private Point scoreTextDisplayPosition = new Point(dWidth/2, dHeight/8*5);
            private Point pressToContinueTextDisplayPosition = new Point(dWidth/2, dHeight/8*6);
            @Override
            public void initialize() {
                allowToSwitchActivity = false;
                gameOverTextDisplay = new TextDisplay(context, "GAME OVER", gameOverTextDisplayPosition);
                yourScoreTextDisplay = new TextDisplay(context, "YOUR SCORES : ", yourScoreTextDisplayPosition);
                scoreTextDisplay = new TextDisplay(context, Integer.toString(gameManager.gameData.scorePoints),scoreTextDisplayPosition);
                pressToContinueTextDisplay = new TextDisplay(context, "Press To Continue !",pressToContinueTextDisplayPosition);
                pressToContinueTextDisplay.setFontSize(50);
                pressToContinueTextDisplay.setBlinkCapability(500);
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
                gameOverTextDisplay.draw(canvas);
                yourScoreTextDisplay.draw(canvas);
                scoreTextDisplay.draw(canvas);

                if (allowToSwitchActivity) {
                    pressToContinueTextDisplay.draw(canvas);

                }
            }

            @Override
            public void onTouchInteraction(Rect _userTouchPointer) {
                //This activity must stay for the minimumGameOverShowingTime, so that user will not miss the score display
                if (allowToSwitchActivity) {
                    //When touch screen, trigger back to main activity
                    gameManager.backToMainMenu();
                }
            }
        };

        this.runnable = new Runnable() {
            @Override
            public void run() {
                invalidate();
                gameManager.run();
            }
        };

    }

    @Override
    public boolean onTouchEvent(MotionEvent e){
        //GameView Class Inform InputControlsManager for touch event on the view
        //InputControlsManager will process events and pass to GameManager to decide the actions to be done
        return inputControlsManager.gameScreenPressedDetected(e);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        gameMenuActivity.draw(canvas);
        startGameActivity.draw(canvas);
        gameOverActivity.draw(canvas);
        handler.postDelayed(runnable, UPDATE_MILLIS);
    }

    public void onTouchPointInteraction(RealTimeInputControlsParameters realTimeInputControlsParameters) {
        //GameManager inform GameView for all touch related user input
        //GameView decide on the function that response to the touch surface on GameView
        if (realTimeInputControlsParameters.onReleased()) {
            gameMenuActivity.touchInteraction(realTimeInputControlsParameters.userTouchPointer);
            startGameActivity.touchInteraction(realTimeInputControlsParameters.userTouchPointer);
            gameOverActivity.touchInteraction(realTimeInputControlsParameters.userTouchPointer);
        }
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

class TextButton {
    private Context context;
    private Rect area;
    private int width = 65;
    private int height = 100;
    private int offsetButtonToMatchTextDisplay = 29;
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
        this.area = new Rect(position.x - width/2 ,position.y - height/2 - offsetButtonToMatchTextDisplay,position.x + width/2,position.y + height/2 - offsetButtonToMatchTextDisplay);
        textPaint.setTextAlign(TextPaint.Align.CENTER);
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setColor(Color.parseColor("#EF8F3F"));
        textPaint.setTypeface(ResourcesCompat.getFont(context,R.font.kenney_blocks));
    }

    public void setButtonBoxVisibility(boolean visible) {
        showButtonBox = visible;
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
        this.context = _context;
        this.text = _text;
        this.position = _position;
        textPaint.setTextAlign(TextPaint.Align.CENTER);
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setColor(Color.parseColor("#EF8F3F"));
        textPaint.setTypeface(ResourcesCompat.getFont(context,R.font.kenney_blocks));
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

    public void setBlinkCapability(long _blinkTiming) {
        blinkTiming = _blinkTiming;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
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

    abstract public void initialize();
    abstract public void onDraw(Canvas canvas);
    abstract public void onTouchInteraction(Rect _userTouchPointer);

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