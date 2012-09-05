/*
radare2 installer for Android
(c) 2012 Pau Oliva Fora <pof[at]eslack[dot]org>
*/
package org.radare.installer;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

public class Utils {

	private Context mContext;

	public Utils(Context context) {
		mContext = context;
	}

	public boolean isAppInstalled(String namespace) {
		try{
			ApplicationInfo info = mContext.getPackageManager().getApplicationInfo(namespace, 0 );
			return true;
		} catch( PackageManager.NameNotFoundException e ){
			return false;
		}
	}

}
