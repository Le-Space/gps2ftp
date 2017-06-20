package de.le_space.gps2ftp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import static de.le_space.gps2ftp.PositionUpdateConfigureActivity.loadTitlePref;
import static de.le_space.gps2ftp.PositionUpdateConfigureActivity.saveTitlePref;
import static de.le_space.gps2ftp.R.id.map;

public class MainActivity extends AppCompatActivity  implements OnMapReadyCallback {

	private static final String TAG = "Main";
	private FusedLocationProviderClient mFusedLocationClient;
	private static String[] PERMISSIONS_LOCATION = {android.Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};
	private CoordinatorLayout coordinatorLayout;
	private Activity thisActivity;
	private GoogleMap googleMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		final Context thisContext = this.getApplicationContext();
		thisActivity = this;
		coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		mFusedLocationClient = LocationServices.getFusedLocationProviderClient(thisContext);

		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {

				if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
						android.Manifest.permission.ACCESS_FINE_LOCATION)
						|| ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
						android.Manifest.permission.ACCESS_COARSE_LOCATION)) {

					// Provide an additional rationale to the user if the permission was not granted
					// and the user would benefit from additional context for the use of the permission.
					// For example, if the request has been denied previously.
					Log.i(TAG, "Displaying contacts permission rationale to provide additional context.");

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

		Intent intent = getIntent();
		if(intent.getStringExtra("direct")!=null){
			getLastLocation(intent.getIntExtra("appWidgetId",1));
		}
	}

	public void getLastLocation(final int appWidgetId) {

		int accessFineLocation = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
		int accessCoarseLocation = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);

		if (accessFineLocation != PackageManager.PERMISSION_GRANTED && accessCoarseLocation != PackageManager.PERMISSION_GRANTED) {
			Snackbar.make(coordinatorLayout, "Please give the required permission to the app!", Snackbar.LENGTH_LONG).setAction("Action", null).show();
			return;
		}else{

			mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
				@Override
				public void onSuccess(Location location) {

					Snackbar.make(coordinatorLayout, "Got position - updating FTP", Snackbar.LENGTH_LONG).setAction("Action", null).show();

					double lat = location.getLatitude();
					double lng = location.getLongitude();


					String jsonString = "{\n" +
							"    \"lat\": \""+Double.toString(lat)+"\",\n" +
							"    \"lng\": \""+Double.toString(lng)+"\"\n" +
							"}";

					//save our position to the device
					saveTitlePref(getApplicationContext(),1, "lastPosition", jsonString);

					LatLng latlng = new LatLng(lat, lat);

					if(googleMap!=null)
						googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,9));
					//and publish it to the ftp server
					FTPUpdateTask ftpUpdate = new de.le_space.gps2ftp.FTPUpdateTask();
					ftpUpdate.view = findViewById(R.id.coordinatorLayout);

					ftpUpdate.SFTPHOST = loadTitlePref(thisActivity,1,"host");
					ftpUpdate.SFTPUSER = loadTitlePref(thisActivity,1,"username");
					ftpUpdate.SFTPPASS = loadTitlePref(thisActivity,1,"password");
					ftpUpdate.SFTPWORKINGDIR = loadTitlePref(thisActivity,1,"remoteDirectory");
					ftpUpdate.execute(jsonString);
				}
			});
		}

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
			Intent intent = new Intent(this, PositionUpdateConfigureActivity.class);
			intent.putExtra("appWidgetId",1);
			startActivity(intent);
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == 0) {
			// Request for camera permission.
			if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				// Permission has been granted. Start camera preview Activity.
				Snackbar.make(coordinatorLayout, "Location permission was granted. Getting Location",
						Snackbar.LENGTH_SHORT)
						.show();


				getLastLocation(1); //i hope its 0 - but its not sure... can somebody check this?

			} else {
				// Permission request was denied.
				Snackbar.make(coordinatorLayout, "Location permission request was denied.",Snackbar.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {

		try {
			JSONObject lastPosJson = new JSONObject(loadTitlePref(thisActivity,1,"lastPosition"));
			Log.i(TAG, "lastPosition"+lastPosJson.toString());
			LatLng latlng = new LatLng(lastPosJson.getDouble("lat"), lastPosJson.getDouble("lng"));
			MarkerOptions marker = new MarkerOptions().position(latlng);
			marker.title("I am here");
			googleMap.addMarker(marker);
		

		} catch (JSONException e) {
			Snackbar.make(coordinatorLayout, "Last position unknown or invalid",Snackbar.LENGTH_SHORT).show();
			e.printStackTrace();

		}


	}
}


