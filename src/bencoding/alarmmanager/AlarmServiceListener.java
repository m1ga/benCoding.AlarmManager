/**
 * benCoding.AlarmManager Project
 * Copyright (c) 2009-2012 by Ben Bahrenburg. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package bencoding.alarmmanager;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import org.appcelerator.titanium.TiApplication;

public class AlarmServiceListener extends BroadcastReceiver {

    private boolean isServiceRunning(Context context, String serviceName) {

        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
            if (serviceName.equals(service.service.getClassName())) return true;
        return false;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        utils.debugLog("In Alarm Service Listener");
        Bundle bundle = intent.getExtras();
        String fullServiceName = bundle.getString("alarm_service_name");
        boolean forceRestart = bundle.getBoolean("alarm_service_force_restart", false);
        boolean hasInterval = bundle.getBoolean("alarm_service_has_interval", false);

        utils.debugLog("Full Service Name: " + fullServiceName);
        if (this.isServiceRunning(context, fullServiceName)) {
            if (forceRestart) {
                utils.debugLog("Service is already running, we will stop it then restart");
                Intent tempIntent = new Intent();
                tempIntent.setClassName(TiApplication.getInstance().getApplicationContext(), fullServiceName);
                context.stopService(tempIntent);
                utils.debugLog("Service has been stopped");
            } else {
                utils.debugLog("Service is already running not need for us to start");
                return;
            }
        }

        Intent serviceIntent = new Intent();
        serviceIntent.setClassName(TiApplication.getInstance().getApplicationContext(), fullServiceName);


        if (hasInterval) {
            utils.debugLog("This an interval service");
            utils.debugLog("Is this an interval amount " + bundle.getLong("alarm_service_interval", 45 * 60 * 1000L));
            serviceIntent.putExtra("interval", bundle.getLong("alarm_service_interval", 45 * 60 * 1000L)); // Default to 45mins
        }
        serviceIntent.putExtra("customData", bundle.getString("customData", "[]"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
        utils.debugLog("Alarm Service Started");
    }
}
