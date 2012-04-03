/*
radare2 installer for Android
(c) 2012 Pau Oliva Fora <pof[at]eslack[dot]org>
*/
package org.radare.installer;

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

import com.ice.tar.*;

public class MainActivity extends Activity {
	
	private TextView outputView;
	private Handler handler = new Handler();
	private Button remoteRunButton;
	
	private Context context;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		outputView = (TextView)findViewById(R.id.outputView);
		remoteRunButton = (Button)findViewById(R.id.remoteRunButton);
		remoteRunButton.setOnClickListener(onRemoteRunButtonClick);
	}

	private OnClickListener onRemoteRunButtonClick = new OnClickListener() {
		public void onClick(View v) {

			// disable button click if it has been clicked once
			remoteRunButton.setClickable(false);
			outputView.setText("");

			final String localPath = "/data/data/org.radare.installer/radare2-android.tar.gz";
			final CheckBox checkBox = (CheckBox) findViewById(R.id.checkbox);
			final CheckBox checkHg = (CheckBox) findViewById(R.id.checkhg);

			Thread thread = new Thread(new Runnable() {
				public void run() {

					String url;
					String hg;
					String output;
					String arch = "arm";
					String cpuabi = Build.CPU_ABI;

					if (cpuabi.matches(".*mips.*")) arch="mips";
					if (cpuabi.matches(".*x86.*")) arch="x86";
					if (cpuabi.matches(".*arm.*")) arch="arm";
					
					output ("Detected CPU: " + cpuabi + "\n");

					if (checkHg.isChecked()) {
						output("Download: unstable/development version from nightly build.\nNote: this version can be broken!\n");
						hg = "hg";
					} else {
						output("Download: stable version\n");
						hg = "stable";
					}

					url = "http://radare.org/get/pkg/android/" + arch + "/" + hg;

					/* fix broken stable URL in radare2 0.9 */
					if (cpuabi.matches(".*arm.*")) {
						if (!checkHg.isChecked()) url = "http://x90.es/radare2tar";
					}

					output("Downloading radare2-android... please wait\n");
					//output("URL: "+url+"\n");

					if (isInternetAvailable() == false) {
						output("\nCan't connect to download server. Check that internet connection is available.\n");
					} else {

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

						boolean isRooted = false;
        					isRooted = detectSuBinaryInPath();

						boolean simlinksCreated = false;
						if (checkBox.isChecked()) {
							if(!isRooted) {
								output("\nCould not create xbin symlinks, do you have root?\n");
							} else { // device is rooted

								output("\nCreating xbin symlinks... ");
								exec("su -c 'mount -o remount -o rw /system'");
								// remove old path
								exec("su -c 'rm -r /data/local/radare2'");
								// remove old symlinks in case they exist in old location
								exec("su -c 'rm -r /system/xbin/radare2 /system/xbin/r2 /system/xbin/rabin2 /system/xbin/radiff2 /system/xbin/ragg2 /system/xbin/rahash2 /system/xbin/ranal2 /system/xbin/rarun2 /system/xbin/rasm2 /system/xbin/rax2 /system/xbin/rafind2 /system/xbin/ragg2-cc'");

								// show output for the first link, in case there's any error with su
								output = exec("su -c 'ln -s /data/data/org.radare.installer/radare2/bin/radare2 /system/xbin/radare2 2>&1'");
								output(output);

								String file;
								File folder = new File("/data/data/org.radare.installer/radare2/bin/");
								File[] listOfFiles = folder.listFiles(); 
								for (int i = 0; i < listOfFiles.length; i++) {
									if (listOfFiles[i].isFile()) {
										file = listOfFiles[i].getName();
										exec("su -c 'ln -s /data/data/org.radare.installer/radare2/bin/" + file + " /system/xbin/" + file + "'");
									}
								}

								exec("su -c 'mount -o remount -o ro /system'");
								File radarelink = new File("/system/xbin/radare2");
								if (radarelink.exists()) {
									output("done\n");
									simlinksCreated = true;
								} else {
									output("\nFailed to create xbin symlinks\n");
									simlinksCreated = false;
								}
							}
						}

						File radarebin = new File("/data/data/org.radare.installer/radare2/bin/radare2");
						if (!radarebin.exists()) {
							output("\n\nsomething went wrong during installation :(\n");
						} else {
							if (simlinksCreated == false) output("\nRadare2 is installed in:\n   /data/data/org.radare.installer/radare2/\n");
							output("\nTesting installation:\n\n$ radare2 -v\n");
							output = exec("/data/data/org.radare.installer/radare2/bin/radare2 -v");
							output(output);
						}
					}
					// enable button again
					remoteRunButton.setClickable(true);
				}
			});
			thread.start();
		}
	};


	private Boolean detectSuBinaryInPath() {
	// search for su binaries in PATH

		String[] pathToTest = System.getenv("PATH").split(":");
		for (String path : pathToTest) {
			File suBinary = new File(path + "/su");
			if (suBinary.exists()) return true;
		}
		return false;
	}


	private String exec(String command) {
	// execute a shell command, returning output in a string
		try {
			Runtime rt = Runtime.getRuntime();
			Process process = rt.exec("sh");
			DataOutputStream os = new DataOutputStream(process.getOutputStream()); 
			os.writeBytes(command + "\n");
			os.flush();
			os.writeBytes("exit\n");
			os.flush();

			BufferedReader reader = new BufferedReader(
			new InputStreamReader(process.getInputStream()));
			int read;
			char[] buffer = new char[4096];
			StringBuffer output = new StringBuffer();
			while ((read = reader.read(buffer)) > 0) {
				output.append(buffer, 0, read);
			}
			reader.close();

			process.waitFor();

			return output.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}


	private void output(final String str) {
		Runnable proc = new Runnable() {
			public void run() {
				if (str!=null) outputView.append(str);
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



	public final boolean isInternetAvailable(){
	// check if we are connected to the internet
		ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		if(info == null)
		    return false;

		return connectivityManager.getActiveNetworkInfo().isConnected();
	}


	private void download(String urlStr, String localPath) {
		try {
			URL url = new URL(urlStr);
			HttpURLConnection urlconn = (HttpURLConnection)url.openConnection();
			urlconn.setRequestMethod("GET");
			urlconn.setInstanceFollowRedirects(true);
			urlconn.connect();
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
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
    
}
