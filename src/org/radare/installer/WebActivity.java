/*
radare2 installer for Android
(c) 2012 Pau Oliva Fora <pof[at]eslack[dot]org>
*/
package org.radare.installer;

import org.radare.installer.Utils;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;

import android.view.LayoutInflater;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.TextView;

import android.content.Intent;
import android.net.Uri;

import java.io.File;

import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.stericson.RootTools.*;

public class WebActivity extends Activity {

	private Utils mUtils;

        WebView webview;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webactivity);

		RootTools.useRoot = false;
		CommandCapture command = new CommandCapture(0, "export TMPDIR=/data/data/org.radare.installer/radare2/tmp ; /data/data/org.radare.installer/radare2/bin/radare2 -c=h /system/bin/toolbox");
		try {
			RootTools.getShell(RootTools.useRoot).add(command).waitForFinish();
		} catch (Exception e) {
			e.printStackTrace();
		}

		webview = (WebView) findViewById(R.id.webview);
		webview.setWebViewClient(new RadareWebViewClient());
		webview.getSettings().setJavaScriptEnabled(true);
		webview.loadUrl("http://localhost:9090");
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
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
