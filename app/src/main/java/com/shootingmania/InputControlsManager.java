package com.shootingmania;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

public class InputControlsManager {
    RealTimeInputControlsParameters realTimeInputControlsParameters;
    public KeyboardControl keyboardControl;
    GameManager gameManager;

    public InputControlsManager(Context context, Display display, GameManager gameManager, View view) {
        this.gameManager = gameManager;
        realTimeInputControlsParameters = new RealTimeInputControlsParameters();
        //Touch screen user control (Receive touch event from GameView class)
        realTimeInputControlsParameters.userTouchPointer = new Rect(0,0,0,0);

        //Swipe sensor
        view.setOnTouchListener(new SwipeSensorListener(context, this));

        //Accelerometer user control
        realTimeInputControlsParameters.accelerometerSensorValue = new FloatPoint(0,0);
        AccelerometerSensor accelerometerSensor = new AccelerometerSensor();
        accelerometerSensor.registerListener(display, context, this);

        //Keyboard input
        keyboardControl = new KeyboardControl(view, context, this);
    }

    //Touch screen user control related
    public boolean gameScreenPressedDetected(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_UP) {
            realTimeInputControlsParameters.userTouchPointer = new Rect((int) e.getX()-10, (int) e.getY()-10,(int) e.getX()+10, (int) e.getY()+10);
            realTimeInputControlsParameters.userTouchType = RealTimeInputControlsParameters.TOUCH_TYPE.KEY_UP;
            gameManager.updateTouchControls(realTimeInputControlsParameters);
        }
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            realTimeInputControlsParameters.userTouchPointer = new Rect((int) e.getX()-10, (int) e.getY()-10,(int) e.getX()+10, (int) e.getY()+10);
            realTimeInputControlsParameters.userTouchType = RealTimeInputControlsParameters.TOUCH_TYPE.KEY_DOWN;
            gameManager.updateTouchControls(realTimeInputControlsParameters);
        }
        return true;
    }

    //Keyboard Input Related
    public void keyboardInputDetected(char key) {
        gameManager.updateKeyboardInput(key);
    }

    public void keyboardInputStringDetected(String string) {
        gameManager.updateKeyboardInputString(string);
    }

    public void swipeMotionDetected(RealTimeInputControlsParameters.SWIPE_DIR swipeDir) {
        realTimeInputControlsParameters.swipeDirection = swipeDir;
        gameManager.updateSwipeMotionControls(realTimeInputControlsParameters);
    }

    //Accelerometer user control related
    public void accelerometerValueChange(FloatPoint floatPoint) {
        realTimeInputControlsParameters.accelerometerSensorValue = floatPoint;
        gameManager.updateAccelerometerControls(realTimeInputControlsParameters);
    }

}

class RealTimeInputControlsParameters {

    public RealTimeInputControlsParameters() {

    }
    //Touch screen user control related
    public enum TOUCH_TYPE {
        KEY_DOWN,
        KEY_UP,
    }
    //Swipe screen user control related
    public enum SWIPE_DIR {
        NONE,
        SWIPE_UP,
        SWIPE_DOWN,
        SWIPE_LEFT,
        SWIPE_RIGHT,
    }
    public Rect userTouchPointer;
    public TOUCH_TYPE userTouchType;

    public Boolean onPressed() {
        if (userTouchType == TOUCH_TYPE.KEY_DOWN) {
            return true;
        } else {
            return false;
        }
    }
    public Boolean onReleased() {
        if (userTouchType == TOUCH_TYPE.KEY_UP) {
            return true;
        } else {
            return false;
        }
    }

    //Swipe Sensor user control related
    public SWIPE_DIR swipeDirection = SWIPE_DIR.NONE;

    //Accelerometer user control related
    public FloatPoint accelerometerSensorValue;

}

class KeyboardControl extends Activity {
    private static final String TAG = "KeyboardControl";
    private static String stringBuffer = "";
    private View view;
    private Context context;
    private boolean keyboardShowing;
    private InputControlsManager inputControlsManager;
    public KeyboardControl(View _view, Context _context, InputControlsManager _inputControlsManager) {
        view = _view;
        context = _context;
        keyboardShowing = false;
        inputControlsManager = _inputControlsManager;
    }

    public void showKeyboard() {
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        InputMethodManager mgr = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        keyboardShowing = true;
        stringBuffer = "";

    }

    public void hideKeyboard() {
        if (keyboardShowing) {
            // Retrieving the token if the view is hosted by the fragment.
            IBinder windowToken = view.getWindowToken();

            // Retrieving the token if the view is hosted by the activity.
            if (windowToken == null) {
                if (view.getContext() instanceof Activity) {
                    final Activity activity = (Activity) view.getContext();
                    if (activity.getWindow() != null && activity.getWindow().getDecorView() != null) {
                        windowToken = activity.getWindow().getDecorView().getWindowToken();
                    }
                }
            }

            // Hide if shown before.
            InputMethodManager inputMethodManager = (InputMethodManager) view
                    .getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(windowToken, 0);
            view.clearFocus();
        }
    }

    public char retrieveKeyboardInput(int keyCode, KeyEvent event) {
        char key = (char) event.getUnicodeChar();
        Log.i(TAG, Character.toString(key) + " " + keyCode);
        //Supporting range of keyboard key input
        inputControlsManager.keyboardInputDetected(key);
        //Processing key to string
        //Handling special character
        if (keyCode == 67) {
            //Backspace key
            if (stringBuffer.length() > 0) {
                stringBuffer = stringBuffer.substring(0, stringBuffer.length() - 1);
            } else {
                stringBuffer = "";
            }
            Log.i(TAG, stringBuffer);
        } else if (keyCode == 66) {
            //Enter key
            hideKeyboard();
        }else {
            stringBuffer = stringBuffer.concat(Character.toString(key));
        }

        inputControlsManager.keyboardInputStringDetected(stringBuffer);
        return key;
    }
}

class SwipeSensorListener implements View.OnTouchListener {
    private String TAG = "SwipeSensorListener";
    private final GestureDetector gestureDetector;
    private InputControlsManager inputControlsManager;

    public SwipeSensorListener (Context ctx, InputControlsManager inputControlsManager){
        gestureDetector = new GestureDetector(ctx, new GestureListener());
        this.inputControlsManager = inputControlsManager;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 50;
        private static final int SWIPE_VELOCITY_THRESHOLD = 0;

        /*@Override
        Future usage if we need to class a mouse input
        public boolean onDown(MotionEvent e) {
            return false;
        }*/

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                        result = true;
                    }
                }
                else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom();
                    } else {
                        onSwipeTop();
                    }
                    result = true;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

    public void onSwipeRight() {
        inputControlsManager.swipeMotionDetected(RealTimeInputControlsParameters.SWIPE_DIR.SWIPE_RIGHT);
        Log.i(TAG, "SWIPE RIGHT");
    }

    public void onSwipeLeft() {
        inputControlsManager.swipeMotionDetected(RealTimeInputControlsParameters.SWIPE_DIR.SWIPE_LEFT);
        Log.i(TAG, "SWIPE LEFT");
    }

    public void onSwipeTop() {
        inputControlsManager.swipeMotionDetected(RealTimeInputControlsParameters.SWIPE_DIR.SWIPE_UP);
        Log.i(TAG, "SWIPE TOP");
    }

    public void onSwipeBottom() {
        inputControlsManager.swipeMotionDetected(RealTimeInputControlsParameters.SWIPE_DIR.SWIPE_DOWN);
        Log.i(TAG, "SWIPE BOTTOM");
    }
}

class AccelerometerSensor {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static float sensorSensitivityX = 5;
    private static float sensorSensitivityY = 3;
    private static int sensorAccelerationX = 500;    //Increase smoothness
    private static int sensorAccelerationY = 400;    //Increase smoothness
    private FloatPoint xyAxisAcceleration;
    FloatPoint tunedData;
    //Effective X sensing range = -9 ~ 9 +-sensor sensitivity
    Float centerX = 0f;
    //Effective Y sensing range = -9 ~ 9 +-sensor sensitivity
    Float centerY = 6f;


    public AccelerometerSensor() {
        xyAxisAcceleration = new FloatPoint(0,0);
    }

    public void registerListener(Display display, Context context, InputControlsManager inputControlsManager) {
        SensorControlListener sensorControlListener = new SensorControlListener(display) {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
                    return;
                }
                switch (display.getRotation()) {
                    case Surface.ROTATION_0:
                        xyAxisAcceleration.set(event.values[0], event.values[1]);
                        xyAxisAcceleration = fineTuning(xyAxisAcceleration);
                        inputControlsManager.accelerometerValueChange(xyAxisAcceleration);
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
    }

    private FloatPoint fineTuning(FloatPoint rawData){
        //Set the center of the sensor
        rawData.x -= centerX;
        rawData.y -= centerY;

        //Tune accelerometer sensitivity and acceleration base on preset value
        //Sensitivity of sensor controls the range of angle rotation from center X axis
        if (rawData.x > AccelerometerSensor.sensorSensitivityX) {
            rawData.x = AccelerometerSensor.sensorSensitivityX;
        }
        if (rawData.x < -AccelerometerSensor.sensorSensitivityX) {
            rawData.x = -AccelerometerSensor.sensorSensitivityX;
        }

        if (rawData.y > AccelerometerSensor.sensorSensitivityY) {
            rawData.y = AccelerometerSensor.sensorSensitivityY;
        }
        if (rawData.y < -AccelerometerSensor.sensorSensitivityY) {
            rawData.y = -AccelerometerSensor.sensorSensitivityY;
        }

        //Acceleration to amplify the direction changes speeds proportional to rotation angle
        rawData.x = AccelerometerSensor.sensorSensitivityX*AccelerometerSensor.sensorAccelerationX*rawData.x;
        rawData.y = AccelerometerSensor.sensorSensitivityY*AccelerometerSensor.sensorAccelerationY*rawData.y;

        tunedData = new FloatPoint(rawData.x, rawData.y);

        return tunedData;
    }
}
