package com.example.shootingmania;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Handler;
import android.text.TextPaint;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;

public class GameView extends View {
    final long UPDATE_MILLIS = 30;
    public static int dHeight;
    public static int dWidth;
    private final Point dialogBoxYesCenter;
    private final Point dialogBoxNoCenter;
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
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float sensorSensitivityX = 3;
    private float sensorSensitivityY = 3;
    private int sensorAccelerationX = 10;    //Increase smoothness
    private int sensorAccelerationY = 10;    //Increase smoothness
    private int shootTriggeredFrame = 0;
    private int scorePoints = 0;
    private TextPaint textPaint;
    private Rect menuButton;
    private Paint menuButtonPaint;
    Point menuButtonPosition = new Point(790,150);
    Rect userTouchPointer = new Rect(0,0,0,0);
    private boolean debugForButtonTouchArea = false;
    private Paint dialogBoxPaint = new Paint();
    private Point dialogBoxCenter;
    private int dialogBoxWidth = 800;
    private int dialogBoxHeight = 500;
    private int dialogBoxYesNoWidth = 230;
    private int dialogBoxYesNoHeight = 150;
    private Rect dialogBox;
    private boolean gotoMenu = false;
    private Rect noButton, yesButton;

    public GameView(Context context) {
        super(context);
        this.context = context;
        this.handler = new Handler();
        Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
        SensorControlListener sensorControlListener = new SensorControlListener(display) {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
                    return;
                }
                switch (display.getRotation()) {
                    case Surface.ROTATION_0:
                        userInputBySensorControlX(event.values[0]);
                        userInputBySensorControlY(event.values[1]);
                        break;
                    case Surface.ROTATION_90:
                        break;
                    case Surface.ROTATION_180:
                        break;
                    case Surface.ROTATION_270:
                        break;
                }
            }
        };
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(sensorControlListener, accelerometer, SensorManager.SENSOR_DELAY_UI);

        Point size = new Point();
        display.getRealSize(size);
        dWidth = size.x;
        dHeight = size.y;
        gameBackground = new Rect(0, 0, dWidth, dHeight);
        gameBackgroundColor = new Paint();
        gameBackgroundColor.setColor(Color.parseColor("#DEEBF7"));
        targetMoveArea = new Rect(0, 500, dWidth, 900);
        targetMoveAreaColor = new Paint();
        targetMoveAreaColor.setColor(Color.parseColor("#0AAAFF"));
        int TEXT_SIZE = 80;
        textPaint = new TextPaint();
        textPaint.setTextAlign(TextPaint.Align.LEFT);
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setColor(Color.parseColor("#EF8F3F"));
        textPaint.setTypeface(ResourcesCompat.getFont(context,R.font.kenney_blocks));

        menuButton = new Rect(menuButtonPosition.x,menuButtonPosition.y - 80,menuButtonPosition.x + 280,menuButtonPosition.y + 20);
        menuButtonPaint = new Paint();
        menuButtonPaint.setColor(Color.parseColor("#EF8F3F"));
        menuButtonPaint.setAlpha(255);    //Set Transparent

        dialogBoxPaint.setColor(Color.parseColor("#FFFFFF"));
        dialogBoxCenter = new Point(dWidth/2,dHeight/2);
        dialogBoxYesCenter = new Point(dialogBoxCenter.x - dialogBoxCenter.x/3, dialogBoxCenter.y + dialogBoxCenter.y/10);
        dialogBoxNoCenter = new Point(dialogBoxCenter.x + dialogBoxCenter.x/3, dialogBoxCenter.y + dialogBoxCenter.y/10);
        dialogBox = new Rect(dialogBoxCenter.x - dialogBoxWidth/2, dialogBoxCenter.y - dialogBoxHeight/2, dialogBoxCenter.x + dialogBoxWidth/2, dialogBoxCenter.y + dialogBoxHeight/2);
        yesButton = new Rect(dialogBoxYesCenter.x - dialogBoxYesNoWidth/2 , dialogBoxYesCenter.y - dialogBoxYesNoHeight/2,dialogBoxYesCenter.x + dialogBoxYesNoWidth/2 , dialogBoxYesCenter.y + dialogBoxYesNoHeight/2);
        noButton = new Rect(dialogBoxNoCenter.x - dialogBoxYesNoWidth/2 , dialogBoxNoCenter.y - dialogBoxYesNoHeight/2,dialogBoxNoCenter.x + dialogBoxYesNoWidth/2 , dialogBoxNoCenter.y + dialogBoxYesNoHeight/2);

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
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            userTouchPointer = new Rect((int) e.getX()-10, (int) e.getY()-10,(int) e.getX()+10, (int) e.getY()+10);
            Log.i("JACK", Integer.toString(userTouchPointer.centerX()));
            if (Rect.intersects(menuButton,userTouchPointer))
            {
                Log.i("JACK", "BACK TO MENU");
                //Return to main menu
                backToMainMenu(userTouchPointer);
                return true;
            }
            for (Target t : targets) {
                t.verifyShoot(gun.shoot(aimCross), t.animateFrame(t.frame));
                shootTriggeredFrame = 3;
            }
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
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
        canvas.drawText("SCORE: " + Integer.toString(scorePoints),50,150, textPaint);
        canvas.drawText("MENU",menuButtonPosition.x,menuButtonPosition.y, textPaint);
        if (debugForButtonTouchArea) {
            canvas.drawRect(menuButton,menuButtonPaint);
        }
        //Menu Design
        if(gotoMenu) {
            canvas.drawRect(dialogBox, dialogBoxPaint);
            textPaint.setTextAlign(TextPaint.Align.CENTER);

            canvas.drawText("BACK TO MENU ?", dialogBoxCenter.x, dialogBoxCenter.y - dialogBoxHeight/4, textPaint);
            canvas.drawText("YES", dialogBoxCenter.x - dialogBoxCenter.x/3, dialogBoxCenter.y + dialogBoxHeight/4, textPaint);
            canvas.drawText("NO", dialogBoxCenter.x + dialogBoxCenter.x/3 , dialogBoxCenter.y + dialogBoxHeight/4, textPaint);
            if (debugForButtonTouchArea) {
                canvas.drawRect(yesButton, menuButtonPaint);
                canvas.drawRect(noButton, menuButtonPaint);
            }
            textPaint.setTextAlign(TextPaint.Align.LEFT);
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
        if (x_dir_rotation > sensorSensitivityX) {
            x_dir_rotation = sensorSensitivityX;
        }
        if (x_dir_rotation < -sensorSensitivityX) {
            x_dir_rotation = -sensorSensitivityX;
        }
        aimCross.posX -= sensorSensitivityX*sensorAccelerationX*x_dir_rotation;
        gun.posX -= sensorSensitivityX*sensorAccelerationX*x_dir_rotation;
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
        if (y_dir_rotation > sensorSensitivityY) {
            y_dir_rotation = sensorSensitivityY;
        }
        if (y_dir_rotation < -sensorSensitivityY) {
            y_dir_rotation = -sensorSensitivityY;
        }
        aimCross.posY += sensorSensitivityY*sensorAccelerationY*y_dir_rotation;
        if (aimCross.posY < targetMoveArea.top){
            aimCross.posY = targetMoveArea.top;
        }
        if (aimCross.posY > targetMoveArea.bottom){
            aimCross.posY = targetMoveArea.bottom;
        }
    }

    public void backToMainMenu(Rect userTouchPointer) {
        gotoMenu = !gotoMenu;
        if (gotoMenu) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(gotoMenu) {
                        if (Rect.intersects(yesButton,getUserTouchPointer())) {
                            Intent intent = new Intent(context, MainActivity.class);
                            context.startActivity(intent);
                            gotoMenu = false;
                        }
                        if (Rect.intersects(noButton,getUserTouchPointer())) {
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
}
