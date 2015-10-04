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

import java.util.ArrayList;
import java.util.Iterator;

public abstract class AbstractSensor {
    private final String TAG = this.getClass().getSimpleName();
    /** the watchdog is currently unimplemented */
    private ArrayList<SensorApi> listenerList = new ArrayList<>();

    /**
     * Registers a listener (visitor) that wants to listen to this sensor.  The listener
     * provides a instantiation of the SensorApi; the sensor will use this SensorApi instantiation
     * to callback the listener when a sensor event occurs.
     *
     * If this is the first listener, this method will enable the sensor.  (No point in running
     * a sensor to which no one is listening.)
     *
     * @param sensorManager An instance of the Android SensorManager
     * @param callback      The listener's callback class
     */
    public void registerListener(SensorManager sensorManager, SensorApi callback) {
        HromatkaLog.getInstance().enter(TAG);
        if (listenerList.isEmpty()) {
            /* this is the first listener for this sensor.  enable it */
            enableSensor(sensorManager);
        }

        listenerList.add(callback);
        HromatkaLog.getInstance().exit(TAG);
    }

    /**
     * Unregisters (Removes) a listener.
     *
     * If removing this listener results in no one is listening to the sensor, this method will
     * disable the sensor.  (Let's save some power, eh?)
     *
     * @param sensorManager An instance of the Android SensorManager
     * @param callback      The listener's callback class
     */
    public void unregisterListener(SensorManager sensorManager, SensorApi callback) {
        HromatkaLog.getInstance().enter(TAG);

        boolean foundListener = false;
        for (Iterator<SensorApi> iterator = listenerList.iterator(); iterator.hasNext();) {
            SensorApi sensorApi = iterator.next();
            if (callback == sensorApi) {
                /* remove this listener */
                iterator.remove();
                foundListener = true;
                break;
            }
        }

        /* blow up if we didn't find a listener to unregister.  this shouldn't ever happen */
        if (!foundListener) {
            HromatkaLog.getInstance().logError(TAG, "Failed to unregister the specified listener: " + callback);
        }

        if (listenerList.isEmpty()) {
            /* there are no more listeners.  disable the sensor to save power. */
            disableSensor(sensorManager);
        }
        HromatkaLog.getInstance().exit(TAG);
    }

    /**
     * Notify all registered listeners that this sensor has generated new data.  The data is an
     * array of floats in the values parameter.  Each sensor will define the length and definition
     * of the values[] array.
     *
     * @param timestamp timestamp at which this data was generated
     * @param values    array of values associated with this sensor/timestamp
     */
    protected void notifyListenersDataReceived(long timestamp, float[] values) {
        HromatkaLog.getInstance().enter(TAG);
        for (Iterator<SensorApi> iterator = listenerList.iterator(); iterator.hasNext();) {
            SensorApi sensorApi = iterator.next();
            sensorApi.onDataReceived(timestamp, values);
        }
        HromatkaLog.getInstance().exit(TAG);
    }

    /**
     * Notify all registered listeners that the accuracy of this sensor has changed.  This is an
     * extension of the onAccuracyChanged() method in Android.  See each individual sensor for
     * its usage of this method.
     *
     * @param accuracy new accuracy of this sensor
     */
    protected void notifyListenersAccuracyChanged(int accuracy) {
        HromatkaLog.getInstance().enter(TAG);
        for (Iterator<SensorApi> iterator = listenerList.iterator(); iterator.hasNext();) {
            SensorApi sensorApi = iterator.next();
            sensorApi.onAccuracyChanged(accuracy);
        }
        HromatkaLog.getInstance().exit(TAG);
    }

    /**
     * Method that must be overridden by the concrete sensor class to enable the sensor.  For
     * built-in Android sensors (e.g. accelerometer, gyroscope, etc.), this method will likely
     * invoke the registerListener() method in the SensorManager.  Custom sensors (e.g.
     * inclinometer) may do something different entirely.
     *
     * @param sensorManager An instance of the Android SensorManager
     */
    protected abstract void enableSensor(SensorManager sensorManager);

    /**
     * Method that must be overridden by the concrete sensor class to disable the sensor.  For
     * built-in Android sensors (e.g. accelerometer, gyroscope, etc.), this method will likely
     * invoke the unregisterListener() method in the SensorManager.  Custom sensors (e.g.
     * inclinometer) may do something different entirely.
     *
     * @param sensorManager An instance of the Android SensorManager
     */
    protected abstract void disableSensor(SensorManager sensorManager);

    /**
     * Method that may be overridden by the concrete sensor class to destroy the sensor.  This
     * method will be called by HromatkaService when the service is being destroyed.  We don't want
     * to waste power by leaving sensors running when no one is listening
     *
     * @param sensorManager An instance of the Android SensorManager
     */
    public void destroySensor(SensorManager sensorManager) {
        listenerList.clear();
    }
}
