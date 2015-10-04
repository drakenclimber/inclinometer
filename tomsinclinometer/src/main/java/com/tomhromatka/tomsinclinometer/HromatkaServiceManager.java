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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.tomhromatka.service.HromatkaLog;
import com.tomhromatka.service.HromatkaService;
import com.tomhromatka.service.HromatkaServiceApi;

public class HromatkaServiceManager {
    private final String TAG = this.getClass().getSimpleName();
    private HromatkaServiceBindApi callback = null;
    private HromatkaServiceApi hromatkaServiceApi = null;

    /**
     * method to bind the calling activity to HromatkaService
     * @param context        Android context of the calling activity
     * @param serviceBindApi Class to callback when the service is bound
     */
    public void bindServiceConnection(Context context, HromatkaServiceBindApi serviceBindApi) {
        HromatkaLog.getInstance().enter(TAG);
        callback = serviceBindApi;

        Intent intent = new Intent(context, HromatkaService.class);
        context.bindService(intent, hromatkaServiceConnection, Context.BIND_AUTO_CREATE);
        HromatkaLog.getInstance().exit(TAG);
    }

    /**
     * Unbind this context from HromatkaService
     * @param context Android context of the calling activity
     */
    public void unbindServiceConnection(Context context) {
        try {
            context.unbindService(hromatkaServiceConnection);
        }
        catch (IllegalArgumentException iae) {
            HromatkaLog.getInstance().logError(TAG, "Failed to unbind from service: " + iae.getLocalizedMessage());
        }
    }

    public HromatkaServiceApi getHromatkaServiceApi() {
        HromatkaLog.getInstance().logVerbose(TAG, "hromatkaServiceApi = " + hromatkaServiceApi);
        return hromatkaServiceApi;
    }

    /**
     * Inner class that Android will notify when the requesting activity has been bound to the
     * service
     */
    private ServiceConnection hromatkaServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            HromatkaLog.getInstance().enter(TAG);
            hromatkaServiceApi = (HromatkaServiceApi) binder;

            /* notify the callback class that we are now bound to the service */
            callback.onHromatkaServiceBind();
            HromatkaLog.getInstance().exit(TAG);
        }

        public void onServiceDisconnected(ComponentName className) {
            HromatkaLog.getInstance().enter(TAG);
            hromatkaServiceApi = null;
            HromatkaLog.getInstance().exit(TAG);
        }
    };
}
