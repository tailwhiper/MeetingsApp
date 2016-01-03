package tavi.tiki.niki.meetingsapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import model.Meeting;

public class MeetingsBroadcastReceiver extends BroadcastReceiver {

    private int interval=0;
    public MeetingsBroadcastReceiver() {
        interval = 10000;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.wtf("broadcastreceiver", "log on receive");
        Intent intentToSelf = new Intent(context, MeetingsBroadcastReceiver.class);
        intentToSelf.putExtra("purpose", "alarmToSelf");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intentToSelf, 0);
        AlarmManager alarmManager =(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + interval, pendingIntent);

        Intent downloadIntent = new Intent(context, MeetingService.class);
        downloadIntent.putExtra("purpose",MeetingService.BACKGROUND_DOWNLOAD);
        context.startService(downloadIntent);
    }
}
