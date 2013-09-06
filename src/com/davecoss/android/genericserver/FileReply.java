package com.davecoss.android.genericserver;

import java.io.PrintWriter;
import java.io.InputStream;
import java.io.OutputStream;

public class FileReply extends StreamReply {

	protected UserFile.FileType filetype = UserFile.FileType.TEXT;

	public String get_content_type() {
		if (filetype == UserFile.FileType.JPEG)
			return "Content-type: image/jpeg";
		else if (filetype == UserFile.FileType.HTML)
			return "Content-type: text/html";

		return "Content-type: text/plain";
	}
	
	public void set_file_type(UserFile.FileType type) {
		this.filetype = type;
	}

}
