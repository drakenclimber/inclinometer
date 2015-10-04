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

package com.tomhromatka.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;

import com.tomhromatka.service.sensors.SensorAccelerometer;
import com.tomhromatka.service.sensors.SensorApi;
import com.tomhromatka.service.sensors.SensorInclinometer;
import com.tomhromatka.service.sensors.SensorOrientedAccelerometer;

public class HromatkaService extends Service {
    private final String TAG = this.getClass().getSimpleName();

    private final Binder binder = new LocalBinder();

    /**
     * Class that implements the listeners for HromatkaServiceApi.  No logic should be performed here.
     */
    public class LocalBinder extends Binder implements HromatkaServiceApi {

        @Override
        public void registerAccelerometerListener(SensorApi callback) {
            HromatkaLog.getInstance().enter(TAG);
            SensorAccelerometer.getInstance().registerListener(getSensorManager(), callback);
            HromatkaLog.getInstance().exit(TAG);
        }

        @Override
        public void unregisterAccelerometerListener(SensorApi callback) {
            HromatkaLog.getInstance().enter(TAG);
            SensorAccelerometer.getInstance().unregisterListener(getSensorManager(), callback);
            HromatkaLog.getInstance().exit(TAG);
        }

        @Override
        public void registerOrientedAccelerometerListener(SensorApi callback) {
            HromatkaLog.getInstance().enter(TAG);
            SensorOrientedAccelerometer.getInstance().registerListener(getSensorManager(), callback);
            HromatkaLog.getInstance().exit(TAG);
        }

        @Override
        public void unregisterOrientedAccelerometerListener(SensorApi callback) {
            HromatkaLog.getInstance().enter(TAG);
            SensorOrientedAccelerometer.getInstance().unregisterListener(getSensorManager(), callback);
            HromatkaLog.getInstance().exit(TAG);
        }

        @Override
        public void registerInclinometerListener(SensorApi callback) {
            HromatkaLog.getInstance().enter(TAG);
            SensorInclinometer.getInstance().registerListener(getSensorManager(), callback);
            HromatkaLog.getInstance().exit(TAG);
        }

        @Override
        public void unregisterInclinometerListener(SensorApi callback) {
            HromatkaLog.getInstance().enter(TAG);
            SensorInclinometer.getInstance().unregisterListener(getSensorManager(), callback);
            HromatkaLog.getInstance().exit(TAG);
        }

        @Override
        public void updateInclinometerOffsets() {
            SensorInclinometer.getInstance().updateOffsets();
        }
    }

    /**
     * Standard Android method that must be overridden to allow an activity to bind to
     * this service.
     *
     * @param intent Android Intent that contains a reference to the Activity that wants to
     *               interact with this service
     * @return The binder that implements the HromatkaServiceApi interface
     */
    @Override
    public IBinder onBind(Intent intent) {
        HromatkaLog.getInstance().enter(TAG);
        HromatkaLog.getInstance().exit(TAG);
        return binder;
    }

    /**
     * Standard Android method to create this class (and thus this service)
     */
    @Override
    public void onCreate() {
        HromatkaLog.getInstance().enter(TAG);
        super.onCreate();

        SensorOrientedAccelerometer.getInstance().setOrientation(getOrientation());
        HromatkaLog.getInstance().exit(TAG);
    }

    /**
     * Standard Android method to destroy this class (and thus this service)
     */
    @Override
    public void onDestroy() {
        HromatkaLog.getInstance().enter(TAG);
        super.onDestroy();

        SensorAccelerometer.getInstance().destroySensor(getSensorManager());
        SensorInclinometer.getInstance().destroySensor(getSensorManager());
        HromatkaLog.getInstance().exit(TAG);
    }

    /**
     * Listen to configuration changes and notify the oriented accelerometer
     *
     * @param newConfig New Configuration
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        HromatkaLog.getInstance().enter(TAG);
        super.onConfigurationChanged(newConfig);

        SensorOrientedAccelerometer.getInstance().setOrientation(getOrientation());
        HromatkaLog.getInstance().exit(TAG);
    }

    /**
     * Helper method to get the Android SensorManager instance for this service
     *
     * @return This service's SensorManager instance
     */
    private SensorManager getSensorManager() {
        HromatkaLog.getInstance().logVerbose(TAG, "SensorManager = " + this.getSystemService(Context.SENSOR_SERVICE));
        return (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
    }

    /**
     * get the current orientation of the device
     *
     * @return Configuration.ORIENTATION_*
     */
    private int getOrientation() {
        return getResources().getConfiguration().orientation;
    }
}
