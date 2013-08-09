package com.davecoss.android.genericserver;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

public class Console extends Activity {
    private ServerBundle server;
	TextView txt_rx;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_console);
		txt_rx = (TextView)this.findViewById(R.id.txt_rx);
		
		server = new ServerBundle();

		server.start_server(null);
		
		String msg = "IPs are ";
		try{
			for(Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces(); nics.hasMoreElements();)
			{
				NetworkInterface nic = nics.nextElement();
				for(Enumeration<InetAddress> addrs = nic.getInetAddresses(); addrs.hasMoreElements();)
				{
					InetAddress addr = addrs.nextElement();
					msg += "\n" + addr.getHostAddress();
				}
			}
		}
		catch(SocketException se)
		{
			Log.e("Console", "Socket Exception: " + se.getMessage());
			msg = "Could not get interface information.";
		}
		
		txt_rx.setText(msg);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.console, menu);
		return true;
	}

	public void stop_server(View view)
	{
	    try {
		this.stop_server();
	    } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	
}
