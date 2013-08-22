package com.davecoss.android.genericserver;

public interface ServerHandler {

	public void traceback(Exception e);
	public void error(String tag, String msg);
	public void debug(String tag, String msg);
	public void info(String tag, String msg);
}
