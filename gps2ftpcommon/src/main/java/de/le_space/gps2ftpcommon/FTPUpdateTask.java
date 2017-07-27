package de.le_space.gps2ftpcommon;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import javax.net.SocketFactory;


/**
 * Created by Nico Krause (nico@le-space.de) on 16.06.17. (Le Space UG)
 */
public class FTPUpdateTask extends AsyncTask<String, Integer, String> {

	private static final String TAG = "Main";

	public static String SFTPHOST = "";
	public static int SFTPPORT = 22;
	public static String SFTPUSER = "";
	public static String SFTPPASS = "";
	public static String SFTPWORKINGDIR = "";
	private SocketFactory socketFactory;
	public Activity activity;

	Exception ex;

	public class MySocketFactory implements com.jcraft.jsch.SocketFactory
	{
		SocketFactory socketFactory;
		public MySocketFactory(SocketFactory socketFactory) throws IOException {
				this.socketFactory = socketFactory;
		}
		@Override
		public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
					return this.socketFactory.createSocket(host, port);
		}

		@Override
		public InputStream getInputStream(Socket socket) throws IOException {
			return socket.getInputStream();
		}

		@Override
		public OutputStream getOutputStream(Socket socket) throws IOException {
			return socket.getOutputStream();
		}
	};


	private FTPUpdateTask.PostFTPTaskListener<String> postTaskListener;

	public void setSocketFactory(SocketFactory socketFactory) {
		this.socketFactory = socketFactory;
	}

	public interface PostFTPTaskListener<K> {
		// K is the type of the result object of the async task
		void onError(K result);
		void onSuccess(K result);
	}

	public FTPUpdateTask(FTPUpdateTask.PostFTPTaskListener<String> postTaskListener) {
		this.postTaskListener = postTaskListener;
	};

	protected String doInBackground(String... jsonContent) {

		Log.d(TAG, "starting ftp transfer for position:" + jsonContent);

		Session session = null;
		Channel channel = null;
		ChannelSftp channelSftp;

		try {
			JSch jsch = new JSch();

			//new JSch();
			session = jsch.getSession(SFTPUSER, SFTPHOST, SFTPPORT);

			if(socketFactory!=null) session.setSocketFactory(new MySocketFactory(socketFactory));

			session.setPassword(SFTPPASS);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			config.put("PreferredAuthentications", "password");
			session.setConfig(config);
			session.connect();
			channel = session.openChannel("sftp");
			channel.connect();
			channelSftp = (ChannelSftp) channel;
			channelSftp.cd(SFTPWORKINGDIR);

			InputStream stream = new ByteArrayInputStream(jsonContent[0].getBytes(StandardCharsets.UTF_8));
			channelSftp.put(stream, "latlng.json");
			stream.close();
			String successString = "finished ftp transfer to:" + SFTPHOST;
			Log.d(TAG, successString);
			postTaskListener.onSuccess(successString);
		} catch (Exception ex) {
			this.ex = ex;
			publishProgress(0);
			Log.e(TAG, ex.getMessage());
			ex.printStackTrace();
		} finally {
			if(channel!=null && channel.isConnected())channel.disconnect();
			if(session!=null && session.isConnected())session.disconnect();
		}

		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		Toast.makeText(activity,"Please check FTP-Config:"+ex.getMessage(), Toast.LENGTH_LONG).show();
		//Snackbar.make(view, "Please check FTP-Config:"+ex.getMessage(), Snackbar.LENGTH_LONG).setAction("Action", null).show();

		postTaskListener.onError(ex.getMessage());
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
	}
}

