package org.molgenis.omx.harmonization.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipFileUtil
{
	private static void copyInputStream(InputStream in, OutputStream out) throws IOException
	{
		byte[] buffer = new byte[1024];
		int len;

		while ((len = in.read(buffer)) >= 0)
			out.write(buffer, 0, len);

		in.close();
		out.close();
	}

	public static List<File> unzip(File file) throws FileNotFoundException, IOException
	{
		List<File> unzippedFiles = new ArrayList<File>();
		Enumeration<? extends ZipEntry> entries;
		ZipFile zipFile;

		zipFile = new ZipFile(file);
		entries = zipFile.entries();
		while (entries.hasMoreElements())
		{
			ZipEntry entry = (ZipEntry) entries.nextElement();
			if (entry.isDirectory())
			{
				System.err.println("Extracting directory: " + entry.getName());
				(new File(file.getParentFile(), entry.getName())).mkdir();
				continue;
			}
			System.err.println("Extracting file: " + entry.getName());
			File newFile = new File(file.getParent(), entry.getName());
			copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(newFile)));
			unzippedFiles.add(newFile);
		}
		zipFile.close();
		return unzippedFiles;
	}
}