package com.davecoss.android.genericserver;

import java.io.PrintWriter;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamReply extends HTTPReply {

	protected InputStream input = null;	
	
	@Override
	public String get_content_type() {
		return "Content-type: image/x-icon";
	}
	
	@Override
	public void dump_body(PrintWriter output) {
		output.println(this.content);
		output.println("");
		output.flush();
	}

	public void set_input_stream(InputStream input) {
		this.input = input;
	}
	
	@Override
	public void dump_head(OutputStream output) throws java.io.IOException {
		byte[] line_return = "\n".getBytes();
		output.write(this.status.getBytes());output.write(line_return);
		output.write(get_content_type().getBytes());output.write(line_return);
		output.write(line_return);
		output.flush();
		
	}
	
	@Override
	public void write(OutputStream output) throws java.io.IOException {
		
		dump_head(output);
		
		int bytes_read = -1;
		byte[] buffer = new byte[4096];
		while((bytes_read = input.read(buffer)) != -1)
		{
			output.write(buffer, 0, bytes_read);
		}
		output.flush();
	
	}	
}
