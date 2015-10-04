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

package com.tomhromatka.tomsinclinometer;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.tomhromatka.service.HromatkaLog;
import com.tomhromatka.service.HromatkaServiceApi;
import com.tomhromatka.service.sensors.SensorApi;

public class PageCalibrate implements PageApi {
    private final String TAG = this.getClass().getSimpleName();

    private Button wSetOffsetsButton = null;
    private InclinometerListener inclinometerListener = new InclinometerListener();

    private static PageCalibrate instance = null;

    /**
     * Constructor - note this will force the class to be a singleton
     */
    protected PageCalibrate() {
    }

    /**
     * Public constructor.  Returns the instance of this singleton class.  This method will
     * create the instance if it doesn't exist.
     *
     * @return the instance of this class
     */
    public static PageCalibrate getInstance() {
        if (null == instance) {
            instance = new PageCalibrate();
        }

        return instance;
    }

    /** We need to implement an inclinometer listener here so that the accelerometer
     * sensor is running.  This will allow us to compute the average acclerometer offset.
     */
    private static class InclinometerListener implements SensorApi {
        private final String TAG = this.getClass().getSimpleName();

        @Override
        public void onDataReceived(long timestamp, float[] values) {
            HromatkaLog.getInstance().enter(TAG);
            HromatkaLog.getInstance().exit(TAG);
        }

        @Override
        public void onAccuracyChanged(int accuracy) {
            HromatkaLog.getInstance().enter(TAG);
            HromatkaLog.getInstance().exit(TAG);
        }
    }

    public void onCreate(final Activity activity, final HromatkaServiceApi hromatkaServiceApi) {
        HromatkaLog.getInstance().enter(TAG);
        wSetOffsetsButton = (Button) activity.findViewById(R.id.wSetOffsetsButton);
        wSetOffsetsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                HromatkaLog.getInstance().logVerbose(TAG, "Set offsets button pressed.");
                hromatkaServiceApi.updateInclinometerOffsets();

                Toast.makeText(
                        activity,
                        activity.getString(R.string.toast_calibration_complete),
                        Toast.LENGTH_SHORT).show();
                activity.finish();
            }
        });

        hromatkaServiceApi.registerInclinometerListener(inclinometerListener);
        HromatkaLog.getInstance().exit(TAG);
    }

    @Override
    public void onDestroy(Activity activity, HromatkaServiceApi hromatkaServiceApi) {
        HromatkaLog.getInstance().enter(TAG);
        hromatkaServiceApi.unregisterInclinometerListener(inclinometerListener);
        HromatkaLog.getInstance().exit(TAG);
    }
}
