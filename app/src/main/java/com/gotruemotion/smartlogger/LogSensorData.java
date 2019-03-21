package com.gotruemotion.smartlogger;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class LogSensorData extends Service
        implements SensorEventListener {

    private static final String TAG = "TagSmartLogger";

    private static final String CHANNEL_ID = "logSensorDataChannel";

    private String logFileName = "";
    private FileOutputStream logFile = null;

    private long startTime;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    void showNotification() {
        // Oreo required notification.
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        String textMsg = "Logging to " + MainActivity.logFileName;

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Smart Sensor Data Logger")
                .setContentText(textMsg)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Log.i(TAG, "onStartCommand()");

        // Open Log file or give up.
        // (Just being paranoid. Copy is not strictly needed. There
        // will be a stop before another start.)
        logFileName = MainActivity.logFileName;
        if ( ! openLog()) {
            return START_NOT_STICKY;
        }

        // Required for Oreo.
        showNotification();
        Log.i(TAG, "onStartCommand after showNotification()");

        Log.i(TAG, "Creating service timer");

        // Setup for logging.
        Handler handler = new Handler();

        Runnable runnable = new Runnable() {

            @Override
            public void run() {

                updateLog();
            }
        };

        // Only end of test timer.
        handler.postDelayed(runnable, MainActivity.testLengthValue);

        startTime = System.currentTimeMillis();
        lastLog = startTime;

        updateLog();

        startLocationSensor();

        initAllMotionSensor();
        startAllMotionSensor();

        return START_NOT_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        // onBind not used.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "onDestroy()");
        stopLocationSensor();
        stopAllMotionSensor();
        writeLog(MainActivity.runMsg);
        closeLog();
    }

    boolean openLog() {

        closeLog();

        // Build full path from copied logFileName.
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        dirPath += "/" + "smart_logger";
        String filePath = dirPath + "/" + logFileName;

        // Open file based on testName.
        try {
            @SuppressWarnings("unused")
            boolean newDir = new File(dirPath).mkdir();
            logFile = new FileOutputStream(filePath);

        } catch (IOException e) {
            Log.e( TAG, "Unable to close log file: " + logFileName);
            return false;
        }

        writeLog(MainActivity.runMsg);

        return true;
    }

    void closeLog() {

        if (logFile != null) {

            try {
                logFile.close();

            } catch (IOException e) {
                Log.e( TAG, "Unable to close log file: " + logFileName);
            }

            logFile = null;
        }
    }

    void writeLog(String msg) {

        // Ignore writes if no open file.
        if (logFile == null)
            return;

        // Write msg string to the log file.
        try {
            logFile.write(msg.getBytes());

        } catch (IOException e) {
            Log.e(TAG, "writeLog() failure");

            // No more writes.
            closeLog();
        }
    }

    //String eventListMsg = "";
    void checkLog() {

        long nowTime = System.currentTimeMillis();

        //eventListMsg += " " + (nowTime - lastLog);

        if ( nowTime - lastLog >= MainActivity.logFreqValue) {

            updateLog();
        }
    }

    long updateCount = 0;
    long lastLog = 0;
    void updateLog() {

        long nowTime = System.currentTimeMillis();
        long ticks = (nowTime - startTime) / MainActivity.logFreqValue;
        long lostTicks = ticks - updateCount;
        updateCount++;

        String msgData = "Log: " + MainActivity.getDate() + "\n";
        msgData += "Ticks = " + ticks + " Lost ticks = " + lostTicks + "\n";
        writeLog(msgData);

        //msgData = "Last Log: " + (lastLog - startTime) + "  Event List: " + eventListMsg + "\n";
        //writeLog(msgData);
        lastLog = nowTime;
        //eventListMsg = "";

        logBatteryData();
        logLocationSensor();
        logMotionSensorData();

        writeLog("\n");

        // Is test over?
        long elapsedTime = System.currentTimeMillis() - startTime;
        if (MainActivity.testIsRunning && elapsedTime >= MainActivity.testLengthValue) {
            // Yes.
            Log.i(TAG, "Test is complete");

            Log.i(TAG, "Sending STOP TEST from service ...");
            Intent intent = new Intent("com.gotruemotion.smartlogger.stop_test");
            sendBroadcast(intent);


            stopForeground(true);
            stopSelf();
        }
    }

    private void logBatteryData() {
        Intent batteryStatus;

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = this.registerReceiver(null, ifilter);

        String msgData;

        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            float batteryPct = (float) (100.0 * (float) level / (float) scale);
            msgData = "level=" + level + " scale=" + scale + " Percent=" + batteryPct;

        } else {
            msgData = "batteryStatus == null";
        }

        msgData = "Battery: " + msgData + "\n";

        writeLog(msgData);
    }

    // Count location events and save the most recent.
    private int locationEventCount = 0;
    @SuppressWarnings("unused")
    private Location locationEvent = null;

    void logLocationSensor() {

        long delta = System.currentTimeMillis() - startTime;
        float hz;

        String msgData = "";

        // Log motion data counts.
        if (MainActivity.gpsFreqValue != 0) {
            hz = (float) (1000.0 * locationEventCount) / (float) delta;
            msgData += "locationEventCount: " + locationEventCount + " rate: " + hz + "\n";
        }

        writeLog(msgData);
    }

    void startLocationSensor() {

        // Clear any old sensor data.
        locationEventCount = 0;
        locationEvent = null;

        if (MainActivity.gpsFreqValue != 0) {
            int permission = ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION);

            if (permission == PackageManager.PERMISSION_GRANTED) {
                locationManager = (LocationManager) getSystemService(MainActivity.LOCATION_SERVICE);

                if (locationManager != null) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MainActivity.gpsFreqValue, 0, locationListener);
                }
            }
        }

    }

    void stopLocationSensor() {
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    LocationManager locationManager;
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            if (locationEventCount < 10) {
                Log.i(TAG, location.toString());
            }

            locationEventCount++;
            locationEvent = location;
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    public void logMotionSensorData() {
        long delta = System.currentTimeMillis() - startTime;
        float hz;

        String msgData = "";

        // Log motion data counts.
        if (MainActivity.accFreqValue != 0) {
            hz = (float) (1000.0 * accelerometerEventCount) / (float) delta;
            msgData += "accelerometerEventCount: " + accelerometerEventCount + " rate: " + hz + "\n";
        }

        if (MainActivity.gyroFreqValue != 0) {
            hz = (float) (1000.0 * gyroscopeEventCount) / (float) delta;
            msgData += "gyroscopeEventCount: " + gyroscopeEventCount + " rate: " + hz + "\n";
        }

        if (MainActivity.magFreqValue != 0) {
            hz = (float) (1000.0 * magneticFieldEventCount) / (float) delta;
            msgData += "magneticFieldEventCount: " + magneticFieldEventCount + " rate: " + hz + "\n";
        }

        if (MainActivity.rotationVectorFreqValue != 0) {
            hz = (float) (1000.0 * rotationVectorEventCount) / (float) delta;
            msgData += "rotationVectorEventCount: " + rotationVectorEventCount + " rate: " + hz + "\n";
        }

        writeLog(msgData);
    }

    SensorManager sensorManager;

    Sensor accelerometerSensor;
    Sensor geomagneticRotationVectorSensor;
    Sensor gravitySensor;
    Sensor gyroscopeSensor;
    Sensor linearAccelerationSensor;
    Sensor magneticFieldSensor;
    Sensor poseSensor;
    Sensor rotationVectorSensor;

    void initAllMotionSensor() {
        // Get the sensor Manager once.
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        if (sensorManager == null) {
            throw new AssertionError("sensorManager cannot be null");
        }

        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        geomagneticRotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        linearAccelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        poseSensor = sensorManager.getDefaultSensor(Sensor.TYPE_POSE_6DOF);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    void startAllMotionSensor() {

        // Clear any old sensor data.
        accelerometerEventCount = 0;
        accelerometerEvent = null;

        gomagneticRotationVectorEventCount = 0;
        gomagneticRotationVectorEvent = null;

        gravityEventCount = 0;
        gravityEvent = null;

        gyroscopeEventCount = 0;
        gyroscopeEvent = null;

        linearAccelerationEventCount = 0;
        linearAccelerationEvent = null;

        magneticFieldEventCount = 0;
        magneticFieldEvent = null;

        poseEventCount = 0;
        poseEvent = null;

        rotationVectorEventCount = 0;
        rotationVectorEvent = null;

        // Register the sensors for the test.
        if (MainActivity.accFreqValue != 0) {
            sensorManager.registerListener(this,
                    accelerometerSensor,
                    MainActivity.accFreqValue, MainActivity.reportLatencyValue);
        }

        if (MainActivity.gyroFreqValue!= 0) {
            sensorManager.registerListener(this,
                    gyroscopeSensor,
                    MainActivity.gyroFreqValue, MainActivity.reportLatencyValue);
        }

        if (MainActivity.magFreqValue != 0) {
            sensorManager.registerListener(this,
                    magneticFieldSensor,
                    MainActivity.magFreqValue, MainActivity.reportLatencyValue);
        }

        if (MainActivity.rotationVectorFreqValue != 0) {

            sensorManager.registerListener(this,
                    rotationVectorSensor,
                    MainActivity.rotationVectorFreqValue, MainActivity.reportLatencyValue);
        }
    }

    void stopAllMotionSensor() {
        sensorManager.unregisterListener(this);
    }

    // Count accelerometer events and save the most recent.
    private int accelerometerEventCount = 0;
    @SuppressWarnings("unused")
    private SensorEvent accelerometerEvent = null;

    private void accelerometerEventAction(SensorEvent event) {

        accelerometerEventCount++;
        accelerometerEvent = event;
    }

    // Count gomagneticRotationVectorEvent events and save the most recent.
    @SuppressWarnings("unused")
    private int gomagneticRotationVectorEventCount = 0;
    @SuppressWarnings("unused")
    private SensorEvent gomagneticRotationVectorEvent = null;

    private void geomagneticRotationVectorEventAction(SensorEvent event) {

        gomagneticRotationVectorEventCount++;
        gomagneticRotationVectorEvent = event;
    }

    // Count gravity events and save the most recent.
    @SuppressWarnings("unused")
    private int gravityEventCount = 0;
    @SuppressWarnings("unused")
    private SensorEvent gravityEvent = null;

    private void gravityEventAction(SensorEvent event) {

        gravityEventCount++;
        gravityEvent = event;
    }

    // Count gyroscope events and save the most recent.
    private int gyroscopeEventCount = 0;
    @SuppressWarnings("unused")
    private SensorEvent gyroscopeEvent = null;

    private void gyroscopeEventAction(SensorEvent event) {

        gyroscopeEventCount++;
        gyroscopeEvent = event;
    }

    // Count linearAcceleration events and save the most recent.
    @SuppressWarnings("unused")
    private int linearAccelerationEventCount = 0;
    @SuppressWarnings("unused")
    private SensorEvent linearAccelerationEvent = null;

    private void linearAccelerationEventAction(SensorEvent event) {

        linearAccelerationEventCount++;
        linearAccelerationEvent = event;
    }

    // Count magneticFieldEvent events and save the most recent.
    private int magneticFieldEventCount = 0;
    @SuppressWarnings("unused")
    private SensorEvent magneticFieldEvent = null;

    private void magneticFieldEventAction(SensorEvent event) {

        magneticFieldEventCount++;
        magneticFieldEvent = event;
    }

    // Count pose6DofEvent events and save the most recent.
    @SuppressWarnings("unused")
    private int poseEventCount = 0;
    @SuppressWarnings("unused")
    private SensorEvent poseEvent = null;

    private void poseEventAction(SensorEvent event) {

        poseEventCount++;
        poseEvent = event;
    }

    // Count rotationVector events and save the most recent.
    private int rotationVectorEventCount = 0;
    @SuppressWarnings("unused")
    private SensorEvent rotationVectorEvent = null;

    private void rotationVectorEventAction(SensorEvent event) {

        rotationVectorEventCount++;
        rotationVectorEvent = event;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        int type = event.sensor.getType();
        switch(type) {
            case Sensor.TYPE_ACCELEROMETER:
                accelerometerEventAction(event);
                break;
            case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR:
                geomagneticRotationVectorEventAction(event);
                break;
            case Sensor.TYPE_GRAVITY:
                gravityEventAction(event);
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyroscopeEventAction(event);
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                linearAccelerationEventAction(event);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magneticFieldEventAction(event);
                break;
            case Sensor.TYPE_POSE_6DOF:
                poseEventAction(event);
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                rotationVectorEventAction(event);
                break;
        }

        checkLog();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
