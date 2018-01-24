package com.freakz.matukaa.examprep;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.ProgressBar;

import com.freakz.matukaa.examprep.domain.MyEntity;
import com.freakz.matukaa.examprep.service.ExamService;
import com.freakz.matukaa.examprep.service.ServiceFactory;

import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by Matukaa on 2018-01-24.
 */

class Manager {
    private ExamApp app;
    private ExamService service;

    Manager(Application application){
        app = (ExamApp)application;
        service = ServiceFactory.createRetrofitService(ExamService.class, ExamService.SERVICE_ENDPOINT);
    }

    boolean networkConnectivity(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    void loadEvents(final ProgressBar progressBar, final MyCallback callback) {

        service.getEntities()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<MyEntity>>() {
                    @Override
                    public void onCompleted() {
                        Timber.v("Exam Service completed");
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "Error while loading the entities"); // TODO: rename this
                        callback.showError("Not able to retrieve the data. Displaying local data!");
                    }

                    @Override
                    public void onNext(final List<MyEntity> entities) {
                        new Thread(new Runnable() {
                            public void run() {
                                //app.db.getEntityDao().deleteBooks(); TODO: implement delete
                                //app.db.getEntityDao().addBooks(books); TODO: implement add
                            }
                        }).start();
                        Timber.v("Entities persisted"); // TODO: rename this
                    }
                });
    }

    void save(final MyEntity entity /*TODO: rename this */, final MyCallback callback) {
        service.addEntity(entity)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<MyEntity>() {
                    @Override
                    public void onCompleted() {
                        addDataLocally(entity);
                        Timber.v("Book Service completed");
                        callback.clear();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "Error while persisting an book");
                        callback.showError("Not able to connect to the server, will not persist!");
                    }

                    @Override
                    public void onNext(MyEntity book) {
                        Timber.v("MyEntity persisted");
                    } // TODO: rename this
                });
    }

    private void addDataLocally(final MyEntity entity) { // TODO: rename this
        new Thread(new Runnable() {
            public void run() {
                //app.db.getEntityDao().addBook(book); // TODO: implement add
            }
        }).start();
    }
}
