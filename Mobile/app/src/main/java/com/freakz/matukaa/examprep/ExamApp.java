package com.freakz.matukaa.examprep;

import android.app.Application;
import android.arch.persistence.room.Room;

import com.freakz.matukaa.examprep.database.ExamDatabase;

import timber.log.Timber;

/**
 * Created by Matukaa on 2018-01-24.
 */

public class ExamApp extends Application {
    public ExamDatabase db;

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
        db = Room.databaseBuilder(getApplicationContext(),
                ExamDatabase.class, "database-name").build();
    }
}