/*
radare2 installer for Android
(c) 2012 Pau Oliva Fora <pof[at]eslack[dot]org>
*/
package org.radare.installer;

import org.radare.installer.Utils;

import android.app.Activity;
import android.os.Bundle;

import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import android.widget.Toast;

import com.stericson.RootTools.*;

public class WebActivity extends Activity {

	private Utils mUtils;

        WebView webview;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mUtils = new Utils(getApplicationContext());

		setContentView(R.layout.webactivity);

		// make sure we don't start a second instance of radare webserver
		// we can't use killradare() here because it finishes the activity
		RootTools.useRoot = false;

		// get shell first
		try {
			RootTools.getShell(RootTools.useRoot);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (RootTools.isProcessRunning("radare2")) {
			RootTools.killProcess("radare2");
		}

		Bundle b = getIntent().getExtras();
		String file_to_open = b.getString("filename");

		//int exitcode = -1;
		CommandCapture command = new CommandCapture(0, "/data/data/org.radare.installer/radare2/bin/radare2 -c=h " + file_to_open );
		try {
			RootTools.getShell(RootTools.useRoot).add(command).waitForFinish();
			//exitcode = RootTools.getShell(RootTools.useRoot).add(command).exitCode();
		} catch (Exception e) {
			e.printStackTrace();
		}

/*
		if (exitcode != 0) {
			mUtils.myToast("Could not open file " + file_to_open, Toast.LENGTH_SHORT);
			finish();
		}
*/
/*
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
                        e.printStackTrace();
                }
*/

		if (RootTools.isProcessRunning("radare2")) {
			webview = (WebView) findViewById(R.id.webview);
			webview.setWebViewClient(new RadareWebViewClient());
			webview.getSettings().setJavaScriptEnabled(true);
			webview.loadUrl("http://localhost:9090");
		} else {
			mUtils.myToast("Could not open file " + file_to_open, Toast.LENGTH_SHORT);
			finish();
		}

	}

	private void killradare() {
		RootTools.useRoot = false;
		if (RootTools.isProcessRunning("radare2")) {
			RootTools.killProcess("radare2");
		}
		finish();
	}

	@Override
	public void onStop()
	{
		super.onStop();
		killradare();
	}

	private class RadareWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url)
		{
			view.loadUrl(url);
			return true;
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
			webview.goBack();
			killradare();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
