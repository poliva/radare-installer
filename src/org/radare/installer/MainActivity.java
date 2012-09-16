/*
radare2 installer for Android
(c) 2012 Pau Oliva Fora <pof[at]eslack[dot]org>
*/
package org.radare.installer;

import org.radare.installer.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.DataOutputStream;

import java.util.zip.GZIPInputStream;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.radare.installer.R;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;
import android.os.Build;

import android.content.Intent;
import android.net.Uri;

import android.view.Menu;
import android.view.MenuItem;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.SystemClock;

import com.ice.tar.*;
import com.stericson.RootTools.*;

public class MainActivity extends Activity {
	
	private TextView outputView;
	private Handler handler = new Handler();
	private Button remoteRunButton;
	private Button localRunButton;
	
	private Context context;
	private Utils mUtils;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		outputView = (TextView)findViewById(R.id.outputView);
		remoteRunButton = (Button)findViewById(R.id.remoteRunButton);
		remoteRunButton.setOnClickListener(onRemoteRunButtonClick);

		localRunButton = (Button)findViewById(R.id.localRunButton);
		localRunButton.setOnClickListener(onLocalRunButtonClick);

		mUtils = new Utils(getApplicationContext());

		if (mUtils.isInternetAvailable()) {
			Thread thread = new Thread(new Runnable() {
				public void run() {
					String version = mUtils.GetPref("version");
					String ETag = mUtils.GetPref("ETag");
					if (!version.equals("unknown") && !ETag.equals("unknown")) {
						output ("radare2 " + version + " is installed.\n");
						String arch = mUtils.GetArch();
						String url = "http://radare.org/get/pkg/android/" + arch + "/" + version;
						boolean update = mUtils.UpdateCheck(url);
						if (update) {
							output ("New radare2 " + version + " version available!\n");
							//mUtils.SendNotification("Radare2 update", "New radare2 " + version + " version available!\n");
						}
					}
				}
			});
			thread.start();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, 0, 0, "Settings");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case 0:
				startActivity(new Intent(this, SettingsActivity.class));
				return true;
		}
		return false;
	}



	private OnClickListener onLocalRunButtonClick = new OnClickListener() {
		public void onClick(View v) {
			Thread thread = new Thread(new Runnable() {
				public void run() {

					localRunButton.setClickable(false);
					
					Intent intent = new Intent(MainActivity.this, LaunchActivity.class);
					startActivity(intent);      
					//finish();

					localRunButton.setClickable(true);
				}
			});
			thread.start();
		}
	};

	private OnClickListener onRemoteRunButtonClick = new OnClickListener() {
		public void onClick(View v) {

			//RootTools.debugMode = true;

			// disable button click if it has been clicked once
			remoteRunButton.setClickable(false);
			localRunButton.setClickable(false);
			//outputView.setText("");
			output ("");

			final String localPath = "/data/data/org.radare.installer/radare2-android.tar.gz";
			final CheckBox checkBox = (CheckBox) findViewById(R.id.checkbox);
			final CheckBox checkHg = (CheckBox) findViewById(R.id.checkhg);

			Thread thread = new Thread(new Runnable() {
				public void run() {

					String url;
					String hg;
					String output;
					String arch = mUtils.GetArch();
					String cpuabi = Build.CPU_ABI;

					output ("Detected CPU: " + cpuabi + " (" + arch +")\n");

					if (checkHg.isChecked()) {
						output("Download: unstable/development version from nightly build.\nNote: this version can be broken!\n");
						hg = "unstable";
					} else {
						output("Download: stable version\n");
						hg = "stable";
					}

					// store installed version in preferences
					mUtils.StorePref("version",hg);

					url = "http://radare.org/get/pkg/android/" + arch + "/" + hg;

					/* fix broken stable URL in radare2 0.9 */
					if (cpuabi.matches(".*arm.*")) {
						if (!checkHg.isChecked()) url = "http://x90.es/radare2tar";
						else url = "http://pof.eslack.org/tmp/radare2-0.9.1git-android-arm.tar.gz"; //for my tests
					}

					long space = 0;
					if (checkBox.isChecked()) {
						// getSpace needs root, only try it the symlinks checkbox has been checked
						space = (RootTools.getSpace("/data") / 1000);
						output("Free space in /data partition: "+ space +" MB\n");
					}

					if (space <= 0) {
						output("Warning: could not check space in /data partition, installation can fail!\n");
					} else if (space < 15) {
						output("Warning: low space in /data partition, installation can fail!\n");
					}

					output("Downloading radare2-android... please wait\n");
					//output("URL: "+url+"\n");

					if (mUtils.isInternetAvailable() == false) {
						output("\nCan't connect to download server. Check that internet connection is available.\n");
					} else {

						RootTools.useRoot = false;
						// remove old traces of previous r2 install
						exec("rm -r /data/data/org.radare.installer/radare2/");
						exec("rm -r /data/rata/org.radare.installer/files/");
						exec("rm /data/data/org.radare.installer/radare2-android.tar");
						exec("rm /data/data/org.radare.installer/radare2-android.tar.gz");

						// real download
						download(url, localPath);
						output("Installing radare2... please wait\n");

						try {
							unTarGz(localPath, "/data/data/org.radare.installer/");
						} catch (Exception e) {
							e.printStackTrace();
						}

						// make sure we delete temporary files
						exec("rm /data/data/org.radare.installer/radare2-android.tar");
						exec("rm /data/data/org.radare.installer/radare2-android.tar.gz");

						// make sure bin files are executable
						exec("chmod 755 /data/data/org.radare.installer/radare2/bin/*");
						exec("chmod 755 /data/data/org.radare.installer/radare2/bin/");
						exec("chmod 755 /data/data/org.radare.installer/radare2/");
						exec("mkdir /data/data/org.radare.installer/radare2/tmp/");
						exec("chmod 777 /data/data/org.radare.installer/radare2/tmp/");

						boolean symlinksCreated = false;
						if (checkBox.isChecked()) {

							boolean isRooted = false;
							isRooted = RootTools.isAccessGiven();

							if(!isRooted) {
								output("\nCould not create xbin symlinks, do you have root?\n");
							} else { // device is rooted

								RootTools.useRoot = true;

								output("\nCreating xbin symlinks...\n");
								RootTools.remount("/system", "rw");
								// remove old path
								exec("rm -r /data/local/radare2");
								// remove old symlinks in case they exist in old location
								exec("rm -r /system/xbin/radare2 /system/xbin/r2 /system/xbin/rabin2 /system/xbin/radiff2 /system/xbin/ragg2 /system/xbin/rahash2 /system/xbin/ranal2 /system/xbin/rarun2 /system/xbin/rasm2 /system/xbin/rax2 /system/xbin/rafind2 /system/xbin/ragg2-cc");

								if (RootTools.exists("/data/data/org.radare.installer/radare2/bin/radare2")) {

									// show output for the first link, in case there's any error with su
									output = exec("ln -s /data/data/org.radare.installer/radare2/bin/radare2 /system/xbin/radare2 2>&1");
									if (!output.equals("")) output(output);

									String file;
									File folder = new File("/data/data/org.radare.installer/radare2/bin/");
									File[] listOfFiles = folder.listFiles(); 
									for (int i = 0; i < listOfFiles.length; i++) {
										if (listOfFiles[i].isFile()) {
											file = listOfFiles[i].getName();
											exec("ln -s /data/data/org.radare.installer/radare2/bin/" + file + " /system/xbin/" + file);
											output("linking /system/xbin/" + file + "\n");
										}
									}
								}

								RootTools.remount("/system", "ro");
								if (RootTools.exists("/system/xbin/radare2")) {
									output("done\n");
									symlinksCreated = true;
								} else {
									output("\nFailed to create xbin symlinks\n");
									symlinksCreated = false;
								}

								RootTools.useRoot = false;
							}
						}

						RootTools.useRoot = false;
						if (!RootTools.exists("/data/data/org.radare.installer/radare2/bin/radare2")) {
							localRunButton.setClickable(false);
							output("\n\nsomething went wrong during installation :(\n");
						} else {
							localRunButton.setClickable(true);
							if (symlinksCreated == false) output("\nRadare2 is installed in:\n   /data/data/org.radare.installer/radare2/\n");
							output("\nTesting installation:\n\n$ radare2 -v\n");
							output = exec("/data/data/org.radare.installer/radare2/bin/radare2 -v");
							if (!output.equals("")) output(output);
							else output("Radare was not installed successfully, make sure you have enough space in /data and try again.");
						}
					}
					// enable button again
					remoteRunButton.setClickable(true);
					localRunButton.setClickable(true);
				}
			});
			thread.start();
		}
	};


	private String exec(String command) {
		final StringBuffer radare_output = new StringBuffer();
		Command command_out = new Command(0, command)
		{
        		@Override
        		public void output(int id, String line)
        		{
				radare_output.append(line);
        		}
		};
		try {
			RootTools.getShell(RootTools.useRoot).add(command_out).waitForFinish();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return radare_output.toString();
	}

	private void output(final String str) {
		Runnable proc = new Runnable() {
			public void run() {
				if (str!=null) outputView.append(str);
				if (str.equals("")) outputView.setText("");
			}
		};
		handler.post(proc);
	}


	public static void unTarGz(final String zipPath, final String unZipPath) throws Exception {
		GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(zipPath));

		//path and name of the tempoary tar file
		String tempDir = unZipPath.substring(0, unZipPath.lastIndexOf('/'));
		String tempFile = "radare-android.tar";
		String tempPath = tempDir + "/" + tempFile;

		//first we create the gunzipped tarball...
		OutputStream out = new FileOutputStream(tempPath);

		byte[] data = new byte[1024];
		int len;
		while ((len = gzipInputStream.read(data)) > 0) {
			out.write(data, 0, len);
		}

		gzipInputStream.close();
		out.close();

		//...then we use com.ice.tar to extract the tarball contents
		TarArchive tarArchive = new TarArchive(new FileInputStream(tempPath));
		tarArchive.extractContents(new File("/"));
		tarArchive.closeArchive();

		//remove the temporary gunzipped tar
		new File(tempPath).delete();
	}

	private void download(String urlStr, String localPath) {
		try {
			URL url = new URL(urlStr);
			HttpURLConnection urlconn = (HttpURLConnection)url.openConnection();
			urlconn.setRequestMethod("GET");
			urlconn.setInstanceFollowRedirects(true);
			urlconn.getRequestProperties();
			urlconn.connect();
			String mETag = urlconn.getHeaderField("ETag");
			mUtils.StorePref("ETag",mETag);
			InputStream in = urlconn.getInputStream();
			FileOutputStream out = new FileOutputStream(localPath);
			int read;
			byte[] buffer = new byte[4096];
			while ((read = in.read(buffer)) > 0) {
				out.write(buffer, 0, read);
			}
			out.close();
			in.close();
			urlconn.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onResume() {
		// if updates are enabled, make sure the alarm is set...
		super.onResume();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String hoursStr = prefs.getString("updates_interval", "12");
		int hours = Integer.parseInt(hoursStr);
		boolean perform_updates = prefs.getBoolean("perform_updates", true);
		if (perform_updates) {
			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
			Intent i = new Intent(this, UpdateCheckerService.class);
			PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
			am.cancel(pi);
			am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + hours*60*60*1000, hours*60*60*1000, pi);
		}
	}
}
