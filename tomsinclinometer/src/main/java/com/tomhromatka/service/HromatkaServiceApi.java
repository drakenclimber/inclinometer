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

import com.tomhromatka.service.sensors.SensorApi;

/**
 * Interface that HromatkaService implements.  Android activities can call these
 * methods to request the service to perform specific operations.
 */
public interface HromatkaServiceApi {
    /**
     * Method for Android activities to register an accelerometer listener
     *
     * @param callback The listener's callback class
     */
    void registerAccelerometerListener(SensorApi callback);

    /**
     * Method for Android activities to unregister an accelerometer listener
     *
     * @param callback The listener's callback class
     */
    void unregisterAccelerometerListener(SensorApi callback);

    /**
     * Method for Android activities to register an oriented accelerometer listener
     *
     * @param callback The listener's callback class
     */
    void registerOrientedAccelerometerListener(SensorApi callback);

    /**
     * Method for Android activities to unregister an oriented accelerometer listener
     *
     * @param callback The listener's callback class
     */
    void unregisterOrientedAccelerometerListener(SensorApi callback);

    /**
     * Method for Android activities to register an inclinometer listener
     *
     * @param callback The listener's callback class
     */
    void registerInclinometerListener(SensorApi callback);

    /**
     * Method for Android activities to unregister an inclinometer listener
     *
     * @param callback The listener's callback class
     */
    void unregisterInclinometerListener(SensorApi callback);

    /**
     * Method for an Android activity to request the inclinometer offsets to be updated.  This will
     * cause the inclinometer sensor to save the current oriented accelerometer values and subtract
     * them from future inclinometer measurements; thus allowing the device to be mounted at an
     * arbitrary angle in the vehicle.
     */
    void updateInclinometerOffsets();
}
