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
        AccelerometerSensor accelerometerSensor = new AccelerometerSensor();
        accelerometerSensor.registerListener(display, context, this);
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
    //Accelerometer user control related
    public FloatPoint accelerometerSensorValue;

}

class AccelerometerSensor {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static float sensorSensitivityX = 3;
    private static float sensorSensitivityY = 3;
    private static int sensorAccelerationX = 10;    //Increase smoothness
    private static int sensorAccelerationY = 10;    //Increase smoothness
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
