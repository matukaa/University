package com.freakz.matukaa.examprep.service;

import com.freakz.matukaa.examprep.domain.MyEntity;

import java.util.List;

import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import rx.Observable;

import retrofit2.http.GET;

/**
 * Created by Matukaa on 2018-01-24.
 */

public interface ExamService {
    String SERVICE_ENDPOINT = "http://10.0.2.2:3000";

    @GET("entity")
    Observable<List<MyEntity>> getEntities(@Header("If-Modified-Since") String lastModified);

    @POST("entity")
    Observable<MyEntity> addEntity(@Body MyEntity e);
}
