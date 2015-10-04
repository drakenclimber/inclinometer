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

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.tomhromatka.service.HromatkaLog;
import com.tomhromatka.service.HromatkaServiceApi;

public class ActivityInclinometer extends AppCompatActivity implements HromatkaServiceBindApi {
    private final String TAG = this.getClass().getSimpleName();
    private HromatkaServiceManager hromatkaServiceManager = new HromatkaServiceManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HromatkaLog.getInstance().enter(TAG);

        /* bind to the service during onCreate().  Once we have successfully bound to the
         * service, we can then display the page
         */
        hromatkaServiceManager.bindServiceConnection(ActivityInclinometer.this, this);

        HromatkaLog.getInstance().exit(TAG);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HromatkaLog.getInstance().enter(TAG);
        PageInclinometer.getInstance().onDestroy(this, getHromatkaServiceApi());
        hromatkaServiceManager.unbindServiceConnection(ActivityInclinometer.this);
        HromatkaLog.getInstance().exit(TAG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        HromatkaLog.getInstance().enter(TAG);
        getMenuInflater().inflate(R.menu.menu_inclinometer, menu);
        HromatkaLog.getInstance().exit(TAG);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        HromatkaLog.getInstance().enter(TAG);
        int id = item.getItemId();

        switch(id) {
            case R.id.action_calibrate:
                Intent intent = new Intent(this, ActivityCalibrate.class);
                this.startActivity(intent);
                break;

            default:
                throw new AssertionError("Unhandled option: " + id);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Callback method that HromatkaServiceManager will call once this activity is bound to
     * HromatkaService
     */
    @Override
    public void onHromatkaServiceBind() {
        HromatkaLog.getInstance().enter(TAG);
        setContentView(R.layout.page_inclinometer);
        PageInclinometer.getInstance().onCreate(this, getHromatkaServiceApi());

        Intent intent = new Intent(this, ActivityCalibrate.class);
        this.startActivity(intent);

        HromatkaLog.getInstance().exit(TAG);
    }

    private HromatkaServiceApi getHromatkaServiceApi() {
        return hromatkaServiceManager.getHromatkaServiceApi();
    }
}
