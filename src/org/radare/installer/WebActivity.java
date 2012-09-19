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

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Enumeration;
import java.net.NetworkInterface;
import java.net.InetAddress;
import java.net.SocketException;
import org.apache.http.conn.util.InetAddressUtils;

import com.stericson.RootTools.*;

public class WebActivity extends Activity {

	private Utils mUtils;

        WebView webview;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mUtils = new Utils(getApplicationContext());

		setContentView(R.layout.webactivity);

		RootTools.useRoot = false;

		// get shell first
		try {
			RootTools.getShell(RootTools.useRoot);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// make sure we don't start a second instance of radare webserver
		// we can't use killradare() here because it finishes the activity
		if (RootTools.isProcessRunning("radare2")) {
			RootTools.killProcess("radare2");
		}

		Bundle b = getIntent().getExtras();
		String file_to_open = b.getString("filename");

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		boolean http_public = prefs.getBoolean("http_public", false);

		String http_eval = "";
		if (http_public) {
			http_eval = "-e http.public=true";  // radare2 is currently not starting webserver with this parameter
			String localip = getLocalIpAddress();
			if (localip != null) {
				mUtils.myToast("r2 http server\n" + localip + ":9090", Toast.LENGTH_LONG);
			}
		}

		mUtils.exec("/data/data/org.radare.installer/radare2/bin/radare2 -c=h " + http_eval + " " + file_to_open );

/*
		String output = mUtils.exec("/data/data/org.radare.installer/radare2/bin/radare2 " + http_eval + " -c=h " + file_to_open + " &");

		// if radare2 is launched in background we need to wait
		// for it to start before opening the webview
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

		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			mUtils.myToast("Error: radare2 webserver did not start", Toast.LENGTH_LONG);
			finish();
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

	public String getLocalIpAddress() {
		try {
			String ipv4;
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(ipv4=inetAddress.getHostAddress())) {
						return ipv4;
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return null;
	}
}
