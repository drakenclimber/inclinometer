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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.tomhromatka.service.HromatkaLog;
import com.tomhromatka.service.HromatkaServiceApi;

public class ActivityCalibrate extends AppCompatActivity implements HromatkaServiceBindApi {
    private final String TAG = this.getClass().getSimpleName();

    private HromatkaServiceManager hromatkaServiceManager = new HromatkaServiceManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HromatkaLog.getInstance().enter(TAG);

        /* bind to the service during onCreate().  Once we have successfully bound to the
         * service, we can then display the page
         */
        hromatkaServiceManager.bindServiceConnection(ActivityCalibrate.this, this);

        HromatkaLog.getInstance().exit(TAG);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HromatkaLog.getInstance().enter(TAG);
        PageCalibrate.getInstance().onDestroy(this, getHromatkaServiceApi());
        hromatkaServiceManager.unbindServiceConnection(ActivityCalibrate.this);
        HromatkaLog.getInstance().exit(TAG);
    }

    /**
     * Callback method that HromatkaServiceManager will call once this activity is bound to
     * HromatkaService
     */
    @Override
    public void onHromatkaServiceBind() {
        HromatkaLog.getInstance().enter(TAG);

        /* only set the content view if the calibrate page is inflated.  this is intended to
         * limit the number of calibration pages in the activity stack.
         *
         * TODO - it seems like this isn't sufficient in all cases.
         */
        if (!isCalibratePageInflated()) {
            setContentView(R.layout.page_calibrate);
        }
        PageCalibrate.getInstance().onCreate(this, getHromatkaServiceApi());
        HromatkaLog.getInstance().exit(TAG);
    }

    /**
     * Is the calibration page inflated?  i.e. is the calibration page currently in the activity
     * stack
     *
     * @return if the page is inflated (or not)
     */
    private boolean isCalibratePageInflated() {
        HromatkaLog.getInstance().enter(TAG);
        boolean isInflated = false;

        View calibrateView = findViewById(R.id.wCalibratePage);
        HromatkaLog.getInstance().logVerbose(TAG, "page calibrate id == " + calibrateView);
        ViewGroup rootView = (ViewGroup) findViewById(android.R.id.content);
        for (int i = 0; i < rootView.getChildCount(); i++) {
            HromatkaLog.getInstance().logVerbose(TAG, "child ID == " + rootView.getChildAt(i));
            if(rootView.getChildAt(i) == calibrateView) {
                isInflated = true;
            }
        }

        HromatkaLog.getInstance().exit(TAG);
        return isInflated;
    }

    private HromatkaServiceApi getHromatkaServiceApi() {
        return hromatkaServiceManager.getHromatkaServiceApi();
    }
}
