package com.freakz.matukaa.examprep.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.freakz.matukaa.examprep.domain.MyEntity;

/**
 * Created by Matukaa on 2018-01-24.
 */

@Database(entities = {MyEntity.class}, version = 1)
@TypeConverters({Converter.class})
public abstract class ExamDatabase extends RoomDatabase {
    public abstract EntityDao getEntityDao();
}
