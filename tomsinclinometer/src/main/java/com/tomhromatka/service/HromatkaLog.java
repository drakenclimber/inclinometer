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

import android.util.Log;

import com.tomhromatka.tomsinclinometer.BuildConfig;

public class HromatkaLog {
    private static HromatkaLog instance = null;

    /**
     * Use logging judiciously.  On my test device (LG Volt), I saw the following using the CPU
     * monitor in ADB/Android Studio
     *
     *                              CPU User %         CPU Kernel %
     * With Logging Enabled         20%                5%
     * With Logging Disabled        <2%                0%
     */
    private static boolean enableLogging = false;

    /**
     * Constructor - note this will force the class to be a singleton
     */
    protected HromatkaLog() {
        if (BuildConfig.DEBUG) {
            enableLogging = true;
        }
    }

    /**
     * Public constructor.  Returns the instance of this singleton class.  This method will
     * create the instance if it doesn't exist.
     *
     * @return the instance of this class
     */
    public static HromatkaLog getInstance() {
        if (null == instance) {
            instance = new HromatkaLog();
        }

        return instance;
    }

    /**
     * Log method to call when entering a method
     *
     * @param tag Android class tag
     */
    public void enter(String tag) {
        if (enableLogging) {
            Log.v(tag, "Entering " + getCallerMethodName());
        }
    }

    /**
     * Log method to call when exiting a method
     *
     * @param tag Android class tag
     */
    public void exit(String tag) {
        if (enableLogging) {
            Log.v(tag, "Exiting " + getCallerMethodName());
        }
    }

    /**
     * Log method to call when logging error messages
     *
     * @param tag     Android class tag
     * @param message Log string
     */
    public void logError(String tag, String message) {
        Log.e(tag, message);
    }

    /**
     * Log method to call when logging verbose messages
     *
     * @param tag     Android class tag
     * @param message Log string
     */
    public void logVerbose(String tag, String message) {
        if (enableLogging) {
            Log.v(tag, message);
        }
    }

    /**
     * Method to get the name of the calling method
     *
     * @return String containing method name
     */
    private String getCallerMethodName() {
        /*
         * Stack trace:
         * 0 == getThreadStackTrace()
         * 1 == getStackTrace()
         * 2 == getCallerMethodName()
         * 3 == enter() || exit()
         * 4 == name of the method that invoked this log
         */
        return Thread.currentThread().getStackTrace()[4].getMethodName();
    }
}
