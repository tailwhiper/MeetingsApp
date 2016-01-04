package tavi.tiki.niki.meetingsapplication;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import java.io.Serializable;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import model.Meeting;
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
    public static final int ERROR_CODE = -10;
    public static final int BACKGROUND_DOWNLOAD = 0;
    public static final int TODAY_MEETINGS = 1;
    public static final int FULL_INFO = 2;
    public static final int SEARCH = 3;
    public static final int DELETE_MEETING = 4;
    public static final int ADD_MEETING = 5;
    public static final int ADD_PARTICIPANT = 6;

    RestAdapter adapter;
    Restapi api;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public MeetingService(String name) {
        super(name);

    }

    public void initRest() {

    }

    public MeetingService() {
        super("name");
    }

    ;

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String username = preferences.getString("login", "nikita");
        String password = preferences.getString("password", "password");
        int purpose = intent.getIntExtra("purpose", -1);
        PendingIntent pi = intent.getParcelableExtra(MeetingsActivity.PENDING_INTENT);
        switch (purpose) {
            case BACKGROUND_DOWNLOAD:
                getTodayMeetingsInBackground(username, password);
                break;
            case TODAY_MEETINGS:
                getTodayMeetings(username, password, pi);
                break;
            case SEARCH:
                String searchinfo = intent.getStringExtra(MeetingsActivity.SEARCH_INFO);
                searchMeeting(username, password, searchinfo, pi);
                break;
            case DELETE_MEETING: {
                String id = intent.getStringExtra(MeetingsActivity.ID);
                deleteMeeting(username, password, id, pi);
            }
            break;
            case ADD_MEETING:

                addMeeting(username, password, pi, intent.getStringExtra("title"),
                        intent.getStringExtra("summary"),
                        intent.getStringExtra("startDate"),
                        intent.getStringExtra("endDate"),
                        intent.getIntExtra("priority", -1));
                break;

            case FULL_INFO: {
                getFullMeeting(username, password, intent.getIntExtra(FullMeetingActivity.ID, -1), pi);

            }
            break;
            case ADD_PARTICIPANT: {
                int id = intent.getIntExtra(FullMeetingActivity.ID, -1);
                String name = intent.getStringExtra(FullMeetingActivity.PARTICIPANT_NAME);
                String job = intent.getStringExtra(FullMeetingActivity.PARTICIPANT_JOB);
                addParticipant(username, password, id, name, job, pi);

            }
            break;
            default:
                Log.wtf("MeetingService", "intent with no purpose ");
                break;
        }

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

    public void getTodayMeetingsInBackground(String username, String password) {
        Date date = new Date(System.currentTimeMillis());
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
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


                if (oldInfo.size() < shortInfos.size()) {
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


    public void getTodayMeetings(String username, String password, PendingIntent pi) {
        final PendingIntent localpi = pi;
        Date date = new Date(System.currentTimeMillis());
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

                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable("listOfResults", (Serializable) shortInfos);

                intent.putExtras(bundle);

                try {
                    localpi.send(MeetingService.this, MeetingsActivity.RESULT_MEETINGS_LIST_UPDATED, intent);
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void failure(RetrofitError error) {

                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("errorMessage", error.getMessage());
                intent.putExtras(bundle);
                try {
                    localpi.send(MeetingService.this, MeetingsActivity.RESULT_CODE_ERROR, intent);
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void deleteMeeting(String username, String password, String id, PendingIntent pi) {
        final PendingIntent localpi = pi;
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(getString(R.string.baseURL))
                .setRequestInterceptor(new ApiRequestInterceptor(username, password))
                .setClient(new OkClient())
                .build();                                        //create an adapter for retrofit with base url

        Restapi api = restAdapter.create(Restapi.class);
        api.deleteMeeting(id, new Callback<String>() {
            @Override
            public void success(String info, Response response) {
                Intent intent = new Intent();

                try {
                    localpi.send(MeetingService.this, MeetingsActivity.RESULT_MEETINGS_LIST_UPDATED, intent);
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void failure(RetrofitError error) {

                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("errorMessage", error.getMessage());
                intent.putExtras(bundle);
                try {
                    localpi.send(MeetingService.this, MeetingsActivity.RESULT_CODE_ERROR, intent);
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void searchMeeting(String username, String password, String part, PendingIntent pi) {
        final PendingIntent localpi = pi;
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(getString(R.string.baseURL))
                .setRequestInterceptor(new ApiRequestInterceptor(username, password))
                .setClient(new OkClient())
                .build();                                        //create an adapter for retrofit with base url

        Restapi api = restAdapter.create(Restapi.class);
        api.searchMeeting(part, new Callback<List<MeetingShortInfo>>() {
            @Override
            public void success(List<MeetingShortInfo> shortInfos, Response response) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable("listOfResults", (Serializable) shortInfos);
                intent.putExtras(bundle);
                try {
                    localpi.send(MeetingService.this, MeetingsActivity.RESULT_MEETINGS_LIST_UPDATED, intent);
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void failure(RetrofitError error) {

                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("errorMessage", error.getMessage());
                intent.putExtras(bundle);
                try {
                    localpi.send(MeetingService.this, MeetingsActivity.RESULT_CODE_ERROR, intent);
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    public void addMeeting(String username, String password, PendingIntent pi, String title, String summary, String startDate, String endDate, int priority) {
        final PendingIntent localpi = pi;
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(getString(R.string.baseURL))
                .setRequestInterceptor(new ApiRequestInterceptor(username, password))
                .setClient(new OkClient())
                .build();                                        //create an adapter for retrofit with base url

        Restapi api = restAdapter.create(Restapi.class);
        api.putMeeting(title, summary, startDate, endDate, priority, new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                Intent intent = new Intent();

                try {
                    localpi.send(MeetingService.this, MeetingsActivity.RESULT_MEETINGS_LIST_UPDATED, intent);
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("errorMessage", error.getMessage());
                intent.putExtras(bundle);
                try {
                    localpi.send(MeetingService.this, MeetingsActivity.RESULT_CODE_ERROR, intent);
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void getFullMeeting(String username, String password, int id, PendingIntent pi) {
        final PendingIntent localpi = pi;

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(getString(R.string.baseURL))
                .setRequestInterceptor(new ApiRequestInterceptor(username, password))
                .setClient(new OkClient())
                .build();                                        //create an adapter for retrofit with base url

        Restapi api = restAdapter.create(Restapi.class);
        api.getFullInfo(id, new Callback<Meeting>() {
            @Override
            public void success(Meeting m, Response response) {

                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable("meeting", (Serializable) m);

                intent.putExtras(bundle);

                try {
                    localpi.send(MeetingService.this, FullMeetingActivity.RESULT_OK, intent);
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void failure(RetrofitError error) {

                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("errorMessage", error.getMessage());
                intent.putExtras(bundle);
                try {
                    localpi.send(MeetingService.this, ERROR_CODE, intent);
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void addParticipant(String username, String password, int id, String name, String job, PendingIntent pi) {
        final PendingIntent localpi = pi;

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(getString(R.string.baseURL))
                .setRequestInterceptor(new ApiRequestInterceptor(username, password))
                .setClient(new OkClient())
                .build();                                        //create an adapter for retrofit with base url

        Restapi api = restAdapter.create(Restapi.class);
        api.putParticipantMeeting(id, name, job, new Callback<String>() {
            public void success(String s, Response response) {
                Intent intent = new Intent();

                try {
                    localpi.send(MeetingService.this, FullMeetingActivity.RESULT_OK, intent);
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();

                }
            }

            @Override
            public void failure(RetrofitError error) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("errorMessage", error.getMessage());
                intent.putExtras(bundle);
                try {
                    localpi.send(MeetingService.this, MeetingsActivity.RESULT_CODE_ERROR, intent);
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
