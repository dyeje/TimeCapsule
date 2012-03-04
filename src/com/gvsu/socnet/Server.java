package com.gvsu.socnet;

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

import android.util.Log;

/****************************************************************
 * com.gvsusocnet.Server
 * @author Caleb Gomer, Jeremy Dye
 * @version 1.0
 ***************************************************************/
public class Server {

	// Get Capsule
	private static final String GETCAPS = "http://www.cis.gvsu.edu/~scrippsj/socNet/functions/getCapsule.php?";

	// Create Capsule
	private static final String NEWCAPSULE = "http://www.cis.gvsu.edu/~scrippsj/socNet/functions/setCapsule.php?";

	// Get User
	private static final String GETUSER = "http://www.cis.gvsu.edu/~scrippsj/socNet/functions/getUser.php?id=";

	// Set User
	private static final String SETUSER = "http://www.cis.gvsu.edu/~scrippsj/socNet/functions/setUser.php?id=";

	public static String newUser(String name, String location,
	    String state, String gender, String age, String interests,
	    String about, String password, String username) {
		String command = SETUSER + "&name=" + name + "&location="
		    + location + "&state=" + state + "&gender=" + gender
		    + "&age=" + age + "&interest=" + interests + "&about="
		    + about + "&password=" + password + "&userName="
		    + username;
		Log.d("debug", command);
		return get(command);
	}

	public static String newCapsule(String lat, String lon,
	    String title, String description) {
		String command = NEWCAPSULE + "title=" + title + "&locLat="
		    + lat + "&locLong=" + lon + "&description=" + description;
		Log.d("debug", command);
		return get(command);
	}

	public static String getUser(String id) {
		String command = GETUSER + id;
		return get(command);
	}

	public static String getCapsule(String id) {
		String command = GETCAPS + "id=" + id;
		return get(command);
	}

	public static String getTreasure(String lat, String lng) {
		String command = GETCAPS + "lat=" + lat + "&long=" + lng
		    + "&radius=4";
		return get(command);
	}

	public static String uploadTreasure(String path) {
		String command = "http://api.imgur.com/2/upload?"
		    + "key=30b2407b8988775ad0f9e9339cfb4ddd" + "&image="
		    + path
		// + "&name=GVSUSOCNETHOLYBALLS"
		// + "&title=JMDJMDJMD"
		;
		return get(command);
	}

	private static String get(String command) {
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(command);
		HttpResponse response;

		try {
			response = client.execute(post);
		} catch (ClientProtocolException e2) {
			// TODO
			e2.printStackTrace();
			return "Connection Failed (Client)";
		} catch (IOException e2) {
			// TODO
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

	private static String _getResponseBody(final HttpEntity entity)
	    throws IOException, ParseException {
		if (entity == null) {
			throw new IllegalArgumentException(
			    "HTTP entity may not be null");
		}
		InputStream instream = entity.getContent();
		if (instream == null) {
			return "";
		}
		if (entity.getContentLength() > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(
			    "HTTP entity too large to be buffered in memory");
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

	private static String getContentCharSet(final HttpEntity entity)
	    throws ParseException {
		if (entity == null) {
			throw new IllegalArgumentException(
			    "HTTP entity may not be null");
		}
		String charset = null;
		if (entity.getContentType() != null) {
			HeaderElement values[] = entity.getContentType()
			    .getElements();
			if (values.length > 0) {
				NameValuePair param = values[0]
				    .getParameterByName("charset");
				if (param != null) {
					charset = param.getValue();
				}
			}
		}
		return charset;
	}
}
