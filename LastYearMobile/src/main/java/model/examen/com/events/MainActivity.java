package model.examen.com.events;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import model.examen.com.events.adapter.CustomAdapter;
import model.examen.com.events.model.Note;
import model.examen.com.events.model.ResponseNote;
import model.examen.com.events.services.ApiService;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@ContentView(R.layout.activity_main)
public class MainActivity extends BaseActivity {
    private static final String LIST_FROM_SHARED_PREFERENCES = "notes";
    private static final String LAST_MODIFIED = "LAST_MODIFIED";
    private static final String NOTE = "note";

    private String TAG = MainActivity.class.getSimpleName();

    @InjectView(R.id.listview)
    private ListView listView;

    @InjectView(R.id.status)
    private TextView status;

    CustomAdapter adapter;

    @Inject
    private ApiService apiService;

    @Inject
    private SharedPreferences sharedPreferences;

    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkStateReceiver, filter);

        if(isNetworkConnected()) {
            Handler h = new Handler();
            int delay = 8000;

            h.postDelayed(new Runnable() {
                public void run() {
                    page = 1;
                    notes.clear();
                    callMaked = true;
                    getNotesFromServer(page);
                    h.postDelayed(this, delay);
                }
            }, delay);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(adapter.getItem(position) != null) {
                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                    intent.putExtra(NOTE, adapter.getItem(position));
                    startActivity(intent);
                }
            }
        });
    }

    private int page = 1;
    private boolean more;

    @Override
    protected void onResume() {
        super.onResume();

        notes.clear();

        if (isNetworkConnected()) {

        } else {
            List<Note> noteList = getListFromSharedPreferences(LIST_FROM_SHARED_PREFERENCES);
            setListViewAdapter(noteList);
            stopProgressDialog();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkStateReceiver);
    }

    List<Note> notes = new ArrayList<>();
    boolean isMore = true;
    boolean callMaked = false;

    private void getNotesFromServer(int page) {
        Log.d(TAG, "get notes from server");
        startProgressDialog("Loading...");
        apiService.getNotes(sharedPreferences.getString(LAST_MODIFIED, ""), page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Subscriber<ResponseNote>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e1) {
                        Throwable e = e1;
                        if(e.getMessage() != null && e.getMessage().trim().equals("HTTP 304 Not Modified") && isMore == false) {
                            setListViewAdapter(getListFromSharedPreferences(LIST_FROM_SHARED_PREFERENCES));
                            stopProgressDialog();
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();

                        } else if (!e.getMessage().startsWith("failed to connect to /10.0.2.2")) {
                            Log.e(TAG, "failed to get notes from server", e);
//                            setListViewAdapter(getListFromSharedPreferences(LIST_FROM_SHARED_PREFERENCES));
                            stopProgressDialog();
//                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            alertDialog = new AlertDialog.Builder(MainActivity.this)
                                    .setMessage("Loading failed - " + e.getMessage() + " touch retry")
                                    .setNeutralButton("Retry", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            getNotesFromServer(MainActivity.this.page);
                                        }
                                    }).create();
                            alertDialog.show();
                        }
                        stopProgressDialog();
                    }

                    @Override
                    public void onNext(ResponseNote responseNote1) {
                        Log.d(TAG, "get notes from server - success");
                        callMaked = true;

                        ResponseNote responseNote = responseNote1;
                        if (responseNote.isMore() || responseNote.getNotes().size() > 0) {
                            Log.d("isMore", "true");

                            if(MainActivity.this.page >= 2){
                                status.setVisibility(View.GONE);
                            }
                            notes.addAll(responseNote.getNotes());
                            MainActivity.this.page += 1;
                            notes.add(null);
                            setListViewAdapter(notes);
                            stopProgressDialog();
                            if(notes.size() >= 1) {
                                saveListToSharedPreferences(LIST_FROM_SHARED_PREFERENCES, notes);
                            }
                            getNotesFromServer(MainActivity.this.page);
                        } else if(!responseNote.isMore() && responseNote.getNotes().size() == 0){
//                            notes.clear();
                            Log.d("isMore", "false");
                            isMore = false;
                            String lastModified = new Date().toString();
                            sharedPreferences.edit().putString(LAST_MODIFIED, lastModified).apply();
                            MainActivity.this.page = 1;
                            stopProgressDialog();
                        }
                        stopProgressDialog();
                    }
                });
    }

    private void saveListToSharedPreferences(String key, List<Note> notes) {
        Gson gson = new Gson();
        String json = gson.toJson(notes);
        sharedPreferences.edit().putString(key, json).apply();
    }

    private List<Note> getListFromSharedPreferences(String key) {
        Log.d(TAG, "get notes from shared preferences");

        Gson gson = new Gson();
        String jsonPreferences = sharedPreferences.getString(key, "");

        Type type = new TypeToken<List<Note>>() {
        }.getType();

        return gson.fromJson(jsonPreferences, type);
    }

    private void setListViewAdapter(List<Note> notes) {
        Log.d(TAG, "set listview adapter");
        adapter = new CustomAdapter(MainActivity.this, notes);
        listView.setAdapter(adapter);
    }


    BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetInfo != null) {
                status.setVisibility(View.VISIBLE);
                status.setText("Online");
                page = 1;
                if(!callMaked) {
                    getNotesFromServer(page);
                }
            } else {
                status.setVisibility(View.VISIBLE);
                callMaked = false;
                status.setText("Offline");
            }
        }
    };
}