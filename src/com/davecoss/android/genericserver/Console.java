package com.davecoss.android.genericserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.view.Menu;
import android.widget.TextView;

public class Console extends Activity {
	Handler text_updater;
	private Thread serverd;
	private ServerSocket server_socket;
	private int port = 4242;
	TextView txt_rx;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_console);
		txt_rx = (TextView)this.findViewById(R.id.txt_rx);
		
		text_updater = new Handler();
		this.serverd = new Thread(new ServerD());
		this.serverd.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.console, menu);
		return true;
	}

	class ServerD implements Runnable {

		
		public void run() {
			Socket socket = null;
			try {
				server_socket = new ServerSocket(port);
			} catch (IOException e) {
				e.printStackTrace();
			}
			while (!Thread.currentThread().isInterrupted()) {

				try {

					socket = server_socket.accept();

					Communicator commd = new Communicator(socket);
					new Thread(commd).start();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	class Communicator implements Runnable {

		private Socket client_socket;

		private BufferedReader input;
		private PrintWriter output;

		public Communicator(Socket client_socket) {

			this.client_socket = client_socket;

			try {

				this.input = new BufferedReader(new InputStreamReader(this.client_socket.getInputStream()));
				this.output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(this.client_socket.getOutputStream())));

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void run() {

			while (!Thread.currentThread().isInterrupted()) {

				try {

					String read = input.readLine();
					if(read != null)
					{
						text_updater.post(new ViewModifier(read));
						output.write("HTTP/1.0 200 OK\r\n");
						output.write("Date: Fri, 31 Dec 1999 23:59:59 GMT\r\n");
						output.write("Server: Apache/0.8.4\r\n");
						output.write("Content-Type: text/html\r\n");
						output.write("Content-Length: 59\r\n");
						output.write("Expires: Sat, 01 Jan 2000 00:59:59 GMT\r\n");
						output.write("Last-modified: Fri, 09 Aug 1996 14:21:40 GMT\r\n");
						output.write("\r\n");
						output.write("<TITLE>You said</TITLE>");
						output.write("<P>" + read + "</P>");
					    output.flush();
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
	
	class ViewModifier implements Runnable {
		private String new_string;

		public ViewModifier(String str) {
			this.new_string = str;
		}

		@Override
		public void run() {
			txt_rx.setText(this.new_string);
		}
	}
	
}
