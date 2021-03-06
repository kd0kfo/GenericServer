package com.davecoss.android.genericserver;

import java.io.Console;
import java.io.PrintWriter;
import java.io.PrintStream;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.FileNotFoundException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


import com.davecoss.android.genericserver.SSLServer;
import com.davecoss.android.genericserver.ServerBundle;

public class Standalone {

	public static void main(String[] args) throws IOException {
		
		// Define args
		Options options = new Options();
		int debug_level = 1;
		boolean should_daemon = false;
		options.addOption("c", false, "Use config file. (Default: off)");
		options.addOption("d", true, "Set Debug Level (Default: 1 (DEBUG) )");
		options.addOption("daemon", false, "Run without console. (Default: " + Boolean.valueOf(should_daemon) + ")");
		options.addOption("log", true, "Send log output to a file. (Default: Standard output/error)");
		options.addOption("provider", true, "Set KeyStore Provider (Default: "
				+ SSLServer.DEFAULT_PROVIDER);
		options.addOption("ssl", true, "Use SSL (Default: No)");
		
		CommandLineParser parser = new GnuParser();
		CommandLine cmd = null;
		try
		{
			cmd = parser.parse( options, args);
		}
		catch(ParseException pe)
		{
			System.err.println("Error parsing command line arguments.");
			System.err.println(pe.getMessage());
			System.exit(1);
		}
		
		// Parse args
		boolean use_initial_config = cmd.hasOption("c");
		File keystore = null;
		PrintStream logstream = null;
		String ssl_provider = null;
		if(cmd.hasOption("d"))
		{
			debug_level = Integer.valueOf(cmd.getOptionValue("d"));
			System.out.println("Setting debug level to " + debug_level);
		}
		if(cmd.hasOption("daemon"))
			should_daemon = true;
		if(cmd.hasOption("log"))
			logstream = new PrintStream(cmd.getOptionValue("log"));	
		if(cmd.hasOption("provider"))
			ssl_provider = cmd.getOptionValue("provider");
		if(cmd.hasOption("ssl"))
		{
			keystore = new File(cmd.getOptionValue("ssl"));
		}

	
		StandaloneHandler handler = new Standalone().new StandaloneHandler(debug_level);
		if(logstream != null)
			handler.set_logstream(logstream);
		ServerBundle server = new ServerBundle(handler);
		if(ssl_provider != null)
			server.set_provider(ssl_provider);
		
		Console console = System.console();

		// Start up server
		if(use_initial_config)
		{
			server.load_config(keystore);
			handler.info("Standalone.main", "Waiting for server start");
			while(!server.is_running())
			{
				continue;
			}
		}
		else
		{
			if(keystore != null)
				handler.debug("Standalone.main", "Starting server with SSL");
			try {
				server.start_server(keystore);
				handler.error("Standalone.main", "Waiting for server start");
				while(!server.is_running())
				{
					continue;
				}
			} catch (UnknownHostException uhe) {
				handler.error("Standalone.main", "Unable to start server. Host Unknown");
				handler.traceback(uhe);
			}
		}

		if(should_daemon || console == null) {
			System.out.println("Running simple server. Press Ctrl-C to terminate.");
			server.blocking_wait();
			System.exit(0);
		}


		PrintWriter cout = console.writer();
		cout.println("Server is running on port " + server.get_port());

		String input;
		while ((input = console.readLine(">")) != null) {
			if (input.equals("stop")) {
				break;
			} else if (input.equals("getaddr")) {
				InetAddress addr = server.get_address();
				if (addr == null)
					cout.println("Not connected");
				else
					cout.println(addr.getHostAddress());
			} else if (input.equals("setaddr")) {
				String addr = console.readLine("What address? ");
				try {
					server.stop_server();
				} catch (InterruptedException ie) {
					cout.println("Error ending server: " + ie.getMessage());
				}
				try {
					server.start_server(InetAddress.getByName(addr));
					cout.println("Server is running on port "
							+ server.get_port());
				} catch (UnknownHostException uhe) {
					cout.println("Unknown host: " + addr);
				}
			} else if (input.equals("getport")) {
				if(server == null)
					cout.println("Not connected");
				else
					cout.println(server.get_port());
			} else if (input.equals("setport")) {
				String port_string = console.readLine("What port? ");
				try {
					int port = Integer.parseInt(port_string);
					InetAddress addr = server.get_address();
					server.stop_server();
					server.start_server(addr, port);
					cout.println("Server is running on port "
							+ server.get_port());
				} catch (NumberFormatException nfe) {
					cout.println("Not a vaild port number:" + port_string);
				} catch (InterruptedException ie) {
					cout.println("Error ending server: " + ie.getMessage());
				} catch (UnknownHostException uhe) {
					cout.println("Unknown host: " + uhe.getMessage());
				}
			} else if (input.equals("getdir")) {
				cout.println(server.getdir());
			} else if (input.equals("setdir")) {
				String dir = console.readLine("What directory? ");
				server.setdir(dir);
			} else if (input.equals("setpin")) {
				String pin = console.readLine("New Pin?");
				try {
					int pin_int = Integer.valueOf(pin);
					//server.setpin(pin_int);
				} catch(NumberFormatException nfe) {
					cout.println("Invalid PIN: " + pin);
				}
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
			} else if (input.equals("allowwrite")) {
				if(server != null)
					server.set_write_permission(true);
			} else if (input.equals("disallowwrite")) {
				if(server != null)
					server.set_write_permission(false);
			} else if (input.equals("dumpconfig")) {
				if(server != null)
				{
					try
					{
						server.dump_config();
					} catch(Exception e) {
						cout.println("Could not save configuration");
						handler.traceback(e);
					}
				}
			} else if (input.equals("loadconfig")) {
				if(server != null)
				{
					try
					{
						if(server.is_running())
							server.stop_server();
						server.load_config(keystore);
						//server.start();
					} catch(Exception e) {
						cout.println("Could not load configuration");
						handler.traceback(e);
					}
				}
			} else if (input.equals("plugin")) {
				cout.println("Plugin path: " + server.get_plugin_path());
				String new_path = console.readLine("Set plugin path (leave empty to ignore): ");
				if(new_path.length() > 0)
				{
					server.set_plugin_path(new_path);
					cout.println("Path set to " + server.get_plugin_path());
				}
			} else {
				if(input.trim().length() > 0)
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
		private PrintStream logstream = System.out;	

		public StandaloneHandler()
		{
			
		}
		
		public StandaloneHandler(int verbosity)
		{
			this.verbosity = verbosity;
		}
		
		public void traceback(Exception e)
		{
			e.printStackTrace(logstream);
		}
		
		public void error(String tag, String msg)
		{
			logstream.println(get_prefix() + tag + ": " + msg);
		}
		
		public void debug(String tag, String msg) {
			if(verbosity >= 1)
				logstream.println(get_prefix() + tag + ": " + msg);
		}

		public void info(String tag, String msg) {
			
			if(verbosity >= 2)
				logstream.println(get_prefix() + tag + ": " + msg);
		}
		
		public char[] get_password() throws HTTPError {
			Console console = System.console();
			if(console == null)
				throw new HTTPError("Could not access Console");
			return console.readPassword("Enter password: ");
			
		}
		
		public String get_prefix() {
			return "[" + Thread.currentThread().getName() + "]";
		}
		
		public void set_logstream(PrintStream newstream) {
			logstream = newstream;
		}
		
	}
}
