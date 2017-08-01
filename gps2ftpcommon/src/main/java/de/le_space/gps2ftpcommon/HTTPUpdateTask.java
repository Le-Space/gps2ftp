package de.le_space.gps2ftpcommon;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.net.SocketFactory;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;


/**
 * Created by Nico Krause (nico@le-space.de) on 16.06.17. (Le Space UG)
 */
public class HTTPUpdateTask extends AsyncTask<String, Integer, String> {

	private static final String TAG = "Main";

	public static String SHTTPHOST = "";
	public static int SFTPPORT = 22;
	public static String SHTTPUSER = "";
	public static String SHTTPPASS = "";
	public static String SHTTURL = "";
	private SocketFactory socketFactory;
	public Activity activity;

	Exception ex;

	private PostHTTPTaskListener<String> postTaskListener;

	public void setSocketFactory(SocketFactory socketFactory) {
		this.socketFactory = socketFactory;
	}

	public interface PostHTTPTaskListener<K> {
		// K is the type of the result object of the async task
		void onError(K result);
		void onSuccess(K result);
	}

	public HTTPUpdateTask(PostHTTPTaskListener<String> postTaskListener) {
		this.postTaskListener = postTaskListener;
	};


	public String POST(String url, String jsonContent){
		InputStream inputStream = null;
		String result = "";
		try {

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
			httpPost.setHeader("Content-type", "application/json");

			// 8. Execute POST request to the given URL
			HttpResponse httpResponse = httpclient.execute(httpPost);

			// 9. receive response as inputStream
			inputStream = httpResponse.getEntity().getContent();

			// 10. convert inputstream to string
			if(inputStream != null)
				result = convertInputStreamToString(inputStream);
			else
				result = "Did not work!";

		} catch (Exception e) {
			publishProgress(0);

			Log.e(TAG, e.getLocalizedMessage());
		}

		// 11. return result
		return result;
	}

	protected String doInBackground(String... jsonContent) {

		Log.d(TAG, "starting http transfer for position:" + jsonContent);

		String firstPart = SHTTPHOST;
		String secondPart = SHTTURL;

		if(!firstPart.toLowerCase().startsWith("http:") || !firstPart.toLowerCase().startsWith("https:"))
			firstPart = "https://"+firstPart;

		if(firstPart.endsWith("/"))
			firstPart = firstPart.substring(0,firstPart.length()-1);

		if(secondPart.startsWith(("/")))
			secondPart = secondPart.substring(1);
		if(secondPart.endsWith("/"))
			secondPart = secondPart.substring(0,secondPart.length()-1);

		String url = firstPart+"/"+secondPart;

		String result = POST(url,jsonContent[0]);
		postTaskListener.onSuccess(result);
		return result;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		Toast.makeText(activity,"Please check HTTP-Config:"+ex.getMessage(), Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
	}

	private static String convertInputStreamToString(InputStream inputStream) throws IOException{
		BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
		String line = "";
		String result = "";
		while((line = bufferedReader.readLine()) != null)
			result += line;

		inputStream.close();
		return result;

	}
}

