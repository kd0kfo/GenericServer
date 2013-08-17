package com.davecoss.android.genericserver;

import java.io.IOException;
import java.net.InetAddress;
import java.lang.InterruptedException;

public class ServerBundle
{
    private GenericServer serverd;
    private Thread server_thread;
	
    public ServerBundle()
    {
	serverd = null;
	server_thread = null;
    }
	
    public void start()
    {
	server_thread.start();
    }
	
    public String get_port()
    {
	if(serverd == null)
	    return "";
	return serverd.get_port();
    }

    public String get_address()
    {
	if(serverd == null)
	    return "";
	return serverd.get_address();
    }
	
    public String setdir(String dir)
    {
	if(serverd == null)
	    return "";
	return serverd.setdir(dir);
    }

    public String getdir()
    {
	if(serverd == null)
	    return "";
	return serverd.getdir();
    }

    public void stop_server() throws IOException, InterruptedException
    {
	if(server_thread != null)
	    server_thread.interrupt();
	if(serverd != null)
	    serverd.stop_server();
	if(server_thread != null)
	    server_thread.join();
	server_thread = null;
	serverd = null;
    }
	
    public void start_server()
    {
	serverd = new GenericServer();
	server_thread = new Thread(serverd);
	server_thread.start();
    }

    public void start_server(String address) throws java.net.UnknownHostException
    {
	serverd = new GenericServer(InetAddress.getByName(address));
	server_thread = new Thread(serverd);
	server_thread.start();
    }
    
    public boolean is_running()
    {
    	if(this.serverd == null)
    		return false;
    	return this.serverd.is_running();
    }

}

