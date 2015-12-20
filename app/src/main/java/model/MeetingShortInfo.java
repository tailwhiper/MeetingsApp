package model;


import org.joda.time.DateTime;

import java.io.Serializable;


public class MeetingShortInfo implements Serializable {
    private int id;
    private DateTime startDate;
    private DateTime endDate;
    private String title;
    private int priority;


    public MeetingShortInfo(Meeting m) {
        this.id = m.getId();
        this.title = m.getTitle();
        this.priority = m.getPriority();
    }

    public MeetingShortInfo(int id, String title, int priority, DateTime startDate, DateTime endDate) {
        this.id = id;

        this.title = title;
        this.priority = priority;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public DateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(DateTime endDate) {
        this.endDate = endDate;
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
