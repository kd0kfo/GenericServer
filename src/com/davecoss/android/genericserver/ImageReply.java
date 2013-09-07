package com.davecoss.android.genericserver;


public class ImageReply extends StreamReply {

	protected String image_type = "jpeg";

	public String get_content_type() {
		return "Content-type: image/" + image_type;
	}
	
	public void set_image_type(String type) {
		this.image_type = type;
	}
}
