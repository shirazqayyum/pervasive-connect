package com.example.wifidemo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

@SuppressWarnings("rawtypes")
public final class FileSenderTask extends AsyncTask {
	
	/** Sender buffer size in bytes */
	public static final int SENDER_BUFFER_SIZE = 1024;
	
	public FileSenderTask(Context context, View view, String hostName, String hostPortNum) {
		
		this.context = context;
		this.view = view;
		
		this.hostAddress = hostName;
		this.hostPortNum = Integer.parseInt(hostPortNum);
	}

	public FileSenderTask(Context context, View view, WifiP2pInfo info) {
		
		this.context = context;
		this.view = view;
		
		this.hostAddress = info.groupOwnerAddress.getHostAddress();
		this.hostPortNum = FileReceiverTask.FILE_RECEIVER_PORT;
	}
	
	public FileSenderTask(Context context, ProgressBar progressBar, WifiP2pInfo info) {
		
		this.context = context;
		this.progressBar = progressBar;
		
		this.hostAddress = info.groupOwnerAddress.getHostAddress();
		this.hostPortNum = FileReceiverTask.FILE_RECEIVER_PORT;
	}
	
	public void sendFile(String fileNameWithPath) {
		
		this.fileNamewithPath = fileNameWithPath;
	}
	
	@Override
	protected void onPreExecute() {
		this.progress = 0;
	}
	 
	@Override
	protected void onProgressUpdate(Object... values) {
		
		if (values instanceof Object[]) {
			Integer[] progressStatus = (Integer[]) values[0];
			this.progressBar.setProgress(progressStatus[0]);
		}		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected Object doInBackground(Object... params) {
		
		Socket socket = new Socket();
		byte buffer[]  = new byte[SENDER_BUFFER_SIZE];
		int len = 0;
		
		try {
			
		    /* Create a client socket with the host, port, and timeout information. */
		    socket.bind(null);
		    socket.connect((new InetSocketAddress(this.hostAddress, this.hostPortNum)), 600);

		    /* 
		     * Create a byte stream from the file and pipe it to the output stream of the socket. This data will be retrieved 
		     * by the server device.
		     */
		    
		    OutputStream outputStream = socket.getOutputStream();
		    ContentResolver cr = context.getContentResolver();
		    
		    InputStream inputStream = null;
		    inputStream = cr.openInputStream(Uri.parse(fileNamewithPath));
		    
		    while ((len = inputStream.read(buffer)) != -1) {
		        outputStream.write(buffer, 0, len);
		    }
		    
		    outputStream.close();
		    inputStream.close();
		    
		    /* File transferred. Inform MainActivity of this by publishing a progress update */
		    this.progress = 1000;
		    Integer[] values = new Integer[1];
		    values[0] = this.progress;
		    
		    /* Publish progress information in the main activity thread */
		    publishProgress((Object) values);
		    
		} catch (FileNotFoundException fnfe) {
			Log.e(SendActivity.class.getName(), fnfe.getMessage());
		} catch (IOException ioe) {
			Log.e(SendActivity.class.getName(), ioe.getMessage());
		} finally {
		    if (socket != null) {
		        if (socket.isConnected()) {
		            try {
		                socket.close();
		            } catch (IOException e) {
		            	Log.d(SendActivity.class.getName(), e.getMessage());
		            }
		        }
		    }
		}
		
		return null;
	}
	
	Context context;
	View view;
	ProgressBar progressBar;
	
	String fileNamewithPath;
	String hostAddress;
	int hostPortNum;
	
	WifiP2pInfo info;
	
	// Show progress while the file is getting transferred to the server
	int progress = 0;
}

