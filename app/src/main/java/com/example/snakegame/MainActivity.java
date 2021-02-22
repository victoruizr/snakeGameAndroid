package com.example.snakegame;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Display;


public class MainActivity extends Activity implements SensorEventListener {

    motorSerpiente m;

    private CountDownTimer countDownTimer;
    private long timeLeft = 100000;
    private boolean timeRunning;
    private Point size;
    private AlertDialog dialog;
    private float x, y;
    private SensorManager sm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Display pantalla = getWindowManager().getDefaultDisplay();
        size = new Point();
        pantalla.getSize(size);
        m = new motorSerpiente(this, size);
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor smRotacion = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, smRotacion, SensorManager.SENSOR_DELAY_GAME);

        setContentView(m);

    }


    @Override
    protected void onResume() {
        super.onResume();
        m.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sm.unregisterListener(this);
        finish();
        //m.pause();
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        if (m.jugando) {
            m.moverSerpiente(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sm.unregisterListener(this);
    }
}