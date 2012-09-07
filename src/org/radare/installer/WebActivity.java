/*
radare2 installer for Android
(c) 2012 Pau Oliva Fora <pof[at]eslack[dot]org>
*/
package org.radare.installer;

import org.radare.installer.Utils;

import android.app.Activity;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;

import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import android.content.DialogInterface;
import android.app.AlertDialog;
import android.widget.EditText;

import com.stericson.RootTools.*;

public class WebActivity extends Activity {

	private Utils mUtils;

        WebView webview;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webactivity);

		RequestFileName();
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


	private void RequestFileName() {
		LayoutInflater factory = LayoutInflater.from(this);

		final View textEntryView = factory.inflate(R.layout.dialog, null);
		AlertDialog.Builder alert = new AlertDialog.Builder(this);                 

		alert.setTitle("File to open?");  
		alert.setMessage("Enter Filename:");                
		alert.setView(textEntryView);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {  
				EditText mUserText;
				mUserText = (EditText) textEntryView.findViewById(R.id.dialog_ret);
				String strFileName = mUserText.getText().toString();

				RootTools.useRoot = false;

				if (RootTools.isProcessRunning("radare2")) {
					RootTools.killProcess("radare2");
				}

				CommandCapture command = new CommandCapture(0, "/data/data/org.radare.installer/radare2/bin/radare2 -c=h " + strFileName + " &");
				try {
					RootTools.getShell(RootTools.useRoot).add(command).waitForFinish();
				} catch (Exception e) {
					e.printStackTrace();
				}

				webview = (WebView) findViewById(R.id.webview);
				webview.setWebViewClient(new RadareWebViewClient());
				webview.getSettings().setJavaScriptEnabled(true);
				webview.loadUrl("http://localhost:9090");

				return;
			}  
		});  

		alert.show();
	}

}
