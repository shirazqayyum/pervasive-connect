package com.example.wifidemo;

import java.util.ArrayList;
import java.util.List;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements WifiP2pManager.PeerListListener {
	/** Errors */
	public static final int WIFI_ACCESS_SUCCESSFUL = 0;
	public static final int UNABLE_TO_CONNECT_WIFI = -1;
	public static final int UNABLE_TO_ACCESS_WIFI_CHANNEL = -2;
	public static final int PEERLIST_IS_EMPTY = -3;
	public static final int UNABLE_TO_REGISTER_WIFI_BROADCAST_RECEIVER = -4;

	/** Error messages */
	public static final String PEER_DISCOVERY_FAIL_MESSAGE = "Peer discovery could not detect devices";
	
	/** Information messages */
	public static final String FILE_RECEIVER_TASK_STARTED = "File receiver is listening for files";
	public static final String WIFI_P2P_LIST = "List of Wifi P2P devices discovered";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Acquire all system resources such as Wifi objects
		initWifiResources();

		// Start peer discovery after initializing all Wifi resources
		startPeerDiscovery();

		// Initialize UI elements
		this.textView1 = (TextView) findViewById(R.id.textView1);
		this.textView2 = (TextView) findViewById(R.id.textView2);
		this.sendButton = (Button) findViewById(R.id.button1);
		this.receiveButton = (Button) findViewById(R.id.button2);

		// Register listeners for buttons
		this.sendButton.setOnClickListener(new SendButtonClickListener(this.wifip2pManager, this.wifiChannel, this, this.broadcastReceiver));
		this.receiveButton.setOnClickListener(new ReceiveButtonClickListener(this.wifip2pManager, this.wifiChannel, this));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private int initWifiResources() {
		
		// Acquire system resources
		if (this.wifip2pManager == null) {
			
			this.wifip2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
			
			if (this.wifip2pManager == null) {
				return UNABLE_TO_CONNECT_WIFI;
			}
		}

		if (this.wifiChannel == null) {
			
			this.wifiChannel = this.wifip2pManager.initialize(this, getMainLooper(), null);
			
			if (this.wifiChannel == null) {
				return UNABLE_TO_ACCESS_WIFI_CHANNEL;
			}
		}

		// Create intent filter and register Wifi broadcast receiver to receive broadcast events
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);

		if (this.broadcastReceiver == null) {
			this.broadcastReceiver = new WifiP2pBroadcastReceiver(this.wifip2pManager, this.wifiChannel, this);
		}
		registerReceiver(this.broadcastReceiver, intentFilter);

		return WIFI_ACCESS_SUCCESSFUL;
	}

	private void releaseWifiResources() {
		
		// Release all Wifi resources
		if (this.wifip2pManager != null) {
			this.wifip2pManager = null;
		}

		if (this.wifiChannel != null) {
			this.wifiChannel = null;
		}

		// Unregister broadcast receiver
		unregisterReceiver(this.broadcastReceiver);
		if (this.broadcastReceiver != null) {
			this.broadcastReceiver = null;
		}
	}

	private void startPeerDiscovery() {
		this.wifip2pManager.discoverPeers(this.wifiChannel,
				this.broadcastReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Acquire all system resources. Initialize all Wifi objects
		initWifiResources();

		// Start peer discovery after initializing all Wifi resources
		startPeerDiscovery();
	}

	@Override
	protected void onPause() {
		super.onRestart();

		// Release all system resources to prevent a memory leak
		releaseWifiResources();
	}
	
	@Override
	public void onPeersAvailable(WifiP2pDeviceList peers) {
		if (peers == null) {
			return;
		}

		if ((peers.getDeviceList() == null) || (peers.getDeviceList().size() == 0)) {
			return;
		}

		MainActivity.devices = new ArrayList<WifiP2pDevice>();
		for (WifiP2pDevice device : peers.getDeviceList()) {
			MainActivity.devices.add(device);
		}
	}
	
	static class SendButtonClickListener implements OnClickListener {
		
		WifiP2pManager wifiP2pManager;
		Channel wifiChannel;
		Activity activity;
		WifiP2pBroadcastReceiver p2pBroadcastReceiver;

		SendButtonClickListener(WifiP2pManager wifiP2pManager, Channel channel, Activity activity, BroadcastReceiver broadcastReceiver) {
			
			this.wifiP2pManager = wifiP2pManager;
			this.wifiChannel = channel;
			this.activity = activity;
			this.p2pBroadcastReceiver = (WifiP2pBroadcastReceiver) broadcastReceiver;
		}

		@Override
		public void onClick(View view) {
			if ((MainActivity.devices == null) || (MainActivity.devices.size() == 0)) {
				Toast.makeText(this.activity, PEER_DISCOVERY_FAIL_MESSAGE, Toast.LENGTH_SHORT).show();
				return;
			}

			ArrayList<String> peerNames = new ArrayList<String>();
			for (WifiP2pDevice device : MainActivity.devices) {
				peerNames.add(device.deviceName);
			}

			/* Start a new list activity that displays the list of discovered devices */
			if (peerNames.size() > 0) {
				
				DisplayListActivity.setWifiP2pEnvironmentInfo(new WifiP2pEnvironmentInfo(this.wifiP2pManager, this.wifiChannel, 
						MainActivity.devices));
				
				Intent intent = new Intent(this.activity, DisplayListActivity.class);
				intent.putStringArrayListExtra(MainActivity.WIFI_P2P_LIST, peerNames);
				this.activity.startActivity(intent);
			}
		}
	}

	static class ReceiveButtonClickListener implements OnClickListener {
		
		WifiP2pManager wifiP2pManager;
		Channel wifiChannel;
		Activity activity;
		FileReceiverTask fileReceiverTask;

		ReceiveButtonClickListener(WifiP2pManager wifiP2pManager, Channel channel, Activity activity) {
			
			this.wifiP2pManager = wifiP2pManager;
			this.wifiChannel = channel;
			this.activity = activity;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void onClick(View view) {
			if (this.fileReceiverTask == null) {
				this.fileReceiverTask = new FileReceiverTask(this.activity);
				this.fileReceiverTask.execute(new Object());
			}
			
			Toast.makeText(this.activity, FILE_RECEIVER_TASK_STARTED, Toast.LENGTH_SHORT).show();
		}
	}

	static class WifiP2pEnvironmentInfo {
		
		public WifiP2pEnvironmentInfo(final WifiP2pManager manager, final Channel channel, final List<WifiP2pDevice> devices) {
			
			this.manager = manager;
			this.channel = channel;
			this.devices = devices;
		}
		
		public WifiP2pDevice connectionToDeviceAvailable(String deviceName) {
			
			if (deviceName == null) {
				return null;
			}
			
			if ((this.manager == null) || (this.channel == null)) {
				return null;
			}
			
			if ((this.devices == null) || (this.devices.size() == 0)) {
				return null;
			}
			
			for (WifiP2pDevice device : this.devices) {
				if (device.deviceName.equals(deviceName)) {
					return device;
				}
			}
			
			return null;
		}
		
		WifiP2pManager manager;
		Channel channel;
		List<WifiP2pDevice> devices;
	}
	
	TextView textView1 = null;
	TextView textView2 = null;
	Button sendButton = null;
	Button receiveButton = null;

	WifiP2pManager wifip2pManager = null;
	Channel wifiChannel = null;

	WifiP2pBroadcastReceiver broadcastReceiver;
	
	// List of Wifi P2P devices that have been discovered during the peer discovery phase
	static List<WifiP2pDevice> devices;
}