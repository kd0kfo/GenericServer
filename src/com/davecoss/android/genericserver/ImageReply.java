package com.davecoss.android.genericserver;

public class ImageReply extends HTTPReply {

	public ImageReply(String title, String content) {
		super(title, content);
	}
	
	public ImageReply(String title, String content, String status) {
		super(title, content, status);
	}
	
	public String get_content_type() {
		return "Content-type: image/jpeg";
	}
	
}
