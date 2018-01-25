package model.examen.com.events.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by iulia on 1/10/2017.
 */

public class Note implements Serializable {
    private int id;
    private String text;
    private String date;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
