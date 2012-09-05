/*
radare2 installer for Android
(c) 2012 Pau Oliva Fora <pof[at]eslack[dot]org>
*/
package org.radare.installer;

import org.radare.installer.Utils;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;


public class LauncherActivity extends Activity {

	private Utils mUtils;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		// we don't need a layout for this activity as we finish() it right after the intent has started
		//setContentView(R.layout.launcher);

		mUtils = new Utils(getApplicationContext());

		if (mUtils.isAppInstalled("jackpal.androidterm")) {
			try {
				Intent i = new Intent("jackpal.androidterm.RUN_SCRIPT");
				i.addCategory(Intent.CATEGORY_DEFAULT);
				i.putExtra("jackpal.androidterm.iInitialCommand", "export PATH=$PATH:/data/data/org.radare.installer/radare2/bin/ ; radare2 /system/bin/ls");
				startActivity(i);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		finish();
	}
}
