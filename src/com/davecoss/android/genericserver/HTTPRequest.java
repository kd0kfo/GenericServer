package com.davecoss.android.genericserver;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.net.URLDecoder;

public class HTTPRequest {
	public static String DEFAULT_PROTOCOL = "HTTP/1.1";
	public static String DEFAULT_ENCODING = "UTF-8";
	
	enum RequestType {
		GET, POST
	}
	
	private RequestType type;
	private HashMap<String, String> request_data, post_data;
	private String protocol;
	private String uri;
	
	public HTTPRequest(String Type, String uri)
	{
		init_request_data();
		
		type = RequestType.valueOf(Type);
		protocol = DEFAULT_PROTOCOL;
		this.uri = uri;
		this.post_data = null;
	}
	
	private void init_request_data(){request_data = new HashMap<String, String>();}
	
	public String toString()
	{
		String retval = type.toString() + " " + uri + " " + protocol + "\n";
		
		Iterator<Map.Entry<String, String>> it = request_data.entrySet().iterator();
		while(it.hasNext())
		{
			Entry<String, String> pair = it.next();
			retval += pair.getKey() + ": " + pair.getValue() + "\n";
		}
		
		return retval;
	}
	
	public String put_request_data(String data) throws InvalidRequestData
	{
		if(request_data == null)
			init_request_data();
		if(!data.contains(":"))
		{
			throw new InvalidRequestData("Invalid Request Data: " + data);
		}
		
		String key = data.substring(0, data.indexOf(":")).trim();
		String val = data.substring(data.indexOf(":")+1).trim();
		
		return request_data.put(key, val);
		
	}
	
	public String get_request_data(String key)
	{
		if(request_data == null)
			return null;
		
		return request_data.get(key);
	}
	
	public String get_uri()
	{
		return this.uri;
	}


	public String get_path()
	{
		int query_loc = this.uri.indexOf('?');
		if(query_loc != -1)
			return this.uri.substring(0, query_loc);
		return this.uri;
	}

	public String get_query()
	{
		int query_loc = this.uri.indexOf('?');
		if(query_loc != -1)
			return this.uri.substring(query_loc + 1);
		return null;
	}
	
	public HashMap<String, String> get_query_map() throws HTTPError
	{
		String query = get_query();
		if(query == null)
			return null;
		
		HashMap<String, String> retval = new HashMap<String, String>();
		String[] tags = query.split("&");
		
		for(String tag: tags)
		{
			String[] parameter;
			try {
				parameter = split_parameter(tag);
			} 
			catch (InvalidPostData ipd) {
				throw new HTTPError("Invalid query string: " + query);
			}
			retval.put(parameter[0], parameter[1]);
		}
		
		return retval;
	}
	
	public RequestType get_type()
	{
		return this.type;
	}
	
	private String post_decode(String encoded, String encoding) throws InvalidPostData
	{
		String retval;
		try{
			retval = URLDecoder.decode(encoded, encoding);
		}
		catch(java.io.UnsupportedEncodingException uee)
		{
			throw new InvalidPostData("Invalid Post Encoding: " + encoded + "\n" + uee.getMessage());
		}
		return retval;
	}
	
	public void put_post_data(String data) throws InvalidPostData
	{
		if(post_data == null)
			post_data = new HashMap<String, String>();
		String[] key_val_pairs = data.split("&");
		for(String key_val_pair : key_val_pairs)
		{
			String[] key_val = split_parameter(key_val_pair);
			post_data.put(key_val[0], key_val[1]);
		}
	}
	
	public String[] split_parameter(String parameter) throws InvalidPostData
	{
		return split_parameter(parameter, DEFAULT_ENCODING);
	}
	
	public String[] split_parameter(String parameter, String encoding) throws InvalidPostData
	{
		String[] pair = parameter.split("=");
		if(pair.length == 0)
			throw new InvalidPostData("Invalid Post data: " + parameter);
		else if(pair.length == 1)
		{
			String tmp = pair[0];
			pair = new String[]{tmp, ""};
		}
		
		pair[0] = post_decode(pair[0], encoding);
		pair[1] = post_decode(pair[1], encoding);
		
		return pair;
	}
	
	public String get_post_data(String key)
	{
		if(post_data == null)
			return null;
		return post_data.get(key);
	}
	
	public Set<String> get_post_keys()
	{
		if(post_data == null)
			return null;
		return post_data.keySet();
	}
	
	public boolean has_post_data()
	{
		return post_data != null && !post_data.isEmpty();
	}
	
	public HashMap<String, String> get_full_post_data()
	{
		return post_data;
	}
	
}
