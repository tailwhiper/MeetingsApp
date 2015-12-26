package tavi.tiki.niki.meetingsapplication;

import java.util.List;

import model.Meeting;
import model.MeetingShortInfo;
import retrofit.Callback;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.PUT;
import retrofit.http.Path;


public interface Restapi {

    @GET("/get/all/short")
    public void getAllShort(Callback<List<MeetingShortInfo>> response);

    @GET("/get/date/{date}")
    public void getMeetingsForDay(@Path("date") String date, Callback<List<MeetingShortInfo>> response);


    @GET("/get/{id}")
    public void getFullInfo(@Path("id") int id, Callback<Meeting> response);

    @GET("/get/search/{part}")
    public void searchMeeting(@Path("part") String part, Callback<List<MeetingShortInfo>> response);

    @DELETE("/delete/{id}")
    public void deleteMeeting(@Path("id") String id, Callback<String> response);

    @PUT("/put/{title}/{summary}/{startDate}/{endDate}/{priority}")
    public void putMeeting(@Path("title") String title,
                           @Path("summary") String summary,
                           @Path("startDate") String startDate,
                           @Path("endDate") String endDate,
                           @Path("priority") int priority,
                           Callback<String> response);

    @PUT("/put/{id}/{name}/{job}")
    public void putParticipantMeeting(@Path("id") int id,
                                      @Path("name") String name,
                                      @Path("job") String job,
                                      Callback<String> response);


}
