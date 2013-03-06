package com.example.wifidemo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.util.Log;

public class WifiP2pBroadcastReceiver extends BroadcastReceiver implements WifiP2pManager.ActionListener {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		if (intent != null) {
			
			String actionName = intent.getAction();
			
			if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(actionName)) {
				this.wifiP2pManager.discoverPeers(this.channel, this);
			} else if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(actionName)) {
				
				int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
				if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
					this.wifiP2pManager.discoverPeers(this.channel, this);
				}
			} else if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(actionName)) {
				
				int state = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1);
				if (state == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED) {					
					
					// Request new peer information if the old peer list is either null or empty
					if ((MainActivity.devices == null) || (MainActivity.devices.size() == 0)) {
						
						try {
							this.wifiP2pManager.requestPeers(this.channel, MainActivity.class.newInstance());
						} catch (InstantiationException e) {
							Log.e(WifiP2pBroadcastReceiver.class.getName(), e.getMessage());
						} catch (IllegalAccessException e) {
							Log.e(WifiP2pBroadcastReceiver.class.getName(), e.getMessage());
						}
					}
				} else if (state == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED) {

					try {
						this.wifiP2pManager.requestPeers(this.channel, MainActivity.class.newInstance());
					} catch (InstantiationException e) {
						Log.e(WifiP2pBroadcastReceiver.class.getName(), e.getMessage());
					} catch (IllegalAccessException e) {
						Log.e(WifiP2pBroadcastReceiver.class.getName(), e.getMessage());
					}
				}
			} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(actionName)) {
				
	            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

	            if (networkInfo.isConnected()) {
	                try {
						this.wifiP2pManager.requestConnectionInfo(channel, SendActivity.class.newInstance());
					} catch (InstantiationException e) {
						Log.e(WifiP2pBroadcastReceiver.class.getName(), e.getMessage());
					} catch (IllegalAccessException e) {
						Log.e(WifiP2pBroadcastReceiver.class.getName(), e.getMessage());
					}
	            } else {
	            	
	            	Log.d(WifiP2pBroadcastReceiver.class.getName(), "Connection to peer failed");
	            }
			}
		}
	}

	public WifiP2pBroadcastReceiver(WifiP2pManager wifiP2pManager, Channel channel, Activity activity) {
		this.wifiP2pManager = wifiP2pManager;
		this.channel = channel;
		this.activity = activity;
	}

	@Override
	public void onFailure(int reason) {
	}

	@Override
	public void onSuccess() {
	}
	
	WifiP2pManager wifiP2pManager;
	Channel channel;
	Activity activity;
}