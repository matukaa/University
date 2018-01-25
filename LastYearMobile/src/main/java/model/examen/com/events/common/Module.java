package model.examen.com.events.common;

import android.app.ProgressDialog;

import com.google.inject.AbstractModule;

import model.examen.com.events.services.ApiService;
import model.examen.com.events.services.ApiServiceProvider;
import roboguice.inject.SharedPreferencesName;

/**
 * Created by iulia on 1/10/2017.
 */

public class Module extends AbstractModule {

    @Override
    protected void configure() {
        bind(ApiService.class).toProvider(ApiServiceProvider.class);
        bindConstant().annotatedWith(SharedPreferencesName.class).to("preferences");

        requestStaticInjection(Module.class);
    }
}
