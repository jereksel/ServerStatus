package com.jereksel.serverstatus;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionItemTarget;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;

public class MainActivity extends SherlockActivity {

    private String[] idList;
    ShowcaseView sv;
    String username, url, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        Helpers.createDatabase();

        final Context context = this;

        idList = Helpers.dbToIdList();

        String[] result = Helpers.dbToListView();
        final ArrayAdapter<String> listViewAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, result);
        final ListView listView = (ListView) findViewById(R.id.main_listview);
        listView.setAdapter(listViewAdapter);


        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                        listView,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {

                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {

                                    String serverId = idList[position];
                                    Helpers.deleteServer(Integer.parseInt(serverId));
                                    recreateListView();

                                }
                                listViewAdapter.notifyDataSetChanged();
                            }
                        });
        listView.setOnTouchListener(touchListener);
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        listView.setOnScrollListener(touchListener.makeScrollListener());


        listView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {


                final View view_ = view;

                // get prompts.xml view
                LayoutInflater layoutInflater = LayoutInflater.from(context);

                View promptView = layoutInflater.inflate(R.layout.dialog_signin, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

                // set prompts.xml to be the layout file of the alertdialog builder
                alertDialogBuilder.setView(promptView);

                final EditText input = (EditText) promptView.findViewById(R.id.password);

                // setup a dialog window
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // get user input and set it to result
                                // editTextMainScreen.setText(input.getText());

                                password = input.getText().toString();

                                String item = ((TextView) view_).getText().toString();

                                String[] split = item.split("@");

                                username = split[0];
                                url = split[1];

                                Boolean riturn = false;

                                try {
                                    riturn = new getInfo().execute().get();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                }


                                if (riturn) {
                                    Intent intent = new Intent(MainActivity.this, ServerStatus.class);
                                    intent.putExtra("host", item);
                                    intent.putExtra("password", password);
                                    startActivity(intent);
                                } else {

                                    new AlertDialog.Builder(context)
                                            .setTitle(R.string.connectErrorTitle)
                                            .setMessage(R.string.connectErrorBody)
                                            .show();

                                }

                            }
                        })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                // create an alert dialog
                AlertDialog alertD = alertDialogBuilder.create();

                alertD.show();


            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {

                Intent intent = new Intent(MainActivity.this, AddEditServer.class);
                intent.putExtra("edit", "edit");
                intent.putExtra("id", idList[position]);
                startActivityForResult(intent, 0);


                return true;
            }
        });


    }

    private class getInfo extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... arg0) {

            StringBuilder builder = new StringBuilder();
            HttpClient client = new DefaultHttpClient();

            String toSend = "http://" + url + "?username=" + username + "&password=" + password + "&action=check";

            HttpGet httpGet = new HttpGet(toSend);
            try {
                HttpResponse response = client.execute(httpGet);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                } else {
                    Log.e(ServerStatus.class.toString(), "Failed to download file");
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (builder.toString().equals("true")) {
                return true;
            } else {
                return false;
            }

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main, menu);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (prefs.getBoolean("firstrun", true)) {
            // Do first run stuff here then set 'firstrun' as false
            // using the following line to edit/commit prefs

            ActionItemTarget target = new ActionItemTarget(this, R.id.new_connection);
            sv = new ShowcaseView.Builder(this)
                    .setTarget(target)
                    .setContentTitle("To add new server - click here")
                    .setContentText("To delete - swipe" + System.getProperty("line.separator") + "To edit - long press")
                    .doNotBlockTouches()
                    .build();

            prefs.edit().putBoolean("firstrun", false).apply();
        }

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.new_connection) {
            Intent intent = new Intent(MainActivity.this, AddEditServer.class);
            startActivityForResult(intent, 0);
        }
        else if (item.getItemId() == R.id.about) {
            Intent intent = new Intent(MainActivity.this, About.class);
            startActivity(intent);
        }

        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        recreateListView();
    }

    private void recreateListView() {
        String[] result = Helpers.dbToListView();
        idList = Helpers.dbToIdList();
        ArrayAdapter<String> listViewAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, result);
        ListView listView = (ListView) findViewById(R.id.main_listview);
        listView.setAdapter(listViewAdapter);
    }


}
