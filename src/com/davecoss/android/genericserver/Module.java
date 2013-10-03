package com.davecoss.android.genericserver;

public interface Module {
	public HTTPReply process_request(HTTPRequest request);
}
