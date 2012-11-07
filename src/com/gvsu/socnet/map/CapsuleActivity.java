package com.gvsu.socnet.map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.gvsu.socnet.data.AsyncCallbackListener;
import com.gvsu.socnet.data.AsyncDownloader;
import com.gvsu.socnet.data.Comment;
import com.gvsu.socnet.user.LoginActivity;
import com.gvsu.socnet.user.ProfileActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import soc.net.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class CapsuleActivity extends Activity implements OnClickListener, AsyncCallbackListener {


  private final String TAG = "CapsuleActivity";

  /**
   * String YOUTUBE
   */
  private final String YOUTUBE = "http://www.youtube.com/watch?v=";

  /**
   * String TAB
   */
  private final String TAB = "\t";

  private String creatorID;
  private String cId;

  private Context mContext;
  private ViewGroup mViewGroup;

  @Override
  public void onCreate(Bundle savedInstanceState) {

    mContext = this;

    requestWindowFeature(Window.FEATURE_NO_TITLE);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.capsule);

    // Increment the number of views on the capsule
    final String cId = getIntent().getStringExtra("cID");
    this.cId = cId;

    String userId = getSharedPreferences(LoginActivity.PROFILE, 0).getString(LoginActivity.PLAYER_ID, "-1");
    if (userId.equals("-1")) // just in case user is not logged in somehow
      startActivity(new Intent(this, LoginActivity.class));
    else
      addAView(userId, cId);

    refresh();

    this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
  }

  /**
   * *************************************************************
   * Get capsule info from the server and display it
   * *************************************************************
   */

  protected void refresh() {
    Log.i(TAG, "capid=" + cId);

    refreshCapsuleInfo();
    refreshCommentsInfo();
    setupAddComments(cId);
    setupAddRating(cId);
  }

  private void refreshCapsuleInfo() {

    HashMap<String, String> requestParams = new HashMap<String, String>();
    requestParams.put(AsyncDownloader.CAPSULEID, cId);

    AsyncDownloader.Payload request = new AsyncDownloader.Payload(AsyncDownloader.GETCAPSULE, requestParams, this, getApplicationContext());

    AsyncDownloader.perform(request);
  }

  private void refreshCommentsInfo() {

    HashMap<String, String> requestParams = new HashMap<String, String>();
    requestParams.put(AsyncDownloader.CAPSULEID, cId);

    AsyncDownloader.Payload request = new AsyncDownloader.Payload(AsyncDownloader.GETCOMMENTS, requestParams, this, getApplicationContext());

    AsyncDownloader.perform(request);

  }

  private void refreshRating() {

    HashMap<String, String> requestParams = new HashMap<String, String>();
    requestParams.put(AsyncDownloader.CAPSULEID, cId);

    AsyncDownloader.Payload request = new AsyncDownloader.Payload(AsyncDownloader.GETRATING, requestParams, this, getApplicationContext());

    AsyncDownloader.perform(request);
  }


  /**
   * *************************************************************
   *
   * @param v *************************************************************
   * @see android.view.View.OnClickListener#onClick(android.view.View)
   */
  @Override
  public void onClick(View v) {
    Log.println(3, TAG, "buttonClicked");
    switch (v.getId()) {
      case R.id.play_button:
        Log.i(TAG, "playButtonClicked");
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(YOUTUBE + "RCUBxgdKZ_Y")));
        break;
      default:
        break;
    }

  }


  private void setCapsuleInfo(String strCapInfo) {

    TextView title = (TextView) findViewById(R.id.capsule_title);
    TextView description = (TextView) findViewById(R.id.description);
    TextView creator = (TextView) findViewById(R.id.capsule_creator);
    TextView leftOn = (TextView) findViewById(R.id.left_on);
    RatingBar rating = (RatingBar) findViewById(R.id.capsule_rating_bar);

    if (strCapInfo.equals("error"))
      return;// return instead
    // information
    try {
      JSONArray capsuleStuff = new JSONArray(strCapInfo);
      JSONObject capsule = capsuleStuff.getJSONObject(0);
      String strTitle = capsule.getString("title");
      String strDescription = capsule.getString("description");
      String strCreateDate = capsule.getString("createDate");
      creatorID = capsule.getString("creatorId");
      String strCreatorUName = capsule.getString("userName");
      String strRating = capsule.getString("avgRate");

      title.setText(strTitle);
      description.setText(strDescription);
      leftOn.setText(leftOnToString(strCreateDate));

      if (strRating == null || strRating.equals("null"))
        rating.setRating(0);
      else
        rating.setRating(Float.parseFloat(strRating));
      if (strCreatorUName == null || strCapInfo.equals("") || strCreatorUName.equals("") || strCreatorUName.equals("null"))
        creator.setText("Left by Anonymous");
      else
        creator.setText("Left by " + strCreatorUName);
    } catch (JSONException e) {
      Log.e(TAG, "Error parsing capsule id=" + cId + ": " + e.getMessage());
    }

    creator.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        if (creatorID != null && !creatorID.equals("null")) {
          Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
          i.putExtra("viewing_id", creatorID);
          startActivity(i);
        }
      }
    });
  }

  private void setComments(final String commentsFromServer) {
    final LinearLayout commentList = (LinearLayout) findViewById(R.id.comment_layout);
    commentList.removeAllViews();
    if (commentsFromServer.equals("error"))
      return;// return instead

    try {
      JSONArray comments = new JSONArray(commentsFromServer);
      int len = comments.length();
      int numComments = 0;
      int numViews = 0;
      ArrayList<String> viewers = new ArrayList<String>();
      int numDistinctViewers = 0;
      for (int i = 0; i < len; i++) {
        JSONObject comment = comments.getJSONObject(i);

        String strComment = comment.getString("comments");
        if (strComment.equals("")) {
          numViews++;
          if (!viewers.contains(comment.getString("userId"))) {
            viewers.add(comment.getString("userId"));
            numDistinctViewers++;
          }
          continue; // this is just a view, not a comment so don't do anything else
        }
        else
          numComments++;
        String strVisitTime = comment.getString("visitTime");
        final String strUserId = comment.getString("userId");
        String strUserName = comment.getString("userName");

        TextView t = new TextView(this);
        t.setText(new Comment(strUserName, strComment, strVisitTime).toString());
        t.setPadding(40, 10, 40, 0);
        t.setOnClickListener(new OnClickListener() {

          @Override
          public void onClick(View arg0) {
            Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
            i.putExtra("viewing_id", strUserId);
            startActivity(i);
          }
        });
        commentList.addView(t);
      }
      TextView preCommentMessage = (TextView) findViewById(R.id.pre_comment_message);
      if (numComments == 0) {
        preCommentMessage.setText("Be the first to comment!");
        preCommentMessage.setVisibility(View.VISIBLE);
      }
      else {
        preCommentMessage.setVisibility(View.GONE);
      }
      ((TextView) findViewById(R.id.timesFound)).setText("Read " + numViews + " time" + ((numViews != 1) ? "s" : "") + " by " + numDistinctViewers
          + ((numDistinctViewers != 1) ? " different users" : " user"));

    } catch (JSONException e) {
      Log.e(TAG, "error parsing comments on capsule (id=" + cId + ")\n" + e.getMessage());
    }
  }

  private void setupAddComments(final String capsuleId) {

    final EditText newComment = (EditText) findViewById(R.id.edit_text_new_comment);
//    final LinearLayout submitLayout = ((LinearLayout) findViewById(R.id.submit_comment_layout));
    final Button submitButton = ((Button) findViewById(R.id.button_add_comment));
//    final Button cancelButton = ((Button) findViewById(R.id.capsule_comment_cancel));

    newComment.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        submitButton.setEnabled(!charSequence.equals(""));
      }

      @Override
      public void afterTextChanged(Editable editable) {
        submitButton.setEnabled(!editable.toString().equals(""));
      }
    });

    submitButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(final View v) {
        final String newCommentString = newComment.getText().toString();
        newComment.setText("");
        newComment.setHint("Add a comment");
        addAComment(getSharedPreferences("profile", 0).getString("player_id", "0"), capsuleId, fixSpaces(newCommentString));
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
      }
    });
  }

  private void setupAddRating(final String capsuleId) {

    final RatingBar ratingBar = ((RatingBar) findViewById(R.id.capsule_rating_bar));
    final LinearLayout submitLayout = ((LinearLayout) findViewById(R.id.submit_rating_layout));
    final Button submitButton = ((Button) findViewById(R.id.capsule_rating_submit));
    final Button cancelButton = ((Button) findViewById(R.id.capsule_rating_cancel));

    final float ratingBeforeUserMessedWithIt = ratingBar.getRating();

    // listens for ratingbar to be changed
    ratingBar.setOnTouchListener(new OnTouchListener() {

      @Override
      public boolean onTouch(View v, MotionEvent event) {
        submitLayout.setVisibility(View.VISIBLE);
        return false;
      }
    });

    // listens for ratingbar submit button
    submitButton.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        submitLayout.setVisibility(View.GONE);
        addRating(ratingBar.getRating(), capsuleId);
        ratingBar.setRating(0);
      }
    });
    cancelButton.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        submitLayout.setVisibility(View.GONE);
        ratingBar.setRating(ratingBeforeUserMessedWithIt);
      }
    });

    ratingBar.requestFocus();
  }

  private String fixSpaces(String str) {
    String result = "";
    char[] stra = str.toCharArray();
    for (int i = 0; i < stra.length; i++) {
      if (stra[i] == ' ')
        result += "+";
      else
        result += stra[i];
    }
    return result;
  }

  private String leftOnToString(String strCreateDate) {
    GregorianCalendar c = Comment.makeSenseOf(strCreateDate);
    // String s = "Left on ";
    String s = "";
    switch (c.get(Calendar.DAY_OF_WEEK)) {
      case 1:
        s += "Sunday, ";
        break;
      case 2:
        s += "Monday, ";
        break;
      case 3:
        s += "Tuesday, ";
        break;
      case 4:
        s += "Wednesday, ";
        break;
      case 5:
        s += "Thursday, ";
        break;
      case 6:
        s += "Friday, ";
        break;
      case 7:
        s += "Saturday, ";
        break;
    }
    switch (c.get(Calendar.MONTH)) {
      case 0:
        s += "January ";
        break;
      case 1:
        s += "February ";
        break;
      case 2:
        s += "March ";
        break;
      case 3:
        s += "April ";
        break;
      case 4:
        s += "May ";
        break;
      case 5:
        s += "June ";
        break;
      case 6:
        s += "July ";
        break;
      case 7:
        s += "August ";
        break;
      case 8:
        s += "September ";
        break;
      case 9:
        s += "October ";
        break;
      case 10:
        s += "November ";
        break;
      case 11:
        s += "December ";
        break;
    }
    s += c.get(Calendar.DAY_OF_MONTH);
    s += " at ";
    int hour = c.get(Calendar.HOUR);
    if (hour == 0)
      hour = 12;
    s += hour;
    s += ":";
    int minute = c.get(Calendar.MINUTE);
    String sminute = "";
    if (minute < 10)
      sminute = "0" + minute;
    else
      sminute = "" + minute;
    s += sminute;
    s += (c.get(Calendar.AM_PM) == 0 ? "am" : "pm");
    return s;
  }

  private void addRating(float rate, String capsuleId) {
    String userId = getSharedPreferences("profile", 0).getString("player_id", "0");
    String rating = Float.toString(rate);

    HashMap<String, String> requestParams = new HashMap<String, String>();
    requestParams.put(AsyncDownloader.USERID, userId);
    requestParams.put(AsyncDownloader.CAPSULEID, capsuleId);
    requestParams.put(AsyncDownloader.RATING, rating);

    AsyncDownloader.Payload request = new AsyncDownloader.Payload(AsyncDownloader.ADDRATING, requestParams, this, getApplicationContext());

    AsyncDownloader.perform(request);
  }

  private void addAView(String userId, String capsuleId) {

    HashMap<String, String> requestParams = new HashMap<String, String>();
    requestParams.put(AsyncDownloader.USERID, userId);
    requestParams.put(AsyncDownloader.CAPSULEID, capsuleId);

    AsyncDownloader.Payload request = new AsyncDownloader.Payload(AsyncDownloader.ADDVIEW, requestParams, this, getApplicationContext());

    AsyncDownloader.perform(request);
  }

  private void addAComment(String userId, String capsuleId, String comment) {

    HashMap<String, String> requestParams = new HashMap<String, String>();
    requestParams.put(AsyncDownloader.USERID, userId);
    requestParams.put(AsyncDownloader.CAPSULEID, capsuleId);
    requestParams.put(AsyncDownloader.COMMENT, comment);

    AsyncDownloader.Payload request = new AsyncDownloader.Payload(AsyncDownloader.ADDCOMMENT, requestParams, this, getApplicationContext());

    AsyncDownloader.perform(request);
  }

  public void asyncDone(AsyncDownloader.Payload payload) {
    if (payload.exception == null) {
      switch (payload.taskType) {
        case AsyncDownloader.GETCAPSULE:
          setCapsuleInfo(payload.result);
          break;

        case AsyncDownloader.GETCOMMENTS:
          setComments(payload.result);
          break;

        case AsyncDownloader.GETRATING:
          String rating = payload.result.split("\":\"")[1].substring(0, 1);
          ((RatingBar) findViewById(R.id.capsule_rating_bar)).setRating(Float.parseFloat(rating));
          break;

        case AsyncDownloader.ADDRATING:
          refreshRating();
          break;

        case AsyncDownloader.ADDCOMMENT:
          refresh();
          break;
      }
    }
    else {
      new AlertDialog.Builder(this)
          .setTitle("Internet Error [" + payload.taskType + "](" + payload.result + ")")
          .setMessage("Sorry, we're having trouble talking to the internet. Please try that again...")
          .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
          })
          .show();
    }
  }
}