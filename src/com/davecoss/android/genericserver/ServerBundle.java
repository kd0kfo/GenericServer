package com.davecoss.android.genericserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.lang.InterruptedException;


public class ServerBundle {
	private GenericServer serverd;
	private Thread server_thread;
	private ServerHandler handler;
	private String ssl_provider = SSLServer.DEFAULT_PROVIDER;
	
	public ServerBundle(ServerHandler handler) {
		serverd = null;
		server_thread = null;
		this.handler = handler;
	}

	public void start() {
		handler.info("ServerBundle.start", "Starting thread");
		server_thread.start();
		handler.info("ServerBundle.start", "Thread started");
		
	}

	public String get_port() {
		handler.info("ServerBundle.get_port", "Getting port");
		if (serverd == null)
		{
			handler.debug("ServerBundle.get_port", "Port requested when server is null");
			return "";
		}
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
	
	public void set_address(InetAddress address) {
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

	public void init_server(InetAddress addr, int port, File keystore)
			throws UnknownHostException {
		if(keystore != null)
		{
			SSLServer ssl = new SSLServer(addr, port, this.handler);
			ssl.set_keystore(keystore);
			ssl.set_provider(this.ssl_provider);
			serverd = ssl;
		}
		else
		{
			serverd = new GenericServer(addr, port, this.handler);
		}
		server_thread = new Thread(serverd);
	}
	
	public void init_server(File keystore)
			throws UnknownHostException {
		init_server(InetAddress.getByName("localhost"), GenericServer.DEFAULT_PORT, keystore);
	}
	
	public void init_server() throws UnknownHostException {
		init_server((File)null);
	}
	
	public void start_server(File keystore) throws UnknownHostException {
		init_server(keystore);
		start();
	}
	
	public void start_server() throws UnknownHostException {
		start_server((File)null);
	}

	public void start_server(InetAddress address, int port, File keystore)
			throws java.net.UnknownHostException {
		init_server(address, port, keystore);
		start();
	}
	
	public void start_server(InetAddress address, int port)
			throws java.net.UnknownHostException {
		start_server(address, port, (File)null);
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
	
	public void load_config() throws FileNotFoundException, IOException {
		load_config(null);
	}
	
	public void load_config(File keystore) throws FileNotFoundException, IOException 
	{
		handler.info("Standalone.load_config", "Creating new server");
		init_server(keystore);
		handler.info("Standalone.load_config", "Loading config");
		serverd.load_config();
		handler.info("Standalone.load_config", "Starting thread");
		server_thread = new Thread(serverd);
		start();
	}
	
	public void set_provider(String new_provider) {
		this.ssl_provider = new_provider;
	}
	
	public String get_provider() {
		return this.ssl_provider;
	}
}
