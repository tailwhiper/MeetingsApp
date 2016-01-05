package tavi.tiki.niki.meetingsapplication;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
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

import com.google.gson.Gson;

import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import SwipeToDissimiss.SwipeDismissListViewTouchListener;
import model.MeetingShortInfo;



public class MeetingsActivity extends AppCompatActivity {
    List<MeetingShortInfo> meetingShortInfos;
    MeetingsListAdapter adapter;

    SwipeRefreshLayout mSwipeRefreshLayout;


    public final static String SEARCH_INFO = "searchInfo";
    public final static String ID = "id";

    private final static int ADD_MEETING__CODE = 1;
    private final static int SERVICE_REQUEST_CODE = 2;
    public final static int RESULT_MEETINGS_LIST_UPDATED = 0;
    public final static int RESULT_MEETING_DELETED = 4;
    public final static int RESULT_MEETING_ADDED = 3;
    public final static int RESULT_CODE_ERROR = -1;
    public final static String fileName = "savedMeetings.json";
    public final static String PENDING_INTENT = "PendingIntent";




    public void initAdapter() {
        adapter = new MeetingsListAdapter(this, meetingShortInfos);
        ListView lvMeetings = (ListView) findViewById(R.id.listview_meetings);
        lvMeetings.setAdapter(adapter);
    }

    public void updateAdapter(List<MeetingShortInfo> shortInfos) {
        mSwipeRefreshLayout.setRefreshing(false);
        meetingShortInfos.clear();
        meetingShortInfos.addAll(shortInfos);
        adapter.notifyDataSetChanged();
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
                                    deleteMeetingFromService(Integer.toString(id));
                                    meetingShortInfos.remove(position);
                                }
                                adapter.notifyDataSetChanged();
                            }
                        });
        listView.setOnTouchListener(touchListener);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String meetingId = ((TextView) view.findViewById(R.id.meetingId)).getText().toString();
                Intent intent = new Intent(getApplicationContext(), FullMeetingActivity.class);
                String newId = meetingId.replace("Id: ", "");
                intent.putExtra("id", Integer.parseInt(newId));
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
                //getTodayMeetings(new Date(System.currentTimeMillis()));
                getTodayMeetingsFromService();
            }
        });


    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_meetings);

        meetingShortInfos = new ArrayList<MeetingShortInfo>();
        initAdapter();
        initSwipe();
        initSwipeRefresh();

        getTodayMeetingsFromService();//load meetings for today
        Intent intent = new Intent("backgroundLoad");
        sendBroadcast(intent);

    }


    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onPause() {
        super.onPause();
        Gson gson = new Gson();

        String string = gson.toJson(meetingShortInfos);
        FileOutputStream outputStream;

        try {

            outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(string.getBytes(Charset.forName("UTF-8")));
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

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
        if (id == R.id.search_button) {
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
        switch (resultCode) {
            case RESULT_MEETINGS_LIST_UPDATED: {
                switch (requestCode) {
                    case ADD_MEETING__CODE: {
                        Bundle bundle = data.getExtras();
                        addMeetingFromService(bundle);
                        getTodayMeetingsFromService();}
                    break;
                    case SERVICE_REQUEST_CODE: {

                        Bundle bundle = data.getExtras();
                        List<MeetingShortInfo> infos = (List<MeetingShortInfo>) bundle.getSerializable("listOfResults");
                        updateAdapter(infos);

                    }
                    break;
                    case RESULT_MEETING_DELETED:
                        getTodayMeetingsFromService(); // автообновление
                        Toast.makeText(this, "Meeting has been deleted", Toast.LENGTH_SHORT).show();
                        break;
                    case RESULT_MEETING_ADDED:
                        getTodayMeetingsFromService(); // автообновление
                        Toast.makeText(this, "Meeting has been added", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
            break;
            case RESULT_CODE_ERROR: {
                Toast.makeText(this, data.getStringExtra("errorMessage"), Toast.LENGTH_SHORT).show();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }

    }

    public void showSearchDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText edittext = new EditText(getApplicationContext());
        alert.setMessage("Write what you want to find");
        alert.setTitle("Search in all meetings");
        edittext.setTextColor(Color.BLACK);
        alert.setView(edittext);

        alert.setPositiveButton("Search", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //What ever you want to do with the value
                // searchMeeting(edittext.getText().toString());
                searchMeetingsFromService(edittext.getText().toString());
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

    public void openPreferences() {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(intent);
    }

    public void getTodayMeetingsFromService() {
        PendingIntent pi;
        Intent intent;
        pi = createPendingResult(SERVICE_REQUEST_CODE, new Intent(), 0);
        intent = new Intent(this, MeetingService.class).putExtra("purpose", MeetingService.TODAY_MEETINGS)
                .putExtra(PENDING_INTENT, pi);
        // стартуем сервис
        startService(intent);

    }

    public void searchMeetingsFromService(String searchInfo) {
        PendingIntent pi;
        Intent intent;
        pi = createPendingResult(SERVICE_REQUEST_CODE, new Intent(), 0);
        intent = new Intent(this, MeetingService.class).putExtra("purpose", MeetingService.SEARCH)
                .putExtra(SEARCH_INFO, searchInfo)
                .putExtra(PENDING_INTENT, pi);
        // стартуем сервис
        startService(intent);

    }

    public void deleteMeetingFromService(String id) {
        PendingIntent pi;
        Intent intent;
        pi = createPendingResult(RESULT_MEETING_DELETED, new Intent(), 0);
        intent = new Intent(this, MeetingService.class).putExtra("purpose", MeetingService.DELETE_MEETING)
                .putExtra(ID, id)
                .putExtra(PENDING_INTENT, pi);
        // стартуем сервис
        startService(intent);

    }

    public void addMeetingFromService(Bundle bundle) {
        PendingIntent pi;
        Intent intent;
        pi = createPendingResult(RESULT_MEETING_ADDED, new Intent(), 0);
        intent = new Intent(this, MeetingService.class).putExtra("purpose", MeetingService.ADD_MEETING)
                .putExtras(bundle)
                .putExtra(PENDING_INTENT, pi);
        // стартуем сервис
        startService(intent);


    }

}
