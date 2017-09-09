package de.le_space.gps2ftp;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.DismissOverlayView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;

import de.le_space.gps2ftpcommon.DataLayerListener;
import de.le_space.gps2ftpcommon.LocationUpdates;
import de.le_space.gps2ftpcommon.Utils;

import static de.le_space.gps2ftpcommon.Constants.LOCATION_FOUND;
import static de.le_space.gps2ftpcommon.Constants.LOCATION_NOT_FOUND;
import static de.le_space.gps2ftpcommon.Constants.LOCATION_PUBLISH_ERROR;
import static de.le_space.gps2ftpcommon.Constants.LOCATION_PUBLISH_SUCCESS;
import static de.le_space.gps2ftpcommon.Constants.loadTitlePref;
import static de.le_space.gps2ftpcommon.Constants.mGoogleApiClient;

public class WearMainActivity extends Activity implements
		OnMapReadyCallback,
		GoogleMap.OnMapLongClickListener,
		GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener,
		DataApi.DataListener
{

	private static final String TAG = "Main";
	private MapFragment mMapFragment;
	private GoogleMap googleMap;
	private DismissOverlayView mDismissOverlay;
	private Activity thisActivity;
	private Location mLastLocation;

	private static final int GPS2FTP_NOTIFICATION_ID = 2017090301;

	private static String[] PERMISSIONS_LOCATION = {android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

	private LocalBroadcastManager bManager;
	private LocationUpdates lu;

	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);

		setContentView(R.layout.activity_maps);
		thisActivity = this;

		final FrameLayout topFrameLayout = findViewById(R.id.root_container);
		final FrameLayout mapFrameLayout = findViewById(R.id.map_container);

		lu = new LocationUpdates(this);

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

		mDismissOverlay = findViewById(R.id.dismiss_overlay);
		mDismissOverlay.setIntroText(R.string.intro_text);
		mDismissOverlay.showIntroIfNecessary();

		MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		if (mGoogleApiClient == null) {
			mGoogleApiClient = new GoogleApiClient.Builder(this)
					.addApi(LocationServices.API)
					.addApi(Wearable.API)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.build();
		}

		mGoogleApiClient.connect();
		registerReceiver();
	}

	public void registerReceiver(){
		//receives a message when Server was updated
		bManager = LocalBroadcastManager.getInstance(this);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(LOCATION_FOUND);
		intentFilter.addAction(LOCATION_NOT_FOUND);
		intentFilter.addAction(LOCATION_PUBLISH_SUCCESS);
		intentFilter.addAction(LOCATION_PUBLISH_ERROR);

		bManager.registerReceiver(bReceiver, intentFilter);
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		this.googleMap = googleMap;
		this.googleMap.setOnMapLongClickListener(this);
		lu.startLocationUpdates(googleMap);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Wearable.DataApi.addListener(mGoogleApiClient, this);
		mGoogleApiClient.connect();
		registerReceiver();
	}

	@Override
	protected void onStop() {
		super.onStop();

		bManager.unregisterReceiver(bReceiver);
		if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
		//	Wearable.DataApi.removeListener(mGoogleApiClient, this);
			mGoogleApiClient.disconnect();
		}
		lu.stopLocationUpdates();
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver();
		lu.startLocationUpdates(this.googleMap);
	}

	@Override
	protected void onPause() {
		super.onPause();
		bManager.unregisterReceiver(bReceiver);
		if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
			//Wearable.DataApi.removeListener(mGoogleApiClient, this);
			mGoogleApiClient.disconnect();
		}
		lu.stopLocationUpdates();
	}

	@Override
	public void onMapLongClick(LatLng latLng) {
		mDismissOverlay.show();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

	}

	@Override
	public void onDataChanged(DataEventBuffer dataEventBuffer) {
				new DataLayerListener(getApplicationContext()).onDataChanged(dataEventBuffer);
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {

		Wearable.DataApi.addListener(mGoogleApiClient, this);

		/*// Create the LocationRequest object
		LocationRequest locationRequest = LocationRequest.create();
		// Use high accuracy
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		// Set the update interval to 2 seconds
		locationRequest.setInterval(TimeUnit.MINUTES.toMillis(30));
		// Set the fastest update interval to 2 seconds
		locationRequest.setFastestInterval(TimeUnit.MINUTES.toMillis(1));
		// Set the minimum displacement
		locationRequest.setSmallestDisplacement(200);

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(thisActivity, PERMISSIONS_LOCATION, 0);
		}else{
			//LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, thisActivity);
		}*/
	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		if (event.getRepeatCount() == 0) {
			if (keyCode == KeyEvent.KEYCODE_STEM_1) {
				lu.startLocationUpdates(this.googleMap);
				//Utils.publishPosition(thisActivity);
				//lu.processLocation(mLastLocation);
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_STEM_2) {
				//lu.processLocation(mLastLocation);
				Utils.publishPosition(thisActivity);
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_STEM_3) {
				Toast.makeText(WearMainActivity.this," no function yet", Toast.LENGTH_SHORT).show();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * After a server update we get informed about sucess or error
	 * Success: Update Widget
	 * Error: Open Settings
	 */
	private BroadcastReceiver bReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(LOCATION_FOUND)) {
				String lastAddress = loadTitlePref(context,1,"lastAddress");
				Toast.makeText(WearMainActivity.this,lastAddress,Toast.LENGTH_LONG).show();
			}
			if(intent.getAction().equals(LOCATION_NOT_FOUND)) {
				Toast.makeText(WearMainActivity.this,R.string.location_not_found,Toast.LENGTH_LONG).show();
			}
			if(intent.getAction().equals(LOCATION_PUBLISH_SUCCESS)) {
				Toast.makeText(WearMainActivity.this,R.string.location_published_successfully,Toast.LENGTH_LONG).show();
			}
			if(intent.getAction().equals(LOCATION_PUBLISH_ERROR)) {
				Toast.makeText(WearMainActivity.this,R.string.location_published_error,Toast.LENGTH_LONG).show();
			}

			//always update
			if(intent.getAction().equals(LOCATION_FOUND) || intent.getAction().equals(LOCATION_NOT_FOUND)){
				JSONObject lastPosition = null;
				try {
					lastPosition = new JSONObject(
							loadTitlePref(context,1,"lastPosition"));

					Log.d(TAG, lastPosition.toString());
					mLastLocation = new Location("");
					mLastLocation.setLatitude(lastPosition.getDouble("lat"));//your coords of course
					mLastLocation.setLongitude(lastPosition.getDouble("lng"));
					googleMap.animateCamera( CameraUpdateFactory.newLatLng(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude())));
					googleMap.animateCamera( CameraUpdateFactory.zoomTo(new Float(lastPosition.getInt("zoom"))));

				} catch (JSONException e) {
					Log.e(TAG, e.getMessage());
				}//R.string.location_found
			}
		}
	};

}
