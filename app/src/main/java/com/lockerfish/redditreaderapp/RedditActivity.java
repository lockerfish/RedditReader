package com.lockerfish.redditreaderapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedditActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final String TAG = getClass().getSimpleName();
    private final boolean D = Log.isLoggable(TAG, Log.DEBUG);
    private ArrayAdapter<String> mRedditAdapter;
    private HashMap<String,String> mRedditResults = new HashMap<String,String>();
    public static final String PREFS_NAME = "reddit-androiddev";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (D) { Log.d(TAG, "Starting OnCreate"); }

        // (1)
        // create some dummy data
        String[] data = {
//                "TraincVube Mobile Challenge: We The Best",
//                "Google I/O is finally here",
//                "Android N is for Nutella",
//                "Android Studio on Windows sucks"
        };
        List<String> reddit = new ArrayList<String>(Arrays.asList(data));
        // (2)
        // ArrayAdapter will take data from a source (data) and
        // use it to populate the listView it's attached to
        mRedditAdapter = new ArrayAdapter<String>(
                this, // context
                R.layout.reddit_list_item, // layout to use for each item
                R.id.reddit_list_item_textview, // textview to update with each item
                reddit // data to display
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reddit);

        // (3)
        // attach listview
        ListView listView = (ListView) findViewById(R.id.listview_reddit);
        listView.setOnItemClickListener(new ItemClickListener());
        listView.setAdapter(mRedditAdapter);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        updateAdapterWithRedditResults(sharedPreferences);

        RedditService.startActionFetch(this);

        // (4)
        // connect network

        if (D) { Log.d(TAG, "Ending OnCreate"); }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateAdapterWithRedditResults(sharedPreferences);
    }

    private void updateAdapterWithRedditResults(SharedPreferences sharedPreferences) {
        if (!sharedPreferences.getAll().isEmpty()) {
            for (Map.Entry<String, ?> entry: sharedPreferences.getAll().entrySet()) {
                mRedditResults.put(entry.getKey(), entry.getValue().toString());
            }
            mRedditAdapter.addAll(mRedditResults.keySet());
        }
    }

    public class ItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
            String title = (String) adapter.getItemAtPosition(position);
            String url = mRedditResults.get(title);
            Intent intent = new Intent(Intent.ACTION_VIEW);

            if (D) { Log.d(TAG, String.format("title: %s, url: %s", title, url));}

            Uri uri = Uri.parse(url);
            intent.setData(uri);
            startActivity(intent);

            if (D) { Log.d(TAG, "onItemClick");}

        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.action_refresh) {
            // start reddit task here
            if (D) { Log.i(TAG, "Starting reddit AsyncTask here"); }

            // TODO: manual refresh
        }
        return true;
    }

}
