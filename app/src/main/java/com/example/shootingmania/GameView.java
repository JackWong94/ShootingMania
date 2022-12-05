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

import java.util.ArrayList;

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
    private Rect targetMoveArea;
    private Paint targetMoveAreaColor;
    private Context context;
    private Handler handler;
    private Runnable runnable;
    private Gun gun;
    private AimCross aimCross;
    private ArrayList<Target> targets;
    private int numberOfTarget = 1;

    private int shootTriggeredFrame = 0;
    private int scorePoints = 0;
    private TextPaint textPaint;

    TextButton menuButton;
    Point menuButtonPosition;
    Point scoreDisplayPosition;

    private Rect userTouchPointer = new Rect(0,0,0,0);

    private boolean debugForButtonTouchArea = false;
    private boolean gotoMenu = false;
    private DialogBox menuDialogBox;

    public GameView(Context context) {
        super(context);
        this.context = context;
        this.handler = new Handler();
        display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
        gameManager = new GameManager(this);
        inputControlsManager = new InputControlsManager(context, display, gameManager);

        Point displaySize = new Point();
        display.getRealSize(displaySize);
        dWidth = displaySize.x;
        dHeight = displaySize.y;
        gameBackground = new Rect(0, 0, dWidth, dHeight);
        gameBackgroundColor = new Paint();
        gameBackgroundColor.setColor(Color.parseColor("#DEEBF7"));
        targetMoveArea = new Rect(0, 500, dWidth, 900);
        targetMoveAreaColor = new Paint();
        targetMoveAreaColor.setColor(Color.parseColor("#0AAAFF"));

        scoreDisplayPosition = new Point(280,150);
        menuButtonPosition = new Point(scoreDisplayPosition.x + 600,150);
        menuButton = new TextButton(context, "MENU",  menuButtonPosition);

        int TEXT_SIZE = 80;
        textPaint = new TextPaint();
        textPaint.setTextAlign(TextPaint.Align.CENTER);
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setColor(Color.parseColor("#EF8F3F"));
        textPaint.setTypeface(ResourcesCompat.getFont(context,R.font.kenney_blocks));

        menuDialogBox = new DialogBox(context, new Point(dWidth/2,dHeight/2), "BACK TO MENU ?");

        this.runnable = new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        };

        targets = new ArrayList<>();
        for (int i=0; i<numberOfTarget; i++) {
            Target target = new Target(context);
            target.setMovingArea(targetMoveArea);
            targets.add(target);
        }
        aimCross = new AimCross(context);
        aimCross.resetAimCrossPosition(targetMoveArea.centerX(), targetMoveArea.centerY());
        gun = new Gun(context);
        gun.resetGunPosition(dWidth/2, dHeight*4/5);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e){
        //GameView Class Inform InputControlsManager for touch event on the view
        //InputControlsManager will process events and pass to GameManager to decide the actions to be done
        return inputControlsManager.gameScreenPressedDetected(e);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //Update game data before any drawing of game element sprite
        gameManager.run();
        updateGameData();
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        super.onDraw(canvas);
        canvas.drawRect(gameBackground, gameBackgroundColor);
        canvas.drawRect(targetMoveArea, targetMoveAreaColor);
        for (Target t : targets) {
            canvas.drawBitmap(t.animateFrame(t.frame), t.posX - t.getTargetWidth(t.animateFrame(t.frame))/2, t.posY - t.getTargetHeight(t.animateFrame(t.frame))/2, null);
            //canvas.drawCircle(t.posX , t.posY , 10, paint);
            if (t.bulletMarks.size() > 0) {
                for (BulletMarks b : t.bulletMarks) {
                    canvas.drawBitmap(b.animateFrame(), b.posX - b.getTargetWidth() / 2, b.posY - b.getTargetHeight() / 2, null);
                }
            }
        }

        if (shootTriggeredFrame > 0) {
            canvas.drawBitmap(aimCross.animateFrame(1),aimCross.posX - aimCross.getAimCrossWidth((aimCross.animateFrame(0)))/2, aimCross.posY - aimCross.getAimCrossHeight((aimCross.animateFrame(0)))/2,null);
            shootTriggeredFrame--;
        } else {
            canvas.drawBitmap(aimCross.animateFrame(0),aimCross.posX - aimCross.getAimCrossWidth((aimCross.animateFrame(0)))/2, aimCross.posY - aimCross.getAimCrossHeight((aimCross.animateFrame(0)))/2,null);
            //canvas.drawCircle(aimCross.posX, aimCross.posY, 10, paint);
        }

        canvas.drawBitmap(gun.animateFrame(gun.getCurrentFrame()),gun.posX - gun.getGunWidth((gun.animateFrame(gun.getCurrentFrame())))/4, gun.posY - gun.getGunHeight((gun.animateFrame(gun.getCurrentFrame())))/2,null);
        //canvas.drawCircle(gun.posX, gun.posY, 10, paint);

        //UI
        textPaint.setTextAlign(TextPaint.Align.CENTER);
        canvas.drawText("SCORE: " + Integer.toString(scorePoints),scoreDisplayPosition.x,scoreDisplayPosition.y, textPaint);

        menuButton.draw(canvas);

        //Menu Design
        if(gotoMenu) {
            menuDialogBox.draw(canvas);
        }
        if (debugForButtonTouchArea) {
            canvas.drawRect(userTouchPointer, new Paint(R.color.black));
        }



        handler.postDelayed(runnable, UPDATE_MILLIS);
    }

    public void updateGameData() {
        int tempScore = 0;
        for (Target t : targets) {
            tempScore += t.returnTotalScore();
        }
        scorePoints = tempScore;
    }

    public void userInputBySensorControlX(Float x_dir_rotation) {
        //Effective range = -9 ~ 9 +-sensor sensitivity
        Float centerY = 0f;
        x_dir_rotation -= centerY;
        if (x_dir_rotation > AccelerometerSensor.sensorSensitivityX) {
            x_dir_rotation = AccelerometerSensor.sensorSensitivityX;
        }
        if (x_dir_rotation < -AccelerometerSensor.sensorSensitivityX) {
            x_dir_rotation = -AccelerometerSensor.sensorSensitivityX;
        }
        aimCross.posX -= AccelerometerSensor.sensorSensitivityX*AccelerometerSensor.sensorAccelerationX*x_dir_rotation;
        gun.posX -= AccelerometerSensor.sensorSensitivityX*AccelerometerSensor.sensorAccelerationX*x_dir_rotation;
        if (aimCross.posX < targetMoveArea.left + aimCross.getAimCrossWidth((aimCross.animateFrame(0)))/2) {
            aimCross.posX = targetMoveArea.left + aimCross.getAimCrossWidth((aimCross.animateFrame(0)))/2;
            gun.posX = aimCross.posX;
        }
        if (aimCross.posX > targetMoveArea.right - aimCross.getAimCrossWidth((aimCross.animateFrame(0)))/2) {
            aimCross.posX = targetMoveArea.right - aimCross.getAimCrossWidth((aimCross.animateFrame(0)))/2;
            gun.posX = aimCross.posX;
        }
    }

    public void userInputBySensorControlY(Float y_dir_rotation) {
        //Effective range = -9 ~ 9 +-sensor sensitivity
        Float centerY = 6f;
        y_dir_rotation -= centerY;
        if (y_dir_rotation > AccelerometerSensor.sensorSensitivityY) {
            y_dir_rotation = AccelerometerSensor.sensorSensitivityY;
        }
        if (y_dir_rotation < -AccelerometerSensor.sensorSensitivityY) {
            y_dir_rotation = -AccelerometerSensor.sensorSensitivityY;
        }
        aimCross.posY += AccelerometerSensor.sensorSensitivityY*AccelerometerSensor.sensorAccelerationY*y_dir_rotation;
        if (aimCross.posY < targetMoveArea.top){
            aimCross.posY = targetMoveArea.top;
        }
        if (aimCross.posY > targetMoveArea.bottom){
            aimCross.posY = targetMoveArea.bottom;
        }
    }

    public void backToMainMenu() {
        gotoMenu = !gotoMenu;
        if (gotoMenu) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(gotoMenu) {
                        if (menuDialogBox.yesButton.clicked(getUserTouchPointer())) {
                            Intent intent = new Intent(context, MainActivity.class);
                            context.startActivity(intent);
                            gotoMenu = false;
                        }
                        if (menuDialogBox.noButton.clicked(getUserTouchPointer())) {
                            gotoMenu = false;
                        }

                    }
                }
            });
            thread.start();
        }

    }

    public Rect getUserTouchPointer() {
        return userTouchPointer;
    }

    public void touchPointInteraction(Rect _userTouchPointer) {
        //GameManager inform GameView for all touch related user input
        //GameView decide on the function that response to the touch surface on GameView
        userTouchPointer = _userTouchPointer;

        if (menuButton.clicked(_userTouchPointer)) {
            backToMainMenu();
        }

        for (Target t : targets) {
            t.verifyShoot(gun.shoot(aimCross), t.animateFrame(t.frame));
            shootTriggeredFrame = 3;
        }
    }
}

class DialogBox {
    private boolean DEBUG_FOR_TOUCH_AREA = false;
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
        //Dialog box UI
        dialogBoxPaint.setColor(Color.parseColor("#FFFFFF"));
        canvas.drawRect(dialogBox, dialogBoxPaint);
        yesButton.draw(canvas);
        noButton.draw(canvas);

        canvas.drawText(this.dialogString, centerXY.x, centerXY.y - dialogBoxHeight/4, textPaint);
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

    public boolean clicked(Rect _userTouchPointer) {
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