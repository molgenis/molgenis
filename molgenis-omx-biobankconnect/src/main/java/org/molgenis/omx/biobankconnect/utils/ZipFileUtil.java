package org.molgenis.omx.biobankconnect.utils;

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

import org.apache.log4j.Logger;

public class ZipFileUtil
{
	private static final Logger logger = Logger.getLogger(ZipFileUtil.class);

	private static void copyInputStream(InputStream in, OutputStream out) throws IOException
	{
		byte[] buffer = new byte[1024];
		int len;
		try
		{
			while ((len = in.read(buffer)) >= 0)
				out.write(buffer, 0, len);
		}
		finally
		{
			if (in != null) in.close();
			if (out != null) out.close();
		}
	}

	public static List<File> unzip(File file) throws FileNotFoundException, IOException
	{

		List<File> unzippedFiles = new ArrayList<File>();
		Enumeration<? extends ZipEntry> entries;
		ZipFile zipFile = null;
		try
		{
			zipFile = new ZipFile(file);
			entries = zipFile.entries();
			while (entries.hasMoreElements())
			{
				ZipEntry entry = (ZipEntry) entries.nextElement();
				if (entry.isDirectory())
				{
					logger.info("Extracting directory: " + entry.getName());
					(new File(file.getParentFile(), entry.getName())).mkdir();
					continue;
				}
				logger.info("Extracting directory: " + entry.getName());
				File newFile = new File(file.getParent(), entry.getName());
				copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(newFile)));
				unzippedFiles.add(newFile);
			}
		}
		finally
		{
			if (zipFile != null) zipFile.close();
		}
		return unzippedFiles;
	}
}