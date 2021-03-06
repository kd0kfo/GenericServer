package com.davecoss.android.genericserver;


public class FileReply extends StreamReply {

	protected UserFile.FileType filetype = UserFile.FileType.TEXT;

	public String get_content_type() {
		if (filetype == UserFile.FileType.JPEG)
			return "Content-type: image/jpeg";
		else if (filetype == UserFile.FileType.HTML)
			return "Content-type: text/html";
		else if (filetype == UserFile.FileType.JAVASCRIPT)
			return "Content-type: text/javascript";
		else if (filetype == UserFile.FileType.CSS)
			return "Content-type: text/css";

		return "Content-type: text/plain";
	}
	
	public void set_file_type(UserFile.FileType type) {
		this.filetype = type;
	}

}
