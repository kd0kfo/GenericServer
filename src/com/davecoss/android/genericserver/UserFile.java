package com.davecoss.android.genericserver;

import java.io.File;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

// TODO: Consider using polymorphism instead.
public class UserFile {
	public enum FileType {
		HTML, TEXT, JSON, JPEG
	}
	
	public static final long MAX_OUTFILE_SIZE = 33554432L;
	private BufferedOutputStream outstream = null;

	private File the_file;

	public UserFile(File file) {
		this.the_file = file;	
	}

	public String get_parent() {
		return the_file.getParent();
	}
	
	public String get_absolute_path() {
		return the_file.getAbsolutePath();
	}

	public String get_filename() {
		return the_file.getName();
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

	public void init_output() throws IOException, FileNotFoundException, HTTPError {
		String filename = the_file.getName();
		
		if (filename.length() == 0)
			throw new HTTPError("filename not specified");
		
		if(this.outstream != null)
			this.outstream.close();
		this.outstream = new BufferedOutputStream(new FileOutputStream(the_file, true));
	}
	
	public void flush() throws IOException {
		if(outstream == null)
			return;
		outstream.flush();
	}
	
	public void close() throws IOException {
		if(outstream == null)
			return;
		
		outstream.close();
	}
	
	public void write(byte[] bytes, int offset, int len) 
			throws FileError, HTTPError, IOException {
	
		String filename = the_file.getName();
	
		if (filename.length() == 0)
			throw new HTTPError("filename not specified");
		
		long file_size = the_file.length();
		if(file_size + len >= MAX_OUTFILE_SIZE)
			throw new FileError("Max file size already reached");
		
		if(outstream == null)
			init_output();
		
		outstream.write(bytes, offset, len);
		
	}
}
