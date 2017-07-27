package de.le_space.gps2ftpcommon;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import javax.net.SocketFactory;

import static de.le_space.gps2ftpcommon.Constants.loadTitlePref;
import static de.le_space.gps2ftpcommon.Constants.saveTitlePref;

/**
 * Created by Nico Krause (nico@le-space.de) on 28.06.17. (Le Space UG)
 */
public class AddressResultReceiver extends ResultReceiver {

	protected Location mLastLocation;

	private Activity activity;
	private GoogleMap googleMap;
	private GoogleApiClient mGoogleApiClient;

	public AddressResultReceiver(Handler handler, Location mLastLocation, Activity activity, GoogleMap googleMap, GoogleApiClient mGoogleApiClient) {
			super(handler);
			this.mLastLocation = mLastLocation;
			this.activity = activity;
			this.googleMap = googleMap;
			this.mGoogleApiClient = mGoogleApiClient;
	}

	@Override
	protected void onReceiveResult(int resultCode, Bundle resultData) {

		double lat = mLastLocation.getLatitude();
		double lng = mLastLocation.getLongitude();
		int zoom = (int) googleMap.getCameraPosition().zoom;

		String googleMapsApikey = loadTitlePref(activity.getApplicationContext(),1,"googleMapsApiKey");

		if(googleMapsApikey==null || googleMapsApikey.length()!=39)
			googleMapsApikey=activity.getString(R.string.googleMapsApiKey);

		String jsonStringPositionConfig = "{\n" +
				"    \"lat\": \""+Double.toString(lat)+"\",\n" +
				"    \"lng\": \""+Double.toString(lng)+"\",\n" +
				"    \"zoom\": "+(zoom+1)+",\n" +
				"    \"googleMapsApiKey\": \""+googleMapsApikey+"\"\n" +
				"}";

		JSONObject lastPositionJsonObject = null;

		try {

			lastPositionJsonObject = new JSONObject(jsonStringPositionConfig);

		String mAddressOutputLastAddress = resultData.getString(Constants.RESULT_DATA_FULLADDRESS);
		String mAddressOutputLastCityName = resultData.getString(Constants.RESULT_DATA_CITYNAME);
		String mAddressOutputLastZipCode = resultData.getString(Constants.RESULT_DATA_ZIP_CODE);
		String mAddressOutputLastCountryCode = resultData.getString(Constants.RESULT_DATA_COUNTRY_CODE);

			// Show a toast message if an address was found.
		if (resultCode == Constants.SUCCESS_RESULT) {
			//save our position to the device
			lastPositionJsonObject.put("lastAddress", mAddressOutputLastAddress.replaceAll("\n","<br/>"));
			lastPositionJsonObject.put("lastCity", mAddressOutputLastCityName);
			lastPositionJsonObject.put("lastZipCode", mAddressOutputLastZipCode);
			lastPositionJsonObject.put("lastCountryCode", mAddressOutputLastCountryCode);

			saveTitlePref(activity.getApplicationContext(),1, "lastPosition", lastPositionJsonObject.toString()+":"+new Date().getTime());
			saveTitlePref(activity.getApplicationContext(),1, "lastAddress", mAddressOutputLastAddress);
			saveTitlePref(activity.getApplicationContext(),1, "lastCity", mAddressOutputLastCityName);
			saveTitlePref(activity.getApplicationContext(),1, "lastZipCode", mAddressOutputLastZipCode);
			saveTitlePref(activity.getApplicationContext(),1, "lastCountryCode", mAddressOutputLastCountryCode);

			final PutDataMapRequest putRequest = PutDataMapRequest.create("/gps2ftp");
			final DataMap map = putRequest.getDataMap();
			map.putString("lastPosition", lastPositionJsonObject.toString());
			map.putString("lastAddress", mAddressOutputLastAddress);
			map.putString("lastCity", mAddressOutputLastCityName);
			map.putString("lastZipCode", mAddressOutputLastZipCode);
			map.putString("lastCountryCode", mAddressOutputLastCountryCode);

			Utils.sendConfigItems(mGoogleApiClient,putRequest);

			Toast.makeText(activity,
					activity.getString(R.string.address_found)+":\n "+ mAddressOutputLastAddress,
					Toast.LENGTH_LONG).show();
		}
		else{
			Toast.makeText(activity,
					activity.getString(R.string.no_address_found)+":\n "+ mAddressOutputLastAddress,
					Toast.LENGTH_LONG).show();

		}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		AlertDialog.Builder adb = new AlertDialog.Builder(activity);
		//adb.setView(R.layout.activity_main);
		adb.setTitle(R.string.publish_ftp);
		adb.setIcon(android.R.drawable.ic_dialog_alert);

		//final String finalJsonString = jsonStringPositionConfig;
		final JSONObject finalLastPositionJsonObject = lastPositionJsonObject;
		adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {


				ConnectivityManager.NetworkCallback mNetworkCallback = new ConnectivityManager.NetworkCallback() {
						@Override
						public void onAvailable(Network network) {
							SocketFactory sf = network.getSocketFactory();
							publishToServer(finalLastPositionJsonObject.toString(), sf);
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
						publishToServer(finalLastPositionJsonObject.toString(), activeNetwork.getSocketFactory());
					}
				}


			} });

		adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				//finish();
			} });
		adb.show();

	}


	FTPUpdateTask.PostFTPTaskListener<String> postFTPSTaskListener = new FTPUpdateTask.PostFTPTaskListener<String>() {
		@Override
		public void onError(String result) {
			Intent intent = new Intent(Constants.LOCATION_UPDATE_ERROR);
			intent.putExtra("error", result);
			LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
		}

		@Override
		public void onSuccess(String result) {
			Intent intent = new Intent(Constants.LOCATION_UPDATE_SUCCESS);
			intent.putExtra("info", result);
			LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);

		}
	};

	HTTPUpdateTask.PostHTTPTaskListener<String> postHTTPTaskListener = new HTTPUpdateTask.PostHTTPTaskListener<String>() {
		@Override
		public void onError(String result) {
			Intent intent = new Intent(Constants.LOCATION_UPDATE_ERROR);
			intent.putExtra("error", result);
			LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
		}

		@Override
		public void onSuccess(String result) {
			Intent intent = new Intent(Constants.LOCATION_UPDATE_SUCCESS);
			intent.putExtra("info", result);
			LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);

		}
	};

	public void publishToServer(String finalJsonString, SocketFactory sf){

		String protocol = loadTitlePref(activity,1,"protocol");

		if(protocol.equals("SFTP")){
			FTPUpdateTask ftpUpdate = new FTPUpdateTask(postFTPSTaskListener);
			ftpUpdate.setSocketFactory(sf);
			//ftpUpdate.view = activity.getCurrentFocus(); //activity.findViewById(R.id.coordinatorLayout);
			ftpUpdate.activity = activity;
			ftpUpdate.SFTPHOST = loadTitlePref(activity,1,"host"); //"ftp.le-space.de"; //
			ftpUpdate.SFTPUSER = loadTitlePref(activity,1,"username"); //"le-space"; //
			ftpUpdate.SFTPPASS = loadTitlePref(activity,1,"password"); //"verbatim-stroll-month"; //
			ftpUpdate.SFTPWORKINGDIR = loadTitlePref(activity,1,"remoteDirectory"); //"/home/le-space/public_html"; //
			ftpUpdate.execute(finalJsonString);
		}

		if(protocol.equals("HTTP(S)")){
			HTTPUpdateTask httpUpdate = new HTTPUpdateTask(postHTTPTaskListener);
			httpUpdate.activity = activity;
			httpUpdate.SHTTPHOST = loadTitlePref(activity,1,"host"); //"ftp.le-space.de"; //
			httpUpdate.SHTTPUSER = loadTitlePref(activity,1,"username"); //"le-space"; //
			httpUpdate.SHTTPPASS = loadTitlePref(activity,1,"password"); //"verbatim-stroll-month"; //
			httpUpdate.SHTTURL = loadTitlePref(activity,1,"remoteDirectory"); //"/home/le-space/public_html"; //
			httpUpdate.execute(finalJsonString);
		}

	}
}

