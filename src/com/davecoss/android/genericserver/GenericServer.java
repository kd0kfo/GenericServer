package com.davecoss.android.genericserver;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
import java.util.HashMap;

public class GenericServer implements Runnable {
	int port = 4242;
	private ServerSocket listener;

	public GenericServer() {
		try {
			listener = new ServerSocket(port);
		} catch (IOException ioe) {
			debug("IOException: " + ioe.getMessage());
		}
	}

	public static void json_write(String request,
			HashMap<String, String> content, PrintWriter output) {
		/*
		 * JSONObject json_content = new JSONObject(content); JSONObject
		 * json_data = (new JSONObject()).put(request, json_content);
		 * output.println("HTTP/1.1 200 Ok");
		 * output.println("Content-Type: text/json"); output.println("");
		 * output.println(json_data.toString()); output.println("");
		 */
		output.println("HTTP/1.1 500 Not Yet Implemented");
		output.println("");
		output.println("JSON is not yet implemented.");
		output.println("");

	}

	public static void html_write(String title, String content,
			PrintWriter output) {
		print_header(output, title);
		output.println(content);
		print_footer(output);
	}

	public static void do_get(String input, PrintWriter output) {
		String[] tokens = input.split(" ");
		if (tokens.length < 2)
			return;

		String request = tokens[1];

		if (request.length() >= 5 && request.substring(0, 5).equals("/date")) {
			String date_string = "";
			debug(request.substring(5));
			if (request.substring(5).equals("/unixtime")) {
				long unixtime = System.currentTimeMillis() / 1000L;
				date_string = Long.toString(unixtime);
			} else {
				date_string = (new Date()).toString();
			}
			if (request.contains("json")) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("date", date_string);
				json_write(request, map, output);
			} else {
				html_write(request, date_string, output);
			}
		} else if (request.equals("/favicon.ico")) {
			output.println("HTTP/1.1 200 Ok");
			output.println("");
			output.println(":-P");
			output.println("");
		} else {
			output.println("You asked for (" + request + ")");
		}

	}

	public static void debug(String msg) {
		System.out.println(msg);
	}

	public static void print_header(PrintWriter output, String request) {
		output.println("HTTP/1.1 200 Ok");
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
					while (input_text != null) {
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
}
