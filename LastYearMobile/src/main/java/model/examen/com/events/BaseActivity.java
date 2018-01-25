package model.examen.com.events;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import roboguice.activity.RoboActionBarActivity;

/**
 * Created by iulia on 1/10/2017.
 */

public class BaseActivity extends RoboActionBarActivity {

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.d(getClass().getSimpleName(), "onCreate");
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d(getClass().getSimpleName(), "onResume");
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.d(getClass().getSimpleName(), "onPause");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.d(getClass().getSimpleName(), "onDestroy");
    }

    protected void startProgressDialog(String message){
        Log.d(getClass().getSimpleName(), "start progress dialog");
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    protected void stopProgressDialog(){
        if(progressDialog != null) {
            Log.d(getClass().getSimpleName(), "stop progress dialog");
            progressDialog.dismiss();
            progressDialog.hide();
        }
    }

    protected boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

}
