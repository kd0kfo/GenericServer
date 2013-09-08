package com.davecoss.android.genericserver;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;


public class Console extends Activity {
    private ServerBundle server = null;
    private AndroidHandler handler;
	TextView txt_rx;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_console);
		
		handler = new AndroidHandler();
		if(this.server == null)
			server = new ServerBundle(handler);
		if(!this.server.is_running())
			this.start_server("localhost");
		
		
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if(this.server != null)
		{
			try
			{
				this.stop_server();
			}
			catch(Exception e)
			{
				Log.e("Console.onDestroy", "Error stopping server: " + e.getMessage());
			}
		}

		this.server = null;		
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.console, menu);
		return true;
	}

	public void start_server(String address)
	{
		txt_rx = (TextView)this.findViewById(R.id.txt_rx);
		String msg = "";
		String status = "";
		try
		{
			server.start_server(InetAddress.getByName(address));
			while(!server.is_running())
				continue;
			msg = "IP is " + server.get_address();
			status = "Status: Running";
		}
		catch(UnknownHostException nhe)
		{
			handler.error("Console.start_server", "Unknown Host Exception");
			handler.traceback(nhe);
			msg = "Could not start server: " + nhe.getMessage();
			status = "Status: Failed";
		}
		catch(Exception e)
		{
			handler.error("Console.start_server", "Exception");
			handler.traceback(e);
			msg = "Could not start server: " + e.getMessage();
			status = "Status: Failed";
		}
		
		try{
			msg += "\nKnown addresses: \n";
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
		status_message(status);
	}
	
	public void stop_server(View view)
	{
		this.stop_server();
	}
	
	public void stop_server()
	{
	    try {
	    	if(this.server == null)
	    		return;
	    	this.server.stop_server();
	    } catch(IOException ioe){
	    	Log.e("Console.stop_server", "Error stoping server: " + ioe.getMessage());
	    } catch (InterruptedException e) {
	    
			Log.e("Console.stop_server", "Interrupted: " + e.getMessage());
	    }
	    status_message("Status: Stopped");
	}
	
	public void setWritePerm(View view)
	{
		ToggleButton tb = (ToggleButton) findViewById(R.id.btn_write_perm);
		
		if(server != null)
			server.set_write_permission(tb.isChecked());
	}
	
	public void setdir(View view)
	{
		ToggleButton tv = (ToggleButton) findViewById(R.id.btn_setdir);

		// If not checked, set user directory to null.
		// If is checked, set user directory to external storage directory
		if(!tv.isChecked())
		{
			this.server.setdir(null);
			return;
		}
		
		boolean mExternalStorageAvailable = false;
    	boolean mExternalStorageWriteable = false;
    	String state = Environment.getExternalStorageState();
    	
    	if (Environment.MEDIA_MOUNTED.equals(state)) {
    	    // We can read and write the media
    	    mExternalStorageAvailable = mExternalStorageWriteable = true;
    	} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
    	    // We can only read the media
    	    mExternalStorageAvailable = true;
    	    mExternalStorageWriteable = false;
    	} else {
    	    // Something else is wrong. It may be one of many other states, but all we need
    	    //  to know is we can neither read nor write
    	    mExternalStorageAvailable = mExternalStorageWriteable = false;
    	}
    	
    	if(mExternalStorageAvailable && mExternalStorageWriteable)
    	{
    		File dir = getExternalFilesDir(null);
    		if(!dir.exists())
    		{
    			if(!dir.mkdirs())
    			{
    				status_message("Could not make directory.");
    				return;
    			}
    		}

    		this.server.setdir(dir.getAbsolutePath());
    	}
	}
	
	private void status_message(String msg)
	{
		TextView status = (TextView)findViewById(R.id.txt_message);
		status.setText(msg);
	}
	
	public void setaddr(View view)
	{
		TextView txt_addr = (TextView)findViewById(R.id.txt_addr);
		String addr = txt_addr.getText().toString().trim();
		this.stop_server(view);
		if(this.server == null)
			this.server = new ServerBundle(handler);
		this.start_server(addr);
	}
	
	public class AndroidHandler implements ServerHandler
	{
		public void error(String tag, String msg)
		{
			Log.e(tag, msg);
		}
		
		public void debug(String tag, String msg)
		{
			Log.d(tag, msg);
		}
		
		public void info(String tag, String msg)
		{
			Log.i(tag, msg);
		}

		public void traceback(Exception e) {
			this.error("STACKTRACE", Log.getStackTraceString(e));
		}

		public char[] get_password() throws HTTPError {
			// TODO: Implement dialog
			return "pass123".toCharArray();
		}
	}
}
