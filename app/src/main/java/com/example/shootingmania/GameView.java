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

public class GameView extends View {
    private GameManager gameManager;
    private InputControlsManager inputControlsManager;
    private Display display;
    final long UPDATE_MILLIS = 30;
    public static int dHeight;
    public static int dWidth;
    private String TAG = "GameView";
    private Rect gameBackground;
    private Paint gameBackgroundColor;
    private int displayScorePoints;
    private Point displayScorePointsPosition;
    private Paint displayScorePointsPositionTextPaint;
    private TextButton displayMenuButton;
    private Point displayMenuButtonPosition;
    private DialogBox displayMenuDialogBox;
    private Rect displayTargetMoveArea;
    private Paint displayTargetMoveAreaColor;
    private Context context;
    private Handler handler;
    private Runnable runnable;
    private Gun displayGun;
    private AimCross displayAimCross;
    private Target displayTarget;
    private int TEXT_SIZE = 80;

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
        gameBackground = new Rect(0, 0, dWidth, dHeight);
        gameBackgroundColor = new Paint();
        gameBackgroundColor.setColor(Color.parseColor("#DEEBF7"));

        gameManager = new GameManager(this);
        inputControlsManager = new InputControlsManager(context, display, gameManager);

        //Get all display object from gameManager.gameData
        displayScorePoints = gameManager.gameData.scorePoints;
        displayScorePointsPosition = new Point(50,150);
        displayScorePointsPositionTextPaint = new TextPaint();
        displayScorePointsPositionTextPaint.setTextAlign(TextPaint.Align.LEFT);   //For Text That Updates It Self CENTER Align may cause unwanted swift in display if Text become longer
        displayScorePointsPositionTextPaint.setTextSize(TEXT_SIZE);
        displayScorePointsPositionTextPaint.setColor(Color.parseColor("#EF8F3F"));
        displayScorePointsPositionTextPaint.setTypeface(ResourcesCompat.getFont(context,R.font.kenney_blocks));
        displayMenuButtonPosition = new Point(displayScorePointsPosition.x + 850,150);
        displayMenuButton = new TextButton(context, "MENU",  displayMenuButtonPosition);
        displayMenuDialogBox = new DialogBox(context, new Point(dWidth/2,dHeight/2), "BACK TO MENU ?");
        displayTargetMoveArea = gameManager.gameData.targetMoveArea;
        displayTargetMoveAreaColor = new Paint();
        displayTargetMoveAreaColor.setColor(Color.parseColor("#0AAAFF"));
        displayGun = gameManager.gameData.gun;
        displayAimCross = gameManager.gameData.aimCross;
        displayTarget = gameManager.gameData.target;

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

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);

        //Drawing sequence affecting the overlay sequence, be cautios during changing the sequence at these draw method
        canvas.drawRect(gameBackground, gameBackgroundColor);
        displayScorePoints = gameManager.gameData.scorePoints;
        canvas.drawText("SCORE: " + Integer.toString(displayScorePoints),displayScorePointsPosition.x,displayScorePointsPosition.y, displayScorePointsPositionTextPaint);
        displayMenuButton.draw(canvas);
        displayMenuDialogBox.draw(canvas);
        canvas.drawRect(displayTargetMoveArea, displayTargetMoveAreaColor);
        canvas.drawBitmap(displayTarget.animateFrame(displayTarget.frame), displayTarget.posX - displayTarget.getTargetWidth(displayTarget.animateFrame(displayTarget.frame))/2, displayTarget.posY - displayTarget.getTargetHeight(displayTarget.animateFrame(displayTarget.frame))/2, null);
        for (BulletMarks b : displayTarget.bulletMarks) {
            canvas.drawBitmap(b.animateFrame(), b.posX - b.getTargetWidth() / 2, b.posY - b.getTargetHeight() / 2, null);
        }
        canvas.drawBitmap(displayGun.animateFrame(displayGun.getCurrentFrame()),displayGun.posX - displayGun.getGunWidth((displayGun.animateFrame(displayGun.getCurrentFrame())))/4, displayGun.posY - displayGun.getGunHeight((displayGun.animateFrame(displayGun.getCurrentFrame())))/2,null);
        canvas.drawBitmap(displayAimCross.animateFrame(displayAimCross.getCurrentFrame()),displayAimCross.posX - displayAimCross.getAimCrossWidth((displayAimCross.animateFrame(0)))/2, displayAimCross.posY - displayAimCross.getAimCrossHeight((displayAimCross.animateFrame(0)))/2,null);

        handler.postDelayed(runnable, UPDATE_MILLIS);
    }

    public void backToMainMenu() {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    public void touchPointInteraction(Rect _userTouchPointer) {

        //GameManager inform GameView for all touch related user input
        //GameView decide on the function that response to the touch surface on GameView

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
            case YES: backToMainMenu(); break;
            default: break;
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
    private int width = 280;
    private int height = 100;
    private int offsetButtonToMatchTextDisplay = 30;
    private String text;
    private Point position;
    private int TEXT_SIZE = 80;
    private TextPaint textPaint = new TextPaint();

    public TextButton(Context _context, String _text, Point _position) {
        this.context = _context;
        this.text = _text;
        this.position = _position;
        this.area = new Rect(position.x - width/2 ,position.y - height/2 - offsetButtonToMatchTextDisplay,position.x + width/2,position.y + height/2 - offsetButtonToMatchTextDisplay);
        textPaint.setTextAlign(TextPaint.Align.CENTER);
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setColor(Color.parseColor("#EF8F3F"));
        textPaint.setTypeface(ResourcesCompat.getFont(context,R.font.kenney_blocks));
    }

    public boolean isClicked(Rect _userTouchPointer) {
        if (Rect.intersects(area, _userTouchPointer)) {
            return true;
        } else {
            return false;
        }
    }

    public void draw(Canvas canvas) {
        Paint paint = new Paint(R.color.black);
        paint.setAlpha(0);
        canvas.drawText(text ,position.x,position.y, textPaint);
        canvas.drawRect(area, paint);
    }
}