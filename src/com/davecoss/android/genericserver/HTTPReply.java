package com.davecoss.android.genericserver;

import java.io.PrintWriter;
import java.io.OutputStream;


public class HTTPReply {
	
	// Status messages
	public static final String STATUS_OK = "HTTP/1.1 200 Ok";
	public static final String STATUS_FORBIDDEN = "HTTP/1.1 403 Forbidden";
	public static final String STATUS_NOT_FOUND = "HTTP/1.1 403 Not Found";
	public static final String STATUS_ERROR = "HTTP/1.1 500 Server Error";
	
	protected String content;
	protected String title;
	protected String status;
	
	public HTTPReply() {
		this.content = "";
		this.title = "";
		this.status = STATUS_OK;
	}

	public HTTPReply(String content) {
		this.content = content;
		this.title = content;
		this.status = STATUS_OK;
	}
	
	public HTTPReply(String title, String content)
	{
		this.content = content;
		this.title = title;
		this.status = STATUS_OK;
	}
	
	public HTTPReply(String title, String content, String status)
	{
		this.content = content;
		this.title = title;
		this.status = status;
	}
	
	public String get_content()
	{
		return this.content;
	}
	
	public HTTPReply set_content(String content) {
		this.content = content;
		return this;
	}
	
	public String get_title()
	{
		return this.title;
	}
	
	public HTTPReply set_title(String title) {
		this.title = title;
		return this;
	}
	
	public String get_status()
	{
		return this.status;
	}
	
	public HTTPReply set_status(String status) {
		this.status = status;
		return this;
	}
	
	public String get_content_type() {
		return "Content-type: text/plain";
	}
	
	public void dump(PrintWriter output)
	{
		output.println(this.status);
		
		output.println(get_content_type());
		output.println("");
		output.println(this.content);
		output.println("");
		output.flush();
	}

	public void write(OutputStream output) throws java.io.IOException {
		dump(new PrintWriter(output));
	}
	
}
