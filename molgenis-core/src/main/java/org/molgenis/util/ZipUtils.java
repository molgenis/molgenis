package org.molgenis.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils
{

	public enum DirectoryStructure
	{
		/** includes path of files in zip */
		INCLUDE_DIR
		{
			@Override
			String getPath(File f)
			{
				return f.getAbsolutePath();
			}
		},
		/** exclude path of files in zip */
		EXCLUDE_DIR
		{
			@Override
			String getPath(File f)
			{
				return f.getName();
			}
		};
		abstract String getPath(File f);
	}

	public static void compress(List<File> files, File outputZIP, DirectoryStructure ds) throws IOException
	{
		if (outputZIP.isDirectory())
		{
			throw new IllegalArgumentException(String.format("Zip outputfile can't be a directory. %s ", outputZIP));
		}

		final int BUFFER = 2048;

		BufferedInputStream origin = null;
		FileOutputStream dest = new FileOutputStream(outputZIP.getAbsolutePath());
		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
		// out.setMethod(ZipOutputStream.DEFLATED);
		byte data[] = new byte[BUFFER];
		// get a list of files from current directory

		for (File f : files)
		{
			FileInputStream fi = new FileInputStream(f);
			origin = new BufferedInputStream(fi, BUFFER);
			ZipEntry entry = new ZipEntry(ds.getPath(f));
			out.putNextEntry(entry);
			int count;
			while ((count = origin.read(data, 0, BUFFER)) != -1)
			{
				out.write(data, 0, count);
			}
			origin.close();
		}
		out.close();
	}
}
