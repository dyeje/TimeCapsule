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

import java.util.HashMap;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;


public class AsyncDownloader extends AsyncTask<AsyncDownloader.Payload, Object, AsyncDownloader.Payload> {
  public static final String TAG = "async";

  public static final int RETRIEVECAPSULES = 1;
  public static final int GETCAPSULE = 2;
  public static final int NEWCAPSULE = 3;
  public static final int LOGIN = 4;
  public static final int GETUSER = 5;
  public static final int NEWUSER = 6;
  public static final int EDITUSER = 7;
  public static final int GETCOMMENTS = 8;
  public static final int ADDCOMMENT = 9;
  public static final int ADDVIEW = 10;
  public static final int GETRATING = 11;
  public static final int ADDRATING = 12;
  public static final int UPLOADFILE = 13;

  public static final String USERID = "userId";
  public static final String PASSWORD = "password";
  public static final String NAME = "name";
  public static final String LOCATION = "location";
  public static final String STATE = "state";
  public static final String GENDER = "gender";
  public static final String AGE = "age";
  public static final String INTERESTS = "interests";
  public static final String ABOUT = "about";
  public static final String USERNAME = "username";
  public static final String CAPSULEID = "capsuleId";
  public static final String LATITUDE = "lat";
  public static final String LONGITUDE = "lon";
  public static final String TITLE = "title";
  public static final String DESCRIPTION = "description";
  public static final String TO = "to";
  public static final String FROM = "from";
  public static final String RATING = "rating";
  public static final String COMMENT = "comment";
  public static final String FILEPATH = "filePath";
  public static final String INNEROUTERSPLIT = "\n-\r-\t-\r-\n";

  public static void perform(Payload request) {
    new AsyncDownloader().execute(request);
  }

  /*
  * Runs on GUI thread
  */
  @Override
  protected void onPreExecute() {
    Log.i(TAG, "*******GOING*****");
  }

  /*
  * Runs on GUI thread
  */
  @Override
  public void onPostExecute(AsyncDownloader.Payload payload) {
    if (payload.result == null && payload.exception == null)
      payload.exception = new AsyncException("Unknown Error");

    payload.callback.asyncDone(payload);
  }

  /*
  * Runs on background thread
  */
  @Override
  public AsyncDownloader.Payload doInBackground(AsyncDownloader.Payload... _params) {
    Log.d(TAG, "*******BACKGROUND********");

    AsyncDownloader.Payload payload = _params[0];

    if (!isOnline(payload.context.getApplicationContext())) {
      payload.exception = new AsyncException("No Internet Connection");
      return payload;
    }

    payload.result = "";
    HashMap<String, String> params = payload.params;
    String userId = params.get(USERID);
    String password = params.get(PASSWORD);
    String name = params.get(NAME);
    String location = params.get(LOCATION);
    String state = params.get(STATE);
    String gender = params.get(GENDER);
    String age = params.get(AGE);
    String interests = params.get(INTERESTS);
    String about = params.get(ABOUT);
    String username = params.get(USERNAME);
    String capsuleId = params.get(CAPSULEID);
    String lat = params.get(LATITUDE);
    String lon = params.get(LONGITUDE);
    String description = params.get(DESCRIPTION);
    String from = params.get(FROM);
    String to = params.get(TO);
    String rating = params.get(RATING);
    String comment = params.get(COMMENT);
    String filePath = params.get(FILEPATH);

    boolean valid = false;

    switch (payload.taskType) {
      case RETRIEVECAPSULES:
        valid = lat != null && lon != null && to != null && from != null && rating != null;
        if (valid)
          payload.result = retrieveCapsules(lat, lon, from, to, rating);
        else
          payload.exception = new AsyncException("Bad Params");
        break;

      case GETCAPSULE:
        valid = capsuleId != null;
        if (valid)
          payload.result = Server.getCapsule(capsuleId);
        else
          payload.exception = new AsyncException("Bad Params");
        break;

      case NEWCAPSULE:
        valid = userId != null && lat != null && lon != null && description != null;
        if (valid)
          payload.result = Server.newCapsule(userId, lat, lon, description);
        else
          payload.exception = new AsyncException("Bad Params");
        break;

      case LOGIN:
        valid = username != null && password != null;
        if (valid)
          payload.result = Server.authenticate(username, password);
        else
          payload.exception = new AsyncException("Bad Params");
        break;

      case GETUSER:
        valid = userId != null;
        if (valid)
          payload.result = Server.getUser(userId);
        else
          payload.exception = new AsyncException("Bad Params");
        break;

      case NEWUSER:
        valid = name != null && location != null && state != null && gender != null && age != null && interests != null && about != null && password != null && username != null;
        if (valid)
          payload.result = Server.newUser(name, location, state, gender, age, interests, about, password, username);
        else
          payload.exception = new AsyncException("Bad Params");
        break;

      case EDITUSER:
        valid = userId != null && name != null && location != null && state != null && gender != null && age != null && interests != null && about != null && password != null && username != null;
        if (valid)
          payload.result = Server.editUser(userId, name, location, state, gender, age, interests, about, password, username);
        else
          payload.exception = new AsyncException("Bad Params");
        break;

      case GETCOMMENTS:
        valid = capsuleId != null;
        if (valid)
          payload.result = Server.getComments(capsuleId);
        else
          payload.exception = new AsyncException("Bad Params");
        break;

      case ADDCOMMENT:
        valid = userId != null && capsuleId != null && comment != null;
        if (valid)
          payload.result = Server.addComment(userId, capsuleId, comment);
        else
          payload.exception = new AsyncException("Bad Params");
        break;

      case ADDVIEW:
        valid = userId != null && capsuleId != null;
        if (valid)
          payload.result = Server.addAView(userId, capsuleId);
        else
          payload.exception = new AsyncException("Bad Params");
        break;

      case GETRATING:
        valid = capsuleId != null;
        if (valid)
          payload.result = Server.getRating(capsuleId);
        else
          payload.exception = new AsyncException("Bad Params");
        break;

      case ADDRATING:
        valid = userId != null && capsuleId != null && rating != null;
        if (valid)
          payload.result = Server.addRating(userId, capsuleId, rating);
        else
          payload.exception = new AsyncException("Bad Params");
        break;

      case UPLOADFILE:
        valid = userId != null && filePath != null;
        if (valid)
          payload.result = Boolean.toString(Server.uploadFile(filePath));
        else
          payload.exception = new AsyncException("Bad Params");
        break;
      default:
        payload.exception = new AsyncException("[" + payload.taskType + "] is not a valid task type");
        break;
    }


    return payload;
  }

  public static class Payload {
    public int taskType;
    public AsyncCallbackListener callback;
    public Context context;
    public HashMap<String, String> params;
    public String result;
    public Exception exception;

    public Payload(int taskType, HashMap<String, String> params, AsyncCallbackListener callback, Context context) {
      this.taskType = taskType;
      this.callback = callback;
      this.params = params;
      this.context = context;
    }

    public String errorString() {
      return this.exception.getMessage() + " [ID-" + this.taskType + "-T]";
    }
  }

  protected String retrieveCapsules(String lat, String lon, String from, String to, String minRating) {

    final String retrieveInner = Server.getCapsules(lat, lon, "1", from, to, minRating);
    final String retrieveOuter = Server.getCapsules(lat, lon, "2", from, to, minRating);
    return retrieveInner + INNEROUTERSPLIT + retrieveOuter;
  }

  public boolean isOnline(Context mainContext) {
    ConnectivityManager cm = (ConnectivityManager) mainContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo info = cm.getActiveNetworkInfo();
    return (info != null && info.isConnected());
  }
}
