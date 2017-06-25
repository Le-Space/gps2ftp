package de.le_space.gps2ftp;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * The configuration screen for the {@link PositionUpdate PositionUpdate} AppWidget.
 */
public class PositionUpdateConfigureActivity extends Activity {

	private static final String PREFS_NAME = "de.le_space.gps2ftp.PositionUpdate";
	private static final String PREF_PREFIX_KEY = "appwidget_";
	int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	EditText mAppWidgetHost;
	EditText mAppWidgetUsername;
	EditText mAppWidgetPassword;
	EditText mAppWidgetRemoteDirectory;
	EditText mAppWidgetGoogleMapsApiKey;

	View.OnClickListener mOnClickListenerSave = new View.OnClickListener() {
		public void onClick(View v) {

			final Context context = PositionUpdateConfigureActivity.this;

			// When the button is clicked, store the string locally
			String widgetHost = mAppWidgetHost.getText().toString();
			String widgetUsername = mAppWidgetUsername.getText().toString();
			String widgetPassword = mAppWidgetPassword.getText().toString();
			String widgetRemoteDirectory = mAppWidgetRemoteDirectory.getText().toString();
			String widgetGoogleMapsApiKey= mAppWidgetGoogleMapsApiKey.getText().toString();

			saveTitlePref(context, mAppWidgetId, "host", widgetHost);
			saveTitlePref(context, mAppWidgetId, "username", widgetUsername);
			saveTitlePref(context, mAppWidgetId, "password", widgetPassword);
			saveTitlePref(context, mAppWidgetId, "remoteDirectory", widgetRemoteDirectory);
			saveTitlePref(context, mAppWidgetId, "googleMapsApiKey", widgetGoogleMapsApiKey);


			// It is the responsibility of the configuration activity to update the app widget
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			PositionUpdate.updateAppWidget(context, appWidgetManager, mAppWidgetId);

			// Make sure we pass back the original appWidgetId
			Intent resultValue = new Intent();
			resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
			setResult(RESULT_OK, resultValue);
			finish();
		}
	};

	public PositionUpdateConfigureActivity() {
		super();
	}

	// Write the prefix to the SharedPreferences object for this widget
	static void saveTitlePref(Context context, int appWidgetId, String element, String text) {
		SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
		prefs.putString(PREF_PREFIX_KEY + appWidgetId+"_"+element, text);
		prefs.apply();
	}

	// Read the prefix from the SharedPreferences object for this widget.
	// If there is no preference saved, get the default from a resource
	static String loadTitlePref(Context context, int appWidgetId, String element) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		String titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId+"_"+element, null);
		if (titleValue != null) {
			return titleValue;
		} else {
			return "";
		//	return activity.getString(R.string.appwidget_text);
		}
	}

	static void deleteTitlePref(Context context, int appWidgetId) {
		SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
		prefs.remove(PREF_PREFIX_KEY + appWidgetId);
		prefs.apply();
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		// Set the result to CANCELED.  This will cause the widget host to cancel
		// out of the widget placement if the user presses the back button.
		setResult(RESULT_CANCELED);

		setContentView(R.layout.position_update_configure);
		mAppWidgetHost = (EditText) findViewById(R.id.appwidget_host);
		mAppWidgetUsername = (EditText) findViewById(R.id.appwidget_username);
		mAppWidgetPassword = (EditText) findViewById(R.id.appwidget_password);
		mAppWidgetUsername = (EditText) findViewById(R.id.appwidget_username);
		mAppWidgetRemoteDirectory = (EditText) findViewById(R.id.appwidget_remoteDirectory);
		mAppWidgetGoogleMapsApiKey = (EditText) findViewById(R.id.appwidget_googleMapsApiKey);

		findViewById(R.id.save_button).setOnClickListener(mOnClickListenerSave);

		// Find the widget id from the intent.
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(
					AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
			mAppWidgetId=1; //we overwrite this here since we only have one widget at the moment

			String error = extras.getString("error");
			if(error!=null){
				Toast.makeText(this,error+" - please check hostname, username, password and directory",Toast.LENGTH_LONG).show();
			}
		}

		// If this activity was started with an intent without an app widget ID, finish with an error.
		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish();
			return;
		}

		mAppWidgetHost.setText(loadTitlePref(PositionUpdateConfigureActivity.this, mAppWidgetId, "host"));
		mAppWidgetUsername.setText(loadTitlePref(PositionUpdateConfigureActivity.this, mAppWidgetId, "username"));
		mAppWidgetPassword.setText(loadTitlePref(PositionUpdateConfigureActivity.this, mAppWidgetId ,"password"));
		mAppWidgetRemoteDirectory.setText(loadTitlePref(PositionUpdateConfigureActivity.this, mAppWidgetId, "remoteDirectory"));
		mAppWidgetGoogleMapsApiKey.setText(loadTitlePref(PositionUpdateConfigureActivity.this, mAppWidgetId, "googleMapsApiKey"));

	}
}

