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

}
