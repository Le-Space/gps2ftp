package de.le_space.gps2ftpcommon;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.concurrent.TimeUnit;

import static de.le_space.gps2ftpcommon.Constants.mGoogleApiClient;
import static de.le_space.gps2ftpcommon.Constants.saveTitlePref;

/**
 * Created by Nico Krause (nico@le-space.de) on 04.09.17. (Le Space UG)
 */

public class LocationUpdates {

	private static final String TAG = "LocationUpdates";
	private FusedLocationProviderClient mFusedLocationClient;
	private LocationCallback mLocationCallback;
	private LocationRequest mLocationRequest;
	private Activity activity;
	private AddressResultReceiver mResultReceiver;
	private GoogleMap googleMap;
	Location mLastLocation = null;

	public LocationUpdates(Activity activity){
		this.activity = activity;

		//what are we doing here? we use location from settings.
		mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
		mLocationRequest = LocationRequest.create();
		mLocationRequest.setSmallestDisplacement(100);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		// Set the update interval to 2 seconds
		mLocationRequest.setInterval(TimeUnit.MINUTES.toMillis(30));
		// Set the fastest update interval to 2 seconds
		mLocationRequest.setFastestInterval(TimeUnit.MINUTES.toMillis(1));
		// Set the minimum displacement
		mLocationRequest.setSmallestDisplacement(200);

		//mLocationRequest.setFastestInterval(2000);
		mLocationCallback = new LocationCallback() {

			@Override
			public void onLocationAvailability(LocationAvailability locationAvailability) {

			}

			@Override
			public void onLocationResult(LocationResult locationResult) {
				processLocation(locationResult.getLocations().get(0));
			}
		};

	}
	private static String[] PERMISSIONS_LOCATION = {android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

	public void startLocationUpdates(GoogleMap googleMap) {
		this.googleMap = googleMap;

		int accessFineLocation = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION);
		int accessCoarseLocation = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION);

		if (accessFineLocation != PackageManager.PERMISSION_GRANTED && accessCoarseLocation != PackageManager.PERMISSION_GRANTED) {
			if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
					android.Manifest.permission.ACCESS_FINE_LOCATION)
					|| ActivityCompat.shouldShowRequestPermissionRationale(activity,
					android.Manifest.permission.ACCESS_COARSE_LOCATION)) {

				ActivityCompat.requestPermissions(activity, PERMISSIONS_LOCATION, 0);

				/*
				// Display a SnackBar with an explanation and a button to trigger the request.
			//	Snackbar.make(activity.getCurrentFocus(), "Requesting Permissions",Snackbar.LENGTH_INDEFINITE)
			//			.setAction("OK", new View.OnClickListener() {
							@Override
							public void onClick(View view) {
								//ActivityCompat.requestPermissions(activity, PERMISSIONS_LOCATION, 0);
			//				}
						})
			//			.show();*/
			} else {
				//permissions have not been granted yet. Request them directly.
				ActivityCompat.requestPermissions(activity, PERMISSIONS_LOCATION, 0);
			}

			return;
		} else {
			if (hasGps()) {
				Toast.makeText(activity, "requesting location", Toast.LENGTH_LONG).show();
				mFusedLocationClient.flushLocations();
				mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null /* Looper */);
			} else {
				mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
					@Override
					public void onSuccess(Location location) {
						Toast.makeText(activity, "last location", Toast.LENGTH_SHORT).show();
						processLocation(location);
					}
				});
			}
		}
	}


	public void processLocation(Location location) {

		if(mLastLocation!=null && location.toString().equals(mLastLocation.toString()))return;

		mLastLocation = location;
		Log.d(TAG, "process Location called: with:"+mLastLocation.toString());

		LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
		googleMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
		MarkerOptions marker = new MarkerOptions().position(latlng);
		marker.title("I am here");
		googleMap.addMarker(marker);

		if (!Geocoder.isPresent()) {
			return;
		}

		mResultReceiver = new AddressResultReceiver(new Handler(), location, activity, googleMap, mGoogleApiClient);
		startAddressIntentService(location);
	}

	protected void startAddressIntentService(Location location) {
		Intent intent = new Intent(activity, FetchAddressIntentService.class);
		intent.putExtra(Constants.RECEIVER, mResultReceiver);
		intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
		activity.getApplicationContext().startService(intent);
	}

	private boolean hasGps() {
		return activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
	}


	public void stopLocationUpdates() {
		if(googleMap!=null){
			saveTitlePref(activity.getApplicationContext(),1, "CameraPosition",String.valueOf(googleMap.getCameraPosition().zoom));
			Log.d(TAG,"saved CameraPosition Zoom:"+googleMap.getCameraPosition().zoom);
		}

		//mFusedLocationClient.removeLocationUpdates(mLocationCallback); creates an error... not sure why
		//mFusedLocationClient.removeLocationUpdates(mLocationCallback);
	}


}
