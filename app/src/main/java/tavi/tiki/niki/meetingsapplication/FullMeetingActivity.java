package tavi.tiki.niki.meetingsapplication;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import model.Meeting;
import model.Participant;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

public class FullMeetingActivity extends AppCompatActivity {
    private final static String USERNAME = "nikita";
    private final static String PASSWORD = "password";
    Meeting mMeeting;
    int mId;
    String mName ="Nikita";
    String mJob ="BydloCoder";
    ArrayAdapter<String> listadapter;
    ArrayList<String> stringsParticipants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mId = getIntent().getIntExtra("id", -1);

        setContentView(R.layout.activity_full_meeting);

        initSwipeRefresh();
        getFullMeeting(mId);

    }

    public void initSwipeRefresh() {
        SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayoutFullMeeting);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFullMeeting(mId);
            }
        });


    }

    public void refillList() {

        ArrayList<String> stringParticipants = new ArrayList<String>();
        for (Participant p : mMeeting.getParticipants()) {
            stringParticipants.add(p.getName() + " " + p.getJob());
        }
        listadapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, stringParticipants);
        ((ListView) findViewById(R.id.participantsListView)).setAdapter(listadapter);
    }

    public void putParticipant(int id, String name, String job) {

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(getString(R.string.baseURL))
                .setRequestInterceptor(new ApiRequestInterceptor(USERNAME, PASSWORD))
                .setClient(new OkClient())
                .build();                                        //create an adapter for retrofit with base url

        Restapi api = restAdapter.create(Restapi.class);
        api.putParticipantMeeting(id, name, job, new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                Toast.makeText(getApplicationContext(), "participant added.", Toast.LENGTH_SHORT).show();
                View button = findViewById(R.id.iwillgo_button);
                button.setVisibility(View.INVISIBLE);
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    public void updateMeetingViews() {
        ((TextView) findViewById(R.id.titleFull)).setText(getString(R.string.title) + mMeeting.getTitle());
        ((TextView) findViewById(R.id.summaryFull)).setText(getString(R.string.summary) + mMeeting.getSummary());
        SimpleDateFormat sdf = new SimpleDateFormat("dd.M.yyyy HH:mm");
        ((TextView) findViewById(R.id.startDateFull)).setText(getString(R.string.start_date) + sdf.format(mMeeting.getStartdate()));
        ((TextView) findViewById(R.id.endDateFull)).setText(getString(R.string.end_date) + sdf.format(mMeeting.getEnddate()));
        String priority;
        switch (mMeeting.getPriority()) {
            case Meeting.PRIORITY_IF_POSSIBLE:
                priority = "Priority: if possible";
                break;
            case Meeting.PRIORITY_PLANNED:
                PRIORITY_PLANNED:
                priority = "Priority: planned";
                break;
            case Meeting.PRIORITY_URGENT:
                priority = "Priority: urgent";
                break;
            default:
                priority = "Priority: not specified";
                break;
        }
        ((TextView) findViewById(R.id.priorityFull)).setText(getString(R.string.priority) + priority);
        refillList();
        if ( mMeeting.getParticipants().contains(new Participant(mName,mJob)))findViewById(R.id.iwillgo_button).setVisibility(View.INVISIBLE);
    }

    public void getFullMeeting(int id) {

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(getString(R.string.baseURL))
                .setRequestInterceptor(new ApiRequestInterceptor(USERNAME, PASSWORD))
                .setClient(new OkClient())
                .build();                                        //create an adapter for retrofit with base url

        Restapi api = restAdapter.create(Restapi.class);
        api.getFullInfo(id, new Callback<Meeting>() {
            @Override
            public void success(Meeting meeting, Response response) {
                mMeeting = meeting;
                updateMeetingViews();

            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getApplicationContext(), "Unable to load meeting.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void iWillGO(View view) {
        putParticipant(mId, mName, mJob);

    }

}
