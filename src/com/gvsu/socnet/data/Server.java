package com.gvsu.socnet.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/****************************************************************
 * com.gvsusocnet.Server
 * @author Caleb Gomer, Jeremy Dye
 * @version 1.0
 ***************************************************************/
public class Server {

	// Get Capsule
	private static final String GETCAPSULE = "http://www.cis.gvsu.edu/~scrippsj/socNet/functions/getCapsule.php?";
	// Create Capsule
	private static final String NEWCAPSULE = "http://www.cis.gvsu.edu/~scrippsj/socNet/functions/setCapsule.php?";
	// Get User
	private static final String GETUSER = "http://www.cis.gvsu.edu/~scrippsj/socNet/functions/getUser.php?id=";
	// Authenticate user
	private static final String AUTHENTICATE = "http://www.cis.gvsu.edu/~scrippsj/socNet/functions/getUser.php?userName=";
	// Set User
	private static final String SETUSER = "http://www.cis.gvsu.edu/~scrippsj/socNet/functions/setUser.php?id=";
	// Get Comments
	private static final String GETCOMMENTS = "http://www.cis.gvsu.edu/~scrippsj/socNet/functions/getVisit.php?";
	// Add Comment
	private static final String ADDCOMMENT = "http://www.cis.gvsu.edu/~scrippsj/socNet/functions/setVisit.php?";
	// Get Rating
	private static final String GETRATING = "http://www.cis.gvsu.edu/~scrippsj/socNet/functions/getRate.php?capsuleId=";
	// Set Rating
	private static final String ADDRATING = "http://www.cis.gvsu.edu/~scrippsj/socNet/functions/setRate.php?";

	public static String newUser(String name, String location, String state, String gender, String age, String interests, String about, String password, String username) {
		String command = SETUSER + "&name=" + name + "&location=" + location + "&state=" + state + "&gender=" + gender + "&age=" + age + "&interest=" + interests + "&about=" + about + "&password="
		    + password + "&userName=" + username;
		// Log.d("debug", command);
		return valid(get(command));
	}

	public static String editUser(String id, String name, String location, String state, String gender, String age, String interests, String about, String password, String username) {
		String command = SETUSER + id + "&name=" + name + "&location=" + location + "&state=" + state + "&gender=" + gender + "&age=" + age + "&interest=" + interests + "&about=" + about
		    + "&password=" + password + "&userName=" + username;
		// Log.d("debug", command);
		return valid(get(command));
	}

	public static String newCapsule(String userId, String lat, String lon, String title, String description) {
		// String command = NEWCAPSULE + "title=" + title + "&locLat=" + lat + "&locLong=" + lon +
		// "&description=" + description;
		String command = NEWCAPSULE + "title=" + title + "&locLat=" + lat + "&locLong=" + lon + "&description=" + description + "&creatorId=" + userId;
		Log.i("server", "newCapsule request:" + command);
		// Log.d("debug", command);
		String response = valid(get(command));
		Log.i("server", "newCapsule response:" + response);
		return response;
	}

	public static String getUser(String id) {
		String command = GETUSER + id;
		// Log.d("debug", "tried: " + command);
		return valid(get(command));
	}

	public static String authenticate(String id, String password) {
		String command = AUTHENTICATE + id + "&password=" + password;
		return valid(get(command));
	}

	public static String getCapsule(String id) {
		String command = GETCAPSULE + "id=" + id;
		return valid(get(command));
	}

	public static String getTreasure(String lat, String lng) {
		String command = GETCAPSULE + "lat=" + lat + "&long=" + lng + "&radius=4";
		return valid(get(command));
	}

	public static String getComments(String capsuleId) {
		String command = GETCOMMENTS + "capsuleId=" + capsuleId;
		return valid(get(command));
	}

	public static String getComments(String capsuleId, String userId) {
		String command = GETCOMMENTS + "userId=" + userId + "&capsuleId=" + capsuleId;
		return valid(get(command));
	}

	public static String getRating(String capsuleId) {
		String command = GETRATING + capsuleId;
		return valid(get(command));
	}

	public static String addRating(String userId, String capsuleId, String rating) {
		String command = ADDRATING + "userId=" + userId + "&capsuleId=" + capsuleId + "&rating=" + rating;
		return valid(get(command));
	}

	// Adds a comment left by a user on a specific capsule
	public static String addComment(String userId, String capsuleId, String comment) {
		String result;
		try {
			String command = ADDCOMMENT + "userId=" + userId + "&capsuleId=" + capsuleId + "&comments=" + comment;
			// Log.d("debug", "addComment command: " + command);
			result = get(command);
			// Log.d("debug", "result: " + result);
			return valid(result);
		} catch (IllegalStateException e) {
			result = "An error occured";
		}
		return valid(result);
	}

	// increment the number of views
	public static String addAView(String userId, String capsuleId) {
		// add comment and add view are the same, except a view has no comment
		return addComment(userId, capsuleId, "");
	}

	public static String uploadTreasure(String path) {
		String command = "http://api.imgur.com/2/upload?" + "key=30b2407b8988775ad0f9e9339cfb4ddd" + "&image=" + path
		// + "&name=GVSUSOCNETHOLYBALLS"
		// + "&title=JMDJMDJMD"
		;
		return get(command);
	}

	/****************************************************************
	 * @param latitude float value of latitude (not E6)
	 * @param longitude float value of longitude (not E6)
	 * @param radiusCode 1 for inner radius, 2 for outer radius
	 * @param startDate yyyy/mm/dd date to start filtering by
	 * @param endDate yyyy/mm/dd date to end filtering by
	 * @param minRating minimum rating to return, integers 1 to 5
	 * @return String the capsules returned by the query
	 ***************************************************************/
	public static String getCapsules(String latitude, String longitude, String radiusCode, String startDate, String endDate, String minRating) {
		String request = GETCAPSULE;
		request += "lat=" + latitude + "&long=" + longitude + "&radiusCode=" + radiusCode + "&dateStart=" + startDate + "&dateEnd=" + endDate + "&minRate=" + minRating;
		Log.i("server", "getCapsules request:" + request);
		String result = valid(get(request));
		Log.i("server", "getCapsules response:" + result);
		// if (!result.equals("error")) {
		// JSONArray capsules = new JSONArray();
		// try {
		// capsules = new JSONArray(result);
		// // capsules.getJSONObject(0)
		// } catch (JSONException e) {
		// e.printStackTrace();
		// }
		// for (int i = 0; i < capsules.length(); i++) {
		// try {
		// JSONObject capsule = capsules.getJSONObject(i);
		// Log.d("debug", "capsule id "+ capsule.getString("id"));
		// } catch (JSONException e) {
		// Log.e("debug", "error: " + capsules.toString() + " is not valid JSON");
		// e.printStackTrace();
		// }
		// }
		// }

		// Log.d("debug", "getCapsule\nrequest:" + request + "\nresult:" + result);
		return result;
	}

	public static String login(String username, String password) {
		String request = GETUSER + "&userName=" + username + "&password=" + password;
		// Log.d("debug", "logging in with: username=" + username + " password=" + password);
		String result = valid(get(request));
		// Log.d("debug", "login response: " + result);
		return result;
	}

	/**makes sure the server's response is valid before returning it*/
	private static String valid(String response) {
		// prevent server from getting/returning garbage
		/** a possible way to do this */
		if (response.length() >= 11 && response.substring(0, 11).equals("SocNetData:")) {
			return response.substring(11);
		} else {
			Log.d("debug", "response:" + response + " *****NOT VALID*****");
			return "error";
		}
	}

	/****************************************************************
	/****************************************************************
	/****************************************************************
	/****************************************************************
	/****************************************************************
	/****************************************************************
	/****************************************************************
	/****************************************************************
	/****************************************************************
	/****************************************************************
	/****************************************************************
	 * Basic server post code
	 * @param command
	 * @return String
	 ***************************************************************/
	private static String get(String command) {
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(command);
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
		return response_text;
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
}
