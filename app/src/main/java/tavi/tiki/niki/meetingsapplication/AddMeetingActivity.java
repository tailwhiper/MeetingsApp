package tavi.tiki.niki.meetingsapplication;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Date;

import model.Meeting;

public class AddMeetingActivity extends AppCompatActivity {
    private String mTitle;
    private String mSummary;
    private Date mDateStart;
    private Date mDateEnd;
    private int mPriority;
    SimpleDateFormat sdf;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meeting);
        initDates();
        initSpinner();
    }

    private void initDates() {

        sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        mDateStart = new Date(System.currentTimeMillis());
        mDateEnd = new Date(System.currentTimeMillis() + 1800000);
        TextView dateStartView = (TextView) findViewById(R.id.textViewStartDate);
        TextView dateEndView = (TextView) findViewById(R.id.textViewEndDate);
        dateStartView.setText(getString(R.string.start_date) + sdf.format(mDateStart));
        dateEndView.setText(getString(R.string.end_date) + sdf.format(mDateEnd));
    }

    private void updateDate(boolean isStartDate) {
        if (isStartDate) {
            TextView dateStartView = (TextView) findViewById(R.id.textViewStartDate);
            dateStartView.setText(getString(R.string.start_date) + sdf.format(mDateStart));
        } else {
            TextView dateEndView = (TextView) findViewById(R.id.textViewEndDate);
            dateEndView.setText(getString(R.string.end_date) + sdf.format(mDateEnd));
        }
    }

    private void initSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.spinner_priority);
        // Создаем адаптер ArrayAdapter с помощью массива строк и стандартной разметки элемета spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.priority_array, android.R.layout.simple_spinner_item);
        // Определяем разметку для использования при выборе элемента
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Применяем адаптер к элементу spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mPriority = Meeting.PRIORITY_URGENT;
                        break;
                    case 1:
                        mPriority = Meeting.PRIORITY_PLANNED;
                        break;
                    case 2:
                        mPriority = Meeting.PRIORITY_IF_POSSIBLE;
                        break;
                    default:
                        mPriority = Meeting.PRIORITY_IF_POSSIBLE;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void back(View v) {
        finish();
    }

    public void chooseStartDate(View v){
        createDateDialog("Choose when meeting starts", true);
    }
    public void chooseEndDate(View v){
        createDateDialog("Choose when meeting ends",false);
    }
    private void createDateDialog(String title, final boolean isForStartDate) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(title);
        RelativeLayout view = (RelativeLayout) getLayoutInflater()
                .inflate(R.layout.datetime_picker_dialog, null);
        dialogBuilder.setView(view);
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Dialog thisDialog = Dialog.class.cast( dialog);
                DatePicker date = (DatePicker) thisDialog.findViewById(R.id.datePicker);
                TimePicker time = (TimePicker) thisDialog.findViewById(R.id.timePicker);
                Date chosenDate;
                TextView chosenDateView;
                if (isForStartDate) {
                    chosenDate = mDateStart;

                } else {
                    chosenDate = mDateEnd;
                }

                chosenDate.setYear(date.getYear()-1900);
                chosenDate.setMonth(date.getMonth());
                chosenDate.setDate(date.getDayOfMonth());
                chosenDate.setHours(time.getCurrentHour());
                chosenDate.setMinutes(time.getCurrentMinute());
                updateDate(isForStartDate);
            }
        });

         dialogBuilder.create().show();
    }
    public void AddMeeting(View view){
        Intent intent = new Intent();
        mTitle = ((EditText)findViewById(R.id.editTitle)).getText().toString();
        mSummary = ((EditText)findViewById(R.id.editSummary)).getText().toString();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd-HH:mm");

        intent.putExtra("title",mTitle);
        intent.putExtra("summary",mSummary);
        intent.putExtra("startDate", sdf.format(mDateStart));
        intent.putExtra("endDate",sdf.format(mDateEnd));
        intent.putExtra("priority",mPriority);

        setResult(MeetingsActivity.RESULT_MEETINGS_LIST_UPDATED, intent);
        finish();
    }

}
