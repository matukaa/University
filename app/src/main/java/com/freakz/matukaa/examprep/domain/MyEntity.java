package com.freakz.matukaa.examprep.domain;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by Matukaa on 2018-01-24.
 */

@Entity
public class MyEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;

    public MyEntity(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
