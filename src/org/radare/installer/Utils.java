/*
radare2 installer for Android
(c) 2012 Pau Oliva Fora <pof[at]eslack[dot]org>
*/
package org.radare.installer;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import android.view.LayoutInflater;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.TextView;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import android.os.Build;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

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

	public void myToast(String myMsg, int myDuration) {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		//LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.toast_layout, null);

		ImageView image = (ImageView) layout.findViewById(R.id.image);
		image.setImageResource(R.drawable.icon);
		TextView text = (TextView) layout.findViewById(R.id.text);
		text.setText(myMsg);

		Toast toast = new Toast(mContext);
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.setDuration(myDuration);
		toast.setView(layout);
		toast.show();
	}

	// store a Key-Value string in preferences
	public void StorePref(String Key, String Value) {
		SharedPreferences settings = mContext.getSharedPreferences("radare-installer-preferences", mContext.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(Key,Value);
		editor.commit();
	}

	// get the String value from key in preferences, returns unknown if not set
	public String GetPref(String Key) {
		SharedPreferences settings = mContext.getSharedPreferences("radare-installer-preferences", mContext.MODE_PRIVATE);
		String version = settings.getString(Key, "unknown");
		return version;
	}

	public String GetArch() {
		String arch = "arm";
		String cpuabi = Build.CPU_ABI;

		if (cpuabi.matches(".*mips.*")) arch="mips";
		if (cpuabi.matches(".*x86.*")) arch="x86";
		if (cpuabi.matches(".*arm.*")) arch="arm";
		return arch;
	}

	public final boolean isInternetAvailable(){
	// check if we are connected to the internet
		ConnectivityManager connectivityManager = (ConnectivityManager)mContext.getSystemService(mContext.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		if(info == null)
		    return false;

		return connectivityManager.getActiveNetworkInfo().isConnected();
	}

}
