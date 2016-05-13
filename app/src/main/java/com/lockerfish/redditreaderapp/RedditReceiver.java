package com.lockerfish.redditreaderapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

public class RedditReceiver extends BroadcastReceiver {

    public static final String ACTION_REFRESH = "lockerfish.intent.action.RESULTS_RECEIVED";

    public RedditReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        HashMap<String, String> results = (HashMap) intent.getSerializableExtra(RedditService.REDDIT_RESULTS);

        SharedPreferences.Editor editor = context.getSharedPreferences(RedditActivity.PREFS_NAME, Context.MODE_PRIVATE).edit();
        for (Map.Entry<String, String> entry: results.entrySet()) {
            editor.putString(entry.getKey(), entry.getValue());
        }
        editor.commit();

    }
}
