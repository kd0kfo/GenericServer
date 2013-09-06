package com.davecoss.android.genericserver;

import java.io.PrintWriter;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamReply extends HTTPReply {

	protected InputStream input = null;	
	
	public String get_content_type() {
		return "Content-type: image/x-icon";
	}
	
	public void dump(PrintWriter output) {
		output.println(this.status);
		
		output.println(get_content_type());
		output.println("");

		output.println(this.content);
		
		output.println("");
		output.flush();
	}

	public void set_input_stream(InputStream input) {
		this.input = input;
	}
	
	public void write(OutputStream output) throws java.io.IOException {
		byte[] line_return = "\n".getBytes();
		byte[] buffer = new byte[4096];
		output.write(this.status.getBytes());output.write(line_return);
		output.write(get_content_type().getBytes());output.write(line_return);
		output.write(line_return);
		output.flush();
		
		int bytes_read = -1;
		while((bytes_read = input.read(buffer)) != -1)
		{
			output.write(buffer, 0, bytes_read);
		}
		output.flush();
	
	}	
}
