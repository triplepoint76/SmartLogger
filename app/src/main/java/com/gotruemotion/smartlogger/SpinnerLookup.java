package com.gotruemotion.smartlogger;

import android.util.Log;

class SpinnerLookup {

    private static String TAG = "TagSpinnerLookup";

    static long lookupTestLengthSpinner(int idx) {

        // Start with minutes.
        long deltaTime = 60 * 1000;

        switch (idx) {
            case 0:
                // 1 minute
                break;
            case 1:
                // 5 minutes
                deltaTime *= 5;
                break;

            case 2:
                // 15 minutes
                deltaTime *= 15;
                break;

            case 3:
                // 30 minutes
                deltaTime *= 30;
                break;

            case 4:
                // 1 hour
                deltaTime *= 60;
                break;

            case 5:
                // 1.5 hours
                deltaTime *= 90;
                break;

            case 6:
                // 2 hours
                deltaTime *= 120;
                break;

            case 7:
                // 2 hours
                deltaTime *= 180;
                break;

            case 8:
                // 4 hours
                deltaTime *= 240;
                break;

            default:
                Log.e(TAG, "Bad test length spinner idx");
                throw new IllegalArgumentException("Bad test length spinner idx");
        }

        return deltaTime;
    }

    static int lookupLogFreqSpinner(int idx) {
        // Units are milliseconds.
        int oneMinute = 60 * 1000;

        switch (idx) {

            case 0:
                // 5 Seconds
                return oneMinute / 12;

            case 1:
                // 15 Seconds
                return oneMinute / 4;

            case 2:
                // 30 Seconds
                return oneMinute / 2;

            case 3:
                // 1 Minute
                return oneMinute;

            case 4:
                // 5 Minutes
                return 5 * oneMinute;

            case 5:
                // 10 Minutes
                return 10 * oneMinute;

            case 6:
                // 30 Minutes
                return 30 * oneMinute;

            case 7:
                // 1 Hour
                return 60 * oneMinute;

            default:
                Log.e(TAG, "Bad log frequency spinner idx");
                throw new IllegalArgumentException("Bad log frequency spinner idx");
        }
    }

    static int lookupGpsFreqSpinner(int idx) {
        // Units are milliseconds.
        int oneSecond = 1000;

        switch (idx) {
            case 0:
                // OFF
                return 0;

            case 1:
                // 8 HZ
                return oneSecond / 8;

            case 2:
                // 4 HZ
                return oneSecond / 4;

            case 3:
                // 2 HZ
                return oneSecond / 2;

            case 4:
                // 1 HZ
                return oneSecond;

            case 5:
                // 0.5 HZ
                return 2 * oneSecond;

            case 6:
                // 0.1 HZ
                return 10 * oneSecond;

            case 7:
                // 0.01 HZ
                return 100 * oneSecond;

            case 8:
                // 0.001 HZ
                return 1000 * oneSecond;

            case 9:
                // 0.0001 HZ
                return 10000 * oneSecond;

            default:
                Log.e(TAG, "Bad GPS frequency spinner idx");
                throw new IllegalArgumentException("Bad GPS frequency spinner idx");
        }
    }

    // All motion sensors have the same set of supported frequencies.
    static int lookupMotionFreqSpinner(int idx) {
        int oneSecond = 1000000;

        switch(idx) {

            case 0:
                // OFF
                return 0;

            case 1:
                // 64 HZ
                return oneSecond / 64;

            case 2:
                // 50 HZ
                return oneSecond / 50;

            case 3:
                // 32 HZ.
                return oneSecond / 32;

            case 4:
                // 30 HZ oneSecond /30;
                return oneSecond / 30;

            case 5:
                // 16 HZ
                return oneSecond / 16;

            case 6:
                // 10 HZ
                return oneSecond /10;

            case 7:
                // 8 Hz
                return oneSecond / 8;

            case 8:
                // 4 HZ
                return oneSecond / 4;

            case 9:
                // 2 HZ
                return oneSecond / 2;

            case 10:
                // 1 HZ
                return oneSecond;

            case 11:
                // 0.5 Hz
                return 2 * oneSecond;

            case 12:
                // 0.1 HZ
                return 10 * oneSecond;

            default:
                Log.e(TAG, "Bad motion frequency spinner idx");
                throw new IllegalArgumentException("Bad motion frequency spinner idx");
        }
    }

    static int lookupReportLatencySpinner(int idx) {
        int oneSecond = 1000000;

        switch(idx) {

            case 0:
                // 1 Minute
                return 60 * oneSecond;

            case 1:
                // 30 Seconds
                return 30 * oneSecond;

            case 2:
                // 10 Seconds
                return 10 * oneSecond;

            case 3:
                // 5 Seconds.
                return 5 * oneSecond;

            case 4:
                // 3 Seconds
                return 3 * oneSecond;

            case 5:
                // 1 Seconds
                return oneSecond;

            case 6:
                // 0.5 Seconds
                return oneSecond / 2;

            case 7:
                // 0.3 Seconds
                return (int) (0.3 * (float) oneSecond);

            case 8:
                // 0.1 Seconds
                return oneSecond / 10;

            default:
                Log.e(TAG, "Bad report latency spinner idx");
                throw new IllegalArgumentException("Bad report latency spinner idx");
        }
    }
}
