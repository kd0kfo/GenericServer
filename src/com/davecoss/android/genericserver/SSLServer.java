package com.davecoss.android.genericserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

public class SSLServer extends GenericServer {

	protected File keyfile = null; 
	
	
	public SSLServer(ServerHandler handler) throws UnknownHostException {
		super(handler);
		handler.info("SSLServer", "Creating SSLServer object");
		keyfile = new File("testkeystore");
	}
	
	public SSLServer(InetAddress addr, ServerHandler handler) {
		super(addr, handler);
		keyfile = new File("testkeystore");
	}
	
	public SSLServer(InetAddress addr, int port, ServerHandler handler) {
		super(addr, port, handler);
		keyfile = new File("testkeystore");
	}
	
	protected ServerSocket get_new_socket() throws IOException, HTTPError {
		handler.debug("SSLServer.get_new_socket", "Creating new SSL Socket");
		
		// init keystore
		if(keyfile == null)
			throw new IOException("Unspecified key file");
		
		char[] pass = handler.get_password();
		FileInputStream keyfilestream = new FileInputStream(keyfile);
		try {
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(keyfilestream, pass);
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
					KeyManagerFactory.getDefaultAlgorithm());
			keyManagerFactory.init(keyStore, pass);
			SSLContext sslContext = SSLContext.getDefault();
			SSLServerSocketFactory sslserversocketfactory = sslContext.getServerSocketFactory();
			
			return sslserversocketfactory.createServerSocket(port, 0, addr);
		} catch (Exception e) {
			handler.error("ServerSocket.get_new_socket", "Error Starting SSL Socket");
			handler.traceback(e);
			throw new IOException(e.getMessage());
		} finally {
			if(keyfilestream != null)
				keyfilestream.close();
		}
	}

}
