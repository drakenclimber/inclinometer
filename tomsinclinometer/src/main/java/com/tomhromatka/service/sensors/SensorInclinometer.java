/*
 * Copyright 2011-2015 Tom Hromatka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.tomhromatka.service.sensors;

import android.hardware.SensorManager;

import com.tomhromatka.service.HromatkaLog;

public class SensorInclinometer extends AbstractSensor implements SensorApi {
    private final String TAG = this.getClass().getSimpleName();

    public static final int PITCH_INDEX = 0;
    public static final int ROLL_INDEX = 1;

    private static final double RAD_TO_DEG = 180.0f / Math.PI;
    private static final double ROLL_X_ZERO_THRESH  = 0.25f;
    private static final double ROLL_Y_ZERO_THRESH  = 0.25f;

    /* notify listeners no faster than at a 3 Hz rate (approximately 333 ms) */
    private static final long NOTIFY_LISTENERS_TIME_MS = 333;
    private static long lastTimeListenersNotified = System.currentTimeMillis();

    private static SensorInclinometer instance = null;
    private static FilterMovingAverage filterMovingAverage =
            new FilterMovingAverage(FilterMovingAverage.DEFAULT_SAMPLE_EXPIRATION_NS);

    private static double[] pitchAndRollOffsets = new double[2];

    /**
     * Constructor - note this will force the class to be a singleton
     */
    protected SensorInclinometer() {
    }

    /**
     * Public constructor.  Returns the instance of this singleton class.  This method will
     * create the instance if it doesn't exist.
     *
     * @return the instance of this class
     */
    public static SensorInclinometer getInstance() {
        if (null == instance) {
            instance = new SensorInclinometer();
        }

        return instance;
    }

    /**
     * Enable the inclinometer sensor.
     *
     * @param sensorManager An instance of the Android SensorManager
     */
    @Override
    protected void enableSensor(SensorManager sensorManager) {
        HromatkaLog.getInstance().enter(TAG);
        SensorOrientedAccelerometer.getInstance().registerListener(sensorManager, this);
        HromatkaLog.getInstance().exit(TAG);
    }

    /**
     * Disable the inclinometer sensor.
     *
     * @param sensorManager An instance of the Android SensorManager
     */
    @Override
    protected void disableSensor(SensorManager sensorManager) {
        HromatkaLog.getInstance().enter(TAG);
        SensorOrientedAccelerometer.getInstance().unregisterListener(sensorManager, this);
        HromatkaLog.getInstance().exit(TAG);
    }

    /**
     * Destroy the inclinometer sensor.
     *
     * @param sensorManager An instance of the Android SensorManager
     */
    @Override
    public void destroySensor(SensorManager sensorManager) {
        super.destroySensor(sensorManager);

        HromatkaLog.getInstance().enter(TAG);
        disableSensor(sensorManager);
        HromatkaLog.getInstance().exit(TAG);
    }

    /**
     * This class's listener for new sensor data from SensorOrientedAccelerometer.  Required
     * via the SensorApi implementation.
     *
     * @param timestamp   time at which this measurement occurred
     * @param accelValues array of accelerometer measurements (x == 0, y == 1, z == 2)
     */
    @Override
    public void onDataReceived(long timestamp, float[] accelValues) {
        HromatkaLog.getInstance().enter(TAG);

        filterMovingAverage.add(timestamp, accelValues);
        filterMovingAverage.removeExpired();

        float[] averagedAccelValues = filterMovingAverage.getMovingAverage();
        float[] pitchAndRoll = new float[2];

        pitchAndRoll[PITCH_INDEX] =
                (float) computePitch((double) averagedAccelValues[1], (double) averagedAccelValues[2]) +
                        (float) pitchAndRollOffsets[PITCH_INDEX];
        pitchAndRoll[ROLL_INDEX] =
                (float) computeRoll( (double)averagedAccelValues[0], (double)averagedAccelValues[1]) +
                        (float) pitchAndRollOffsets[ROLL_INDEX];

        /**
         * SensorInclinometer generates the values[] array for onDataReceived() as follows:
         * 0 == pitch (degrees)
         * 1 == roll (degrees)
         *
         * Note to save power, the inclinometer data is only sent to the activity at approximately
         * three hertz.
         */

        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastTimeListenersNotified) > NOTIFY_LISTENERS_TIME_MS) {
            HromatkaLog.getInstance().logVerbose(TAG, "Notifying listeners of new inclinometer data");
            lastTimeListenersNotified = currentTime;
            notifyListenersDataReceived(timestamp, pitchAndRoll);
        }
        else {
            HromatkaLog.getInstance().logVerbose(TAG, "Rate limiting new inclinometer data.  Do not notify listeners.");
        }

        HromatkaLog.getInstance().exit(TAG);
    }

    /**
     * This class's listener for accuracy changes in SensorOrientedAccelerometer.  Required
     * via the SensorApi implementation.  SensorOrientedAccelerometer uses it to notify listeners
     * that the orientation has changed.
     *
     * @param accelAccuracy New accuracy of the accelerometer.
     */
    @Override
    public void onAccuracyChanged(int accelAccuracy) {
        HromatkaLog.getInstance().enter(TAG);
        filterMovingAverage.clear();
        HromatkaLog.getInstance().exit(TAG);
    }

    /**
     * Update the pitch and roll offsets.  This allows the phone to be mounted at any arbitrary
     * angle.
     */
    public void updateOffsets() {
        float[] averagedAccelValues = filterMovingAverage.getMovingAverage();

        pitchAndRollOffsets[PITCH_INDEX] =
                -computePitch((double)averagedAccelValues[1], (double)averagedAccelValues[2]);
        pitchAndRollOffsets[ROLL_INDEX] =
                -computeRoll( (double)averagedAccelValues[0], (double)averagedAccelValues[1]);
    }

    /**
     * Method to compute the pitch of the device
     *
     * @param y accelerometer value in the y axis (m/s^2)
     * @param z accelerometer value in the z axis (m/s^2)
     * @return the pitch of the phone in degrees
     */
    private double computePitch(double y, double z) {
        HromatkaLog.getInstance().enter(TAG);
        double pitch = 90.0f - (Math.atan2(y, z) * RAD_TO_DEG);

        if (pitch < -180.0f) {
            HromatkaLog.getInstance().logVerbose(TAG, "Pitch underflow: " + pitch);
            pitch += 360.0f;
        }
        else if (pitch > 180.0f)
        {
            HromatkaLog.getInstance().logVerbose(TAG, "Pitch overflow: " + pitch);
            pitch -= 360.0f;
        }

        HromatkaLog.getInstance().logVerbose(TAG, String.format("y = %4.1f, z = %4.1f, pitch = %4.1f", y, z, pitch));

        HromatkaLog.getInstance().exit(TAG);
        return pitch;
    }

    /**
     * Method to compute the roll of the device
     *
     * @param x accelerometer value in the x axis (m/s^2)
     * @param y accelerometer value in the y axis (m/s^2)
     * @return the roll of the phone in degrees
     */
    private double computeRoll(double x, double y) {
        HromatkaLog.getInstance().enter(TAG);
        double roll = (Math.atan2(y, x) * RAD_TO_DEG) - 90.0f;

        if (roll < -180.0f) {
            HromatkaLog.getInstance().logVerbose(TAG, "Roll underflow: " + roll);
            roll += 360.0f;
        }
        else if (roll > 180.0f) {
            HromatkaLog.getInstance().logVerbose(TAG, "Roll overflow: " + roll);
            roll -= 360.0f;
        }

        if (x < ROLL_X_ZERO_THRESH && y < ROLL_Y_ZERO_THRESH) {
    		/* zero out roll when both x and y are near-zero. */
            HromatkaLog.getInstance().logVerbose(TAG, "Roll autozero.  x = " + x + ", y = " + y);
            roll = 0.0f;
        }

        HromatkaLog.getInstance().exit(TAG);
        return roll;
    }
}
