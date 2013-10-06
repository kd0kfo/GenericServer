package com.davecoss.android.genericserver;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;


public class Console extends Activity {
    private ServerBundle server = null;
    private AndroidHandler handler;
	TextView txt_rx = null;
	private AndroidMonitor status_updater = null;
	private Thread monitor = null;
	private boolean should_stop_monitor = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_console);
		
		TextView passwd = (TextView)findViewById(R.id.txt_password);
		handler = new AndroidHandler(passwd);
		if(this.server == null)
			server = new_server_instance(handler);
		if(!this.server.is_running())
			this.start_server("localhost");
		
		status_updater = new AndroidMonitor(this);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		should_stop_monitor = false;
		monitor = new  Thread(new Runnable() {

			@Override
			public void run() {
				while(!Thread.currentThread().isInterrupted() && !should_stop_monitor)
				{
					Bundle b = new Bundle();
					Message msg = new Message();
					String status = "";
					boolean userdir = false;
					boolean canwrite = false;
					
					if(server == null)
					{
						status = "Server not initialized";
					}
					else
					{
						if(server.is_running())
						{
							
							status = "Running on " + server.get_address().getHostAddress()
									+ ":" + server.get_port();
							String strUserdir = server.getdir();
							if(strUserdir != null)
								userdir = strUserdir.length() > 0;
							canwrite = server.get_write_permission();
						}
						else
							status = "Stopped";
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// Nothing to do. Interrupting this is ok.
					}
					if(status_updater == null)
						continue;
					b.putString("status", status);
					b.putBoolean("userdir", userdir);
					b.putBoolean("canwrite", canwrite);
					msg.setData(b);
					status_updater.sendMessage(msg);
				} // while not interrupted
			}
			
		});
		if(monitor != null)
			monitor.start();
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
				handler.error("Console.onDestroy", "Error stopping server: " + e.getMessage());
			}
		}
		this.server = null;
		
		should_stop_monitor = true;
		if(this.monitor != null)
		{
			try{
				this.monitor.join();
			} catch(InterruptedException ie) {
				//Nothing to do.
			}
			this.monitor = null;
		}
		this.status_updater = null;
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
		
		try {
			ArrayList<String> ip_list = GenericServer.get_ip_list();
			
			msg += "\nKnown addresses: \n";
			Iterator<String> it = ip_list.iterator();
			while(it.hasNext()) {
				msg += it.next() + "\n";
			}
		}
		catch(SocketException se)
		{
			Log.e("Console", "Socket Exception: " + se.getMessage());
			msg = "Could not get interface information.";
		}
		txt_rx.setText(msg);
		status_message(status, false, false);
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
	    status_message("Status: Stopped", false, false);
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
    				status_message("Could not make directory.", false, false);
    				return;
    			}
    		}

    		this.server.setdir(dir.getAbsolutePath());
    	}
	}
	
	private void status_message(String msg, boolean canwrite, boolean userdir)
	{
		TextView status = (TextView)findViewById(R.id.txt_message);
		status.setText(msg);
		
		ToggleButton tv = (ToggleButton) findViewById(R.id.btn_setdir);
		tv.setChecked(userdir);
		
		tv = (ToggleButton)findViewById(R.id.btn_write_perm);
		tv.setChecked(canwrite);
	}
	
	private File get_keystore() {
		String state = Environment.getExternalStorageState();
    	boolean can_read = false;
    	if (Environment.MEDIA_MOUNTED.equals(state)) {
    	    // We can read and write the media
    		can_read = true;
    	} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
    	    // We can only read the media
    	    can_read = true;
    	} 
    	if(!can_read)
    		return null;
    	
    	File dir = Environment.getExternalStorageDirectory();
    	return new File(dir, "server.keystore");
	}
	
	public void setaddr(View view)
	{
		TextView txt_addr = (TextView)findViewById(R.id.txt_addr);
		String addr = txt_addr.getText().toString().trim();
		this.stop_server(view);
		if(this.server == null)
			this.server = new_server_instance(handler);
		try {
			File keystore = get_keystore();
			if(!keystore.exists())
				keystore = null; // Null indicates use non-ssl
			server.start_server(InetAddress.getByName(addr), GenericServer.DEFAULT_PORT, keystore);
		} catch(UnknownHostException uhe) {
			handler.error("Console.setaddr", "Unknown Host Error");
			handler.traceback(uhe);
			status_message("Unknown host: " + addr, false, false);
		} catch(Exception e) {
			handler.error("Console.setaddr", "Error starting server");
			handler.traceback(e);
			status_message("Error starting server: " + e.getMessage(), false, false);
		}
	}
	
	public class AndroidHandler implements ServerHandler
	{
		private TextView box1 = null;
		
		public AndroidHandler(TextView box) {
			box1 = box;
		}
		
		public String get_prefix() {
			return "[" + Thread.currentThread().getName() + "]";
		}
		
		public void error(String tag, String msg)
		{
			Log.e(get_prefix() + tag, msg);
		}
		
		public void debug(String tag, String msg)
		{
			Log.d(get_prefix() + tag, msg);
		}
		
		public void info(String tag, String msg)
		{
			Log.i(get_prefix() + tag, msg);
		}

		public void traceback(Exception e) {
			this.error(get_prefix() + " STACKTRACE", Log.getStackTraceString(e));
		}

		public char[] get_password() throws HTTPError {
			if(box1 == null)
				throw new HTTPError("Cannot open keystore, password box not defined.");
			
			return box1.getText().toString().toCharArray();
		}
	}
	
	static class AndroidMonitor extends Handler {

		private final WeakReference<Console> parent;
		
		public AndroidMonitor(Console parent) { 
			this.parent = new WeakReference<Console>(parent);
		}
		
		public void handleMessage(Message msg) {
				Console console = parent.get();
				Bundle b = msg.getData();
				String status = b.getString("status");
				boolean canwrite = b.getBoolean("canwrite");
				boolean userdir = b.getBoolean("userdir");
				console.status_message("Status: " + status, canwrite, userdir);
			}
	}
	
	static ServerBundle new_server_instance(AndroidHandler handler) {
		ServerBundle retval = new ServerBundle(handler);
		
		// Get plugin path
		String state = Environment.getExternalStorageState();
    	boolean can_read = false;
    	if (Environment.MEDIA_MOUNTED.equals(state)) {
    	    // We can read and write the media
    		can_read = true;
    	} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
    	    // We can only read the media
    	    can_read = true;
    	} 
    	if(can_read)
    	{
    		File dir = Environment.getExternalStorageDirectory();
    		File plugin_jarfile = new File(dir, "plugins.jar");
    		retval.set_plugin_path("file:" + plugin_jarfile.getAbsolutePath());
    	}
		return retval;
	}
}
