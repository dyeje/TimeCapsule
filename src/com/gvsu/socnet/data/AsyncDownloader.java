/** AsyncTask.java */
package com.gvsu.socnet.data;

/****************************************************************
 * com.ciscomputingclub.silencer.AsyncTask
 * @author
 * @version 1.0
 ***************************************************************/

/*
 * Copyright (C) 2009 
 * Jayesh Salvi <jayesh@altcanvas.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * 
 * 
 * 
 * This file has been modified by GV-Computing-Club
 */

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Hashtable;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import android.util.Log;
import com.gvsu.socnet.data.AsyncException;
import com.gvsu.socnet.map.CapsuleMapActivity;
import com.gvsu.socnet.map.FilterActivity;


public class AsyncDownloader extends AsyncTask<AsyncDownloader.Payload, Object, AsyncDownloader.Payload> {
    public static final String TAG = "async";

    public static final int RETRIEVECAPSULES = 1;

    /*
      * Runs on GUI thread
      */
    @Override
    protected void onPreExecute() {
        Log.i(TAG,"*******GOING*****");
    }

    /*
      * Runs on GUI thread
      */
    @Override
    public void onPostExecute(AsyncDownloader.Payload payload) {

        switch (payload.taskType) {

            case RETRIEVECAPSULES:
                CapsuleMapActivity app = (CapsuleMapActivity) payload.data[0];

                if (payload.result != null) {

                    // Present the result on success
                    String[] result = (String[]) payload.result;
                    loginSuccess(app, result);

                } else {
                    String msg = (payload.exception != null) ? payload.exception.toString() : "";
                    retrieveFailed(app, msg);
                }

                break;
        }
    }

    /*
      * Runs on background thread
      */
    @Override
    public AsyncDownloader.Payload doInBackground(AsyncDownloader.Payload... params) {
      Log.d(TAG,"*******BACKGROUND********");
      AsyncDownloader.Payload payload = params[0];

      try {
        switch (payload.taskType) {
          case RETRIEVECAPSULES:
            Object[] data = (Object[]) payload.data[1];
            double dLat = (Double) data[0];
            double dLng = (Double) data[1];
            long startTime = (Long) data[2];
            long endTime = (Long) data[3];
            double dMinRating = (Float) data[4];
            String lastRetrieve = (String) data[5];

            String[] results = retrieveCapsules(dLat, dLng, startTime, endTime, dMinRating, lastRetrieve);

            payload.result = results;

            break;
        }
      } catch (Exception ae) {
        payload.exception = ae;
        payload.result = null;
      }

      return payload;
    }

    public static class Payload {
        public int taskType;
        public Object[] data;
        public Object result;
        public Exception exception;

        public Payload(int taskType, Object[] data) {
            this.taskType = taskType;
            this.data = data;
        }
    }

    private void retrieveFailed(CapsuleMapActivity app, CharSequence message) {
      try {
        app.setProgressBarIndeterminateVisibility(false);
      } catch (NullPointerException ne) {
      }
    }

    private void loginSuccess(CapsuleMapActivity app, String[] result) {
      app.retrieveSuccess(result);
    }

    /****************************************************************
     * figure out start or end time
     * @param time
     * @return int
     ***************************************************************/
    private int processTime(String time) {
      boolean add12 = false;
      String[] start = time.split(":");
      String startBase = start[0].trim() + start[1].substring(0, 2);
      if ((start[1].substring(3).equals("pm"))) {
        if (start[0].length() == 1) {
          add12 = true;
        } else if (start[0].length() > 1 && !start[0].substring(0, 2).equals("12")) {
          add12 = true;
        }
      }
      int startTime = Integer.parseInt(startBase);
      if (add12) {
        startTime += 1200;
      }
      return startTime;
    }

    protected String[] retrieveCapsules(Double dLat, Double dLng, Long startTime, Long endTime, double dMinRating, String lastRetrieve) {
        /**************************/
        // Debug.startMethodTracing("map_retrieve");
        /**************************/

        String lat = Double.toString(dLat);
        String lng = Double.toString(dLng);

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(startTime);
        String from = "";

        if (c.getTimeInMillis() != 0L) {
            from = c.get(Calendar.YEAR) + "/" + (c.get(Calendar.MONTH) + 1) + "/" + c.get(Calendar.DAY_OF_MONTH);
        }

        c.setTimeInMillis(endTime);
        String to = "";
        if (c.getTimeInMillis() != 0L) {
            to = c.get(Calendar.YEAR) + "/" + (c.get(Calendar.MONTH) + 1) + "/" + c.get(Calendar.DAY_OF_MONTH);
        }

        String minRating = Integer.toString((int)dMinRating);

        final String retrieveInner = Server.getCapsules(lat, lng, "1", from, to, minRating);
        final String retrieveOuter = Server.getCapsules(lat, lng, "2", from, to, minRating);
        String retrieve = retrieveInner + retrieveOuter;

        if (lastRetrieve == null || lastRetrieve.equals(retrieve)) {
            lastRetrieve = retrieve;
            /** new way to update map **/
        }

        Log.v("map", "finished collecting capsules");

        /**************************/
        // Debug.stopMethodTracing();
        /**************************/

        return new String[] {retrieveInner,retrieveOuter};
    }
}
