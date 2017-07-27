package de.le_space.gps2ftp;

import android.Manifest;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
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

import de.le_space.gps2ftpcommon.AddressResultReceiver;
import de.le_space.gps2ftpcommon.Constants;
import de.le_space.gps2ftpcommon.DataLayerListener;
import de.le_space.gps2ftpcommon.FetchAddressIntentService;

import static de.le_space.gps2ftp.R.id.map;
import static de.le_space.gps2ftpcommon.Constants.LOCATION_UPDATE_ERROR;
import static de.le_space.gps2ftpcommon.Constants.LOCATION_UPDATE_SUCCESS;
import static de.le_space.gps2ftpcommon.Constants.mGoogleApiClient;

public class MainActivity extends AppCompatActivity  implements OnMapReadyCallback,	DataApi.DataListener {

	private static final String TAG = "Main";
	private FusedLocationProviderClient mFusedLocationClient;
	private LocationCallback mLocationCallback;
	private LocationRequest mLocationRequest;
	private static String[] PERMISSIONS_LOCATION = {android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
	private CoordinatorLayout coordinatorLayout;
	private Activity thisActivity;
	private GoogleMap googleMap;
	private AddressResultReceiver mResultReceiver;
	protected Location mLastLocation;
	private LocalBroadcastManager bManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		thisActivity = this;
		coordinatorLayout = findViewById(R.id.coordinatorLayout);
		FloatingActionButton fab = findViewById(R.id.fab);
		mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());


		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {

				if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
						android.Manifest.permission.ACCESS_FINE_LOCATION)
						|| ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
						android.Manifest.permission.ACCESS_COARSE_LOCATION)) {


					// Display a SnackBar with an explanation and a button to trigger the request.
					Snackbar.make(view, "Requesting Permissions",
							Snackbar.LENGTH_INDEFINITE)
							.setAction("OK", new View.OnClickListener() {
								@Override
								public void onClick(View view) {
									ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_LOCATION, 0);
								}
							})
							.show();
				} else {
					//permissions have not been granted yet. Request them directly.
					ActivityCompat.requestPermissions(thisActivity, PERMISSIONS_LOCATION, 0);
				}
			}
		});

		MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(map);
		mapFragment.getMapAsync(this);

		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
					@Override
					public void onConnected(Bundle connectionHint) {
						Wearable.DataApi.addListener(mGoogleApiClient, MainActivity.this);
					}
					@Override
					public void onConnectionSuspended(int cause) {
						Wearable.DataApi.removeListener(mGoogleApiClient, MainActivity.this);
					}
				})
				.addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
					@Override
					public void onConnectionFailed(ConnectionResult result) {
						Wearable.DataApi.removeListener(mGoogleApiClient, MainActivity.this);
					}
				})
				.addApi(Wearable.API)
				.build();

		mGoogleApiClient.connect();

		mLocationRequest = LocationRequest.create();
		mLocationCallback = new LocationCallback() {
			@Override
			public void onLocationResult(LocationResult locationResult) {
				mLastLocation =  locationResult.getLocations().get(0);
				processLocation();
			};
		};

		Intent intent = getIntent();
		if (intent.getStringExtra("direct") != null) {
			getLastLocation();
		}
		// ATTENTION: This was auto-generated to handle app links.
		Intent appLinkIntent = getIntent();
		String appLinkAction = appLinkIntent.getAction();
		Uri appLinkData = appLinkIntent.getData();

		//receives a message when FTP was updated
		bManager = LocalBroadcastManager.getInstance(this);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(LOCATION_UPDATE_SUCCESS);
		intentFilter.addAction(LOCATION_UPDATE_ERROR);
		bManager.registerReceiver(bReceiver, intentFilter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		bManager.unregisterReceiver(bReceiver);
	}

	private boolean hasGps() {
		return getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
	}


	public void getLastLocation() {

		int accessFineLocation = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
		int accessCoarseLocation = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);

		if (accessFineLocation != PackageManager.PERMISSION_GRANTED && accessCoarseLocation != PackageManager.PERMISSION_GRANTED) {
			Snackbar.make(coordinatorLayout, "Please give the required permission to the app!", Snackbar.LENGTH_LONG).setAction("Action", null).show();
			return;
		} else {

			if (hasGps()) {
				Log.d(TAG, "This hardware doesn't have GPS.");
				mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallback,null);
			}else{
				mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
					@Override
					public void onSuccess(Location location) {
						mLastLocation = location;
						Snackbar.make(coordinatorLayout, "Got new position", Snackbar.LENGTH_LONG).setAction("Action", null).show();

						// In some rare cases the location returned can be null
						if (mLastLocation == null) {
							return;
						}
						processLocation();
					}
				});
			}
		}

	}

	public void processLocation(){
		LatLng latlng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
		googleMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));

		MarkerOptions marker = new MarkerOptions().position(latlng);
		marker.title("I am here");
		googleMap.addMarker(marker);
		if (!Geocoder.isPresent()) {
			Toast.makeText(MainActivity.this,R.string.no_geocoder_available,
					Toast.LENGTH_LONG).show();
			return;
		}

		mResultReceiver = new AddressResultReceiver(new Handler(),mLastLocation,thisActivity,googleMap, mGoogleApiClient);

		startIntentService();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();

		if (id == R.id.action_settings) {
			startSettingsActivity();
		}

		return super.onOptionsItemSelected(item);
	}

	private void startSettingsActivity() {
		Intent intent = new Intent(this, PositionUpdateConfigureActivity.class);
		intent.putExtra("appWidgetId", 1);
		startActivity(intent);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == 0) {
			// Request for camera permission.
			if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				getLastLocation();
			} else {
				// Permission request was denied.
				Snackbar.make(coordinatorLayout, "Location permission request was denied.", Snackbar.LENGTH_SHORT).show();
			}
		}
	}

	protected void startIntentService() {
		Intent intent = new Intent(this, FetchAddressIntentService.class);
		intent.putExtra(Constants.RECEIVER, mResultReceiver);
		intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
		startService(intent);
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
			this.googleMap = googleMap;
			getLastLocation();
	}

	@Override
	public void onDataChanged(DataEventBuffer dataEventBuffer) {
		new DataLayerListener(getApplicationContext()).onDataChanged(dataEventBuffer);
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
				// It is the responsibility of the configuration activity to update the app widget
				AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(thisActivity);

				ComponentName name = new ComponentName(getApplicationContext(), PositionUpdate.class);
				int [] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(name);
				if(ids.length>0)
					PositionUpdate.updateAppWidget(context, appWidgetManager, ids[0]);

				Toast.makeText(MainActivity.this,R.string.location_published_successfully,Toast.LENGTH_LONG).show();
			}
			if(intent.getAction().equals(LOCATION_UPDATE_ERROR)) {
				startSettingsActivity();
			}
		}
	};
}


