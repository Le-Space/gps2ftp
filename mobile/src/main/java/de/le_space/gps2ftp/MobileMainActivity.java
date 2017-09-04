package de.le_space.gps2ftp;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;

import de.le_space.gps2ftpcommon.DataLayerListener;
import de.le_space.gps2ftpcommon.LocationUpdates;
import de.le_space.gps2ftpcommon.Utils;

import static de.le_space.gps2ftp.R.id.map;
import static de.le_space.gps2ftpcommon.Constants.LOCATION_FOUND;
import static de.le_space.gps2ftpcommon.Constants.LOCATION_NOT_FOUND;
import static de.le_space.gps2ftpcommon.Constants.LOCATION_PUBLISH_ERROR;
import static de.le_space.gps2ftpcommon.Constants.LOCATION_PUBLISH_SUCCESS;
import static de.le_space.gps2ftpcommon.Constants.loadTitlePref;
import static de.le_space.gps2ftpcommon.Constants.mGoogleApiClient;
import static de.le_space.gps2ftpcommon.Constants.saveTitlePref;

public class MobileMainActivity extends AppCompatActivity implements
		OnMapReadyCallback,
		DataApi.DataListener {

	private static final String TAG = "Main";
	private LocationUpdates lu;
	private CoordinatorLayout coordinatorLayout;
	private LocalBroadcastManager bManager;
	private GoogleMap googleMap;
	private Activity thisActivity;
	private Location mLastLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		thisActivity = this;

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		coordinatorLayout = findViewById(R.id.coordinatorLayout);

		lu = new LocationUpdates(this);

		FloatingActionButton fab = findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				if(googleMap!=null)lu.startLocationUpdates(googleMap);
			}
		});

		MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(map);
		mapFragment.getMapAsync(this);

		mGoogleApiClient = new GoogleApiClient.Builder(this)
		.addConnectionCallbacks(
				new GoogleApiClient.ConnectionCallbacks() {
			@Override
			public void onConnected(Bundle connectionHint) {
				Wearable.DataApi.addListener(mGoogleApiClient, MobileMainActivity.this);
			}

			@Override
			public void onConnectionSuspended(int cause) {
				Wearable.DataApi.removeListener(mGoogleApiClient, MobileMainActivity.this);
			}
		})
		.addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
			@Override
			public void onConnectionFailed(ConnectionResult result) {
				Wearable.DataApi.removeListener(mGoogleApiClient, MobileMainActivity.this);
			}
		})
		.addApi(Wearable.API)
		.addApi(LocationServices.API)
		.build();

		mGoogleApiClient.connect();

		//receives a message when FTP was updated
		bManager = LocalBroadcastManager.getInstance(this);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(LOCATION_FOUND);
		intentFilter.addAction(LOCATION_NOT_FOUND);
		intentFilter.addAction(LOCATION_PUBLISH_SUCCESS);
		intentFilter.addAction(LOCATION_PUBLISH_ERROR);
		bManager.registerReceiver(bReceiver, intentFilter);

		// ATTENTION: This was auto-generated to handle app links.
		Intent appLinkIntent = getIntent();
		String appLinkAction = appLinkIntent.getAction();
		Uri appLinkData = appLinkIntent.getData();
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		this.googleMap = googleMap;

		try {
			JSONObject lastPosition = getLastPosition();
			if(lastPosition!=null){
				googleMap.animateCamera(CameraUpdateFactory.zoomTo(lastPosition.getLong("zoom")));

				/*	String sZoom = loadTitlePref(getApplicationContext(),1,"CameraPosition");
				if(googleMap!=null && sZoom!=null){
					googleMap.animateCamera( CameraUpdateFactory.zoomTo(new Float(sZoom)));
					Log.d(TAG,"loaded CameraPosition Zoom:"+sZoom);
				}*/
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}


		lu.startLocationUpdates(googleMap);
	}

	@Override
	public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
		Log.d(TAG,"bla ");
		super.onSaveInstanceState(outState, outPersistentState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		//lu.startLocationUpdates(googleMap);
	/*	String sZoom = loadTitlePref(getApplicationContext(),1,"CameraPosition");
		if(googleMap!=null && sZoom!=null){
			googleMap.animateCamera( CameraUpdateFactory.zoomTo(new Float(sZoom)));
			Log.d(TAG,"loaded CameraPosition Zoom:"+sZoom);
		}*/
	}

	@Override
	protected void onPause() {
		super.onPause();
		lu.stopLocationUpdates();
		saveTitlePref(this.getApplicationContext(),1, "CameraPosition",String.valueOf(googleMap.getCameraPosition().zoom));
		Log.d(TAG,"saved CameraPosition Zoom:"+googleMap.getCameraPosition().zoom);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		lu.stopLocationUpdates();
		bManager.unregisterReceiver(bReceiver);
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
					lu.startLocationUpdates(this.googleMap);
			} else {
				// Permission request was denied.
				Snackbar.make(coordinatorLayout, "Location permission is required", Snackbar.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void onDataChanged(DataEventBuffer dataEventBuffer) {
		new DataLayerListener(getApplicationContext()).onDataChanged(dataEventBuffer);
	}

	public JSONObject getLastPosition() throws JSONException {

		JSONObject lastPosition = null;

		lastPosition = new JSONObject(loadTitlePref(thisActivity.getApplicationContext(),1,"lastPosition"));
		Log.d(TAG, lastPosition.toString());

		return  lastPosition;
	}


	/**
	 * 1. after a lccation published to the server we get informed about success or error
	 * 2. after an address and location was found we get informed about success or error
	 * Success: Update Widget
	 * Error: Open Settings
	 */
	private BroadcastReceiver bReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			if(intent.getAction().equals(LOCATION_FOUND)) {
				String lastAddress = loadTitlePref(context,1,"lastAddress");
				mLastLocation = new Location("");
				try {
					JSONObject lastPosition = getLastPosition();
					mLastLocation.setLatitude(lastPosition.getDouble("lat"));//your coords of course
					mLastLocation.setLongitude(lastPosition.getDouble("lng"));
					googleMap.animateCamera( CameraUpdateFactory.zoomTo(new Float(lastPosition.getInt("zoom"))));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				//R.string.location_found+" ";
				Toast.makeText(MobileMainActivity.this,lastAddress,Toast.LENGTH_LONG).show();
				//
				Utils.publishPosition(thisActivity);
			}

			if(intent.getAction().equals(LOCATION_NOT_FOUND)) {
				Toast.makeText(MobileMainActivity.this,R.string.location_not_found,Toast.LENGTH_LONG).show();
			}

			if(intent.getAction().equals(LOCATION_PUBLISH_SUCCESS)) {
				// It is the responsibility of the configuration activity to update the app widget
				AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

				ComponentName name = new ComponentName(getApplicationContext(), PositionUpdate.class);
				int [] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(name);
				if(ids.length>0)
					PositionUpdate.updateAppWidget(context, appWidgetManager, ids[0]);

				Toast.makeText(MobileMainActivity.this,R.string.location_published_successfully,Toast.LENGTH_LONG).show();
			}
			if(intent.getAction().equals(LOCATION_PUBLISH_ERROR)) {
				startSettingsActivity();
			}
		}
	};
}


