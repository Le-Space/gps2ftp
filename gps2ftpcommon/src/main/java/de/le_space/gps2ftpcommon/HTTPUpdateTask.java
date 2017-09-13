package de.le_space.gps2ftpcommon;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;


/**
 * Created by Nico Krause (nico@le-space.de) on 16.06.17. (Le Space UG)
 */
public class HTTPUpdateTask extends AsyncTask<String, Integer, String> {

	private static final String TAG = "HTTPUpdateTask";
	private boolean test;
	public static String HOST = "";
	public static int PORT = 22;
	public static String USER = "";
	public static String PASS = "";
	public static String HTTURL = "";
	public Activity activity;

	Exception ex;

	private PostHTTPTaskListener<String> postTaskListener;
	private String token;


	public interface PostHTTPTaskListener<K> {
		// K is the type of the result object of the async task
		void onError(K result);
		void onSuccess(K result);
	}

	public HTTPUpdateTask(PostHTTPTaskListener<String> postTaskListener) {
		this.postTaskListener = postTaskListener;
	};

	public boolean AUTHENTICATE(String url){

		InputStream inputStream = null;
		try {

			// 1. create HttpClient
			HttpClient httpclient = new DefaultHttpClient();

			// 2. make POST request to the given URL
			HttpPost httpPost = new HttpPost(url);

			// 5. set json to StringEntity
			String jsonContent = "{\"email\": \""+this.USER+"\", \"password\": \"" + this.PASS + "\"}";
			StringEntity se = new StringEntity(jsonContent);

			// 6. set httpPost Entity
			httpPost.setEntity(se);

			// 7. Set some headers to inform server about the type of the content
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");

			// 8. Execute POST request to the given URL
			HttpResponse httpResponse = httpclient.execute(httpPost);

			// 9. receive response as inputStream
			inputStream = httpResponse.getEntity().getContent();

			// 10. convert inputstream to string
			JSONObject json;
			if(inputStream != null){
				json = convertInputStreamToJSON(inputStream);
				this.token = json.getString("token");
			}


		} catch (Exception e) {
			publishProgress(0);
			Log.e(TAG, e.getLocalizedMessage());
			return false;

		}
		return true;
	}

	public String POST(String url, String jsonContent) throws IOException, JSONException {
		InputStream inputStream = null;

		// 1. create HttpClient
		HttpClient httpclient = new DefaultHttpClient();

		// 2. make POST request to the given URL
		HttpPost httpPost = new HttpPost(url);

		// 5. set json to StringEntity
		StringEntity se = new StringEntity(jsonContent);

		// 6. set httpPost Entity
		httpPost.setEntity(se);

		// 7. Set some headers to inform server about the type of the content
		httpPost.setHeader("Accept", "application/json");
		if(this.token!=null){
			httpPost.setHeader("Authorization","Bearer "+token);
		}

		httpPost.setHeader("Content-type", "application/json");

		// 8. Execute POST request to the given URL
		HttpResponse httpResponse = httpclient.execute(httpPost);

		// 9. receive response as inputStream
		inputStream = httpResponse.getEntity().getContent();

		// 10. convert inputstream to string
		if(inputStream != null){
			return convertInputStreamToString(inputStream).toString();
		}

		// 11. return result
		return null;
	}

	public boolean isTest() {
		return test;
	}

	public void setTest(boolean test) {
		this.test = test;
	}

	protected String doInBackground(String... jsonContent) {

		Log.d(TAG, "starting http transfer for position:" + jsonContent);

		String firstPart = HOST;
		String secondPart = HTTURL;

		if(!firstPart.toLowerCase().startsWith("http:") && !firstPart.toLowerCase().startsWith("https:"))
			firstPart = "https://"+firstPart;

		if(firstPart.endsWith("/"))
			firstPart = firstPart.substring(0,firstPart.length()-1);

		if(secondPart.startsWith(("/")))
			secondPart = secondPart.substring(1);
		if(secondPart.endsWith("/"))
			secondPart = secondPart.substring(0,secondPart.length()-1);

		String authUrl = firstPart+"/users/login";

		String url = firstPart+"/"+secondPart;

		if(test)
			url = firstPart+"/test.html";

		String result = null;

		if(AUTHENTICATE(authUrl)){
			try {
				result =  POST(url,jsonContent[0]);
				postTaskListener.onSuccess(result);
			} catch (IOException ex) {
				publishProgress(0);
				Log.e(TAG, ex.getLocalizedMessage());
				postTaskListener.onError(ex.getMessage());
			} catch (JSONException ex) {
				publishProgress(0);
				Log.e(TAG, ex.getLocalizedMessage());
				postTaskListener.onError(ex.getMessage());
			}
		}

		return result;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
	}

	private static String convertInputStreamToString(InputStream inputStream) throws IOException, JSONException {

		BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
		String line = "";
		String result = "";

		while((line = bufferedReader.readLine()) != null)
			result += line;

		inputStream.close();

		return result;
	}

	private static JSONObject convertInputStreamToJSON(InputStream inputStream) throws IOException, JSONException {

		BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
		String line = "";
		String result = "";

		while((line = bufferedReader.readLine()) != null)
			result += line;

		inputStream.close();
		JSONObject json = new JSONObject(result);

		return json;
	}
}

