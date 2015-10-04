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

import android.app.Service;
import android.content.res.Configuration;
import android.hardware.SensorManager;

import com.tomhromatka.service.HromatkaLog;

/**
 * The "standard" accelerometer generates data based upon the orientation of the accelerometer
 * chip.  On different devices, the chip is mounted in different orientations.  This sensor
 * class attempts to handle that.
 */
public class SensorOrientedAccelerometer extends AbstractSensor implements SensorApi {
    private final String TAG = this.getClass().getSimpleName();

    private static SensorOrientedAccelerometer instance = null;

    private static final int NEGATIVE_SIGN = -1;
    private static final int POSITIVE_SIGN = 1;

    private static final int X_INDEX = 0;
    private static final int Y_INDEX = 1;
    private static final int Z_INDEX = 2;

    private boolean orientationSet = false;

    /* intentionally uninitialized.  these will be initialized in initializeOrientation() */
    private int	xIndex = X_INDEX;
    private int yIndex = Y_INDEX;
    private int zIndex = Z_INDEX;
    private int xSign  = NEGATIVE_SIGN;
    private int ySign  = POSITIVE_SIGN;
    private int zSign  = NEGATIVE_SIGN;

    /**
     * Constructor - note this will force the class to be a singleton
     */
    protected SensorOrientedAccelerometer() {
    }

    /**
     * Public constructor.  Returns the instance of this singleton class.  This method will
     * create the instance if it doesn't exist.
     *
     * @return the instance of this class
     */
    public static SensorOrientedAccelerometer getInstance() {
        if (null == instance) {
            instance = new SensorOrientedAccelerometer();
        }

        return instance;
    }

    /**
     * Enable the oriented accelerometer sensor.
     *
     * @param sensorManager An instance of the Android SensorManager
     */
    @Override
    protected void enableSensor(SensorManager sensorManager) {
        HromatkaLog.getInstance().enter(TAG);

        /* register this class as an accelerometer listener */
        SensorAccelerometer.getInstance().registerListener(sensorManager, this);
        HromatkaLog.getInstance().exit(TAG);
    }

    /**
     * Disable the oriented accelerometer sensor.
     *
     * @param sensorManager An instance of the Android SensorManager
     */
    @Override
    protected void disableSensor(SensorManager sensorManager) {
        HromatkaLog.getInstance().enter(TAG);
        SensorAccelerometer.getInstance().unregisterListener(sensorManager, this);
        HromatkaLog.getInstance().exit(TAG);
    }

    /**
     * Destroy the oriented accelerometer sensor.
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
     * This class's listener for new sensor data from SensorAccelerometer.  Required
     * via the SensorApi implementation.
     *
     * @param timestamp time at which this measurement occurred
     * @param values    array of accelerometer measurements (x == 0, y == 1, z == 2)
     */
    @Override
    public void onDataReceived(long timestamp, float[] values) {
        HromatkaLog.getInstance().enter(TAG);
        if (!orientationSet) {
            /* we may not yet know the orientation if we are in landscape mode.  now that we
             * have accelerometer data, we can know for sure.
             */
            calculateLandscapeOrientation(values[X_INDEX]);
            orientationSet = true;
        }

        float[] rotatedAccelValues = new float[3];
        rotatedAccelValues[0] = xSign * values[xIndex];
        rotatedAccelValues[1] = ySign * values[yIndex];
        rotatedAccelValues[2] = zSign * values[zIndex];

        notifyListenersDataReceived(timestamp, rotatedAccelValues);
        HromatkaLog.getInstance().exit(TAG);
    }

    /**
     * This class's listener for accuracy changes in SensorAccelerometer.  Required
     * via the SensorApi implementation.  Currently we don't do anything with accelerometer
     * accuracy changes.
     *
     * @param accuracy New accuracy of the raw accelerometer.
     */
    @Override
    public void onAccuracyChanged(int accuracy) {
        HromatkaLog.getInstance().enter(TAG);
        HromatkaLog.getInstance().exit(TAG);
    }

    /**
     * this method should be called when the orientation of the device changes.  This will allow
     * the oriented accelerometer sensor to reconfigure its orientation
     * @param orientation Configuration.ORIENTATION_* value
     */
    public void setOrientation(int orientation) {
        HromatkaLog.getInstance().enter(TAG);
        switch (orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                HromatkaLog.getInstance().logVerbose(TAG, "Setting orientation to landscape.");
                xIndex = Y_INDEX;
                yIndex = X_INDEX;
                zIndex = Z_INDEX;
                xSign  = NEGATIVE_SIGN;
                ySign  = POSITIVE_SIGN;
                zSign  = NEGATIVE_SIGN;

                /* we can't claim the orientation is set (yet).  This is because landscape mode
                 * has two valid positions - left side down or right side down.
                 */
                orientationSet = false;
                break;

            case Configuration.ORIENTATION_PORTRAIT:
            default:
                HromatkaLog.getInstance().logVerbose(TAG, "Setting orientation to portrait.");
                xIndex = X_INDEX;
                yIndex = Y_INDEX;
                zIndex = Z_INDEX;
                xSign = POSITIVE_SIGN;
                ySign = POSITIVE_SIGN;
                zSign = NEGATIVE_SIGN;
                orientationSet = true;
                break;
        }

        /* notify listeners that our orientation has changed */
        onAccuracyChanged(0);
        HromatkaLog.getInstance().exit(TAG);
    }

    /**
     * When the device is in landscape mode, we don't know if the left side is up or the right
     * side is up.  Once we have raw accelerometer measurements, we can determine which side is up.
     * @param xAccelValue x-axis accelerometer measurement (m/s^2)
     */
    private void calculateLandscapeOrientation(float xAccelValue) {
        HromatkaLog.getInstance().enter(TAG);
        if (xAccelValue < 0.0f) {
            /* right side of device is down */
            xSign = POSITIVE_SIGN;
            ySign = NEGATIVE_SIGN;
        }
        else {
            /* left side of device is down */
            xSign = NEGATIVE_SIGN;
            ySign = POSITIVE_SIGN;
        }
        HromatkaLog.getInstance().exit(TAG);
    }
}
