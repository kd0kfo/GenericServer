package com.davecoss.android.genericserver;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.lang.InterruptedException;


public class ServerBundle {
	private GenericServer serverd;
	private Thread server_thread;
	private ServerHandler handler;

	public ServerBundle(ServerHandler handler) {
		serverd = null;
		server_thread = null;
		this.handler = handler;
	}

	public void start() {
		server_thread.start();
	}

	public String get_port() {
		if (serverd == null)
			return "";
		return serverd.get_port();
	}
	
	public void set_port(int port) {
		if (serverd == null)
			return;
		serverd.set_port(port);
	}

	public InetAddress get_address() {
		if (serverd == null)
			return null;
		return serverd.get_address();
	}
	
	public void get_address(InetAddress address) {
		if (serverd == null)
			return;
		serverd.set_address(address);
	}

	public String setdir(String dir) {
		if (serverd == null)
			return "";
		return serverd.setdir(dir);
	}

	public String getdir() {
		if (serverd == null)
			return "";
		return serverd.getdir();
	}

	public void stop_server() throws IOException, InterruptedException {
		handler.debug("Standalone.stop_server", "Stopping Server");
		
		if (server_thread != null)
		{
			handler.info("Standalone.stop_server", "Stopping Thread");
			server_thread.interrupt();
		}
		if (serverd != null)
			serverd.stop_server();
		if (server_thread != null)
		{
			server_thread.join();
			handler.debug("Standalone.stop_server", "Thread Stopped");
		}
		server_thread = null;
		serverd = null;
		handler.info("Standalone.stop_server", "Server stopped");
		
	}

	public void init_server(boolean use_ssl) {
		if(use_ssl)
			serverd = new SSLServer(this.handler);
		else
			serverd = new GenericServer(this.handler);
		server_thread = new Thread(serverd);
	}
	
	public void init_server() {
		init_server(false);
	}
	
	public void start_server(boolean use_ssl) {
		init_server(use_ssl);
		start();
	}
	
	public void start_server() {
		start_server(false);
	}

	public void start_server(InetAddress address, int port)
			throws java.net.UnknownHostException {
		serverd = new GenericServer(address, port,
				this.handler);
		server_thread = new Thread(serverd);
		server_thread.start();
	}

	public void start_server(InetAddress address)
			throws java.net.UnknownHostException {
		start_server(address, GenericServer.DEFAULT_PORT);
	}
	
	public boolean is_running() {
		if (this.serverd == null)
			return false;
		return this.serverd.is_running();
	}
	
	public void set_write_permission(boolean can_write)
	{
		if(this.serverd == null)
			return;
		this.serverd.set_write_permission(can_write);
	}
	
	public boolean get_write_permission()
	{
		if(this.serverd == null)
			return false;
		return this.serverd.get_write_permission();
	}
	
	public void dump_config() throws FileNotFoundException, IOException
	{
		if(this.serverd == null)
			return;
		this.serverd.dump_config();
	}
	
	public void load_config() throws FileNotFoundException, IOException 
	{
		/*
		if(this.serverd == null)
			init_server();
		/**/
		handler.info("Standalone.load_config", "Creating new server");
		serverd = new GenericServer(this.handler);
		serverd.stop_server();
		handler.info("Standalone.load_config", "Loading config");
		serverd.load_config();
		handler.info("Standalone.load_config", "Starting thread");
		server_thread = new Thread(serverd);
		start();
	}
}
