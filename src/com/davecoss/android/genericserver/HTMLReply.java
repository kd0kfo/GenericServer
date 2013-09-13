package com.davecoss.android.genericserver;

import java.io.PrintWriter;

public class HTMLReply extends HTTPReply {
	
	public HTMLReply(String title, String content) {
		super(title, content);
	}
	
	public HTMLReply(String title, String content, String status) {
		super(title, content, status);
	}
	
	@Override
	public String get_content_type() {
		return "Content-Type: text/html; charset=UTF-8";
	}
	
	public void dump_html_header(PrintWriter output) {
		output.println("<!DOCTYPE html>");
		output.println("<html>\n<head>");
		output.println("<title>" + this.title + "</title>");
		output.println("</head>\n<body>");

	}
	
	public void dump_footer(PrintWriter output) {
		output.println("</body>");
		output.println("</html>");
		output.println("");
	}
	
	@Override
	public void dump_body(PrintWriter output)
	{
		

		dump_html_header(output);
		
		output.println(this.content);
		
		dump_footer(output);
		
		output.println("");
		output.flush();
	}
	
	public static HTMLReply invalid_request() {
		return new HTMLReply("Invalid Request", "Invalid Request", HTTPReply.STATUS_ERROR);
	}
}
