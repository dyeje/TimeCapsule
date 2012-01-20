package soc.net;




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

/****************************************************************
 * com.gvsusocnet.Server
 * @author Caleb Gomer, Jeremy Dye
 * @version 1.0
 ***************************************************************/
public class Server {
	
	//Get Treasure
	private static final String GETTREAS = "http://www.cis.gvsu.edu/~scrippsj/socNet/functions/getTreasure.php?";

	//Get User
	private static final String GETUSER = "http://www.cis.gvsu.edu/~scrippsj/socNet/functions/getUser.php?id=";
	
	public static String getUser(String id) {
		String command = GETUSER + id;
		return get(command);
	}
	
	public static String getTreasure(String id) {
		String command = GETTREAS + "id=" + id;
		return get(command);
	}
	
	public static String getTreasure(String lat, String lng) {
		String command = GETTREAS + "lat=" + lat + "&long=" + lng + "&radius=1";
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
