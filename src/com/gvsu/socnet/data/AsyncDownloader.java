/** AsyncTask.java */
package com.gvsu.socnet.data;

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
 * This file has been modified by Caleb Gomer
 */

import java.util.Calendar;
import android.os.AsyncTask;
import android.util.Log;


public class AsyncDownloader extends AsyncTask<AsyncDownloader.Payload, Object, AsyncDownloader.Payload> {
    public static final String TAG = "async";

    public static final int RETRIEVECAPSULES = 1;
    public static final int GETCAPSULE= 10;
    public static final int NEWCAPSULE = 2;
    public static final int LOGIN = 3;
    public static final int GETUSER = 4;
    public static final int NEWUSER = 11;
    public static final int EDITUSER = 5;
    public static final int GETCOMMENTS = 6;
    public static final int ADDCOMMENT = 7;
    public static final int GETRATING = 8;
    public static final int ADDRATING = 9;

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
      AsyncCallbackListener app = (AsyncCallbackListener) payload.data[0];
      if (payload.result != null && !payload.result[1].equals("error")) {
        // Present the result on success
        app.asyncSuccess(payload.result);
      } else {
        app.asyncFailure(payload.result);
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

        payload.result = new String[] {Integer.toString(payload.taskType),""};

        Object[] data = (Object[]) payload.data[1];
        String result = "";


        //stupid java scoping...
        String userId = "";
        String password = "";
        String name = "";
        String location = "";
        String state = "";
        String gender = "";
        String age = "";
        String interests = "";
        String about = "";
        String username = "";
        //end stupid java scoping...


        switch (payload.taskType) {
          case RETRIEVECAPSULES:
            double dLat = (Double) data[0];
            double dLng = (Double) data[1];
            long startTime = (Long) data[2];
            long endTime = (Long) data[3];
            double dMinRating = (Float) data[4];
            String lastRetrieve = (String) data[5];

            String results = retrieveCapsules(dLat, dLng, startTime, endTime, dMinRating, lastRetrieve);

            payload.result[1] = results;

            break;
          case GETCAPSULE:
            String capsuleId = (String) data[0];

            result = Server.getCapsule(capsuleId);

            payload.result[1] = result;
            break;
          case NEWCAPSULE:
            userId = (String) data[0];
            String lat = (String) data[1];
            String lon = (String) data[2];
            String title = (String) data[3];
            String description = (String) data[4];

            result = Server.newCapsule(userId,lat,lon,title,description);

            payload.result[1] = result;
            break;
          case LOGIN:
            userId = (String) data[0];
            password = (String) data[1];

            result = Server.authenticate(userId,password);

            payload.result[1] = result;
            break;
          case GETUSER:
            userId = (String) data[0];

            result = Server.getUser(userId);

            payload.result[1] = result;
            break;
          case NEWUSER:
            name = (String) data[0];
            location = (String) data[1];
            state = (String) data[2];
            gender = (String) data[3];
            age = (String) data[4];
            interests = (String) data[5];
            about = (String) data[6];
            password = (String) data[7];
            username = (String) data[8];

            result = Server.newUser(name,location,state,gender,age,interests,about,password,username);

            payload.result[1] = result;
            break;
          case EDITUSER:
            userId = (String) data[0];
            name = (String) data[1];
            location = (String) data[2];
            state = (String) data[3];
            gender = (String) data[4];
            age = (String) data[5];
            interests = (String) data[6];
            about = (String) data[7];
            password = (String) data[8];
            username = (String) data[9];

            result = Server.editUser(userId,name,location,state,gender,age,interests,about,password,username);

            payload.result[1] = result;
            break;
          case GETCOMMENTS:
            capsuleId = (String) data[0];

            result = Server.getComments(capsuleId);

            payload.result[1] = result;
            break;
          case ADDCOMMENT:
            userId = (String) data[0];
            capsuleId = (String) data[1];
            String comment = (String) data[2];

            result = Server.addComment(userId,capsuleId,comment);

            payload.result[1] = result;
            break;
          case GETRATING:
            capsuleId = (String) data[0];

            result = Server.getRating(capsuleId);

            payload.result[1] = result;
            break;
          case ADDRATING:
            userId = (String) data[0];
            capsuleId = (String) data[1];
            String rating = (String) data[2];

            result = Server.addRating(userId,capsuleId,rating);

            payload.result[1] = result;
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
        public String[] result;
        public Exception exception;

        public Payload(int taskType, Object[] data) {
            this.taskType = taskType;
            this.data = data;
        }
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

    protected String retrieveCapsules(Double dLat, Double dLng, Long startTime, Long endTime, double dMinRating, String lastRetrieve) {
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
        Log.v("map", "finished collecting capsules");

        /**************************/
        // Debug.stopMethodTracing();
        /**************************/

        return retrieveInner+"\n-\r-\t-\r-\n"+retrieveOuter;
    }
}
