package tavi.tiki.niki.meetingsapplication;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
    private final static int GET_FULL_MEETING_CODE = 0;
    public final static int ADD_PARTICIPANT_CODE = 1;
    public final static String ID = "id";
    public final static String PARTICIPANT_NAME = "partName";
    public final static String PARTICIPANT_JOB = "partJob";
    private String username = "nikita";
    private String password = "password";
    private String mName = "Nikita";
    private String mJob = "BydloCoder";
    Meeting mMeeting;
    int mId;

    ArrayAdapter<String> listadapter;
    ArrayList<String> stringsParticipants;

    public void initPreferences(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        username = preferences.getString("login", "nikita");
        password = preferences.getString("password", "password");
        mName = preferences.getString("name", "nikita");
        mJob = preferences.getString("job", "koekaker");

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mId = getIntent().getIntExtra("id", -1);

        setContentView(R.layout.activity_full_meeting);

        initSwipeRefresh();

        initPreferences(this);
        getFullMeetingFromService(mId);
        //updateMeetingViews();
    }

    public void initSwipeRefresh() {
        SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayoutFullMeeting);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFullMeetingFromService(mId);
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
        if (mMeeting.getParticipants().contains(new Participant(mName, mJob)))
            findViewById(R.id.iwillgo_button).setVisibility(View.INVISIBLE);
    }

    public void getFullMeeting(int id) {

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(getString(R.string.baseURL))
                .setRequestInterceptor(new ApiRequestInterceptor(username, password))
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
        addParticipantFromService(mId, mName, mJob);

    }

    public void getFullMeetingFromService(int id) {
        PendingIntent pi;
        Intent intent;
        pi = createPendingResult(GET_FULL_MEETING_CODE, new Intent(), 0);
        intent = new Intent(this, MeetingService.class).putExtra("purpose", MeetingService.FULL_INFO)
                .putExtra(ID, id)
                .putExtra(MeetingsActivity.PENDING_INTENT, pi);

        startService(intent);

    }

    public void addParticipantFromService(int id, String name, String job) {
        PendingIntent pi;
        Intent intent;
        pi = createPendingResult(ADD_PARTICIPANT_CODE, new Intent(), 0);
        intent = new Intent(this, MeetingService.class).putExtra("purpose", MeetingService.ADD_PARTICIPANT)
                .putExtra(ID, id)
                .putExtra(PARTICIPANT_NAME, name)
                .putExtra(PARTICIPANT_JOB, job)
                .putExtra(MeetingsActivity.PENDING_INTENT, pi);

        startService(intent);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode){
            case RESULT_OK:{
                switch (requestCode){
                    case ADD_PARTICIPANT_CODE:{
                        getFullMeetingFromService(mId);

                    }
                        break;
                    case GET_FULL_MEETING_CODE:{
                        mMeeting = (Meeting) data.getSerializableExtra("meeting");
                        updateMeetingViews();

                    }
                        break;
                }
            }
            break;
            case MeetingService.ERROR_CODE:{
                Toast.makeText(this, data.getStringExtra("errorMessage"), Toast.LENGTH_SHORT).show();
            }
        }
    }
}

