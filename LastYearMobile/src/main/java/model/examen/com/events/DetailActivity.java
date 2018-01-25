package model.examen.com.events;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.ResponseBody;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import model.examen.com.events.model.Note;
import model.examen.com.events.services.ApiService;
import model.examen.com.events.utils.DateUtils;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by iulia on 1/10/2017.
 */

@ContentView(R.layout.activity_add)
public class DetailActivity extends BaseActivity {
    private static final String TAG = DetailActivity.class.getSimpleName();

    private static final String NOTE = "note";

    @InjectView(R.id.note_id)
    private TextView id;

    @InjectView(R.id.text)
    private TextView text;

    @InjectView(R.id.date)
    private TextView date;

    @InjectView(R.id.delete)
    private Button delete;

    @Inject
    private ApiService apiService;

    Note note = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (getIntent().getExtras() != null)
            note = (Note) getIntent().getSerializableExtra(NOTE);

        if(note != null){
            id.setText(note.getId()+"");
            text.setText(note.getText());

            if (note.getDate() != null && !note.getDate().isEmpty()) {
                try {
                    Date date1 = DateUtils.parseDate(note.getDate());
                    date.setText(DateUtils.changeDateFormat(date1));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNetworkConnected()) {
                    if(note != null) {
                        sendInformationToServer(note.getId());
                    }
                } else {
                    Toast.makeText(DetailActivity.this, "You are offline", Toast.LENGTH_SHORT).show();
                }
            }
        });

        text.setSingleLine(true);

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCalendar();
            }
        });
    }

    public void sendInformationToServer(int id){
        startProgressDialog("Deleting, please wait");

        Log.d(TAG, "delete object from server");

        apiService.deleteNote(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Subscriber<ResponseBody>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        stopProgressDialog();
                        Log.e(TAG, "failed to delete object to server", e);
                        Toast.makeText(DetailActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNext(ResponseBody response) {
                        Log.d(TAG, "delete object to server - success");
                        stopProgressDialog();
                        finish();
                    }
                });
    }

    private void showCalendar() {
        Calendar newCalendar = Calendar.getInstance();
        DatePickerDialog fromDatePickerDialog = new DatePickerDialog(DetailActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, monthOfYear, dayOfMonth);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            date.setText(dateFormat.format(newDate.getTime()));
        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        fromDatePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        fromDatePickerDialog.show();
    }

}
