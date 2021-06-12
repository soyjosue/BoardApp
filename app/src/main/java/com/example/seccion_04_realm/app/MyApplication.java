package com.example.seccion_04_realm.app;

import android.app.Application;

import com.example.seccion_04_realm.models.Board;
import com.example.seccion_04_realm.models.Note;

import java.util.concurrent.atomic.AtomicInteger;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class MyApplication extends Application {

    public static AtomicInteger BoardID = new AtomicInteger();
    public static AtomicInteger NoteID = new AtomicInteger();

    @Override
    public void onCreate() {
        // Inicializando la configuracion del REALM
        setUpRealmConfig();
        super.onCreate();
        // Creando la base de datos
        Realm realm = Realm.getDefaultInstance();

        // Agregando el ID que se puede comenzar a agregar datos
        BoardID = getIdByTable(realm, Board.class);
        NoteID = getIdByTable(realm, Note.class);

        // Cerrando Base de datos
        realm.close();
    }

    private void setUpRealmConfig() {

        // Agregando el contexto al REALM
        Realm.init(getApplicationContext());

        // Creando configuracion del REALM
        RealmConfiguration config = new RealmConfiguration
                .Builder()
                .deleteRealmIfMigrationNeeded()
                .build();

        // Aplicando la configuracion al REALM
        Realm.setDefaultConfiguration(config);
    }

    private <T extends RealmObject> AtomicInteger getIdByTable(Realm realm, Class<T> anyClass) {
        RealmResults<T> results = realm.where(anyClass).findAll();
        return (results.size() > 0) ? new AtomicInteger(results.max("id").intValue()) : new AtomicInteger();
    }

}
