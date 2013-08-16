package com.davecoss.android.genericserver;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import org.json.simple.JSONObject;

public class GenericServer implements Runnable {

    public enum FileType {
	HTML, TEXT, JSON, JPEG
    }
    
    private int port = 4242;
    private ServerSocket listener;
    private String userdir = null;
    private InetAddress addr = null;
    
    private static final String STATUS_OK = "HTTP/1.1 200 Ok";

	public GenericServer() {
		try {
		    start_server(InetAddress.getByName("localhost"),this.port);
		} catch (IOException ioe) {
			debug("IOException: " + ioe.getMessage());

		}
	}


	public GenericServer(InetAddress addr) {
		try {
		    start_server(addr,this.port);
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
	     output.println("Content-type: application/json");
	     output.println("");
	     output.println(json_data.toString());
	     output.println("");
	     output.flush();
	    
	}

    public static void html_write(String title, String content, String status,
				      PrintWriter output) {
	print_header(output, title, status);
	output.println(content);
	print_footer(output);
	output.flush();
    }
    
    public static void html_write(String title, String content, 
				      PrintWriter output) {
	print_header(output, title, STATUS_OK);
	output.println(content);
	print_footer(output);
		output.flush();
    }

	public void do_get(String input, OutputStream raw_output) {
		PrintWriter output = new PrintWriter(raw_output);
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
			if (request.size() > 1 && request.get(1).equals("unixtime")) {
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
		} else if(request.get(0).equals("user") && request.size() > 1) {
		    
		    String filename = request.get(1);
		    FileType filetype = filetype_by_extension(filename);
		    BufferedInputStream file = null;
		    String err = "";
		    try{
			if(userdir == null)
			    err = "User directory not defined.";
			else if(filename.length() != 0)
			    file = new BufferedInputStream(new FileInputStream(new File(userdir, request.get(1))));
		    } catch(SecurityException se) {
			    err = "Cannot read" + filename;
			    file = null;
		    } catch(FileNotFoundException fnfe) {
			err = "File not found " + filename;
			file = null;
		    }

		    if(file == null)
			{
			    html_write("File error", err, "HTTP/1.1 401 Permission Denied", output);
			    return;
			}
		    byte[] buffer = new byte[4096];
		    
		    output.println(STATUS_OK);
		    if(filetype == FileType.JPEG)
		    	output.println("Content-type: image/jpeg");
		    else if(filetype == FileType.HTML)
		    	output.println("Content-type: text/html");
		    else
			output.println("Content-type: text/plain");
		    output.println("");
		    output.flush();
		    try{
			int nchars = -1;
			while((nchars = file.read(buffer, 0, buffer.length)) != -1)
			    {
				raw_output.write(buffer, 0, nchars);
			    }
				raw_output.flush();
		    } catch(IOException ioe) {
			output.println("Error reading file: " + ioe.getMessage());
		    }
		    output.println("");
		    output.flush();
		}
		else if (request.get(0).equals("favicon.ico")) {
			output.println("HTTP/1.1 200 Ok");
			output.println("");
			output.println(":-P");
			output.println("");
		} else if (request.get(0).equals("echo")) {
		    String echo = "";
		    if (request.size() > 1)
			echo = request.get(1);
		    if(request.get(request.size() - 1).equals("json"))
			{
			    HashMap<String, String> content = new HashMap<String, String>();
			    content.put("data", echo);
			    json_write(echo, content, output);
			}
		    else
			{
			    html_write(echo, echo, output);
			}
		} else {
		    html_write(request.get(0), "You asked for (" + request.get(0) + ")",output);
		}
		
		output.flush();

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

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				if(this.listener == null)
					start_server();
				Socket socket = listener.accept();
				debug("Opened socket on port " + port);
				try {
					OutputStream out = socket.getOutputStream();
					BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
			} 
			catch(SocketException se) {
				debug("Socket closed");
			}
			catch (IOException ioe) {
				debug("IOException: " + ioe.getMessage());
			}
		}// while not interrupted
	}

	private void start_server() throws IOException
	{
		try {
		    listener = new ServerSocket(this.port,0,this.addr);
		} catch (IOException ioe) {
			debug("IOException: " + ioe.getMessage());
		}
	}
	
	private void start_server(InetAddress addr, int port) throws IOException
	{
		this.port = port;
		this.addr = addr;
		start_server();
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
	    return this.listener.getInetAddress().toString();
	}
	
	public String get_port()
        {
	    if(this.listener == null)
		return "";
	    return Integer.toString(this.listener.getLocalPort());
	}

    public String getdir(){return userdir;}
    
    public String setdir(String dir)
	{
	    userdir = dir;
	    return dir;
	}


    public FileType filetype_by_extension(String filename)
    {
	if(!filename.contains("."))
	    return FileType.HTML;

	String extension = filename.substring(filename.lastIndexOf(".")+1);
	if(extension.length() > 0)
	    {
		extension = extension.toLowerCase();
		if(extension.equals("jpg") || extension.equals("jpeg"))
		    return FileType.JPEG;
		else if(extension.equals("json"))
		    return FileType.JSON;
		else if(extension.equals("html") || extension.equals("htm"))
		    return FileType.HTML;
	    }
	return FileType.TEXT;
    }

}
