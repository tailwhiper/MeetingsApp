package tavi.tiki.niki.meetingsapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import SwipeToDissimiss.SwipeDismissListViewTouchListener;
import model.MeetingShortInfo;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;


public class Meetings extends AppCompatActivity {
    List<MeetingShortInfo> meetingShortInfos;
    MeetingsListAdapter adapter;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private String username;
    private  String password;

    private final static int ADD_MEETING__CODE = 1;
    private final static int FULL_MEETING__CODE = 2;
    public void initPreferences(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        username = preferences.getString("login","nikita");
        password = preferences.getString("password","password");
    }
    public void initAdapter() {
        adapter = new MeetingsListAdapter(this, meetingShortInfos);
        ListView lvMeetings = (ListView) findViewById(R.id.listview_meetings);
        lvMeetings.setAdapter(adapter);
    }

    public void initSwipe() {
        ListView listView = (ListView) findViewById(R.id.listview_meetings);
        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                        listView,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    int id = meetingShortInfos.get(position).getId();
                                    deleteMeeting(Integer.toString(id));
                                    meetingShortInfos.remove(position);
                                }
                                adapter.notifyDataSetChanged();
                            }
                        });
        listView.setOnTouchListener(touchListener);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               String meetingId = ((TextView)view.findViewById(R.id.meetingId)).getText().toString();
                Intent intent = new Intent(getApplicationContext(),FullMeetingActivity.class);
                String newId =meetingId.replace("Id: ","");
                intent.putExtra("id",Integer.parseInt(newId));
                startActivity(intent);
            }
        });
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        listView.setOnScrollListener(touchListener.makeScrollListener());

    }

    public void initSwipeRefresh() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // getAllMeetings();
                getTodayMeetings(new Date(System.currentTimeMillis()));
            }
        });


    }

    public void fill() {
        meetingShortInfos = new ArrayList<MeetingShortInfo>();
        //meetingShortInfos.add(new MeetingShortInfo(0, "title0", 1, new Date(2015, 10, 11, 10, 15), new Date(2015, 10, 11, 10, 30)));
        // meetingShortInfos.add(new MeetingShortInfo(1, "title1", 2, new Date(2015, 10, 11, 10, 15), new Date(2015, 10, 11, 10, 30)));
        // meetingShortInfos.add(new MeetingShortInfo(2, "title2", 3, new DateTime(2015, 10, 11, 10, 15), new DateTime(2015, 10, 11, 10, 30)));
        // meetingShortInfos.add(new MeetingShortInfo(3, "title3", 1, new DateTime(2015, 10, 11, 10, 15), new DateTime(2015, 10, 11, 10, 30)));
        // meetingShortInfos.add(new MeetingShortInfo(4, "title4", 1, new DateTime(2015, 10, 11, 10, 15), new DateTime(2015, 10, 11, 10, 30)));
    }


    public void getAllMeetings() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(getString(R.string.baseURL))
                .setRequestInterceptor(new ApiRequestInterceptor(username, password))
                .setClient(new OkClient())
                .build();                                        //create an adapter for retrofit with base url

        Restapi api = restAdapter.create(Restapi.class);
        api.getAllShort(new Callback<List<MeetingShortInfo>>() {
            @Override
            public void success(List<MeetingShortInfo> shortInfos, Response response) {
                mSwipeRefreshLayout.setRefreshing(false);
                meetingShortInfos.clear();
                meetingShortInfos.addAll(shortInfos);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void failure(RetrofitError error) {
                mSwipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                Log.d("Retrofit Error", error.getMessage());
            }
        });

    }
    public void getTodayMeetings(Date date) {
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
                mSwipeRefreshLayout.setRefreshing(false);
                meetingShortInfos.clear();
                meetingShortInfos.addAll(shortInfos);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void failure(RetrofitError error) {
                mSwipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                Log.d("Retrofit Error", error.getMessage());
            }
        });

    }
    public void searchMeeting(String part) {

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(getString(R.string.baseURL))
                .setRequestInterceptor(new ApiRequestInterceptor(username, password))
                .setClient(new OkClient())
                .build();                                        //create an adapter for retrofit with base url

        Restapi api = restAdapter.create(Restapi.class);
        api.searchMeeting(part, new Callback<List<MeetingShortInfo>>() {
            @Override
            public void success(List<MeetingShortInfo> shortInfos, Response response) {
                mSwipeRefreshLayout.setRefreshing(false);
                meetingShortInfos.clear();
                meetingShortInfos.addAll(shortInfos);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void failure(RetrofitError error) {
                mSwipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                Log.d("Retrofit Error", error.getMessage());
            }
        });

    }
    public void deleteMeeting(String id) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(getString(R.string.baseURL))
                .setRequestInterceptor(new ApiRequestInterceptor(username, password))
                .setClient(new OkClient())
                .build();                                        //create an adapter for retrofit with base url

        Restapi api = restAdapter.create(Restapi.class);
        api.deleteMeeting(id, new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                Toast.makeText(getApplicationContext(), "Meeting was deleted on server", Toast.LENGTH_LONG).show();
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                Log.d("Retrofit Error", error.getMessage());
            }
        });
    }
    public void addMeeting(String title, String summary, String startDate, String endDate, int priority) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(getString(R.string.baseURL))
                .setRequestInterceptor(new ApiRequestInterceptor(username, password))
                .setClient(new OkClient())
                .build();                                        //create an adapter for retrofit with base url

        Restapi api = restAdapter.create(Restapi.class);
        api.putMeeting(title, summary, startDate, endDate, priority, new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                Toast.makeText(getApplicationContext(), "Meeting was added on server", Toast.LENGTH_LONG).show();
                // getAllMeetings();
                getTodayMeetings(new Date(System.currentTimeMillis()));
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                Log.d("Retrofit Error", error.getMessage());
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetings);
        fill();
        initAdapter();
        initSwipe();
        initSwipeRefresh();
        getTodayMeetings(new Date(System.currentTimeMillis()));//load meetings for today
        initPreferences(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        initPreferences(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_meetings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.pref_button) {
            openPreferences();
        }
        if(id == R.id.search_button){
            showSearchDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    public void startAddMeeting(View v) {
        Intent intent = new Intent(this, AddMeetingActivity.class);
        startActivityForResult(intent, ADD_MEETING__CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        //+ add Meeting to service
        String meeting = data.getStringExtra("title");

        addMeeting(data.getStringExtra("title"), data.getStringExtra("summary"), data.getStringExtra("startDate"), data.getStringExtra("endDate"), data.getIntExtra("priority", -1));


    }
    public void showSearchDialog(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText edittext = new EditText(getApplicationContext());
        alert.setMessage("Write what you want to find");
        alert.setTitle("Search in all meetings");
        edittext.setTextColor(Color.BLACK);
        alert.setView(edittext);

        alert.setPositiveButton("Search", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //What ever you want to do with the value
                searchMeeting( edittext.getText().toString());
            }
        });

       /* alert.setNegativeButton("", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
            }
        });
       */
        alert.show();
    }
    public void openPreferences(){
       Intent intent = new Intent(getApplicationContext(),SettingsActivity.class);
        startActivity(intent);
    }

}
