package com.freakz.matukaa.examprep;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.freakz.matukaa.examprep.adapters.ListAdapter;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements MyCallback {

    private ListAdapter adapter;

    ProgressBar progressBar;

    FloatingActionButton fab;
    private View recyclerView;
    private Manager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manager = new Manager(getApplication());
        fab = findViewById(R.id.addFab);
        progressBar = findViewById(R.id.loadingBar);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        recyclerView = findViewById(R.id.item_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView)recyclerView);
        loadItems();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Timber.d("Back in main activity");
    }

    private boolean loadItems() {
        boolean connectivity = manager.networkConnectivity(getApplicationContext());
        if (connectivity){
            fab.setVisibility(View.VISIBLE);
        } else {
            fab.setVisibility(View.GONE);
            showError("No internet connection");
        }
        manager.loadEvents(progressBar, this);
        return connectivity;
    }

    @Override
    public void showError(String message) {
        progressBar.setVisibility(View.GONE);
        Snackbar.make(recyclerView, message, Snackbar.LENGTH_INDEFINITE)
                .setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        loadItems();
                    }
                }).show();
    }

    @Override
    public void clear() {
        adapter.clear();
    }

    public void onAddClick(View view) {
        Intent intent = new Intent(getApplication(), AddActivity.class);
        startActivityForResult(intent, 10000);
    }

    public void onRefreshClick(View view) {
        manager.loadEvents(progressBar, this);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        adapter = new ListAdapter();
        /*((ExamApp) getApplication()).db.getEntityDao().getEntities() TODO: fix and rename entities
                .observe(this, new Observer<List<MyEntity>>() {
                    @Override
                    public void onChanged(@Nullable List<MyEntity> entities) {
                        adapter.setData(entities);
                    }
                });*/
        recyclerView.setAdapter(adapter);
    }
}
