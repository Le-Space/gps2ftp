package de.le_space.gps2ftpcommon;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import javax.net.SocketFactory;

import static de.le_space.gps2ftpcommon.Constants.loadTitlePref;

/**
 * Created by Nico Krause (nico@le-space.de) on 30.06.17. (Le Space UG)
 */

public class Utils {


	public static String TEST_PROTOCOL, TEST_HOST, TEST_USER, TEST_PASS, TEST_URL;

	public static void sendConfigItems(GoogleApiClient mGoogleApiClient, final PutDataMapRequest putRequest) {
		if(mGoogleApiClient==null)
			return;
		Wearable.DataApi.putDataItem(mGoogleApiClient,  putRequest.asPutDataRequest());
	}

	/**
	 * Publish Position
	 * @param activity activity to send possible informations
	 * @param test test connection
	 */
	public static void publishPosition(final Activity activity,final boolean test){

		if(test) {
			getNetwork(activity,test);
		}else{
			AlertDialog.Builder adb = new AlertDialog.Builder(activity);
			//adb.setView(R.layout.activity_main);
			adb.setTitle(R.string.publish_ftp);
			adb.setIcon(android.R.drawable.ic_dialog_alert);

			//final String finalJsonString = jsonStringPositionConfig;
			//final JSONObject finalLastPositionJsonObject = lastPositionJsonObject;
			adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {

					getNetwork(activity, test);

				} });

			adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {

					//finish();
				} });
			adb.show();
		}

	}

	private static void getNetwork(final Activity activity, final boolean test){

		ConnectivityManager.NetworkCallback mNetworkCallback = new ConnectivityManager.NetworkCallback() {
			@Override
			public void onAvailable(Network network) {
				SocketFactory sf = network.getSocketFactory();
				publishToServer(activity, sf, test);
			}
		};


		NetworkRequest request = new NetworkRequest.Builder()
				.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
				.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
				.addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
				.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
				.build();

		int MIN_BANDWIDTH_KBPS = 320;
		ConnectivityManager mConnectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
		Network activeNetwork = mConnectivityManager.getActiveNetwork();


		if (activeNetwork != null) {
			int bandwidth = mConnectivityManager.getNetworkCapabilities(activeNetwork).getLinkDownstreamBandwidthKbps();

			if (bandwidth < MIN_BANDWIDTH_KBPS) {
				// Request a high-bandwidth network
				mConnectivityManager.requestNetwork(request, mNetworkCallback);

			} else {
				// You already are on a high-bandwidth network, so start your network request
				publishToServer(activity, activeNetwork.getSocketFactory(), test);
			}
		}
	}



	private static void publishToServer(final Activity activity, SocketFactory sf, boolean test){

		 FTPUpdateTask.PostFTPTaskListener<String> postFTPSTaskListener = new FTPUpdateTask.PostFTPTaskListener<String>() {
			@Override
			public void onError(String result) {
				Intent intent = new Intent(Constants.LOCATION_PUBLISH_ERROR);
				intent.putExtra("error", result);
				LocalBroadcastManager.getInstance(activity.getApplicationContext()).sendBroadcast(intent);
			}

			@Override
			public void onSuccess(String result) {
				Intent intent = new Intent(Constants.LOCATION_PUBLISH_SUCCESS);
				intent.putExtra("info", result);
				LocalBroadcastManager.getInstance(activity.getApplicationContext()).sendBroadcast(intent);

			}
		};

		HTTPUpdateTask.PostHTTPTaskListener<String> postHTTPTaskListener = new HTTPUpdateTask.PostHTTPTaskListener<String>() {
			@Override
			public void onError(String result) {
				Intent intent = new Intent(Constants.LOCATION_PUBLISH_ERROR);
				intent.putExtra("error", result);
				LocalBroadcastManager.getInstance(activity.getApplicationContext()).sendBroadcast(intent);
			}

			@Override
			public void onSuccess(String result) {
				Intent intent = new Intent(Constants.LOCATION_PUBLISH_SUCCESS);
				intent.putExtra("info", result);
				LocalBroadcastManager.getInstance(activity.getApplicationContext()).sendBroadcast(intent);

			}
		};

		String protocol = loadTitlePref(activity,1,"protocol");
		String host = loadTitlePref(activity,1,"host");
		String username = loadTitlePref(activity,1,"username");
		String password = loadTitlePref(activity,1,"password");
		String remoteDirectory = loadTitlePref(activity,1,"remoteDirectory");
		if(test){
			protocol = TEST_PROTOCOL;
			host =  TEST_HOST;
			username = TEST_USER;
			password = TEST_PASS;
			remoteDirectory = TEST_URL;
		}
		String finalJsonString = loadTitlePref(activity,1,"lastPosition");
		if(protocol.equals("SFTP")){
			FTPUpdateTask ftpUpdate = new FTPUpdateTask(postFTPSTaskListener);
			ftpUpdate.setSocketFactory(sf);
			ftpUpdate.setTest(test);
			ftpUpdate.activity = activity;

			ftpUpdate.HOST = host;
			ftpUpdate.USER = username;
			ftpUpdate.PASS = password;
			ftpUpdate.WORKINGDIR = remoteDirectory;

			ftpUpdate.execute(finalJsonString);
		}

		if(protocol.equals("HTTP(S)")){
			HTTPUpdateTask httpUpdate = new HTTPUpdateTask(postHTTPTaskListener);
			httpUpdate.setTest(test);
			httpUpdate.activity = activity;
			httpUpdate.HOST = host;
			httpUpdate.USER = username;
			httpUpdate.PASS = password;
			httpUpdate.HTTURL = remoteDirectory;
			httpUpdate.execute(finalJsonString);
		}

	}
}
