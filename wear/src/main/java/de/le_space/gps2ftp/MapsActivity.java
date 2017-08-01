package de.le_space.gps2ftp;

import android.Manifest;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.DismissOverlayView;
import android.view.View;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.TimeUnit;

import de.le_space.gps2ftpcommon.AddressResultReceiver;
import de.le_space.gps2ftpcommon.Constants;
import de.le_space.gps2ftpcommon.DataLayerListener;
import de.le_space.gps2ftpcommon.FetchAddressIntentService;

import static de.le_space.gps2ftpcommon.Constants.LOCATION_UPDATE_ERROR;
import static de.le_space.gps2ftpcommon.Constants.LOCATION_UPDATE_SUCCESS;
import static de.le_space.gps2ftpcommon.Constants.mGoogleApiClient;

public class MapsActivity extends Activity implements OnMapReadyCallback,
		GoogleMap.OnMapLongClickListener, GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener,
		DataApi.DataListener,
		LocationListener {

	private MapFragment mMapFragment;
	private FusedLocationProviderClient mFusedLocationClient;
	private LocationCallback mLocationCallback;
	private LocationRequest mLocationRequest;
	private DismissOverlayView mDismissOverlay;
	private GoogleMap mMap;
	private AddressResultReceiver mResultReceiver;
	private Location mLastLocation;
	private Activity thisActivity;
	private static String[] PERMISSIONS_LOCATION = {android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

	private LocalBroadcastManager bManager;

	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);

		setContentView(R.layout.activity_maps);
		thisActivity = this;

		final FrameLayout topFrameLayout = findViewById(R.id.root_container);
		final FrameLayout mapFrameLayout = findViewById(R.id.map_container);

		// Set the system view insets on the containers when they become available.
		topFrameLayout.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
			@Override
			public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
				// Call through to super implementation and apply insets
				insets = topFrameLayout.onApplyWindowInsets(insets);

				FrameLayout.LayoutParams params =
						(FrameLayout.LayoutParams) mapFrameLayout.getLayoutParams();

				// Add Wearable insets to FrameLayout container holding map as margins
				params.setMargins(
						insets.getSystemWindowInsetLeft(),
						insets.getSystemWindowInsetTop(),
						insets.getSystemWindowInsetRight(),
						insets.getSystemWindowInsetBottom());
				mapFrameLayout.setLayoutParams(params);

				return insets;
			}
		});

		// Obtain the DismissOverlayView and display the introductory help text.
		mDismissOverlay = findViewById(R.id.dismiss_overlay);
		mDismissOverlay.setIntroText(R.string.intro_text);
		mDismissOverlay.showIntroIfNecessary();

		// Obtain the MapFragment and set the async listener to be notified when the map is ready.
		MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		//what are we doing here? we use location from settings.
		mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
		mLocationRequest = LocationRequest.create();
		mLocationRequest.setSmallestDisplacement(200);

		mLocationCallback = new LocationCallback() {
			@Override
			public void onLocationResult(LocationResult locationResult) {
				mLastLocation = locationResult.getLocations().get(0);
				Toast.makeText(MapsActivity.this, "Got new position", Toast.LENGTH_LONG).show();
				processLocation();
			}

			;
		};

		//receives a message when FTP was updated
		bManager = LocalBroadcastManager.getInstance(this);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(LOCATION_UPDATE_SUCCESS);
		intentFilter.addAction(LOCATION_UPDATE_ERROR);
		bManager.registerReceiver(bReceiver, intentFilter);

		if (mGoogleApiClient == null) {
			mGoogleApiClient = new GoogleApiClient.Builder(this)
					.addApi(LocationServices.API)
					.addApi(Wearable.API)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.build();
		}

		mGoogleApiClient.connect();
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;
		mMap.setOnMapLongClickListener(this);
		startLocationUpdates();
	}

	public void startLocationUpdates() {

		int accessFineLocation = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
		int accessCoarseLocation = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);

		if (accessFineLocation != PackageManager.PERMISSION_GRANTED && accessCoarseLocation != PackageManager.PERMISSION_GRANTED) {
			//	Snackbar.make(coordinatorLayout, "Please give the required permission to the app!", Snackbar.LENGTH_LONG).setAction("Action", null).show();
			return;
		} else {


			if (hasGps()) {
				//Toast.makeText(MapsActivity.this, "this device has gps", Toast.LENGTH_LONG).show();
				mFusedLocationClient.flushLocations();

				mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null /* Looper */);

			} else {
				mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
					@Override
					public void onSuccess(Location location) {
						mLastLocation = location;
						// In some rare cases the location returned can be null
						if (mLastLocation == null) {
							return;
						}
						Toast.makeText(MapsActivity.this, "last location", Toast.LENGTH_LONG).show();
						processLocation();
					}
				});
			}
		}

	}
	private void stopLocationUpdates() {
		if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
			Wearable.DataApi.removeListener(mGoogleApiClient, this);
			mGoogleApiClient.disconnect();
		}
		mFusedLocationClient.removeLocationUpdates(mLocationCallback);
	}

	public void processLocation() {
		LatLng latlng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
		mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));

		MarkerOptions marker = new MarkerOptions().position(latlng);
		marker.title("I am here");
		mMap.addMarker(marker);
		if (!Geocoder.isPresent()) {
			return;
		}

		mResultReceiver = new AddressResultReceiver(new Handler(), mLastLocation, thisActivity, mMap, mGoogleApiClient);

		startIntentService();
	}


	@Override
	protected void onStart() {
		super.onStart();
		Wearable.DataApi.addListener(mGoogleApiClient, this);
		mGoogleApiClient.connect();
	}

	@Override
	protected void onStop() {
		super.onStop();
		stopLocationUpdates();
	}

	@Override
	protected void onResume() {
		super.onResume();

		startLocationUpdates();
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopLocationUpdates();
	}

	@Override
	public void onMapLongClick(LatLng latLng) {
		mDismissOverlay.show();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

	}

	protected void startIntentService() {
		Intent intent = new Intent(this, FetchAddressIntentService.class);
		intent.putExtra(Constants.RECEIVER, mResultReceiver);
		intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
		startService(intent);
	}

	@Override
	public void onLocationChanged(Location location) {

		if (location != null) {
			mLastLocation = location;
			LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
			mMap.addMarker(new MarkerOptions().position(latLng).title("I'm here"));
			mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

			if (mLastLocation == null) {
				return;
			}

			if (!Geocoder.isPresent()) {
				Toast.makeText(MapsActivity.this,R.string.no_geocoder_available, Toast.LENGTH_LONG).show();
				return;
			}

			mResultReceiver = new AddressResultReceiver(new Handler(),mLastLocation,thisActivity,mMap, mGoogleApiClient);

			startIntentService();

			//1. update widget with new address
			Intent intent = new Intent(); //activity,PositionUpdate.class
			intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			sendBroadcast(intent);

		}else{
			Toast.makeText(MapsActivity.this,"No position", Toast.LENGTH_LONG).show();
		}
	}


	@Override
	public void onDataChanged(DataEventBuffer dataEventBuffer) {
				new DataLayerListener(getApplicationContext()).onDataChanged(dataEventBuffer);
	}


	@Override
	public void onConnected(@Nullable Bundle bundle) {

		Wearable.DataApi.addListener(mGoogleApiClient, this);

		// Create the LocationRequest object
		LocationRequest locationRequest = LocationRequest.create();
		// Use high accuracy
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		// Set the update interval to 2 seconds
		locationRequest.setInterval(TimeUnit.MINUTES.toMillis(30));
		// Set the fastest update interval to 2 seconds
		locationRequest.setFastestInterval(TimeUnit.MINUTES.toMillis(1));
		// Set the minimum displacement
		locationRequest.setSmallestDisplacement(2);

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(thisActivity, PERMISSIONS_LOCATION, 0);
		}else{
			LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, (com.google.android.gms.location.LocationListener) thisActivity);
		}
	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

	}

	private boolean hasGps() {
		return getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
	}

	/**
	 * After a server update we get informed about sucess or error
	 * Success: Update Widget
	 * Error: Open Settings
	 */
	private BroadcastReceiver bReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(LOCATION_UPDATE_SUCCESS)) {
				Toast.makeText(MapsActivity.this,R.string.location_published_successfully,Toast.LENGTH_LONG).show();
			}
			if(intent.getAction().equals(LOCATION_UPDATE_ERROR)) {
				Toast.makeText(MapsActivity.this,R.string.location_published_error,Toast.LENGTH_LONG).show();
			}
		}
	};

}
