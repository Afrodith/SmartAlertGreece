package com.kospeac.smartgreecealert;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;


interface FallDetectionListener
{
    public void onStatusChanged(boolean newStatus);
}


public class FallDetectionHandler implements SensorEventListener {
    private String TAG = "FALL DETECTION HANDLER";
    private FallDetectionListener listener;
    public SensorManager mSensorManager;
    private Sensor mSensor;
    //private long mlPreviousTime;
    private boolean moIsMin = false;
    private boolean moIsMax = false;
    private Context mContext;
    private int i;
    public static Boolean status;

    public FallDetectionHandler(Context context) {
        mContext = context;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); // sensor  (TYPE_ACCELEROMETER)
        registerListener();
    }


    public void registerListener(){
        status = true;
        mSensorManager.registerListener(this,mSensor,SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregisterListener(){
        status = false;
        mSensorManager.unregisterListener(this);
    }

    public static Boolean getListenerStatus(){
        return status;
    }


    /*
    * Για καθε event απο το επιταχυνσιομετρο
    * */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            double loX = sensorEvent.values[0];
            double loY = sensorEvent.values[1];
            double loZ = sensorEvent.values[2];

            double loAccelerationReader = Math.sqrt(Math.pow(loX, 2)
                    + Math.pow(loY, 2)
                    + Math.pow(loZ, 2));
            if (loAccelerationReader <= 6.0) {
                moIsMin = true;

            }

            if (moIsMin) {
                i++;
                if (loAccelerationReader >= 25) {
                        moIsMax = true;
                        Log.i(TAG, "max");
                }

            }

            if (moIsMin && moIsMax) {
                Toast.makeText(mContext, "A fall has been detected!!", Toast.LENGTH_LONG).show(); //toast message
                i = 0;
                moIsMin = false;
                moIsMax = false;
                setFallDetection(true); //status == true
            }

            if (i > 10) {
                i = 0;
                moIsMin = false;
                moIsMax = false;
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    public void setFallDetection(boolean fallDetectionstatus){
        if(listener !=null){
            listener.onStatusChanged(fallDetectionstatus);

        }
    }


    public void setFallDetectionListener(FallDetectionListener listener){
        this.listener = listener;
    }
}
