package org.molgenis.util.trityper.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * 
 * @author harmjan
 */
public class Gpio
{

	public static String[] getListOfFiles(String dir, String extension)
	{

		File loc = new File(dir);
		String[] fileList = loc.list();

		ArrayList<String> al = new ArrayList<String>();

		for (String fileloc : fileList)
		{
			File file = new File(fileloc);
			if (!file.isDirectory())
			{
				String fileName = file.getName();
				int mid = fileName.lastIndexOf(".");
				String fname = fileName.substring(0, mid);
				String ext = fileName.substring(mid + 1, fileName.length());

				System.out.println(ext);
				if (ext.toLowerCase().equals(extension))
				{
					al.add(loc.getAbsolutePath() + "/" + fileName);
				}
			}
		}

		String[] output = new String[al.size()];
		int i = 0;
		for (String item : al)
		{
			output[i] = al.get(i);
			i++;
		}

		loc = null;

		return output;

	}

	public static void createDir(String dirName) throws IOException
	{
		if (!exists(dirName))
		{

			// Create one directory
			boolean success = (new File(dirName)).mkdirs();
			if (success)
			{
				System.out.println("Directory: " + dirName + " created");
			}

		}
	}

	public static boolean isDir(String dir)
	{
		File loc = new File(dir);
		if (loc.isDirectory())
		{
			loc = null;
			return true;
		}
		else
		{
			loc = null;
			return false;
		}
	}

	public static boolean exists(String dir)
	{
		File file = new File(dir);
		boolean exists = file.exists();
		boolean readable = file.canRead();
		return exists & readable;
	}

	public static void copyFile(File sourceFile, File destFile) throws IOException
	{
		if (!destFile.exists())
		{
			boolean exists = destFile.createNewFile();
			if (exists) throw new IOException(destFile + " already exists");
		}

		FileChannel source = null;
		FileChannel destination = null;
		try
		{
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		}
		finally
		{
			if (source != null)
			{
				source.close();
			}
			if (destination != null)
			{
				destination.close();
			}
		}
	}

	public static String[] getListOfFiles(String dir)
	{
		File loc = new File(dir);
		String[] fileList = loc.list();
		return fileList;
	}

}
