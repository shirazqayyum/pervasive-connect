package com.example.wifidemo;

import java.io.File;

import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class SendActivity extends Activity implements OnClickListener, WifiP2pManager.ConnectionInfoListener {

	/** No file selected warning */
	public static final String NO_FILE_SELECTED_WARNING = "Please enter the name of the file to send";
	
	/** No connection info available */
	public static final String NO_CONNECTION_INFO_AVAILABLE_MESSAGE = "Connection information not available";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_send);
		// Show the Up button in the action bar.
		setupActionBar();
		
		/* Get intent which will give information regarding the device name and the port where the server socket is listening */
		Intent intent = getIntent();
		this.hostName = intent.getStringExtra(DisplayListActivity.DEVICE_NAME);
		this.hostPortNum = intent.getStringExtra(DisplayListActivity.PORT_NUMBER);
		
		/* Initialize UI */
		this.textView1 = (TextView) findViewById(R.id.textView1);
		this.textView2 = (TextView) findViewById(R.id.textView2);
		this.editText = (EditText) findViewById(R.id.editText1);
		this.sendButton = (Button) findViewById(R.id.button1);
		this.progressBar = (ProgressBar) findViewById(R.id.progressBar1);
		
		this.textView1.setText(this.hostName.toCharArray(), 0, hostName.toCharArray().length);
		this.textView2.setText(this.hostPortNum.toCharArray(), 0, this.hostPortNum.toCharArray().length);
		
		/* Enable progress bar if it is not enabled initially. */
		if (!this.progressBar.isEnabled()) {
			this.progressBar.setEnabled(true);
		}
		
		// Register listener for the send button
		this.sendButton.setOnClickListener(this);
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.send, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onClick(View view) {
		
		if (this.editText.getText().length() == 0) {
			
			Toast.makeText(this, NO_FILE_SELECTED_WARNING, Toast.LENGTH_SHORT).show();
			return;
		}
		
		// Connection information should be available by this time
		if (connectionInfo == null) {
			
			Toast.makeText(this, NO_CONNECTION_INFO_AVAILABLE_MESSAGE, Toast.LENGTH_SHORT).show();
			return;
		}
		
		// FileSenderTask fileSenderTask = new FileSenderTask(this, this.progressBar, this.hostName, this.hostPortNum);
		FileSenderTask fileSenderTask = new FileSenderTask(this, this.progressBar, connectionInfo);
		
		/* Currently we are only sending jpg files from a fix location. Camera folder present inside the DCIM directory. */
		final File dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
		final String fileName = "/Camera" + "/" + this.editText.getText();
		String fileNameWithPath = "file://" + dirPath.getAbsolutePath() + fileName;
		
		fileSenderTask.sendFile(fileNameWithPath);
		fileSenderTask.execute(new Object());
	}
	
	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {
		
		if (info == null) {
			return;
		}
		
		if (info.groupFormed && info.isGroupOwner) {
			SendActivity.connectionInfo = info;
			return;
		}
		
		if (info.groupFormed) {
			SendActivity.connectionInfo = info;
			return;
		}
		
		SendActivity.connectionInfo = info;
		Log.d(SendActivity.class.getName(), "Peer connection information not available..." + info.groupFormed + "..." + info.isGroupOwner);
	}
		
	TextView textView1;
	TextView textView2;
	
	EditText editText;
	
	Button sendButton;
	ProgressBar progressBar;
	
	String hostName;
	String hostPortNum;
	
	// Provide all the connection information details
	static WifiP2pInfo connectionInfo = null;
}