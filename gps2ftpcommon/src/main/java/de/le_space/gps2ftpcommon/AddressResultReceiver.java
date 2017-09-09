package de.le_space.gps2ftpcommon;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;

import org.json.JSONException;
import org.json.JSONObject;

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

			//always save position -  or not an address could be found.
			saveTitlePref(activity.getApplicationContext(),1, "lastPosition", lastPositionJsonObject.toString());

			// Show a toast message if an address was found.
			if (resultCode == Constants.SUCCESS_RESULT) {
				//save our position to the device
				lastPositionJsonObject.put("lastAddress", mAddressOutputLastAddress);
				lastPositionJsonObject.put("lastCity", mAddressOutputLastCityName);
				lastPositionJsonObject.put("lastZipCode", mAddressOutputLastZipCode);
				lastPositionJsonObject.put("lastCountryCode", mAddressOutputLastCountryCode);

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

				Intent intent = new Intent(Constants.LOCATION_FOUND);
				LocalBroadcastManager.getInstance(activity.getApplicationContext()).sendBroadcast(intent);
			}
			else{
				Intent intent = new Intent(Constants.LOCATION_NOT_FOUND);
				LocalBroadcastManager.getInstance(activity.getApplicationContext()).sendBroadcast(intent);

			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}



}

