package com.davecoss.android.genericserver;

import java.io.Console;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.io.IOException;

public class Standalone {

	public static void main(String[] args) throws IOException {
		StandaloneHandler handler = new Standalone().new StandaloneHandler();
		ServerBundle server = new ServerBundle(handler);
		Console console = System.console();
		PrintWriter cout = console.writer();

		server.start_server();
		cout.println("Server is running on port " + server.get_port());

		String input;
		while ((input = console.readLine(">")) != null) {
			if (input.equals("stop")) {
				break;
			} else if (input.equals("getaddr")) {
				String addr = server.get_address();
				if (addr == null)
					addr = "Not connected";
				cout.println(addr);
			} else if (input.equals("setaddr")) {
				String addr = console.readLine("What address? ");
				try {
					server.stop_server();
				} catch (InterruptedException ie) {
					cout.println("Error ending server: " + ie.getMessage());
				}
				try {
					server.start_server(addr);
					cout.println("Server is running on port "
							+ server.get_port());
				} catch (UnknownHostException uhe) {
					cout.println("Unknown host: " + addr);
				}

			} else if (input.equals("getdir")) {
				cout.println(server.getdir());
			} else if (input.equals("setdir")) {
				String dir = console.readLine("What directory? ");
				server.setdir(dir);
			} else if (input.equals("debug")) { 
				handler.verbosity = 1;
			} else if (input.equals("info")) { 
				handler.verbosity = 2;
			} else if (input.equals("error")) { 
				handler.verbosity = 0;
			} else if (input.equals("status")) {
				if (server == null || !server.is_running())
					cout.println("Closed");
				else
					cout.println("Listening on " + server.get_address() + ":"
							+ server.get_port());
			} else {
				cout.println("Unknown: " + input);
			}
		}
		cout.println("Stopping");
		try {
			server.stop_server();
		} catch (InterruptedException ie) {
			cout.println("Error ending server: " + ie.getMessage());
		}
	}

	public class StandaloneHandler implements ServerHandler
	{
		public int verbosity = 1;
		
		public StandaloneHandler()
		{
			
		}
		
		public StandaloneHandler(int verbosity)
		{
			verbosity = verbosity;
		}
		
		public void error(String tag, String msg)
		{
			System.err.println(tag + ": " + msg);
		}
		
		public void debug(String tag, String msg) {
			if(verbosity >= 1)
				System.out.println(tag + ": " + msg);
		}

		public void info(String tag, String msg) {
			if(verbosity >= 2)
				System.out.println(tag + ": " + msg);
		}
	}
}
