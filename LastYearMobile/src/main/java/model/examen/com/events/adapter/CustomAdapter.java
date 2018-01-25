package model.examen.com.events.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import model.examen.com.events.R;
import model.examen.com.events.model.Note;
import model.examen.com.events.utils.DateUtils;

/**
 * Created by iulia on 1/10/2017.
 */

public class CustomAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<Note> notes = new ArrayList<>();
    private Context context;

    public CustomAdapter(Context context, List<Note> notes){
        this.notes = notes;
        this.context = context;
    }

    @Override
    public int getCount() {
        if(notes != null) {
            return notes.size();
        }
        return 0;
    }

    @Override
    public Note getItem(int i) {
        return notes.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        if (inflater == null)
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (view == null) {
            view = View.inflate(context, R.layout.listview_cell, null);
        }

        TextView text = (TextView) view.findViewById(R.id.text);
        TextView date = (TextView) view.findViewById(R.id.date);

        if(position <= notes.size()) {
            Note note = notes.get(position);

            if (note != null) {
                text.setText(note.getText());
                text.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);

                if (note.getDate() != null && !note.getDate().isEmpty()) {
                    try {
                        Date date1 = DateUtils.parseDate(note.getDate());
                        date.setText(DateUtils.changeDateFormat(date1));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                text.setText("Loading...");
                text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                date.setText("");
            }
        }

        notifyDataSetChanged();
        return view;
    }
}
