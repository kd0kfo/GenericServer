package com.davecoss.android.genericserver;

import java.io.File;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.util.Locale;

public class UserFile {
	public enum FileType {
		HTML, TEXT, JSON, JPEG
	}

	private File the_file;

	public UserFile(File file) {
		this.the_file = file;	
	}


	public static UserFile.FileType filetype_by_extension(String filename) {
		if (!filename.contains("."))
			return UserFile.FileType.HTML;

		String extension = filename.substring(filename.lastIndexOf(".") + 1);
		if (extension.length() > 0) {
			extension = extension.toLowerCase(Locale.US);
			if (extension.equals("jpg") || extension.equals("jpeg"))
				return UserFile.FileType.JPEG;
			else if (extension.equals("json"))
				return UserFile.FileType.JSON;
			else if (extension.equals("html") || extension.equals("htm"))
				return UserFile.FileType.HTML;
		}
		return UserFile.FileType.TEXT;
	}

	public FileType get_filetype() {
		return filetype_by_extension(the_file.getName());
	}

	public BufferedInputStream get_input_stream() throws SecurityException, FileNotFoundException, HTTPError {
		String filename = the_file.getName();

		if (filename.length() == 0)
			throw new HTTPError("filename not specified");

		return new BufferedInputStream(new FileInputStream(
				the_file));
	
			
	}

	public BufferedOutputStream get_output_stream() throws FileNotFoundException, HTTPError {
	
		String filename = the_file.getName();
	
		if (filename.length() == 0)
			throw new HTTPError("filename not specified");

		return new BufferedOutputStream(new FileOutputStream(the_file, true));
	
	

	}
}
