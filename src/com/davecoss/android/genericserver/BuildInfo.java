package com.davecoss.android.genericserver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BuildInfo {
	private Properties info_data = null;
	public BuildInfo()
	{
		info_data = new Properties();
		InputStream info_file = this.getClass().getResourceAsStream("build.info");
		if(info_file == null)
		{
			info_data = null;
		}
		else
		{
			try {
				info_data.load(info_file);
			} catch (IOException e) {
				info_data = null;
			}
		}
		
	}
	
	public Properties get_build_properties()
	{
		return info_data;
	}
}
