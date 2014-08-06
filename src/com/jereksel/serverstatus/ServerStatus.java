package com.jereksel.serverstatus;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.echo.holographlibrary.PieGraph;
import com.echo.holographlibrary.PieSlice;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class ServerStatus extends SherlockListActivity {

    private StringBuilder builder;
    private ProgressDialog pDialog;
    private String url, username, hostname, kernel, password, uptime;
    private JSONObject jObject;
    private MyCustomAdapter mAdapter;
    private int ram_1, ram_2;
    private String[] processes;

    private String[] info;

/*
info array structure:
(0) hostname
(1) kernel
(2) ram_used
(3) ram_buffers/cached
(4) ram_free
(5) uptime
(6) date
*/

    private int a;

    //TODO: SWAP
    //TODO: Processes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createView();
    }


    private void createView() {

        a = 0;

        Bundle extras = getIntent().getExtras();
        String newString = extras.getString("host");
        password = extras.getString("password");

        String[] split = newString.split("@");

        username = split[0];
        url = split[1];

        info = new String[7];
        processes = new String[6];

        try {
            new getInfo().execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        try {
            jObject = new JSONObject(builder.toString());
            info[0] = jObject.getString("hostname");
            info[1] = jObject.getString("kernel");
            info[2] = jObject.getString("ram_used");
            info[3] = jObject.getString("ram_buffers");
            info[4] = jObject.getString("ram_free");
            info[5] = jObject.getString("uptime");
            info[6] = jObject.getString("date");

            JSONArray jArray = jObject.getJSONArray("processes");

            for (int i = 0; i < jArray.length(); i++) {

                JSONObject json_data = jArray.getJSONObject(i);

                processes[a] = json_data.getString("usage");

                a++;

                processes[a] = json_data.getString("process");

                a++;


            }

        } catch (JSONException e) {
            e.printStackTrace();
            finish();
        }

        mAdapter = new MyCustomAdapter(info, processes);

        mAdapter.addItem("item");
        mAdapter.addItem("item");
        mAdapter.addItem("item");
        setListAdapter(mAdapter);

    }


    private class getInfo extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {

            builder = new StringBuilder();
            HttpClient client = new DefaultHttpClient();

            String toSend = "http://" + url + "?username=" + username + "&password=" + password + "&action=overall";

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

            return null;

        }

    }


    private class MyCustomAdapter extends BaseAdapter {

        private ArrayList mData = new ArrayList();
        private LayoutInflater mInflater;
        private String[] processes;
        private String[] info;

        public MyCustomAdapter(String[] info, String[] processes) {
            this.info = info;
            this.processes = processes;
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void addItem(final String item) {
            mData.add(item);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            int type = getItemViewType(position);
            if (convertView == null) {
                holder = new ViewHolder();
                switch (position) {
                    case 0:
                        convertView = mInflater.inflate(R.layout.server_status_1, null);
                        holder.textView = (TextView) convertView.findViewById(R.id.hostname);
                        holder.textView.setText(getResources().getString(R.string.hostname_only) + ": " + info[0]);
                        holder.textView = (TextView) convertView.findViewById(R.id.kernel);
                        holder.textView.setText(getResources().getString(R.string.kernel) + ": " + info[1]);
                        holder.textView = (TextView) convertView.findViewById(R.id.uptime);
                        holder.textView.setText(getResources().getString(R.string.uptime) + ": " + info[5]);
                        holder.textView = (TextView) convertView.findViewById(R.id.date);
                        holder.textView.setText(getResources().getString(R.string.date) + ": " + info[6]);
                        break;
                    case 1:
                        convertView = mInflater.inflate(R.layout.server_status_2, null);

                        PieGraph pg = (PieGraph) convertView.findViewById(R.id.graph);

                        int ram_used = Integer.parseInt(info[2]);
                        int ram_cached = Integer.parseInt(info[3]);
                        int ram_free = Integer.parseInt(info[4]);


                        PieSlice slice = new PieSlice();
                        slice.setColor(Color.parseColor("#3300cc"));
                        slice.setValue(ram_used);
                        pg.addSlice(slice);
                        slice = new PieSlice();
                        slice.setColor(Color.parseColor("#0099cc"));
                        slice.setValue(ram_cached);
                        pg.addSlice(slice);
                        slice = new PieSlice();
                        slice.setColor(Color.parseColor("#FFFFFF"));
                        slice.setValue(ram_free);
                        pg.addSlice(slice);

                        break;

                    case 2:
                        convertView = mInflater.inflate(R.layout.server_status_3, null);

                        holder.textView = (TextView) convertView.findViewById(R.id.process_1);
                        holder.textView.setText(processes[0] + ": " + processes[1]);
                        holder.textView = (TextView) convertView.findViewById(R.id.process_2);
                        holder.textView.setText(processes[2] + ": " + processes[3]);
                        holder.textView = (TextView) convertView.findViewById(R.id.process_3);
                        holder.textView.setText(processes[4] + ": " + processes[5]);

                        break;
                }
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            //holder.textView.setText(mData.get(position));
            return convertView;
        }

    }

    public static class ViewHolder {
        public TextView textView;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.detailed, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            createView();
        } else if (item.getItemId() == R.id.about) {
            Intent intent = new Intent(ServerStatus.this, About.class);
            startActivity(intent);
        }
        return true;
    }


}

