package de.le_space.gps2ftpcommon;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by Nico Krause (nico@le-space.de) on 30.06.17. (Le Space UG)
 */

public class Utils {

	public static void sendConfigItems(GoogleApiClient mGoogleApiClient, final PutDataMapRequest putRequest) {
		if(mGoogleApiClient==null)
			return;
		Wearable.DataApi.putDataItem(mGoogleApiClient,  putRequest.asPutDataRequest());
	}

}
