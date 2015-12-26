package tavi.tiki.niki.meetingsapplication;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import model.MeetingShortInfo;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

/**
 * Created by Никита on 26.12.2015.
 */

public class MeetingService extends IntentService {


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public MeetingService(String name) {
        super(name);
    }

    public MeetingService() {
        super("name");
    }

    ;

    @Override
    protected void onHandleIntent(Intent intent) {
        getTodayMeetingsInBackground();
        Log.wtf("MeetingService", "onHandle intent started");
    }


    public void SendNotification() {
        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("New meetings")
                        .setAutoCancel(true)
                        .setContentText("Yay! New meetings have been added");

        Intent resultIntent = new Intent(this, MeetingsActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MeetingsActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(0, mBuilder.build());

    }

    public void getTodayMeetingsInBackground() {
        Date date = new Date(System.currentTimeMillis());
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String username = preferences.getString("login", "nikita");
        String password = preferences.getString("password", "password");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        String stringDate = sdf.format(date);
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(getString(R.string.baseURL))
                .setRequestInterceptor(new ApiRequestInterceptor(username, password))
                .setClient(new OkClient())
                .build();                                        //create an adapter for retrofit with base url

        Restapi api = restAdapter.create(Restapi.class);
        api.getMeetingsForDay(stringDate, new Callback<List<MeetingShortInfo>>() {
            @Override
            public void success(List<MeetingShortInfo> shortInfos, Response response) {
                List<MeetingShortInfo> oldInfo = loadSaved();


                if (oldInfo.size()<shortInfos.size()) {
                    SendNotification();
                }
            }

            @Override
            public void failure(RetrofitError error) {

                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                Log.d("Retrofit Error", error.getMessage());
            }
        });

    }

    public List<MeetingShortInfo> loadSaved() {

        try {
            FileInputStream is = openFileInput(MeetingsActivity.fileName);
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                builder.append(line);
            }
            /*
            int ch;
            while((ch = is.read()) != -1){
                builder.append((char)ch);
            }*/

            Type meetListType = new TypeToken<ArrayList<MeetingShortInfo>>() {
            }.getType();
            ArrayList<MeetingShortInfo> m = new Gson().fromJson(builder.toString(), meetListType);
            br.close();
            isr.close();
            is.close();
            if (m == null) {
                return new ArrayList<MeetingShortInfo>();
            } else {
                return m;
            }
        } catch (FileNotFoundException e) {

            e.printStackTrace();
            return new ArrayList<MeetingShortInfo>();
        } catch (IOException e) {
            e.printStackTrace();

        }

        return new ArrayList<MeetingShortInfo>();
    }
}
