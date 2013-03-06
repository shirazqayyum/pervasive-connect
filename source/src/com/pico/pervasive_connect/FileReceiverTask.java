package com.example.wifidemo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

@SuppressWarnings("rawtypes")
public final class FileReceiverTask extends AsyncTask {

	public static final int FILE_RECEIVER_PORT = 8888;
	public static final int RECEIVER_BUFFER_SIZE = 1024;

	public FileReceiverTask(Context context) {

		this.context = context;
	}

	@Override
	protected Object doInBackground(Object... params) {
		
		Log.d(FileReceiverTask.class.getName(), "About to open port for listening");
		
		try {
			/* Create a server socket and wait for client connections. This call blocks until a connection is accepted from a client */
			this.serverSocket = new ServerSocket(FILE_RECEIVER_PORT);
			Socket client = serverSocket.accept();

			/*
			 * If the following portion of the code is executed that means that a file is now available in the specified directory which the
			 * client has sent
			 */
			
			final File dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
			final String fileName = System.currentTimeMillis() + ".jpg";
			final File file = new File(dirPath, fileName);

			File dirs = new File(file.getParent());
			if (!dirs.exists())
				dirs.mkdirs();
			file.createNewFile();

			InputStream inputstream = client.getInputStream();
			copyFile(inputstream, new FileOutputStream(file));
			
			// Log the name of the file received from the sender
			Log.d(FileReceiverTask.class.getName(), file.getAbsolutePath());
			
			return file.getAbsolutePath();
			
		} catch (IOException e) {
			
			Log.e(FileReceiverTask.class.getName(), e.getMessage());
		} finally {
		    
			if (this.serverSocket != null) {
		        if (!this.serverSocket.isClosed()) {
		            try {
		                this.serverSocket.close();
		            } catch (IOException e) {
		            	Log.d(SendActivity.class.getName(), e.getMessage());
		            }
		        }
		    }
		}
		
		return null;
	}

	/*
	 * File copy between two streams. The input stream needs to be cleaned and flushed afterwards so that the bytes get written to the disk.
	 */
	private void copyFile(InputStream inputstream, OutputStream outputstream) {
		
		if (inputstream == null) {
			return;
		}

		if (outputstream == null) {
			return;
		}

		byte[] buffer = new byte[RECEIVER_BUFFER_SIZE];
		int startPos = 0;
		try {

			int readBytes = inputstream.read(buffer);
			while (readBytes != -1) {
				outputstream.write(buffer, startPos, readBytes);
				readBytes = inputstream.read(buffer);
			}

			// Close streams and flush before exiting
			inputstream.close();
			outputstream.flush();
			outputstream.close();

		} catch (IOException e) {
			Log.e(FileReceiverTask.class.getName(), e.getMessage());
		}
	}

	Context context;
	ServerSocket serverSocket = null;
	Thread fileReceiverTaskThread = null;
}