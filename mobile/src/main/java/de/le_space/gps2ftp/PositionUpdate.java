package de.le_space.gps2ftp;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import static de.le_space.gps2ftpcommon.Constants.deleteTitlePref;
import static de.le_space.gps2ftpcommon.Constants.loadTitlePref;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link PositionUpdateConfigureActivity PositionUpdateConfigureActivity}
 */
public class PositionUpdate extends AppWidgetProvider {

	private static final String TAG = "PositionUpdate";
	public static String UPDATE_POSITION_ACTION = "UpdatePositionAction";
	public static String START_CONFIGURATION_ACTION = "StartConfigurationAction";

	static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,int appWidgetId) {

		CharSequence widgetLastAddress = loadTitlePref(context, 1, "lastAddress");
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.position_update);
		views.setTextViewText(R.id.updatePositon, widgetLastAddress);

		Intent intentUpdate = new Intent(context, MainActivity.class);
		intentUpdate.setAction(UPDATE_POSITION_ACTION);
		intentUpdate.putExtra("direct","true");
		intentUpdate.putExtra("appWidgetId",appWidgetId);

		PendingIntent piUpdate = PendingIntent.getActivity(context, 0, intentUpdate, 0);
		views.setOnClickPendingIntent(R.id.updatePositon, piUpdate);
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
		}
		else if(intentAction.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)){ //Update already saved new address from FetchAddressIntentService

			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			ComponentName thisAppWidget = new ComponentName(context.getPackageName(), PositionUpdate.class.getName());
			int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

			onUpdate(context, appWidgetManager, appWidgetIds);
		}else {
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
			deleteTitlePref(context, appWidgetId);
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

