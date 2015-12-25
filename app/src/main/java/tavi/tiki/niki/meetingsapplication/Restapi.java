package tavi.tiki.niki.meetingsapplication;

import com.squareup.okhttp.Response;

import java.util.List;

import model.Meeting;
import model.MeetingShortInfo;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.PUT;
import retrofit.http.Path;


public interface Restapi {

    @GET("/get/all/short")
    public void getAllShort(Callback<List<MeetingShortInfo>> response);

    @GET("/get/all/short")
    public void getTodayMeetings(Callback<List<MeetingShortInfo>> response);

    @GET("/get/{id}")
    public void getMeeting(@Path("id") String id, Callback<List<Meeting>> response);

    @DELETE("/delete/{id}")
    public void deleteMeeting(@Path("id") String id, Callback<String> response);
    @PUT("/put/{title}/{summary}/{startDate}/{endDate}/{priority}")
    public void putMeeting(@Path("title") String title,
                           @Path("summary") String summary,
                           @Path("startDate") String startDate,
                           @Path("endDate") String endDate,
                           @Path("priority") int priority,
                           Callback<String> response);
}