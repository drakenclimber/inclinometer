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

/**
 * Listeners (Visitors) that are interested in listening to a particular sensor will need to
 * implement this interface.  The sensor will then call the methods below upon an event (new
 * data, accuracy changed, etc.).
 */
public interface SensorApi {
    /**
     * The sensor will notify its listeners of new data via this method.  The length and
     * meaning of the values[] array is sensor-dependent.  See that sensor's Javadoc for the
     * meaning of each index in the array.
     * i
     * @param timestamp time at which this measurement occurred
     * @param values    measured sensor values
     */
    void onDataReceived(
            long            timestamp,
            float[]         values);

    /**
     * The sensor will notify its listeners of accuracy changes via this method
     *
     * @param accuracy new accuracy of this sensor
     */
    void onAccuracyChanged(int accuracy);
}
