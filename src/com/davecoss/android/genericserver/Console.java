package com.davecoss.android.genericserver;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.view.Menu;
import android.widget.TextView;

public class Console extends Activity {
	Handler text_updater;
	private Thread serverd;
	private int port = 4242;
	TextView txt_rx;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_console);
		txt_rx = (TextView)this.findViewById(R.id.txt_rx);
		
		text_updater = new Handler();
		this.serverd = new Thread(new GenericServer());
		this.serverd.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.console, menu);
		return true;
	}

	
}
