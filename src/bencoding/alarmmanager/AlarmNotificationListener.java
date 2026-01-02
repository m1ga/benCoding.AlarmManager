/**
 * benCoding.AlarmManager Project
 * Copyright (c) 2009-2012 by Ben Bahrenburg. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package bencoding.alarmmanager;

import android.R;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;

import org.appcelerator.titanium.TiApplication;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class AlarmNotificationListener extends BroadcastReceiver {
    public Context ctx = TiApplication.getInstance().getApplicationContext();

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = null;
        utils.debugLog(">>>>>>> In Alarm Notification Listener >>>>>>>>>>>");
        Bundle bundle = intent.getExtras();
        for (String key : bundle.keySet()) {
            if (bundle.containsKey(key) && bundle.get(key) != null) {
                try {
                    utils.debugLog(key + "=" + bundle.get(key));
                } catch (Exception ignore) {
                }
            }
        }
        if (!bundle.containsKey("notification_requestcode")) {
            utils.debugLog("notification_request_code is null or undefined => assume cancelled");
            return;
        }
        JSONArray actions = null;
        String actionsString = bundle.getString("notification_actions");
        try {
            if (actionsString != null) actions = new JSONArray(actionsString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Bitmap largeIcon = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            largeIcon = bundle.getParcelable("notification_largeIcon", Bitmap.class);
            utils.debugLog("largeIcon=" + largeIcon);
        } else {
            largeIcon = bundle.getParcelable("notification_largeIcon");
            utils.debugLog("largeIcon=" + largeIcon);
        }
        int requestCode = bundle.getInt("notification_requestcode", AlarmmanagerModule.DEFAULT_REQUEST_CODE);
        boolean badge = bundle.getBoolean("notification_badge");
        int importance = bundle.getInt("notification_importance");
        int priority = bundle.getInt("notification_priority");
        boolean ongoing = bundle.getBoolean("notification_ongoing");
        boolean onlyalertonce = bundle.getBoolean("notification_ongoing");
        boolean autocancel = bundle.getBoolean("notification_autocancel");
        long when = bundle.getLong("notification_when");
        long timeoutAfter = bundle.getLong("notification_timeoutAfter");
        int visibility = bundle.getInt("notification_visibility");
        int number = bundle.getInt("notification_notification_number");
        int badgeIconType = bundle.getInt("notification_badgeIconType");
        utils.debugLog("onReceive::requestCode is " + requestCode);
        String contentTitle = bundle.getString("notification_title");
        String group = bundle.getString("notification_group");
        String contentText = bundle.getString("notification_msg");
        String className = bundle.getString("notification_root_classname");
        boolean hasIcon = bundle.getBoolean("notification_has_icon", true);
        int icon = R.drawable.stat_notify_more;
        if (hasIcon) {
            icon = bundle.getInt("notification_icon", R.drawable.stat_notify_more);
            utils.debugLog("User provided an icon of " + icon);
        } else {
            utils.debugLog("No icon provided, default will be used");
        }
        String soundPath = bundle.getString("notification_sound");
        boolean hasCustomSound = !utils.isEmptyString(soundPath);
        // Add default notification flags
        boolean playSound = bundle.getBoolean("notification_play_sound", false);
        if (playSound) {
            utils.debugLog("On notification play sound");
        }
        boolean doVibrate = bundle.getBoolean("notification_vibrate", false);
        if (doVibrate) {
            utils.debugLog("On notification vibrate");
        }
        boolean showLights = bundle.getBoolean("notification_show_lights", false);
        if (showLights) {
            utils.debugLog("On notification show lights");
        }
        String channelName = bundle.getString("notification_channel_name", "notification");

        notificationManager = (NotificationManager) TiApplication.getInstance().getSystemService(TiApplication.NOTIFICATION_SERVICE);
        Intent notifyIntent = createIntent(className);
        notifyIntent.putExtra("requestCode", requestCode);
        String customData = bundle.getString("customData");
        if (!utils.isEmptyString(customData)) {
            notifyIntent.putExtra("customData", customData);
        }
        /* sender opens activity and restart app*/
        PendingIntent sender = PendingIntent.getActivity(ctx, requestCode, notifyIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT | Notification.FLAG_AUTO_CANCEL);
        String channelId = "default";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            if (playSound) {
                // IMPORTANCE_DEFAULT has by default sound so we only have to set custom sound
                if (hasCustomSound) {
                    AudioAttributes.Builder attrs = new AudioAttributes.Builder();
                    attrs.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION);
                    attrs.setUsage(AudioAttributes.USAGE_NOTIFICATION);
                    channel.setSound(Uri.parse(soundPath), attrs.build());
                }
            } else {
                channel.setSound(null, null);
            }
            channel.enableLights(showLights);
            if (doVibrate) {
                channel.enableVibration(doVibrate);
            } else {
                // Bug - see: https://stackoverflow.com/a/47646166/1294832
                channel.setVibrationPattern(new long[]{0});
                channel.enableVibration(true);
            }
            channel.setShowBadge(badge);
            channel.setLockscreenVisibility(visibility);
            notificationManager.createNotificationChannel(channel);
        } // end of OREO work
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ctx, channelId);
        notificationBuilder//
                .setWhen(when) //
                .setContentText(contentText) //
                .setContentTitle(contentTitle) //
                .setSmallIcon(icon) //
                .setOngoing(ongoing).setGroup(group)//
                .setAutoCancel(true) //
                .setTicker(contentTitle)//
                .setContentIntent(sender) //
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))//
                .setOnlyAlertOnce(onlyalertonce)//
                .setAutoCancel(autocancel)//
                .setBadgeIconType(badgeIconType)//
                .setPriority(priority)//
                .setVisibility(visibility);
        if (actions != null) {
            for (int i = 0; i < actions.length(); i++) {
                try {
                    JSONObject a = actions.getJSONObject(i);
                    utils.debugLog(a.toString());
                    String label = a.getString("label");
                    int actionicon = a.getInt("icon");
                    notificationBuilder.addAction(actionicon, label, createPendingIntent(a, className, requestCode));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        if (largeIcon != null) {
            notificationBuilder.setLargeIcon(largeIcon);
        } else {
            utils.debugLog("largeIcon was null ");
        }
        if (number != -1) {
            notificationBuilder.setNumber(number);
        }
        if (timeoutAfter != -1) {
            notificationBuilder.setTimeoutAfter(timeoutAfter * 1000);
        }
        utils.debugLog("setting notification flags in package");
        notificationBuilder = createNotifyFlags(notificationBuilder, playSound, hasCustomSound, soundPath, doVibrate, showLights);

        notificationManager.notify(requestCode, notificationBuilder.build());
        utils.debugLog("You should now see a notification");

        if (bundle.getLong("notification_repeat_ms", 0) > 0) {
            createRepeatNotification(bundle);
        }

    }

    @SuppressLint("MissingPermission")
    private void createRepeatNotification(Bundle bundle) {
        Intent intent = new Intent(ctx, AlarmNotificationListener.class);
        // Use the same extras as the original notification
        // Update date and time by repeat interval (in milliseconds)
        int day = bundle.getInt("notification_day");
        int month = bundle.getInt("notification_month");
        int year = bundle.getInt("notification_year");
        int hour = bundle.getInt("notification_hour");
        int minute = bundle.getInt("notification_minute");
        int second = bundle.getInt("notification_second");

        Calendar cal = new GregorianCalendar(year, month, day);
        cal.add(Calendar.HOUR_OF_DAY, hour);
        cal.add(Calendar.MINUTE, minute);
        cal.add(Calendar.SECOND, second);

        Calendar now = Calendar.getInstance();
        long repeat_ms = bundle.getLong("notification_repeat_ms", 0);
        if (repeat_ms == 0) {
            utils.debugLog("repeat_ms is 0, no good!");
            // Else we can end into an infinite loop below.
            return;
        }
        int repeat_s = (int) repeat_ms / 1000;

        // Add frequence until cal > now
        while (now.getTimeInMillis() > cal.getTimeInMillis()) {
            cal.add(Calendar.SECOND, repeat_s);
            utils.debugLog("Add second");
        }

        utils.debugLog("Update bundle with new calendar: " + cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.MONTH) + "-" + cal.get(Calendar.DAY_OF_MONTH) + " " + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND));
        bundle.putInt("notification_year", cal.get(Calendar.YEAR));
        bundle.putInt("notification_month", cal.get(Calendar.MONTH));
        bundle.putInt("notification_day", cal.get(Calendar.DAY_OF_MONTH));
        bundle.putInt("notification_hour", cal.get(Calendar.HOUR_OF_DAY));
        bundle.putInt("notification_minute", cal.get(Calendar.MINUTE));
        bundle.putInt("notification_second", cal.get(Calendar.SECOND));

        // Update intent with this updated bundle.
        intent.putExtras(bundle);

        int requestCode = bundle.getInt("notification_request_code", AlarmmanagerModule.DEFAULT_REQUEST_CODE);
        intent.setData(Uri.parse("alarmId://" + requestCode));
        long ms = cal.getTimeInMillis();

        Date date = new Date(ms);
        String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        utils.debugLog("Creating Alarm Notification repeat for: " + sdf.format(date));

        // Create the Alarm Manager
        AlarmManager am = (AlarmManager) ctx.getSystemService(TiApplication.ALARM_SERVICE);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        PendingIntent sender = PendingIntent.getBroadcast(TiApplication.getInstance().getApplicationContext(), requestCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        am.setExact(AlarmManager.RTC_WAKEUP, ms, sender);

    }

    private NotificationCompat.Builder createNotifyFlags(NotificationCompat.Builder notification, boolean playSound, boolean hasCustomSound, String soundPath, boolean doVibrate, boolean showLights) {
        if (playSound && !hasCustomSound && doVibrate && showLights) {
            notification.setDefaults(Notification.DEFAULT_ALL);
        } else {
            int defaults = 0;
            if (showLights) {
                defaults = defaults | Notification.DEFAULT_LIGHTS;
                notification.setLights(0xFF0000FF, 200, 3000);
            }
            // DEFAULT_SOUND overrides custom sound, so never set both
            if (playSound) {
                if (hasCustomSound) {
                    // if >= 26, we set it on the channel.
                    // if (android.os.Build.VERSION.SDK_INT < 26) {
                    notification.setSound(Uri.parse(soundPath));
                    // }
                } else {
                    defaults = defaults | Notification.DEFAULT_SOUND;
                }
            }
            if (doVibrate) {
                defaults = defaults | Notification.DEFAULT_VIBRATE;
            }
            notification.setDefaults(defaults);
        }
        return notification;
    }

    private PendingIntent createPendingIntent(JSONObject action, String className, int requestCode) {
        Intent intent = createIntent(className);

        try {
            intent.setAction(action.getString("actionname"));
            intent.putExtra("extradata", action.getString("extradata"));
            intent.putExtra("requestcode", requestCode);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        intent.putExtra("requestCode", requestCode);
        return PendingIntent.getActivity(ctx, requestCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT | Notification.FLAG_AUTO_CANCEL);
    }

    private Intent createIntent(String className) {
        try {
            if (utils.isEmptyString(className)) {
                utils.debugLog("[AlarmManager] Using application Start Activity");
                Intent iStartActivity = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
                iStartActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                iStartActivity.addCategory(Intent.CATEGORY_LAUNCHER);
                iStartActivity.setAction(Intent.ACTION_MAIN);
                return iStartActivity;
            } else {
                utils.debugLog("[AlarmManager] Trying to get a class for name '" + className + "'");
                @SuppressWarnings("rawtypes") Class intentClass = Class.forName(className);
                Intent intentFromClass = new Intent(ctx, intentClass);
                // Add the flags needed to restart
                intentFromClass.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intentFromClass.addCategory(Intent.CATEGORY_LAUNCHER);
                intentFromClass.setAction(Intent.ACTION_MAIN);
                return intentFromClass;
            }
        } catch (ClassNotFoundException e) {
            utils.errorLog(e);
            return null;
        }
    }

    public class NotificationServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context ctx, Intent intent) {

        }
    }
}
