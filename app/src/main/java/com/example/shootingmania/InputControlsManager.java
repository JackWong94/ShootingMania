package com.example.shootingmania;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;

public class InputControlsManager {

    RealTimeInputControlsParameters realTimeInputControlsParameters;
    GameManager gameManager;

    public InputControlsManager(Context context, Display display, GameManager gameManager) {
        this.gameManager = gameManager;
        realTimeInputControlsParameters = new RealTimeInputControlsParameters();
        //Touch screen user control (Receive touch event from GameView class)
        realTimeInputControlsParameters.userTouchPointer = new Rect(0,0,0,0);

        //Accelerometer user control
        realTimeInputControlsParameters.accelerometerSensorValue = new FloatPoint(0,0);
        AccelerometerSensor accelerometerSensor = new AccelerometerSensor(display, context, this);
    }

    //Touch screen user control related
    public boolean gameScreenPressedDetected(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            realTimeInputControlsParameters.userTouchPointer = new Rect((int) e.getX()-10, (int) e.getY()-10,(int) e.getX()+10, (int) e.getY()+10);
            gameManager.updateTouchControls(realTimeInputControlsParameters);
        }
        return true;
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
    public Rect userTouchPointer;
    public FloatPoint accelerometerSensorValue;
    //Accelerometer user control related
}

class AccelerometerSensor {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    public static float sensorSensitivityX = 3;
    public static float sensorSensitivityY = 3;
    public static int sensorAccelerationX = 10;    //Increase smoothness
    public static int sensorAccelerationY = 10;    //Increase smoothness

    public AccelerometerSensor(Display display, Context context, InputControlsManager inputControlsManager) {
        SensorControlListener sensorControlListener = new SensorControlListener(display) {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
                    return;
                }
                switch (display.getRotation()) {
                    case Surface.ROTATION_0:
                        inputControlsManager.accelerometerValueChange(new FloatPoint(event.values[0], event.values[1]));
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


}
