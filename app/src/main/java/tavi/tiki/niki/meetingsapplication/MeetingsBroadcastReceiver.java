package tavi.tiki.niki.meetingsapplication;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;

public class MeetingsBroadcastReceiver extends BroadcastReceiver {

    private int interval = 0;

    public MeetingsBroadcastReceiver() {
        interval = 10000;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.wtf("broadcastreceiver", "log on receive");
        Intent intentToSelf = new Intent(context, MeetingsBroadcastReceiver.class);
        intentToSelf.putExtra("purpose", "alarmToSelf");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intentToSelf, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + interval, pendingIntent);

        if (!isActivityRunning(context)) {
            Intent downloadIntent = new Intent(context, MeetingService.class);
            downloadIntent.putExtra("purpose", MeetingService.BACKGROUND_DOWNLOAD);
            context.startService(downloadIntent);
        }
    }

    public boolean isActivityRunning(Context context) {

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> activitys = activityManager.getRunningTasks(Integer.MAX_VALUE);
        boolean appIsOnForeground = false;


        if (activitys.get(0).baseActivity.toString().equalsIgnoreCase("ComponentInfo{tavi.tiki.niki.meetingsapplication/tavi.tiki.niki.meetingsapplication.MeetingsActivity}")) {
            appIsOnForeground = true;

        }
        return appIsOnForeground;
    }
}
