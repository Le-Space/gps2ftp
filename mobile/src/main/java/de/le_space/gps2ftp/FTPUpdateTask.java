package de.le_space.gps2ftp;

import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Created by Nico Krause (nico@le-space.de) on 16.06.17. (Le Space UG)
 */
public class FTPUpdateTask extends AsyncTask<String, Integer, String> {

	private static final String TAG = "Main";

	public static String SFTPHOST = "ftp.le-space.de";
	public static int SFTPPORT = 22;
	public static String SFTPUSER = "le-space";
	public static String SFTPPASS = "omnamahshivaya2017!";
	public static String SFTPWORKINGDIR = "/home/le-space/public_html";

	public View view;
	Exception ex;

	private MainActivity.PostFTPTaskListener<String> postTaskListener;

	FTPUpdateTask(MainActivity.PostFTPTaskListener<String> postTaskListener) {
		this.postTaskListener = postTaskListener;
	};



	protected String doInBackground(String... jsonContent) {

		Log.d(TAG, "starting ftp transfer for position:" + jsonContent);

		Session session;
		Channel channel;
		ChannelSftp channelSftp;

		try {
			JSch jsch = new JSch();
			session = jsch.getSession(SFTPUSER, SFTPHOST, SFTPPORT);
			session.setPassword(SFTPPASS);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();
			channel = session.openChannel("sftp");
			channel.connect();
			channelSftp = (ChannelSftp) channel;
			channelSftp.cd(SFTPWORKINGDIR);

			InputStream stream = new ByteArrayInputStream(jsonContent[0].getBytes(StandardCharsets.UTF_8));
			channelSftp.put(stream, "latlng.json");

			Log.d(TAG, "finished ftp transfer to:" + SFTPHOST);

		} catch (Exception ex) {
			this.ex = ex;
			publishProgress(0);
			Log.e(TAG, ex.getMessage());
			ex.printStackTrace();
		}

		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		Snackbar.make(view, "Please check FTP-Config:"+ex.getMessage(), Snackbar.LENGTH_LONG).setAction("Action", null).show();

		postTaskListener.onError(ex.getMessage());
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
	}
}

