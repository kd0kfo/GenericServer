package com.davecoss.android.genericserver;

import java.lang.ClassLoader;
import java.net.URL;
import java.net.URLClassLoader;

public class Plugin extends URLClassLoader {

	public static String DEFAULT_PLUGIN_PATH = "file:plugins.jar";
	
	public Plugin(URL[] urls) {
		super(urls);
	}

	public Plugin(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}

	@Override
	public void addURL(URL url) {
		super.addURL(url);
	}

	public static URL[] getDefaultURLs() {
		return ((URLClassLoader)ClassLoader.getSystemClassLoader()).getURLs();	
	}
}
