/**
 * benCoding.AlarmManager Project
 * Copyright (c) 2013 by Ben Bahrenburg. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package bencoding.alarmmanager;

import android.app.Activity;
import android.app.NotificationManager;

import androidx.core.app.NotificationCompat;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;

@Kroll.module(name = "Alarmmanager", id = "bencoding.alarmmanager")
public class AlarmmanagerModule extends KrollModule {
    public static final int DEFAULT_REQUEST_CODE = 192837;
    public static final String MODULE_FULL_NAME = "bencoding.AlarmManager";
    @Kroll.constant
    public static final int NOTIFICATION_IMPORTANCE_DEFAULT = NotificationManager.IMPORTANCE_DEFAULT;
    @Kroll.constant
    public static final int NOTIFICATION_IMPORTANCE_HIGH = NotificationManager.IMPORTANCE_HIGH;
    @Kroll.constant
    public static final int NOTIFICATION_IMPORTANCE_LOW = NotificationManager.IMPORTANCE_LOW;
    @Kroll.constant
    public static final int NOTIFICATION_IMPORTANCE_NONE = NotificationManager.IMPORTANCE_NONE;
    @Kroll.constant
    public static final int NOTIFICATION_IMPORTANCE_MIN = NotificationManager.IMPORTANCE_MIN;
    @Kroll.constant
    public static final int NOTIFICATION_IMPORTANCE_MAX = NotificationManager.IMPORTANCE_MAX;
    @Kroll.constant
    public static final int PRIORITY_HIGH = NotificationCompat.PRIORITY_HIGH;
    @Kroll.constant
    public static final int PRIORITY_LOW = NotificationCompat.PRIORITY_LOW;
    @Kroll.constant
    public static final int PRIORITY_DEFAULT = NotificationCompat.PRIORITY_DEFAULT;
    @Kroll.constant
    public static final int PRIORITY_MIN = NotificationCompat.PRIORITY_MIN;
    @Kroll.constant
    public static final int PRIORITY_MAX = NotificationCompat.PRIORITY_MAX;
    @Kroll.constant
    public static final int VISIBILITY_PRIVATE = NotificationCompat.VISIBILITY_PRIVATE;
    @Kroll.constant
    public static final int VISIBILITY_PUBLIC = NotificationCompat.VISIBILITY_PUBLIC;
    @Kroll.constant
    public static final int VISIBILITY_SECRET = NotificationCompat.VISIBILITY_SECRET;
    @Kroll.constant
    public static final int BADGE_ICON_SMALL = NotificationCompat.BADGE_ICON_SMALL;
    @Kroll.constant
    public static final int BADGE_ICON_NONE = NotificationCompat.BADGE_ICON_NONE;
    @Kroll.constant
    public static final int BADGE_ICON_LARGE = NotificationCompat.BADGE_ICON_LARGE;
    public static String rootActivityClassName = "";

    public AlarmmanagerModule() {
        super();
    }

    @Kroll.method
    public void disableLogging() {
        utils.setDebug(false);
    }

    @Kroll.method
    public void enableLogging() {
        utils.setDebug(true);
    }

    @Override
    public void onStart(Activity activity) {
        rootActivityClassName = TiApplication.getInstance().getApplicationContext().getPackageName() + "." + TiApplication.getAppRootOrCurrentActivity().getClass().getSimpleName();
        Log.d("bencoding.Alarmmanager", "==================\nonStart rootActivityClassName = " + rootActivityClassName);
        super.onStart(activity);
    }
}
