package de.le_space.gps2ftp;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;

import de.le_space.gps2ftpcommon.Utils;

import static de.le_space.gps2ftpcommon.Constants.loadTitlePref;
import static de.le_space.gps2ftpcommon.Constants.mGoogleApiClient;
import static de.le_space.gps2ftpcommon.Constants.saveTitlePref;

/**
 * The configuration screen for the {@link PositionUpdate PositionUpdate} AppWidget.
 */
public class PositionUpdateConfigureActivity extends Activity {


	RadioGroup mAppWidgetProtocol;
	EditText mAppWidgetHost;
	EditText mAppWidgetUsername;
	EditText mAppWidgetPassword;
	EditText mAppWidgetRemoteDirectory;
	EditText mAppWidgetGoogleMapsApiKey;
	int mAppWidgetId = 1;
	Activity thisActivity;

	public PositionUpdateConfigureActivity() {
		super();
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setResult(RESULT_CANCELED);
		thisActivity = this;
		setContentView(R.layout.position_update_configure);
		mAppWidgetProtocol = findViewById(R.id.appwidget_protocol);
		mAppWidgetHost = findViewById(R.id.appwidget_host);
		mAppWidgetUsername = findViewById(R.id.appwidget_username);
		mAppWidgetPassword = findViewById(R.id.appwidget_password);
		mAppWidgetUsername = findViewById(R.id.appwidget_username);
		mAppWidgetRemoteDirectory = findViewById(R.id.appwidget_remoteDirectory);
		mAppWidgetGoogleMapsApiKey = findViewById(R.id.appwidget_googleMapsApiKey);

		findViewById(R.id.connectionTest_button).setOnClickListener(mOnClickListenerConnecdtionTest);
		findViewById(R.id.save_button).setOnClickListener(mOnClickListenerSave);

		String protocol = loadTitlePref(PositionUpdateConfigureActivity.this, mAppWidgetId, "protocol");
		switch (protocol){
			case "HTTP(S)":
				mAppWidgetProtocol.check(mAppWidgetProtocol.getChildAt(0).getId());
				break;
			case "SFTP":
				mAppWidgetProtocol.check(mAppWidgetProtocol.getChildAt(1).getId());
				break;
		}

		mAppWidgetHost.setText(loadTitlePref(PositionUpdateConfigureActivity.this, mAppWidgetId, "host"));
		mAppWidgetUsername.setText(loadTitlePref(PositionUpdateConfigureActivity.this, mAppWidgetId, "username"));
		mAppWidgetPassword.setText(loadTitlePref(PositionUpdateConfigureActivity.this, mAppWidgetId ,"password"));
		mAppWidgetRemoteDirectory.setText(loadTitlePref(PositionUpdateConfigureActivity.this, mAppWidgetId, "remoteDirectory"));
		mAppWidgetGoogleMapsApiKey.setText(loadTitlePref(PositionUpdateConfigureActivity.this, mAppWidgetId, "googleMapsApiKey"));
	}

	View.OnClickListener mOnClickListenerConnecdtionTest = new View.OnClickListener() {

		public void onClick(View v) {

			mAppWidgetProtocol =  findViewById(R.id.appwidget_protocol);

			Utils.TEST_PROTOCOL = ((RadioButton) findViewById(mAppWidgetProtocol.getCheckedRadioButtonId())).getText().toString();
			Utils.TEST_USER = mAppWidgetUsername.getText().toString();
			Utils.TEST_HOST = mAppWidgetHost.getText().toString();
			Utils.TEST_PASS = mAppWidgetPassword.getText().toString();
			Utils.TEST_URL = mAppWidgetRemoteDirectory.getText().toString();

			Utils.publishPosition(thisActivity,true);

		}
	};

	View.OnClickListener mOnClickListenerSave = new View.OnClickListener() {

			public void onClick(View v) {

			final Context context = PositionUpdateConfigureActivity.this;

			// When the button is clicked, store the string locally;
			mAppWidgetProtocol =  findViewById(R.id.appwidget_protocol);

			String widgetProtocol = ((RadioButton) findViewById(mAppWidgetProtocol.getCheckedRadioButtonId())).getText().toString();

			String widgetHost = mAppWidgetHost.getText().toString();
			String widgetUsername = mAppWidgetUsername.getText().toString();
			String widgetPassword = mAppWidgetPassword.getText().toString();
			String widgetRemoteDirectory = mAppWidgetRemoteDirectory.getText().toString();
			String widgetGoogleMapsApiKey= mAppWidgetGoogleMapsApiKey.getText().toString();

			//update config on mobile device
			saveTitlePref(context, mAppWidgetId, "protocol", widgetProtocol);
			saveTitlePref(context, mAppWidgetId, "host", widgetHost);
			saveTitlePref(context, mAppWidgetId, "username", widgetUsername);
			saveTitlePref(context, mAppWidgetId, "password", widgetPassword);
			saveTitlePref(context, mAppWidgetId, "remoteDirectory", widgetRemoteDirectory);
			saveTitlePref(context, mAppWidgetId, "googleMapsApiKey", widgetGoogleMapsApiKey);

			//send new config to wear device (only happens when changed!)
			final PutDataMapRequest putRequest = PutDataMapRequest.create("/gps2ftp");
			final DataMap map = putRequest.getDataMap();
			map.putString("protocol", widgetProtocol);
			map.putString("host", widgetHost);
			map.putString("username", widgetUsername);
			map.putString("password", widgetPassword);
			map.putString("remoteDirectory", widgetRemoteDirectory);
			map.putString("googleMapsApiKey", widgetGoogleMapsApiKey);
			Utils.sendConfigItems(mGoogleApiClient,putRequest);

			Intent resultValue = new Intent();
			resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);

			setResult(RESULT_OK, resultValue);
			finish();
			}
		};
}

