package com.freakz.matukaa.examprep;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.freakz.matukaa.examprep.domain.MyEntity;

public class AddActivity extends AppCompatActivity implements MyCallback {

    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        editText = findViewById(R.id.editText);
    }

    public void save(View view) {
        Manager manager = new Manager(getApplication());
        manager.save(new MyEntity(0), this);
    }


    @Override
    public void showError(String message) {
        Snackbar.make(editText, message, Snackbar.LENGTH_INDEFINITE)
                .setAction("DISMISS", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                }).show();
    }

    @Override
    public void clear() {
        finish();
    }
}
