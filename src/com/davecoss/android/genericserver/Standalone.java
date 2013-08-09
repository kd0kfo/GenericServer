package com.davecoss.android.genericserver;

import java.io.Console;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.io.IOException;

public class Standalone {

	public static void main(String[] args) throws IOException {
	    ServerBundle server = new ServerBundle();    
		Console console = System.console();
		PrintWriter cout = console.writer();

		server.start_server();
		cout.println("Server is running on port " + server.get_port());
		
		String input;
		while((input = console.readLine(">")) != null)
		    {
			if(input.equals("stop"))
			    {
				break;
			    }
			else if(input.equals("getaddr"))
			    {
				String addr = server.get_address();
				if(addr == null)
				    addr = "Not connected";
				cout.println(addr);
			    }
			else if(input.equals("setaddr"))
			    {
				String addr = console.readLine("What address? ");
				try {
				    server.stop_server();
				} catch (InterruptedException ie) {
				    cout.println("Error ending server: " + ie.getMessage());
				}
				try {
				    server.start_server(addr);
				    cout.println("Server is running on port " + server.get_port());
				} catch (UnknownHostException uhe) {
				    cout.println("Unknown host: " + addr);
				}

					    
			    }
			else if(input.equals("getdir"))
			    {
				cout.println(server.getdir());
			    }
			else if(input.equals("setdir"))
			    {
				String dir = console.readLine("What directory? ");
				server.setdir(dir);
			    }
			else
			    {
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

}