/**
 * benCoding.AlarmManager Project
 * Copyright (c) 2009-2012 by Ben Bahrenburg. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package bencoding.alarmmanager;

import java.util.ArrayList;
import java.util.Calendar;

import ti.modules.titanium.filesystem.FileProxy;

import java.util.GregorianCalendar;
import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiFileProxy;
import org.appcelerator.titanium.io.TiBaseFile;
import org.appcelerator.titanium.io.TiFile;
import org.appcelerator.titanium.io.TiFileFactory;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.core.app.NotificationCompat;

import android.os.Build;
import android.util.Log;

import bencoding.alarmmanager.AlarmmanagerModule;

@Kroll.proxy(creatableInModule = AlarmmanagerModule.class)
public class AlarmManagerProxy extends KrollProxy {
    public static String LCAT = "AlarmManager";
    public Context ctx = TiApplication.getInstance().getApplicationContext();

    public AlarmManagerProxy() {
        super();
    }

    private static boolean isInteger(Object object) {
        if (object instanceof Integer) {
            return true;
        } else {
            String string = object.toString();

            try {
                Integer.parseInt(string);
            } catch (Exception e) {
                return false;
            }
        }

        return true;
    }

    private Calendar getSecondBasedCalendar(KrollDict args) {
        int interval = args.getInt("second");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, interval);
        return cal;
    }

    private Calendar getMinuteBasedCalendar(KrollDict args) {
        int interval = args.getInt("minute");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, interval);
        return cal;
    }

    private Calendar getFullCalendar(KrollDict args) {
        Calendar defaultDay = Calendar.getInstance();
        int day = args.optInt("day", defaultDay.get(Calendar.DAY_OF_MONTH));
        int month = args.optInt("month", defaultDay.get(Calendar.MONTH));
        int year = args.optInt("year", defaultDay.get(Calendar.YEAR));
        int hour = args.optInt("hour", defaultDay.get(Calendar.HOUR_OF_DAY));
        int minute = args.optInt("minute", defaultDay.get(Calendar.MINUTE));
        int second = args.optInt("second", defaultDay.get(Calendar.SECOND));
        Calendar cal = new GregorianCalendar(year, month, day);
        cal.add(Calendar.HOUR_OF_DAY, hour);
        cal.add(Calendar.MINUTE, minute);
        cal.add(Calendar.SECOND, second);
        return cal;
    }

    private Intent createAlarmNotifyIntent(KrollDict args, int requestCode) {
        utils.debugLog("createAlarmNotifyIntent::requestCode=" + requestCode);
        int notificationIcon = 0;
        String contentTitle = "";
        String contentText = "";
        String notificationSound = "";
        String group = "defaultgroup";
        String channelName = "notification";
        int priority = NotificationCompat.PRIORITY_MAX;
        int visibility = NotificationCompat.VISIBILITY_PUBLIC;
        long timeoutAfter = -1;
        int importance = NotificationManager.IMPORTANCE_HIGH;
        long when = System.currentTimeMillis();
        int number = -1;
        int badgeIconType = NotificationCompat.BADGE_ICON_NONE;
        boolean showWhen = true;
        boolean badge = true;

        if (args.containsKeyAndNotNull("priority")) {
            priority = args.getInt("priority");
        }
        if (args.containsKeyAndNotNull("timeoutAfter")) {
            timeoutAfter = args.getInt("timeoutAfter");
        }
        if (args.containsKeyAndNotNull("visibility")) {
            visibility = args.getInt("visibility");
        }
        if (args.containsKeyAndNotNull("importance")) {
            importance = args.getInt("importance");
        }
        if (args.containsKeyAndNotNull("when")) {
            when = 1000 * args.getInt("when") + System.currentTimeMillis();
        }
        if (args.containsKeyAndNotNull("group")) {
            group = args.getString("group");
        }
        if (args.containsKeyAndNotNull("badgeIconType")) {
            badgeIconType = args.getInt("badgeIconType");
        }

        if (args.containsKeyAndNotNull("badge")) {
            badge = args.getBoolean("badge");
        }

        boolean ongoing = optionIsEnabled(args, "ongoing");
        boolean playSound = optionIsEnabled(args, "playSound");
        boolean doVibrate = optionIsEnabled(args, "vibrate");
        boolean showLights = optionIsEnabled(args, "showLights");
        boolean onlyalertonce = optionIsEnabled(args, "onlyAlertOnce");
        boolean autocancel = optionIsEnabled(args, "autocancel");
        if (args.containsKeyAndNotNull(TiC.PROPERTY_CONTENT_TITLE)
                || args.containsKeyAndNotNull(TiC.PROPERTY_CONTENT_TEXT)) {
            if (args.containsKeyAndNotNull(TiC.PROPERTY_CONTENT_TITLE)) {
                contentTitle = TiConvert.toString(args, TiC.PROPERTY_CONTENT_TITLE);
            }
            if (args.containsKeyAndNotNull(TiC.PROPERTY_CONTENT_TEXT)) {
                contentText = TiConvert.toString(args, TiC.PROPERTY_CONTENT_TEXT);
            }
        }
        if (args.containsKey("channelName")) {
            channelName = TiConvert.toString(args, "channelName");
        }

        if (args.containsKey(TiC.PROPERTY_ICON)) {
            Object icon = args.get(TiC.PROPERTY_ICON);
            if (icon instanceof Number) {
                notificationIcon = ((Number) icon).intValue();
            } else {
                String iconUrl = TiConvert.toString(icon);
                String iconFullUrl = resolveUrl(null, iconUrl);
                notificationIcon = TiUIHelper.getResourceId(iconFullUrl);
                if (notificationIcon == 0) {
                    utils.debugLog("No image found for " + iconUrl);
                    utils.debugLog("Default icon will be used");
                }
            }
        }

        if (args.containsKey(TiC.PROPERTY_SOUND)) {
            notificationSound = resolveUrl(null, TiConvert.toString(args, TiC.PROPERTY_SOUND));
        }

        Intent intent = new Intent(ctx, AlarmNotificationListener.class);
        // Add some extra information so when the alarm goes off we have enough to
        // create the notification
        if (args.containsKey("largeIcon")) {
            intent.putExtra("notification_largeIcon", readFilenameFromObject(args.get("largeIcon")));
        }
        intent.putExtra("notification_title", contentTitle);
        intent.putExtra("notification_group", group);
        intent.putExtra("notification_msg", contentText);
        intent.putExtra("notification_has_icon", (notificationIcon != 0));
        intent.putExtra("notification_icon", notificationIcon);
        intent.putExtra("notification_sound", notificationSound);
        intent.putExtra("notification_ongoing", ongoing);
        intent.putExtra("notification_play_sound", playSound);
        intent.putExtra("notification_vibrate", doVibrate);
        intent.putExtra("notification_show_lights", showLights);
        intent.putExtra("notification_requestcode", requestCode);
        intent.putExtra("notification_request_code", requestCode);
        intent.putExtra("notification_root_classname", AlarmmanagerModule.rootActivityClassName);
        intent.putExtra("notification_channel_name", channelName);
        intent.putExtra("notification_when", when);
        intent.putExtra("notification_timeoutAfter", timeoutAfter);
        intent.putExtra("notification_badge", badge);
        intent.putExtra("notification_importance", importance);
        intent.putExtra("notification_priority", priority);
        intent.putExtra("notification_visibility", visibility);
        intent.putExtra("notification_onlyalertonce", onlyalertonce);
        intent.putExtra("notification_autocancel", autocancel);
        intent.putExtra("notification_number", number);
        intent.putExtra("notification_badgeIconType", badgeIconType);
        try {
            JSONArray actions = getActions(args.get("actions"));
            if (actions != null)
                intent.putExtra("notification_actions", actions.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // As of API 19 setRepeating == setInexactRepeating, see also:
        // http://developer.android.com/reference/android/app/AlarmManager.html#setRepeating(int,
        // long, long, android.app.PendingIntent)
        if (android.os.Build.VERSION.SDK_INT >= 19 && hasRepeating(args)) {
            intent.putExtra("notification_repeat_ms", repeatingFrequency(args));
            Calendar cal = getFullCalendar(args);
            intent.putExtra("notification_year", cal.get(Calendar.YEAR));
            intent.putExtra("notification_month", cal.get(Calendar.MONTH));
            intent.putExtra("notification_day", cal.get(Calendar.DAY_OF_MONTH));
            intent.putExtra("notification_hour", cal.get(Calendar.HOUR_OF_DAY));
            intent.putExtra("notification_minute", cal.get(Calendar.MINUTE));
            intent.putExtra("notification_second", cal.get(Calendar.SECOND));
        }

        if ((args.containsKeyAndNotNull("customData"))) {
            String customData = (String) args.get("customData");
            intent.putExtra("customData", customData);
        }

        intent.setData(Uri.parse("alarmId://" + requestCode));
        return intent;
    }

    @Kroll.method
    public String findStartActivityName() {
        return ctx.getPackageManager()
                .getLaunchIntentForPackage(ctx.getPackageName())
                .getClass().getName();
    }

    @Kroll.method
    public void cancelAlarmNotification(@Kroll.argument(optional = true) Object requestCode) {
        // To cancel an alarm the signature needs to be the same as the submitting one.
        utils.debugLog("Cancelling Alarm Notification");
        // Set the default request code
        int intentRequestCode = AlarmmanagerModule.DEFAULT_REQUEST_CODE;
        // If the optional code was provided, cast accordingly
        if (requestCode != null) {
            if (requestCode instanceof Number) {
                intentRequestCode = ((Number) requestCode).intValue();
            }
        }

        utils.debugLog(String.format("Cancelling requestCode = {%d}", intentRequestCode));

        // Create a placeholder for the args value
        HashMap<String, Object> placeholder = new HashMap<String, Object>(0);
        KrollDict args = new KrollDict(placeholder);

        // Create the Alarm Manager
        AlarmManager am = (AlarmManager) ctx.getSystemService(TiApplication.ALARM_SERVICE);
        Intent intent = createAlarmNotifyIntent(args, intentRequestCode);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        PendingIntent sender = PendingIntent.getBroadcast(ctx,
                intentRequestCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        am.cancel(sender);

        // Added this due to "alarmIsActivated" method, now it runs as expected
        sender.cancel();

        utils.infoLog("Alarm Notification with requestCode " + intentRequestCode + " cancelled.");

        utils.debugLog("Alarm Notification Canceled");
    }

    @Kroll.method
    public boolean alarmIsActivated(@Kroll.argument(optional = true) Object requestCode) {
        // Set the default request code
        int intentRequestCode = AlarmmanagerModule.DEFAULT_REQUEST_CODE;
        // If the optional code was provided, cast accordingly
        if (requestCode != null) {
            if (requestCode instanceof Number) {
                intentRequestCode = ((Number) requestCode).intValue();
            }
        }

        // Create a placeholder for the args value
        HashMap<String, Object> placeholder = new HashMap<String, Object>(0);
        KrollDict args = new KrollDict(placeholder);

        // Create the Alarm Manager
        AlarmManager am = (AlarmManager) ctx.getSystemService(TiApplication.ALARM_SERVICE);
        Intent intent = createAlarmNotifyIntent(args, intentRequestCode);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);

        boolean alarmUp = (PendingIntent.getBroadcast(ctx, intentRequestCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE) != null);

        if (alarmUp) {
            utils.infoLog("Alarm with requestCode " + intentRequestCode + " is activated.");
            return true;
        } else {
            utils.infoLog("Alarm with requestCode " + intentRequestCode + " is not activated.");
            return false;
        }
    }

    private boolean optionIsEnabled(KrollDict args, String paramName) {
        if (args.containsKeyAndNotNull(paramName)) {
            Object value = args.get(paramName);
            return TiConvert.toBoolean(value);
        } else {
            return false;
        }
    }

    private boolean hasRepeating(KrollDict args) {
        boolean results = (args.containsKeyAndNotNull("repeat"));
        utils.debugLog("Repeat Frequency enabled: " + results);
        return results;
    }

    private long repeatingFrequency(KrollDict args) {
        long freqResults = utils.DAILY_MILLISECONDS;
        Object repeat = args.get("repeat");
        if (repeat instanceof Number) {
            utils.debugLog("Repeat value provided in milliseconds found");
            freqResults = ((Number) repeat).longValue();
        } else {
            String repeatValue = TiConvert.toString(repeat);
            utils.debugLog("Repeat value of " + repeatValue + " found");
            if (repeatValue.equalsIgnoreCase("HOURLY")) {
                freqResults = utils.HOURLY_MILLISECONDS;
            }
            if (repeatValue.equalsIgnoreCase("WEEKLY")) {
                freqResults = utils.WEEKLY_MILLISECONDS;
            }
            if (repeatValue.equalsIgnoreCase("MONTHLY")) {
                freqResults = utils.MONTHLY_MILLISECONDS;
            }
            if (repeatValue.equalsIgnoreCase("YEARLY")) {
                freqResults = utils.YEARLY_MILLISECONDS;
            }
        }
        utils.debugLog("Repeat Frequency in milliseconds is " + freqResults);
        return freqResults;
    }

    @Kroll.method
    public void addAlarmNotification(@SuppressWarnings("rawtypes") HashMap hm) {
        @SuppressWarnings("unchecked")
        KrollDict args = new KrollDict(hm);

        if (!args.containsKeyAndNotNull(TiC.PROPERTY_CONTENT_TITLE)) {
            throw new IllegalArgumentException("The context title field (contentTitle) is required");
        }
        if (!args.containsKeyAndNotNull(TiC.PROPERTY_CONTENT_TEXT)) {
            throw new IllegalArgumentException("The context text field (contentText) is required");
        }
        Calendar calendar = null;
        boolean isRepeating = hasRepeating(args);
        long repeatingFrequency = 0;
        if (isRepeating) {
            repeatingFrequency = repeatingFrequency(args);
        }

        // If seconds are provided but not years, we just take the seconds to mean to
        // add seconds until fire
        boolean secondBased = (args.containsKeyAndNotNull("second") && !args.containsKeyAndNotNull("year"));

        // If minutes are provided but not years, we just take the minutes to mean to
        // add minutes until fire
        boolean minuteBased = (args.containsKeyAndNotNull("minute") && !args.containsKeyAndNotNull("year"));

        // Based on what kind of duration we build our calendar
        if (secondBased) {
            calendar = getSecondBasedCalendar(args);
        } else if (minuteBased) {
            calendar = getMinuteBasedCalendar(args);
        } else {
            calendar = getFullCalendar(args);
        }

        // Get the requestCode if provided, if none provided
        // we use 192837 for backwards compatibility
        int requestCode = args.optInt("requestCode", AlarmmanagerModule.DEFAULT_REQUEST_CODE);
        utils.debugLog("addAlarmNotification::requestCode=" + requestCode);
        String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        utils.debugLog("Creating Alarm Notification for: " + sdf.format(calendar.getTime()));

        // Create the Alarm Manager
        AlarmManager am = (AlarmManager) ctx.getSystemService(TiApplication.ALARM_SERVICE);
        Intent intent = createAlarmNotifyIntent(args, requestCode);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        PendingIntent sender = PendingIntent.getBroadcast(ctx,
                requestCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        if (isRepeating && !intent.hasExtra("notification_repeat_ms")) {
            utils.debugLog("Setting Alarm to repeat");
            am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), repeatingFrequency, sender);
        } else {
            utils.debugLog("Setting Alarm for a single run");
            am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
        }

        utils.debugLog("Alarm Notification Created .....");
    }

    private Intent createAlarmServiceIntent(KrollDict args) {
        String serviceName = args.getString("service");
        Intent intent = new Intent(ctx, AlarmServiceListener.class);
        intent.putExtra("alarm_service_name", serviceName);
        // Pass in flag if we need to restart the service on each call
        intent.putExtra("alarm_service_force_restart", (optionIsEnabled(args, "forceRestart")));
        // Check if the user has selected to use intervals
        boolean hasInterval = (args.containsKeyAndNotNull("interval"));
        long intervalValue = 0;
        if (hasInterval) {
            Object interval = args.get("interval");
            if (interval instanceof Number) {
                intervalValue = ((Number) interval).longValue();
            } else {
                hasInterval = false;
            }
        }
        intent.putExtra("alarm_service_has_interval", hasInterval);
        if (hasInterval) {
            intent.putExtra("alarm_service_interval", intervalValue);
        }

        if ((args.containsKeyAndNotNull("customData"))) {
            String customData = (String) args.get("customData");
            intent.putExtra("customData", customData);
        }

        utils.debugLog("created alarm service intent for " + serviceName + "(forceRestart: "
                + (optionIsEnabled(args, "forceRestart") ? "true" : "false") + ", intervalValue: " + intervalValue
                + ")");

        return intent;
    }

    @Kroll.method
    public void cancelAlarmService(@Kroll.argument(optional = true) Object requestCode) {
        // To cancel an alarm the signature needs to be the same as the submitting one.
        utils.debugLog("Cancelling Alarm Service");
        int intentRequestCode = AlarmmanagerModule.DEFAULT_REQUEST_CODE;
        if (requestCode != null) {
            if (requestCode instanceof Number) {
                intentRequestCode = ((Number) requestCode).intValue();
            }
        }

        // Create a placeholder for the args value
        HashMap<String, Object> placeholder = new HashMap<String, Object>(0);
        KrollDict args = new KrollDict(placeholder);

        // Create the Alarm Manager
        AlarmManager am = (AlarmManager) ctx.getSystemService(TiApplication.ALARM_SERVICE);
        Intent intent = createAlarmServiceIntent(args);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        PendingIntent sender = PendingIntent.getBroadcast(ctx, intentRequestCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        am.cancel(sender);
        sender.cancel();
        utils.debugLog("Alarm Service Canceled");
    }

    @Kroll.method
    public void addAlarmService(@SuppressWarnings("rawtypes") HashMap hm) {
        @SuppressWarnings("unchecked")
        KrollDict args = new KrollDict(hm);
        if (!args.containsKeyAndNotNull("service")) {
            throw new IllegalArgumentException("Service name (service) is required");
        }
        if (!args.containsKeyAndNotNull("minute") && !args.containsKeyAndNotNull("second")) {
            throw new IllegalArgumentException("The minute or second field is required");
        }
        Calendar calendar = null;

        boolean isRepeating = hasRepeating(args);
        long repeatingFrequency = 0;
        if (isRepeating) {
            repeatingFrequency = repeatingFrequency(args);
        }

        // If seconds are provided but not years, we just take the seconds to mean to
        // add seconds until fire
        boolean secondBased = (args.containsKeyAndNotNull("second") && !args.containsKeyAndNotNull("year"));

        // If minutes are provided but not years, we just take the minutes to mean to
        // add minutes until fire
        boolean minuteBased = (args.containsKeyAndNotNull("minute") && !args.containsKeyAndNotNull("year"));

        // If minutes are provided but not years, we just take the minutes to mean to
        // add minutes until fire

        // Based on what kind of duration we build our calendar
        if (secondBased) {
            calendar = getSecondBasedCalendar(args);
        } else if (minuteBased) {
            calendar = getMinuteBasedCalendar(args);
        } else {
            calendar = getFullCalendar(args);
        }

        // Get the requestCode if provided, if none provided
        // we use 192837 for backwards compatibility
        int requestCode = args.optInt("requestCode", AlarmmanagerModule.DEFAULT_REQUEST_CODE);

        String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        utils.debugLog("Creating Alarm Notification for: " + sdf.format(calendar.getTime()));

        AlarmManager am = (AlarmManager) ctx.getSystemService(TiApplication.ALARM_SERVICE);
        Intent intent = createAlarmServiceIntent(args);

        if (isRepeating) {
            utils.debugLog("Setting Alarm to repeat at frequency " + repeatingFrequency);
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, requestCode, intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), repeatingFrequency, pendingIntent);
        } else {
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            PendingIntent sender = PendingIntent.getBroadcast(ctx, requestCode, intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            utils.debugLog("Setting Alarm for a single run");
            am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
        }
        utils.debugLog("Alarm Service Request Created");
    }

    @Kroll.method
    public void cancelNotification(int requestCode) {
        NotificationManager notificationManager = (NotificationManager) TiApplication.getInstance()
                .getSystemService(TiApplication.NOTIFICATION_SERVICE);
        notificationManager.cancel(requestCode);
    }

    @Kroll.method
    public void cancelNotifications() {
        NotificationManager notificationManager = (NotificationManager) TiApplication.getInstance()
                .getSystemService(TiApplication.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    @Kroll.method
    public void setRootActivityClassName(@Kroll.argument(optional = true) Object className) {
        //
        utils.debugLog("Request to set rootActivityClassName");

        if (className != null) {
            if (className instanceof String) {
                utils.debugLog("Setting rootActivityClassName to: " + className);
                AlarmmanagerModule.rootActivityClassName = (String) className;
            }
        }
    }


    @Kroll.getProperty
    public boolean canScheduleExactAlarms() {
        //
        AlarmManager am = (AlarmManager) ctx.getSystemService(TiApplication.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return am.canScheduleExactAlarms();
        }
        return false;
    }

    private JSONArray getActions(Object _actions) throws JSONException {
        if (_actions == null) return null;
        if (!(_actions.getClass().isArray())) {
            throw new IllegalArgumentException("items must be an array");
        }
        /* Casting to array */
        Object[] actionArray = (Object[]) _actions;
        JSONArray actions = new JSONArray();
        for (int index = 0; index < actionArray.length; index++) {
            JSONObject action = new JSONObject();
            KrollDict actionParam = new KrollDict(
                    (Map<? extends String, ? extends Object>) actionArray[index]);
            if (actionParam.containsKeyAndNotNull("actionname")) {
                action.put("actionname", actionParam.getString("actionname"));
            }
            if (actionParam.containsKeyAndNotNull("label")) {
                action.put("label", actionParam.getString("label"));
            }
            if (actionParam.containsKeyAndNotNull("icon")) {
                utils.debugLog("icon=" + actionParam.getInt("icon"));
                action.put("icon", actionParam.getInt("icon"));
            } else utils.debugLog("no icon given for action ");
            if (actionParam.containsKeyAndNotNull("extradata")) {
                action.put("extradata", actionParam.getString("extradata"));
            }
            actions.put(action);
        }
        return actions;
    }

    private Bitmap readFilenameFromObject(Object fileObject) {
        TiBaseFile TiFile = null;
        try {
            if (isInteger(fileObject)) {
                utils.debugLog("file is id of ressources");
                int resourceId = (int) fileObject;
                return BitmapFactory.decodeResource(TiApplication.getInstance().getResources(), resourceId);
            } else if (fileObject instanceof TiFile) {
                utils.debugLog("file is TiFile");
                TiFile = TiFileFactory.createTitaniumFile(((TiFile) fileObject).getFile().getAbsolutePath(), false);
            } else {
                if (fileObject instanceof FileProxy) {
                    utils.debugLog("file is FileProxy");
                    TiFile = ((FileProxy) fileObject).getBaseFile();
                } else {
                    if (fileObject instanceof TiBaseFile) {
                        Log.d(LCAT, "file is TiBaseFile");
                        TiFile = (TiBaseFile) fileObject;
                    } else if (fileObject instanceof String) {
                        // see:
                        // https://github.com/appcelerator/titanium_mobile/blob/master/android/modules/database/src/java/ti/modules/titanium/database/DatabaseModule.java
                        String uriString = resolveUrl(null, (String) fileObject);
                        utils.debugLog("resolvedUrl=" + uriString);
                        TiFile = TiFileFactory.createTitaniumFile(new String[]{uriString}, false);
                    }
                }
            }
            if (TiFile == null) {
                utils.debugLog("TiFile is null");
                return null;
            }
            if (TiFile.exists()) {
                File file = TiFile.getNativeFile().getAbsoluteFile();
                utils.debugLog("absolutePath=" + file.toURI()
                        + "  fileExists=" + file.exists());
                TiFileProxy result = new TiFileProxy(TiFile);
                utils.debugLog(result.getNativePath());
                return Bitmap.createScaledBitmap(TiUIHelper.createBitmap(TiFile.getInputStream()), 100, 100, true);
            } else
                utils.debugLog("File not exists");
        } catch (Exception e) {
            utils.debugLog(e.getMessage());
        }

        return null;
    }

    public class NotificationActionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            fireEvent("", new KrollDict());
            Log.d("AlarmManager", "NotificationActionReceiver::onReceive");
        }
    }
}
