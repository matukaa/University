package model.examen.com.events.services;

import com.squareup.okhttp.ResponseBody;

import java.util.List;

import model.examen.com.events.model.Note;
import model.examen.com.events.model.ResponseNote;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by iulia on 1/10/2017.
 */
public interface ApiService {

    @GET("/note")
    Observable<ResponseNote> getNotes(@Header("If-Modified-Since") String lastModified, @Query("page") int page);

    @DELETE("/note/{id}")
    Observable<ResponseBody> deleteNote(@Path("id") int id);
}
