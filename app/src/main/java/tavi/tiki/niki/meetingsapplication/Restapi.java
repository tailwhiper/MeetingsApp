package tavi.tiki.niki.meetingsapplication;

import java.util.List;

import model.Meeting;
import model.MeetingShortInfo;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;


public interface Restapi {

    @GET("/get/all/short")
    public void getAllShort(Callback<List<MeetingShortInfo>> response);
    @GET("/get/{id}")
    public void getMeeting(@Path ("id") String id ,Callback<List<Meeting>> response);

}
