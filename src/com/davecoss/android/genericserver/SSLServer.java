package com.davecoss.android.genericserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class SSLServer extends GenericServer {

	protected File keyfile = null; 
	
	
	public SSLServer(ServerHandler handler) throws UnknownHostException {
		super(handler);
		handler.info("SSLServer", "Creating SSLServer object");
		keyfile = new File("keystore");
	}
	
	public SSLServer(InetAddress addr, ServerHandler handler) {
		super(addr, handler);
		keyfile = new File("keystore");
	}
	
	public SSLServer(InetAddress addr, int port, ServerHandler handler) {
		super(addr, port, handler);
		keyfile = new File("keystore");
	}
	
	public void set_keystore(File newfile) {
		keyfile = newfile;
	}
	
	protected ServerSocket get_new_socket() throws IOException, HTTPError {
		handler.debug("SSLServer.get_new_socket", "Creating new SSL Socket");
		
		// init keystore
		if(keyfile == null)
			throw new IOException("Unspecified key file");
		
		char[] pass = handler.get_password();
		FileInputStream keyfilestream = new FileInputStream(keyfile);
		SSLServerSocketFactory sslserversocketfactory = null;
		try {
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(keyfilestream, pass);
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
					KeyManagerFactory.getDefaultAlgorithm());
			keyManagerFactory.init(keyStore, pass);
			TrustManagerFactory trust = TrustManagerFactory.getInstance("SunX509");
            trust.init(keyStore);
			SSLContext sslContext = SSLContext.getInstance("TLS");//SSLContext.getDefault();
			sslContext.init(keyManagerFactory.getKeyManagers(), trust.getTrustManagers(), new SecureRandom());
			sslserversocketfactory = sslContext.getServerSocketFactory();
			
		} catch (Exception e) {
			handler.error("ServerSocket.get_new_socket", "Error Starting SSL Socket");
			handler.traceback(e);
			throw new HTTPError(e.getMessage());
		} finally {
			if(keyfilestream != null)
				keyfilestream.close();
		}
		if(sslserversocketfactory == null)
			throw new HTTPError("Error creating SSL Socket Factory.");
		
		return sslserversocketfactory.createServerSocket(port, 0, addr);

	}

}