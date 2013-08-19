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
import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;


import org.json.simple.JSONObject;


public class GenericServer implements Runnable {

	public enum FileType {
		HTML, TEXT, JSON, JPEG
	}

	private int port = 4242;
	private ServerSocket listener;
	private String userdir = null;
	private InetAddress addr = null;
	private ServerHandler handler;

	private static final String STATUS_OK = "HTTP/1.1 200 Ok";

	public GenericServer(ServerHandler handler) {
		this.handler = handler;
		try {
			start_server(InetAddress.getByName("localhost"), this.port);
		} catch (IOException ioe) {
			handler.debug("GenericServer", "IOException: " + ioe.getMessage());

		}
	}

	public GenericServer(InetAddress addr, ServerHandler handler) {
		this.handler = handler;
		try {
			start_server(addr, this.port);
		} catch (IOException ioe) {
			handler.debug("GenericServer", "IOException: " + ioe.getMessage());
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

	public void process_request(HTTPRequest client_request, OutputStream raw_output) 
			throws EmptyRequest{
		PrintWriter output = new PrintWriter(raw_output);
		if(client_request == null)
			throw new EmptyRequest();
		String uri = client_request.get_uri();
		if (uri == null)
			return;

		ArrayList<String> request = new ArrayList<String>(
				Arrays.asList(uri.split("/")));

		if (request.size() < 2) {
			html_write("Welcome", "Welcome to the server.", output);
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
		} else if (request.get(0).equals("user") && request.size() > 1) {

			String filename = request.get(1);
			FileType filetype = filetype_by_extension(filename);
			BufferedInputStream file = null;
			String err = "";
			try {
				if (userdir == null)
					err = "User directory not defined.";
				else if (filename.length() != 0)
					file = new BufferedInputStream(new FileInputStream(
							new File(userdir, request.get(1))));
			} catch (SecurityException se) {
				err = "Cannot read" + filename;
				file = null;
			} catch (FileNotFoundException fnfe) {
				err = "File not found " + filename;
				file = null;
			}

			if (file == null) {
				html_write("File error", err, "HTTP/1.1 401 Permission Denied",
						output);
				return;
			}
			byte[] buffer = new byte[4096];

			output.println(STATUS_OK);
			if (filetype == FileType.JPEG)
				output.println("Content-type: image/jpeg");
			else if (filetype == FileType.HTML)
				output.println("Content-type: text/html");
			else
				output.println("Content-type: text/plain");
			output.println("");
			output.flush();
			try {
				int nchars = -1;
				while ((nchars = file.read(buffer, 0, buffer.length)) != -1) {
					raw_output.write(buffer, 0, nchars);
				}
				raw_output.flush();
			} catch (IOException ioe) {
				output.println("Error reading file: " + ioe.getMessage());
			}
			output.println("");
			output.flush();
		} else if (request.get(0).equals("favicon.ico")) {
			output.println("HTTP/1.1 200 Ok");
			output.println("");
			output.println(":-P");
			output.println("");
		} else if (request.get(0).equals("echo")) {
			String echo = "";
			if (request.size() > 1)
				echo = request.get(1);
			if (request.get(request.size() - 1).equals("json")) {
				HashMap<String, String> content = new HashMap<String, String>();
				content.put("data", echo);
				if(client_request.has_post_data())
				{
					HashMap<String, String> post_data = client_request.get_full_post_data();
					content.putAll(post_data);
				}
				json_write(echo, content, output);
				
			} else {
				if(!client_request.has_post_data())
				{
					html_write(echo, echo, output);
				}
				else
				{
					HashMap<String, String> post_data = client_request.get_full_post_data();
					Iterator<String> it = post_data.keySet().iterator();
					String post_string = "";
					String key;
					while(it.hasNext())
					{
						key = it.next();
						post_string += key + "=" + post_data.get(key) + "\n";
					}
					html_write(echo, echo + "\nPOST:\n" + post_string, output);
				}
			}
		} else {
			html_write(request.get(0),
					"You asked for (" + request.get(0) + ")", output);
		}

		output.flush();

	}

	public static void print_header(PrintWriter output, String request,
			String status) {
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
				if (this.listener == null)
					start_server();
				Socket socket = listener.accept();
				handler.debug("GenericServer.run", "Opened socket on port " + port);
				try {
					OutputStream out = socket.getOutputStream();
					BufferedReader input = new BufferedReader(
							new InputStreamReader(socket.getInputStream()));
					String input_text = input.readLine();
					HTTPRequest request = null;
					while (input_text != null
							&& !Thread.currentThread().isInterrupted()) {
						handler.debug("GenericServer.run", "Client Said: " + input_text);
						String[] request_tokens = input_text.split(" ");
						int request_data_len = input_text.length();
						if(request_data_len > 0 && request_tokens.length < 2)
						{
							handler.debug("GenericServer.run", "Invalid Request String Length: " + input_text);
						}
						else if (request_tokens[0].equals("GET") || request_tokens[0].equals("POST"))
						{
							handler.debug("GenericServer.run", "Receiving " + request_tokens[0]);
							request = new HTTPRequest(request_tokens[0], request_tokens[1]);
						}
						else if(request != null && request_data_len != 0)
						{
							try
							{
								request.put_request_data(input_text);
							}
							catch(InvalidRequestData ird)
							{
								handler.error("GenericServer.run", "Invalid Data from Client: " + ird);
							}
						}
						out.flush();

						if (request_data_len == 0) {
							if(request != null)
							{
								handler.debug("GenericServer.run", "Received Request");
								handler.debug("GenericServer.run", request.toString()); 
							}
							break;
						}
						input_text = input.readLine();
						if (input_text == "")
							handler.debug("GenericServer.run", "Empty string");
					}
					try{
						if(request != null && request.get_type() == HTTPRequest.RequestType.POST)
						{
							String len_as_str = request.get_request_data("Content-Length");
							if(len_as_str != null)
							{
								try
								{
									int post_len = Integer.parseInt(len_as_str);
									char[] buffer = new char[post_len];
									input.read(buffer, 0, post_len);
									String post_data = new String(buffer);
									handler.debug("GenericServer.run", "POST Data: " + post_data);
									request.put_post_data(post_data);
								}
								catch(NumberFormatException nfe)
								{
									handler.error("GenericServer.run", "Invalid Content-Length: " + len_as_str);
									handler.error("GenericServer.run", nfe.getMessage());
								}
							}
						}
						process_request(request, out);
					}
					catch(HTTPError httperr)
					{
						handler.error("GenericServer.run", "HTTP ERROR: " + httperr);
					}
				} finally {
					handler.debug("GenericServer.run", "Closing socket");
					socket.close();
					handler.debug("GenericServer.run", "Socket closed");
				}
			} catch (SocketException se) {
				handler.debug("GenericServer.run", "Socket closed");
			} catch (IOException ioe) {
				handler.debug("GenericServer.run", "IOException: " + ioe.getMessage());
			}
		}// while not interrupted
	}

	private void start_server() throws IOException {
		try {
			listener = new ServerSocket(this.port, 0, this.addr);
		} catch (IOException ioe) {
			handler.debug("GenericServer.start_server", "IOException: " + ioe.getMessage());
		}
	}

	private void start_server(InetAddress addr, int port) throws IOException {
		this.port = port;
		this.addr = addr;
		start_server();
	}

	public void stop_server() {
		if (this.listener != null && !this.listener.isClosed()) {
			try {
				this.listener.close();
			} catch (IOException ioe) {
				handler.debug("GenericServer.stop_server", "Could not close socket listener: " + ioe.getMessage());
			}
		}
		this.listener = null;
	}

	public String get_address() {
		return this.listener.getInetAddress().toString();
	}

	public String get_port() {
		if (this.listener == null)
			return "";
		return Integer.toString(this.listener.getLocalPort());
	}

	public String getdir() {
		return userdir;
	}

	public String setdir(String dir) {
		userdir = dir;
		return dir;
	}

	public FileType filetype_by_extension(String filename) {
		if (!filename.contains("."))
			return FileType.HTML;

		String extension = filename.substring(filename.lastIndexOf(".") + 1);
		if (extension.length() > 0) {
			extension = extension.toLowerCase(Locale.US);
			if (extension.equals("jpg") || extension.equals("jpeg"))
				return FileType.JPEG;
			else if (extension.equals("json"))
				return FileType.JSON;
			else if (extension.equals("html") || extension.equals("htm"))
				return FileType.HTML;
		}
		return FileType.TEXT;
	}

	public boolean is_running() {
		return (this.listener != null && !this.listener.isClosed());
	}

}
