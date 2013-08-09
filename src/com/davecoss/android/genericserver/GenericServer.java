package com.davecoss.android.genericserver;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import 	java.util.ArrayList;
import 	java.util.Arrays;
import org.json.simple.JSONObject;

public class GenericServer implements Runnable {
	int port = 4242;
	private ServerSocket listener;
    
    private static final String STATUS_OK = "HTTP/1.1 200 Ok";

	public GenericServer() {
		try {
			listener = new ServerSocket(port);
		} catch (IOException ioe) {
			debug("IOException: " + ioe.getMessage());
		}
	}

        @SuppressWarnings("unchecked")
	public static void json_write(String request,
			HashMap<String, String> content, PrintWriter output) {
	     JSONObject json_content = new JSONObject(content); 
	     JSONObject json_data = new JSONObject();
	     json_data.put("content", json_content);
	     output.println(STATUS_OK);
	     output.println("Content-type: text/json");
	     output.println("");
	     output.println(json_data.toString());
	     output.println("");
	    
	}

    public static void html_write(String title, String content, String status,
				      PrintWriter output) {
	print_header(output, title, status);
	output.println(content);
	print_footer(output);
    }
    
    public static void html_write(String title, String content, 
				      PrintWriter output) {
	print_header(output, title, STATUS_OK);
	output.println(content);
	print_footer(output);
    }

	public static void do_get(String input, PrintWriter output) {
		String[] tokens = input.split(" ");
		if (tokens.length < 2)
			return;

		ArrayList<String> request = new ArrayList<String>(Arrays.asList(tokens[1].split("/")));
		

		if(request.size() < 2)
		    {
			html_write("Welcome", "Welcome to the server.",output);
			return;
		    }
		request.remove(0);
		if (request.get(0).equals("date")) {
			String date_string = "";
			if (request.size() > 1 && request.get(1).equals("/unixtime")) {
				long unixtime = System.currentTimeMillis() / 1000L;
				date_string = Long.toString(unixtime);
			} else {
				date_string = (new Date()).toString();
			}
			if (request.get(request.size() - 1).equals("json")) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("date", date_string);
				json_write(request.get(0), map, output);
			} else {
			    html_write(request.get(0), date_string, output);
			}
		} else if (request.get(0).equals("/favicon.ico")) {
			output.println("HTTP/1.1 200 Ok");
			output.println("");
			output.println(":-P");
			output.println("");
		} else {
		    html_write(request.get(0), "You asked for (" + request.get(0) + ")",output);
		}

	}

	public static void debug(String msg) {
		System.out.println(msg);
	}

	public static void info(String msg) {
		System.out.println(msg);
	}

    public static void print_header(PrintWriter output, String request, String status) {
		output.println(status);
		output.println("Content-Type: text/html; charset=UTF-8");
		output.println("");
		output.println("<!DOCTYPE html>");
		output.println("<html>\n<head>");
		output.println("<title>Results for " + request + "</title>");
		output.println("</head>\n<body>");

	}

	public static void print_footer(PrintWriter output) {
		output.println("</body>");
		output.println("</html>");
		output.println("");
	}

	public static void main(String[] args) throws IOException {
		GenericServer serverd = new GenericServer();
		info("Starting server on port " + serverd.port);
		Thread server_thread = new Thread(serverd);
		server_thread.start();
	}

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Socket socket = listener.accept();
				debug("Opened socket on port " + port);
				try {
					PrintWriter out = new PrintWriter(socket.getOutputStream(),
							true);
					BufferedReader input = new BufferedReader(
							new InputStreamReader(socket.getInputStream()));
					String input_text = input.readLine();
					while (input_text != null && !Thread.currentThread().isInterrupted()) {
						debug("Client said: \"" + input_text + "\"");
						if (input_text.contains("GET"))
							do_get(input_text, out);
						out.flush();

						if (input_text.length() == 0) {
							debug("EOF");
							break;
						}
						input_text = input.readLine();
						if (input_text == "")
							debug("Empty string");
					}
				} finally {
					debug("Closing socket");
					socket.close();
					debug("Socket closed");
				}
			} catch (IOException ioe) {
				debug("IOException: " + ioe.getMessage());
			}
		}// while not interrupted
	}
	
	public void stop_server()
	{
		if(this.listener != null && !this.listener.isClosed())
		{
			try{
				this.listener.close();
			}
			catch(IOException ioe)
			{
				debug("Could not close socket listener: " + ioe.getMessage());
			}
		}
		this.listener = null;
	}
	
	public String get_address()
	{
		return this.listener.getLocalSocketAddress().toString();
	}
	
	public String get_port()
	{
		return Integer.toString(this.listener.getLocalPort());
	}
}