package model.examen.com.events;

import android.content.Context;

import roboguice.RoboGuice;

/**
 * Created by iulia on 1/10/2017.
 */

public class Application extends android.app.Application {
    private static Context context;

    @Override
    public void onCreate(){
        super.onCreate();
        RoboGuice.setUseAnnotationDatabases(false);
        RoboGuice.injectMembers(this, this);
        context = this;
    }

    @Override
    public void onTerminate(){
        super.onTerminate();
    }

    public static Context getContext(){
        return context;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}
