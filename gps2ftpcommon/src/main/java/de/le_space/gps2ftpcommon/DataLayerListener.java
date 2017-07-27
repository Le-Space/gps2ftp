package de.le_space.gps2ftpcommon;

import android.content.Context;
import android.net.Uri;

import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;

import java.util.Iterator;
import java.util.List;

import static de.le_space.gps2ftpcommon.Constants.saveTitlePref;

/**
 * Created by Nico Krause (nico@le-space.de) on 30.06.17. (Le Space UG)
 */
public class DataLayerListener {

	Context context;
	public DataLayerListener(){
		this.context = null;
	}

	public DataLayerListener(Context context){
			this.context = context;
	}


	public void onDataChanged(DataEventBuffer dataEvents) {
		int mAppWidgetId = 1;
		final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
		for(DataEvent event : events) {
			final Uri uri = event.getDataItem().getUri();
			final String path = uri!=null ? uri.getPath() : null;

			if("/gps2ftp".equals(path)) { //send last position to the other side

				final DataMap map = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
				Iterator<String> i = map.keySet().iterator();
				while(i.hasNext()){
					String configItem = i.next();
					saveTitlePref(context, mAppWidgetId, configItem,  map.getString(configItem));
				}

				/*
				TODO display differently when data where coming from peer device! Thats too much good..
				Toast.makeText(this.context,R.string.got_new_data,
						Toast.LENGTH_LONG).show(); */
			}
		}
	}

}
