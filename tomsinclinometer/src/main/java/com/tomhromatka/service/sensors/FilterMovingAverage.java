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

import com.tomhromatka.service.HromatkaLog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FilterMovingAverage {
    private final String TAG = this.getClass().getSimpleName();
    public static final double SEC_TO_NANOSEC = 1e9;
    /* by default, expire samples after 1/2 of a second */
    public static final double DEFAULT_SAMPLE_EXPIRATION_NS = 0.5 * SEC_TO_NANOSEC;

    /** class that combines the timestamp and sensor data into one (convenient!) location */
    private static class TimestampAndData {
        private long timestamp;
        private float[] data;

        public TimestampAndData(long timestamp, float[] data) {
            this.timestamp = timestamp;
            this.data = data;
        }

        public long getTimestamp() {
            return this.timestamp;
        }

        public float[] getData() {
            return this.data;
        }
    }

    private List<TimestampAndData> timestampAndDataList = new ArrayList<>();
    private double samplesExpireAfterNanoseconds;

    public FilterMovingAverage(double samplesExpireAfterNanoseconds) {
        HromatkaLog.getInstance().enter(TAG);
        this.samplesExpireAfterNanoseconds = samplesExpireAfterNanoseconds;
        HromatkaLog.getInstance().exit(TAG);
    }

    /**
     * Returns the current moving average of the data stored by this filter
     * @return moving average
     */
    public synchronized float[] getMovingAverage() {
        HromatkaLog.getInstance().enter(TAG);
        /* get one sample to determine the length of the float array */
        TimestampAndData oneSample = timestampAndDataList.get(0);
        float[] averages = new float[oneSample.getData().length];

        /*
         * 1) zero out the averages
         */
        for(int index = 0; index < averages.length; index++) {
            averages[index] = 0.0f;
        }

        /*
         * 2) sum up all of the samples
         */
        for (Iterator<TimestampAndData> iterator = timestampAndDataList.iterator(); iterator.hasNext();) {
            TimestampAndData thisSample = iterator.next();

            for(int index = 0; index < averages.length; index++) {
                averages[index] += thisSample.getData()[index];
            }
        }

        /*
         * 3) divide by the number of samples to get the average
         */
        for(int index = 0; index < averages.length; index++) {
            averages[index] = averages[index] / timestampAndDataList.size();
        }

        HromatkaLog.getInstance().exit(TAG);
        return averages;
    }

    /**
     * insert a sample and its timestamp into the moving average filter
     * @param timestamp timestamp of the data
     * @param data      float[] containing the sample data
     */
    public synchronized void add(long timestamp, float[] data) {
        HromatkaLog.getInstance().enter(TAG);
        TimestampAndData newSample = new TimestampAndData(timestamp, data);
        timestampAndDataList.add(newSample);

        HromatkaLog.getInstance().logVerbose(TAG, "timestampAndDataList size = " + timestampAndDataList.size());
        HromatkaLog.getInstance().exit(TAG);
    }

    /**
     * clear the entire moving average filter
     */
    public synchronized void clear() {
        HromatkaLog.getInstance().enter(TAG);
        timestampAndDataList.clear();
        HromatkaLog.getInstance().exit(TAG);
    }

    /**
     * remove expired samples from the moving average filter.  The expiration duration was set
     * during construction of this class.
     */
    public synchronized void removeExpired() {
        HromatkaLog.getInstance().enter(TAG);

        /*
         * get the most recent sample's timestamp.  we will use this as the "current" time.  Not
         * perfect, but it's the only safe comparison for a sensor.  There's no guarantee that
         * the sensor's timestamp is comparable to System.nanoTime().
         */
        TimestampAndData mostCurrentSample = timestampAndDataList.get(timestampAndDataList.size() - 1);
        long currentTime = mostCurrentSample.getTimestamp();
        HromatkaLog.getInstance().logVerbose(TAG, "Current time: " + currentTime);

        for (Iterator<TimestampAndData> iterator = timestampAndDataList.iterator(); iterator.hasNext();) {
            TimestampAndData thisSample = iterator.next();

            if (thisSample.getTimestamp() < (currentTime - (long)samplesExpireAfterNanoseconds)) {
                /* this sample has expired.  remove it */
                HromatkaLog.getInstance().logVerbose(TAG, "Removing: " + thisSample.getTimestamp());
                iterator.remove();
            }
        }

        HromatkaLog.getInstance().logVerbose(TAG, "timestampAndDataList size = " + timestampAndDataList.size());
        HromatkaLog.getInstance().exit(TAG);
    }
}
