package tavi.tiki.niki.meetingsapplication;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

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
    private final static String USERNAME = "nikita";
    private final static String PASSWORD = "password";

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
                                    meetingShortInfos.remove(position);
                                }
                                adapter.notifyDataSetChanged();
                            }
                        });
        listView.setOnTouchListener(touchListener);
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        listView.setOnScrollListener(touchListener.makeScrollListener());

    }
    public void  initSwipeRefresh(){
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAllMeetings();

            }
        });


    }

    public void fill() {
        meetingShortInfos = new ArrayList<MeetingShortInfo>();
        meetingShortInfos.add(new MeetingShortInfo(0, "title0", 1, new Date(2015, 10, 11, 10, 15), new Date(2015, 10, 11, 10, 30)));
        meetingShortInfos.add(new MeetingShortInfo(1, "title1", 2, new Date(2015, 10, 11, 10, 15), new Date(2015, 10, 11, 10, 30)));
        // meetingShortInfos.add(new MeetingShortInfo(2, "title2", 3, new DateTime(2015, 10, 11, 10, 15), new DateTime(2015, 10, 11, 10, 30)));
        // meetingShortInfos.add(new MeetingShortInfo(3, "title3", 1, new DateTime(2015, 10, 11, 10, 15), new DateTime(2015, 10, 11, 10, 30)));
        // meetingShortInfos.add(new MeetingShortInfo(4, "title4", 1, new DateTime(2015, 10, 11, 10, 15), new DateTime(2015, 10, 11, 10, 30)));
    }

    public void getAllMeetings() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(getString(R.string.baseURL))
                .setRequestInterceptor(new ApiRequestInterceptor(USERNAME, PASSWORD))
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
    public void showAddDialog(View view){}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetings);
        fill();
        initAdapter();
        initSwipe();
        initSwipeRefresh();

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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
