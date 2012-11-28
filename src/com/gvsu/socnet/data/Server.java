package com.gvsu.socnet.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import java.io.*;
import java.net.*;

/**
 * *************************************************************
 * com.gvsusocnet.Server
 *
 * @author Caleb Gomer, Jeremy Dye
 * @version 1.0
 *          *************************************************************
 */
public class Server {
  private final static String TAG = "Server";

  //base URL
  private static final String HTTP_TYPE = "http";
  private static final String HOST = "www.cis.gvsu.edu";
  private static final String PATH = "~scrippsj/socNet/functions";
  private static final String BASE_URL = HTTP_TYPE + "://" + HOST + "/" + PATH + "/";

  //PHP Commands
  private static final String GETCAPSULE = "getCapsule.php";
  private static final String NEWCAPSULE = "setCapsule.php";
  private static final String GETUSER = "getUserNew.php";
  private static final String SETUSER = "setUser.php";
  private static final String GETCOMMENTS = "getVisit.php";
  private static final String AUTHENTICATE = "getUser.php";
  private static final String ADDCOMMENT = "setVisit.php";
  private static final String GETRATING = "getRate.php";
  private static final String ADDRATING = "setRate.php";

  public static String newUser(String name, String location, String state, String gender, String age, String interests, String about, String password, String username) {
    String command = SETUSER + "?name=" + name + "&location=" + location + "&state=" + state + "&gender=" + gender + "&age=" + age + "&interest=" + interests + "&about=" + about + "&password="
        + password + "&userName=" + username;
    Log.d(TAG, command);
    return get(command);
  }

  public static String editUser(String id, String name, String location, String state, String gender, String age, String interests, String about, String password, String username) {
    String command = SETUSER + "?id=" + id + "&name=" + name + "&location=" + location + "&state=" + state + "&gender=" + gender + "&age=" + age + "&interest=" + interests + "&about=" + about
        + "&password=" + password + "&userName=" + username;
    Log.d(TAG, command);
    return get(command);
  }

  public static String newCapsule(String userId, String lat, String lon, String description) {
    String command = NEWCAPSULE + "title="+"&locLat=" + lat + "&locLong=" + lon + "&description=" + description + "&creatorId=" + userId;
    Log.d(TAG, command);
    String response = get(command);
    return response;
  }

  public static String getUser(String id) {
    String command = GETUSER + "?id=" + id;
    return get(command);
  }

  public static String authenticate(String userName, String password) {
    String command = AUTHENTICATE + "?userName=" + userName + "&password=" + password;
    return get(command);
  }

  public static String getComments(String capsuleId) {
    String command = GETCOMMENTS + "?capsuleId=" + capsuleId;
    return get(command);
  }

  public static String getCapsule(String id) {
    String command = GETCAPSULE + "?id=" + id;
    return get(command);
  }

  public static String getComments(String capsuleId, String userId) {
    String command = GETCOMMENTS + "?userId=" + userId + "&capsuleId=" + capsuleId;
    return get(command);
  }

  public static String getRating(String capsuleId) {
    String command = GETRATING + "?capsuleId=" + capsuleId;
    return get(command);
  }

  public static String addRating(String userId, String capsuleId, String rating) {
    String command = ADDRATING + "?userId=" + userId + "&capsuleId=" + capsuleId + "&rating=" + rating;
    String response = get(command);
    return response;
  }

  // Adds a comment left by a user on a specific capsule
  public static String addComment(String userId, String capsuleId, String comment) {
    String command = ADDCOMMENT + "?userId=" + userId + "&capsuleId=" + capsuleId + "&comments=" + comment;
    return get(command);
  }

  // increment the number of views
  public static String addAView(String userId, String capsuleId) {
    // add comment and add view are the same, except a view has no comment
    return addComment(userId, capsuleId, "");
  }


  /**
   * *************************************************************
   *
   * @param latitude   float value of latitude (not E6)
   * @param longitude  float value of longitude (not E6)
   * @param radiusCode 1 for inner radius, 2 for outer radius
   * @param startDate  yyyy/mm/dd date to start filtering by
   * @param endDate    yyyy/mm/dd date to end filtering by
   * @param minRating  minimum rating to return, integers 1 to 5
   * @return String the capsules returned by the query
   *         *************************************************************
   */
  public static String getCapsules(String latitude, String longitude, String radiusCode, String startDate, String endDate, String minRating) {
    String request = GETCAPSULE + "?lat=" + latitude + "&long=" + longitude + "&radiusCode=" + radiusCode + "&dateStart=" + startDate + "&dateEnd=" + endDate + "&minRate=" + minRating;
    return get(request);
  }

  /**
   * makes sure the server's response is valid before returning it
   */
  private static String valid(String response) {
    if (response.length() >= 11 && response.substring(0, 11).equals("SocNetData:")) {
      Log.d(TAG, "response: '" + response);
      return response.substring(11);
    }
    else {
      Log.d(TAG, "response: '" + response + "' *****NOT VALID*****");
      return "error";
    }
  }

  /**
   * *************************************************************
   * Basic server post code
   *
   * @param command
   * @return String
   *         *************************************************************
   */
  private static String get(String command) {

    command = command.replace("\n", "%20%20%20");
    command = command.replace(" ", "%20");

    String request = BASE_URL + command;
    Log.d("server", "URL:\n" + request);

    HttpClient client = new DefaultHttpClient();
    HttpPost post = new HttpPost(request);
    HttpResponse response;

    try {
      response = client.execute(post);
    } catch (ClientProtocolException e2) {
      e2.printStackTrace();
      return "Connection Failed (Client)";
    } catch (IOException e2) {
      e2.printStackTrace();
      return "Connection Failed (I/O)";
    }
    String response_text = null;
    HttpEntity entity = null;
    try {
      entity = response.getEntity();
      response_text = _getResponseBody(entity);
    } catch (ParseException e) {
      e.printStackTrace();
    } catch (IOException e) {
      if (entity != null) {
        try {
          entity.consumeContent();
        } catch (IOException e1) {
        }
      }
    }
    return valid(response_text);
  }

  private static String _getResponseBody(final HttpEntity entity) throws IOException, ParseException {
    if (entity == null) {
      throw new IllegalArgumentException("HTTP entity may not be null");
    }
    InputStream instream = entity.getContent();
    if (instream == null) {
      return "";
    }
    if (entity.getContentLength() > Integer.MAX_VALUE) {
      throw new IllegalArgumentException("HTTP entity too large to be buffered in memory");
    }
    String charset = getContentCharSet(entity);
    if (charset == null) {
      charset = HTTP.DEFAULT_CONTENT_CHARSET;
    }
    Reader reader = new InputStreamReader(instream, charset);
    StringBuilder buffer = new StringBuilder();
    try {
      char[] tmp = new char[1024];
      int l;
      while ((l = reader.read(tmp)) != -1) {
        buffer.append(tmp, 0, l);
      }
    } finally {
      reader.close();
    }
    return buffer.toString();
  }

  private static String getContentCharSet(final HttpEntity entity) throws ParseException {
    if (entity == null) {
      throw new IllegalArgumentException("HTTP entity may not be null");
    }
    String charset = null;
    if (entity.getContentType() != null) {
      HeaderElement values[] = entity.getContentType().getElements();
      if (values.length > 0) {
        NameValuePair param = values[0].getParameterByName("charset");
        if (param != null) {
          charset = param.getValue();
        }
      }
    }
    return charset;
  }


  public static boolean uploadFile(String path) {
    Log.i(TAG, "uploading from path: " + path);
    new UploadFileTask().execute(new File(path));
    return true;
  }

  public static Drawable downloadPicture(String url) {

    return null;
  }

  Bitmap bmImg;

  void downloadFile(String fileUrl) {
    URL myFileUrl = null;
    try {
      myFileUrl = new URL(fileUrl);
    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    try {
      HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
      conn.setDoInput(true);
      conn.connect();
      int length = conn.getContentLength();
      int[] bitmapData = new int[length];
      byte[] bitmapData2 = new byte[length];
      InputStream is = conn.getInputStream();

      bmImg = BitmapFactory.decodeStream(is);
//      imView.setImageBitmap(bmImg);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }


//  Read more: http://getablogger.blogspot.com/2008/01/android-download-image-from-server-and.html#ixzz1vuoPl0w7
}
