
package com.gotruemotion.smartlogger;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.text.method.ScrollingMovementMethod;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener {

    private String TAG = "TagSmartLogger";

    static boolean testIsRunning = false;
    static String runMsg = "";

    String startTestDate = "";
    String stopTestDate = "";

    //
    EditText logFileNameInput;

    Spinner testLengthSpinner;
    Spinner logFreqSpinner;
    Spinner gpsFreqSpinner;
    Spinner accFreqSpinner;
    Spinner gyroFreqSpinner;
    Spinner magFreqSpinner;
    Spinner rotationVectorFreqSpinner;
    Spinner reportLatencySpinner;

    ToggleButton runButton;

    TextView statusText;

    void initGUI() {

        // Name of log file.
        logFileNameInput = findViewById(R.id.logFileNameInput);

        // Spinner for Test Length.
        testLengthSpinner = findViewById(R.id.testLengthSpinner);

        ArrayAdapter<CharSequence> testLengthAdapter =
                ArrayAdapter.createFromResource(this, R.array.test_length_options,
                        android.R.layout.simple_spinner_item);

        testLengthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        testLengthSpinner.setAdapter(testLengthAdapter);
        testLengthSpinner.setOnItemSelectedListener(this);

        // Spinner for Log Frequency.
        logFreqSpinner = findViewById(R.id.logFreqSpinner);
        ArrayAdapter<CharSequence> logFreqAdapter =
                ArrayAdapter.createFromResource(this, R.array.log_freq_options,
                        android.R.layout.simple_spinner_item);
        logFreqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        logFreqSpinner.setAdapter(logFreqAdapter);
        logFreqSpinner.setOnItemSelectedListener(this);

        // Spinner for GPS Frequency.
        gpsFreqSpinner = findViewById(R.id.gpsFreqSpinner);
        ArrayAdapter<CharSequence> gpsFreqAdapter =
                ArrayAdapter.createFromResource(this, R.array.gps_freq_options,
                        android.R.layout.simple_spinner_item);
        gpsFreqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gpsFreqSpinner.setAdapter(gpsFreqAdapter);
        gpsFreqSpinner.setOnItemSelectedListener(this);

        // Spinner for acc Frequency.
        accFreqSpinner = findViewById(R.id.accFreqSpinner);
        ArrayAdapter<CharSequence> accFreqAdapter =
                ArrayAdapter.createFromResource(this, R.array.acc_freq_options,
                        android.R.layout.simple_spinner_item);
        accFreqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accFreqSpinner.setAdapter(accFreqAdapter);
        accFreqSpinner.setOnItemSelectedListener(this);

        // Spinner for gyro Frequency.
        gyroFreqSpinner = findViewById(R.id.gyroFreqSpinner);
        ArrayAdapter<CharSequence> gyroFreqAdapter =
                ArrayAdapter.createFromResource(this, R.array.gyro_freq_options,
                        android.R.layout.simple_spinner_item);
        gyroFreqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gyroFreqSpinner.setAdapter(gyroFreqAdapter);
        gyroFreqSpinner.setOnItemSelectedListener(this);

        // Spinner for mag Frequency.
        magFreqSpinner = findViewById(R.id.magFreqSpinner);
        ArrayAdapter<CharSequence> magFreqAdapter =
                ArrayAdapter.createFromResource(this, R.array.mag_freq_options,
                        android.R.layout.simple_spinner_item);
        magFreqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        magFreqSpinner.setAdapter(magFreqAdapter);
        magFreqSpinner.setOnItemSelectedListener(this);

        // Spinner for rotationVector Frequency.
        rotationVectorFreqSpinner = findViewById(R.id.rotationVectorFreqSpinner);
        ArrayAdapter<CharSequence> rotationVectorFreqAdapter =
                ArrayAdapter.createFromResource(this, R.array.rotation_vector_freq_options,
                        android.R.layout.simple_spinner_item);
        rotationVectorFreqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rotationVectorFreqSpinner.setAdapter(rotationVectorFreqAdapter);
        rotationVectorFreqSpinner.setOnItemSelectedListener(this);

        // Spinner for report Latency.
        reportLatencySpinner = findViewById(R.id.reportLatencySpinner);
        ArrayAdapter<CharSequence> reportLatencyAdapter =
                ArrayAdapter.createFromResource(this, R.array.report_latency_options,
                        android.R.layout.simple_spinner_item);
        reportLatencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reportLatencySpinner.setAdapter(reportLatencyAdapter);
        reportLatencySpinner.setOnItemSelectedListener(this);

        // Run and status buttons.
        runButton = findViewById(R.id.runButton);

        // Status text
        statusText = findViewById(R.id.statusText);
        statusText.setMovementMethod(new ScrollingMovementMethod());
    }

    //
    // Get necessary permissions.
    //
    private static final int REQUEST_PERMISSIONS_LIST = 1;
    private static String[] PERMISSIONS_LIST = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    void getPermission() {

        // We don't have permission so prompt the user
        ActivityCompat.requestPermissions(
                this,
                PERMISSIONS_LIST,
                REQUEST_PERMISSIONS_LIST
        );
    }

    //
    // Configure test based on current GUI state.
    //
    static String logFileName;

    static String testLengthMsg;
    static long testLengthValue;

    static String logFreqMsg;
    static int logFreqValue;

    static String gpsFreqMsg;
    static int gpsFreqValue;

    static String accFreqMsg;
    static int accFreqValue;

    static String gyroFreqMsg;
    static int gyroFreqValue;

    static String magFreqMsg;
    static int magFreqValue;

    static String rotationVectorFreqMsg;
    static int rotationVectorFreqValue;

    static String reportLatencyMsg;
    static int reportLatencyValue;

    String cfgMsg;

    void cfgTest() {
        int idx;

        cfgMsg = "";

        // Log file name.
        // (For now, this is our only consistency check.)
        logFileName = logFileNameInput.getText().toString().trim();
        if ("".equals(logFileName)) {
            // If user does not give a name, use the hint name.
            logFileName = getString(R.string.log_file_hint_text);
        }
        cfgMsg += "Log File: " + logFileName + "\n";

        // Test length.
        idx = testLengthSpinner.getSelectedItemPosition();
        testLengthMsg = testLengthSpinner.getSelectedItem().toString();
        testLengthValue = SpinnerLookup.lookupTestLengthSpinner(idx);
        cfgMsg += testLengthMsg;
        cfgMsg += " (value = " + testLengthValue + ")\n";

        // Log frequency.
        idx = logFreqSpinner.getSelectedItemPosition();
        logFreqMsg = logFreqSpinner.getSelectedItem().toString();
        logFreqValue = SpinnerLookup.lookupLogFreqSpinner(idx);
        cfgMsg += logFreqMsg;
        cfgMsg += " (value = " + logFreqValue + ")\n";

        // gps frequency.
        idx = gpsFreqSpinner.getSelectedItemPosition();
        gpsFreqMsg = gpsFreqSpinner.getSelectedItem().toString();
        gpsFreqValue = SpinnerLookup.lookupGpsFreqSpinner(idx);
        cfgMsg += gpsFreqMsg;
        cfgMsg += " (value = " + gpsFreqValue + ")\n";

        // Accelerometer frequency.
        idx = accFreqSpinner.getSelectedItemPosition();
        accFreqMsg = accFreqSpinner.getSelectedItem().toString();
        accFreqValue = SpinnerLookup.lookupMotionFreqSpinner(idx);
        cfgMsg += accFreqMsg;
        cfgMsg += " (value = " + accFreqValue + ")\n";

        // Gyroscope frequency.
        idx = gyroFreqSpinner.getSelectedItemPosition();
        gyroFreqMsg = gyroFreqSpinner.getSelectedItem().toString();
        gyroFreqValue = SpinnerLookup.lookupMotionFreqSpinner(idx);
        cfgMsg += gyroFreqMsg;
        cfgMsg += " (value = " + gyroFreqValue + ")\n";

        // Magnetometer frequency.
        idx = magFreqSpinner.getSelectedItemPosition();
        magFreqMsg = magFreqSpinner.getSelectedItem().toString();
        magFreqValue = SpinnerLookup.lookupMotionFreqSpinner(idx);
        cfgMsg += magFreqMsg;
        cfgMsg += " (value = " + magFreqValue + ")\n";

        // rotationVector frequency.
        idx = rotationVectorFreqSpinner.getSelectedItemPosition();
        rotationVectorFreqMsg = rotationVectorFreqSpinner.getSelectedItem().toString();
        rotationVectorFreqValue = SpinnerLookup.lookupMotionFreqSpinner(idx);
        cfgMsg += rotationVectorFreqMsg;
        cfgMsg += " (value = " + rotationVectorFreqValue + ")\n";

        // Report latency.
        idx = reportLatencySpinner.getSelectedItemPosition();
        reportLatencyMsg = reportLatencySpinner.getSelectedItem().toString();
        reportLatencyValue = SpinnerLookup.lookupReportLatencySpinner(idx);
        cfgMsg += reportLatencyMsg;
        cfgMsg += " (value = " + reportLatencyValue + ")\n";
    }

    private BroadcastReceiver broadcastReceiver;

    private void registerReceiver() {
        broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                Log.i(TAG, "Main activity got STOP TEST from service");
                runButton.setChecked(false);
                updateText();
            }
        };

        registerReceiver(broadcastReceiver, new IntentFilter("com.gotruemotion.smartlogger.stop_test"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Orientation set BEFORE setContentView.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "onCreate");

        //
        // Initialize text regions, spinners, buttons, etc.
        //
        initGUI();

        //
        // Get storage and GPS permissions.
        //
        getPermission();

        registerReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "onDestroy");

        Intent intent = buildIntent();

        stopService(intent);

        if(broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }

    //
    // Common date format.
    //
    static String getDate() {

        // Make sure the same date format is always used.
        Date date = new Date();
        String strDateFormat = "yyyy.MM.dd 'at' HH:mm:ss";

        DateFormat dateFormat = new SimpleDateFormat(strDateFormat, Locale.US);

        return dateFormat.format(date);
    }

    Intent buildIntent() {

        return new Intent(this, LogSensorData.class);
    }

    //
    // Button click required method.
    //
    public void updateText() {
        testIsRunning = runButton.isChecked();
        Log.i(TAG, "runOnClick testIsRunning = " + testIsRunning);

        runMsg = "";

        // Get Current status information.
        if (testIsRunning) {

            startTestDate = getDate();
            stopTestDate = "";

            // Configure test for running.
            cfgTest();

            runMsg += "Test is Running\n";

        } else {
            runMsg += "Test is Stopped\n";
            stopTestDate = getDate();

        }

        runMsg += "Test Start: " + startTestDate + "\n";
        runMsg += "Test Stop: " + stopTestDate + "\n";

        // Add in the details.
        runMsg += "\n";
        runMsg += cfgMsg;

        statusText.setText(runMsg);
    }

    public void runOnClick(View view) {

        updateText();

        runMsg += "\n";
        runMsg += "\n";

        Intent intent = buildIntent();
        if (testIsRunning) {
            Log.i(TAG, "Calling startService()");

            int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Unable to get permission WRITE_EXTERNAL_STORAGE");
            } else {
                Log.i(TAG, "Have permission WRITE_EXTERNAL_STORAGE");
            }

            permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Unable to get permission ACCESS_FINE_LOCATION");
            } else {
                Log.i(TAG, "Have permission ACCESS_FINE_LOCATION");
            }

            startService(intent);

        } else {
            Log.i(TAG, "Calling stopService()");
            stopService(intent);
        }
    }

    //
    // Spinner related required methods.
    //

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        // Ignore spinner values until test start.
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // Ignore spinner values until test start.

    }
}
