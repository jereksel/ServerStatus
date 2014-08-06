package com.jereksel.serverstatus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class AddEditServer extends PreferenceActivity {

    private String host, port, username, path;

    private boolean edit = false;
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (!(extras == null)) {

            edit = true;

            id = Integer.parseInt(extras.getString("id"));

            String[] result = Helpers.getInfo(id);

            prefs.edit().putString("username", result[0]).apply();
            prefs.edit().putString("hostname", result[1]).apply();
            prefs.edit().putString("port", result[2]).apply();
            prefs.edit().putString("path", result[3]).apply();

        } else {

            prefs.edit().putString("username", "admin").apply();
            prefs.edit().putString("hostname", "0.0.0.0").apply();
            prefs.edit().putString("port", "80").apply();
            prefs.edit().putString("path", "/").apply();

        }

        addPreferencesFromResource(R.xml.addnewserver);

        Preference button = (Preference) findPreference("button");
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                username = prefs.getString("username", "admin");
                host = prefs.getString("hostname", "0.0.0.0");
                port = prefs.getString("port", "80");
                path = prefs.getString("path", "/");

                if (edit) {

                    Helpers.updateInfo(id, username, host, Integer.parseInt(port), path);


                } else {

                    Helpers.addServer(username, host, Integer.parseInt(port), path);

                }


                Intent data = new Intent();
                setResult(RESULT_OK, data);

                prefs.edit().putString("username", "admin").apply();
                prefs.edit().putString("hostname", "0.0.0.0").apply();
                prefs.edit().putString("port", "80").apply();
                prefs.edit().putString("path", "/").apply();

                finish();


                return true;
            }
        });
    }
}
