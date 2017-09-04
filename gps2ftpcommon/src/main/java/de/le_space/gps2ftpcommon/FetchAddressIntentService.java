package de.le_space.gps2ftpcommon;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

/**
 * Created by Nico Krause (nico@le-space.de) on 21.06.17. (Le Space UG)
 */

public class FetchAddressIntentService extends IntentService {

	protected ResultReceiver mReceiver;

	public FetchAddressIntentService(){
		super("FetchAddressIntentService");
	}
	/**
	 * Creates an IntentService.  Invoked by your subclass's constructor.
	 *
	 * @param name Used to name the worker thread, important only for debugging.
	 */
	public FetchAddressIntentService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		mReceiver = intent.getParcelableExtra(Constants.RECEIVER);

		Geocoder geocoder = new Geocoder(this, Locale.getDefault());
		String errorMessage = "";

		// Get the location passed to this service through an extra.
		Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);

		if(!Geocoder.isPresent()){
			return;
		}

		List<Address> addresses = null;
		try {
				addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),
					// In this sample, get just a single address.
					1);
		} catch (IOException ioException) {
			// Catch network or other I/O problems.
			errorMessage = getString(R.string.service_not_available);
			Log.e(TAG, errorMessage, ioException);

		} catch (IllegalArgumentException illegalArgumentException) {

			// Catch invalid latitude or longitude values.
			errorMessage = getString(R.string.invalid_lat_long_used);
			Log.e(TAG, errorMessage + ". " +
					"Latitude = " + location.getLatitude() +
					", Longitude = " +
					location.getLongitude(), illegalArgumentException);
		}

		// Handle case where no address was found.
		if (addresses == null || addresses.size()  == 0) {

			if (errorMessage.isEmpty()) {
				errorMessage = getString(R.string.no_address_found);
				Log.e(TAG, errorMessage);
			}

			deliverResultToReceiver(Constants.FAILURE_RESULT,errorMessage, null, null, null);
		} else {

			Address address = addresses.get(0);
			StringBuilder addressFragments = new StringBuilder();
			for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
				addressFragments.append(address.getAddressLine(i)+"\n");
			}

			Log.i(TAG, getString(R.string.address_found));
			//TextUtils.join(System.getProperty("line.separator")
			deliverResultToReceiver(Constants.SUCCESS_RESULT,addressFragments.toString(),address.getLocality(),address.getCountryCode(),address.getPostalCode());
		}
	}

	private void deliverResultToReceiver(int resultCode, String fullAddress, String cityName, String countryCode, String zipCode) {
		Bundle bundle = new Bundle();
		bundle.putString(Constants.RESULT_DATA_FULLADDRESS, fullAddress);
		bundle.putString(Constants.RESULT_DATA_CITYNAME, cityName);
		bundle.putString(Constants.RESULT_DATA_COUNTRY_CODE, countryCode);
		bundle.putString(Constants.RESULT_DATA_ZIP_CODE, zipCode);
		mReceiver.send(resultCode, bundle);
	}

}
