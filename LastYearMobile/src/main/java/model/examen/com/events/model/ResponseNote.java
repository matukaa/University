package model.examen.com.events.model;

import java.util.List;

/**
 * Created by iulia on 1/25/2017.
 */

public class ResponseNote {
    private int page;
    private List<Note> notes;
    private boolean more;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    public boolean isMore() {
        return more;
    }

    public void setMore(boolean more) {
        this.more = more;
    }
}
