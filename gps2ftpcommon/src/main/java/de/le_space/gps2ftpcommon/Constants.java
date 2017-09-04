package de.le_space.gps2ftpcommon;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by Nico Krause (nico@le-space.de) on 21.06.17. (Le Space UG)
 */

public final class Constants {


	public static GoogleApiClient mGoogleApiClient;

	public static final int SUCCESS_RESULT = 0;
	public static final int FAILURE_RESULT = 1;

	private static final String PREFS_NAME = "de.le_space.gps2ftp.PositionUpdate";
	private static final String PREF_PREFIX_KEY = "appwidget_";

	int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	public static final String PACKAGE_NAME = "de.le_space.gps2ftp";
	public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
	public static final String RESULT_DATA_FULLADDRESS = PACKAGE_NAME + ".RESULT_DATA_FULLADDRESS";
	public static final String RESULT_DATA_CITYNAME = PACKAGE_NAME + ".RESULT_DATA_CITYNAME";;
	public static final String RESULT_DATA_COUNTRY_CODE = PACKAGE_NAME + ".RESULT_DATA_COUNTRY_CODE";;
	public static final String RESULT_DATA_ZIP_CODE = PACKAGE_NAME + ".RESULT_DATA_ZIP_CODE";;
	public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +".LOCATION_DATA_EXTRA";

	public static final String LOCATION_PUBLISH_SUCCESS = " de.le_space.gps2ftp.LOCATION_PUBLISH_SUCCESS";
	public static final String LOCATION_PUBLISH_ERROR = " de.le_space.gps2ftp.LOCATION_PUBLISH_ERROR";
	public static final String LOCATION_FOUND = " de.le_space.gps2ftp.LOCATION_FOUND";
	public static final String LOCATION_NOT_FOUND = " de.le_space.gps2ftp.LOCATION_NOT_FOUND";

	// Write the prefix to the SharedPreferences object for this widget
	public static void saveTitlePref(Context context, int appWidgetId, String element, String text) {
		SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
		prefs.putString(PREF_PREFIX_KEY + appWidgetId+"_"+element, text);
		prefs.apply();
	}

	// Read the prefix from the SharedPreferences object for this widget.
	// If there is no preference saved, get the default from a resource
	public static String loadTitlePref(Context context, int appWidgetId, String element) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		String titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId+"_"+element, null);
		if (titleValue != null) {
			return titleValue;
		} else {
			return "";
			//	return activity.getString(R.string.appwidget_text);
		}
	}

	public static void deleteTitlePref(Context context, int appWidgetId) {
		SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
		prefs.remove(PREF_PREFIX_KEY + appWidgetId);
		prefs.apply();
	}
}
