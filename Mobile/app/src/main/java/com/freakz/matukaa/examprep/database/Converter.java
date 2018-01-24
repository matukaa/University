package com.freakz.matukaa.examprep.database;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

/**
 * Created by Matukaa on 2018-01-24.
 */

public class Converter {
    @TypeConverter
    public Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
