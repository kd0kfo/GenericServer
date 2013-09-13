package com.davecoss.android.genericserver;

import java.io.PrintWriter;
import java.util.HashMap;

import org.json.simple.JSONObject;


public class JSONReply extends HTTPReply {
	
	public JSONReply (String content) {
		super(content);
	}
	
	public String get_content_type() {
		return "Content-type: application/json";
	}
	
	@Override
	public void dump_head(PrintWriter output)
	{
		output.println(this.status);
		
		output.println(get_content_type());
		output.println("");
		output.flush();
	}
	

	@Override
	public void dump_body(PrintWriter output) {
		output.println(this.content);
		output.println("");
		output.flush();
	}
	
	@SuppressWarnings("unchecked")
	public static JSONReply fromHashmap(HashMap<String, String> content) {
		JSONObject json_content = new JSONObject(content);
		JSONObject json_data = new JSONObject();
		json_data.put("content", json_content);
		
		return new JSONReply(json_data.toString());
	}
}
