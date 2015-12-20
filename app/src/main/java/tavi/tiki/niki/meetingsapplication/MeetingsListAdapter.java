package tavi.tiki.niki.meetingsapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import model.Meeting;
import model.MeetingShortInfo;


/**
 * Created by Никита on 20.12.2015.
 */
public class MeetingsListAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
   List<MeetingShortInfo> meetings;
    MeetingsListAdapter(Context context, List<MeetingShortInfo> meets) {
        ctx = context;
        meetings = meets;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return meetings.size();
    }

    @Override
    public Object getItem(int position) {
        return meetings.get(position);
    }

    @Override
    public long getItemId(int position) {
        return meetings.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.list_item, parent, false);
        }
        MeetingShortInfo meet = meetings.get(position);
        TextView text = ((TextView) view.findViewById(R.id.meetingId));
        text.setText("Id: "+Integer.toString(meet.getId()));
        ((TextView) view.findViewById(R.id.meetingTitle)).setText("Title:" + meet.getTitle());

        String priority;
        switch (meet.getPriority()) {
            case Meeting.PRIORITY_IF_POSSIBLE:
                priority = "Priority: if possible";
                break;
            case Meeting.PRIORITY_PLANNED:PRIORITY_PLANNED:
                priority = "Priority: planned";
                break;
            case Meeting.PRIORITY_URGENT:
                priority = "Priority: urgent";
                break;
            default:
                priority = "Priority: not specified";
                break;
        }
        ((TextView) view.findViewById(R.id.priority)).setText(priority);
        ((TextView) view.findViewById(R.id.startDate)).setText("Starts at: "+meet.getStartDate().getDayOfMonth()+"/"+meet.getStartDate().getMonthOfYear()+"/"+meet.getStartDate().getYear()+" "+meet.getStartDate().getHourOfDay()+":"+meet.getStartDate().getMinuteOfHour());
        ((TextView) view.findViewById(R.id.endDate)).setText("Ends at: "+meet.getEndDate().getDayOfMonth()+"/"+meet.getEndDate().getMonthOfYear()+"/"+meet.getEndDate().getYear()+" "+meet.getEndDate().getHourOfDay()+":"+meet.getEndDate().getMinuteOfHour());
        return view;
    }

}
