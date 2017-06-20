package de.le_space.gps2ftp;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link PositionUpdateConfigureActivity PositionUpdateConfigureActivity}
 */
public class PositionUpdate extends AppWidgetProvider {

	private static final String TAG = "PositionUpdate";
	public static String UPDATE_POSITION_ACTION = "UpdatePositionAction";
	public static String START_CONFIGURATION_ACTION = "StartConfigurationAction";

	static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
	                            int appWidgetId) {

		CharSequence widgetHost = PositionUpdateConfigureActivity.loadTitlePref(context, appWidgetId, "host");
		CharSequence widgetUsername = PositionUpdateConfigureActivity.loadTitlePref(context, appWidgetId, "username");
		CharSequence widgetPassword = PositionUpdateConfigureActivity.loadTitlePref(context, appWidgetId, "password");
		CharSequence widgetRemoteDirectory = PositionUpdateConfigureActivity.loadTitlePref(context, appWidgetId, "remoteDirectory");

		// Construct the RemoteViews object
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.position_update);
		views.setTextViewText(R.id.appwidget_host, widgetHost);
		views.setTextViewText(R.id.appwidget_username, widgetUsername);
		views.setTextViewText(R.id.appwidget_password, widgetPassword);
		views.setTextViewText(R.id.appwidget_remoteDirectory, widgetRemoteDirectory);

		Intent intentUpdate = new Intent(context, MainActivity.class);
		intentUpdate.setAction(UPDATE_POSITION_ACTION);
		intentUpdate.putExtra("direct","true");
		intentUpdate.putExtra("appWidgetId",1);

		PendingIntent piUpdate = PendingIntent.getActivity(context, 0, intentUpdate, 0);
		views.setOnClickPendingIntent(R.id.updatePositon, piUpdate);

		Intent intentConfig = new Intent(context, PositionUpdateConfigureActivity.class);
		intentConfig.setAction(START_CONFIGURATION_ACTION);
		PendingIntent piConfig = PendingIntent.getActivity(context, 0, intentConfig, 0);

		views.setOnClickPendingIntent(R.id.startConfiguration, piConfig);

		// Instruct the widget manager to update the widget
		appWidgetManager.updateAppWidget(appWidgetId, views);
	}

	private static final String ACTION_CLICK = "ACTION_CLICK_WIDGET";
	@Override
	public void onReceive(Context context, Intent intent) {
		String intentAction = intent.getAction();
		Log.d(TAG, "something happened on widget..");
		if (intentAction.equals(ACTION_CLICK)) {

			Bundle extras = intent.getExtras();
			Integer appWidgetId = extras.getInt("appwidgetid");
			Log.d(TAG, "clicked on widget.."+appWidgetId);
		} else {
			super.onReceive(context, intent);
		}
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// There may be multiple widgets active, so update all of them
		Log.d(TAG, "onUpdate");
		for (int appWidgetId : appWidgetIds) {
			updateAppWidget(context, appWidgetManager, appWidgetId);
		}
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		Log.d(TAG, "onDeleted");
		// When the user deletes the widget, delete the preference associated with it.
		for (int appWidgetId : appWidgetIds) {
			PositionUpdateConfigureActivity.deleteTitlePref(context, appWidgetId);
		}
	}

	@Override
	public void onEnabled(Context context) {
		Log.d(TAG, "onEnabled");
		// Enter relevant functionality for when the first widget is created
	}

	@Override
	public void onDisabled(Context context) {
		Log.d(TAG, "onDisabled");
		// Enter relevant functionality for when the last widget is disabled
	}
}

