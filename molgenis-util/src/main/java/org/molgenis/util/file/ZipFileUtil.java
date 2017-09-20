package org.molgenis.util.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipFileUtil
{
	private static final Logger LOG = LoggerFactory.getLogger(ZipFileUtil.class);

	private ZipFileUtil()
	{
	}

	public static List<File> unzip(File file) throws IOException
	{
		List<File> unzippedFiles = new ArrayList<>();
		Enumeration<? extends ZipEntry> entries;
		try (ZipFile zipFile = new ZipFile(file))
		{
			entries = zipFile.entries();
			while (entries.hasMoreElements())
			{
				ZipEntry entry = entries.nextElement();
				if (entry.getName().startsWith(".") || entry.getName().startsWith("_"))
				{
					continue;
				}

				if (entry.isDirectory())
				{
					LOG.info("Extracting directory: " + entry.getName());
					if (!(new File(file.getParentFile(), entry.getName())).mkdir())
					{
						throw new RuntimeException("Failed to create directory");
					}
					continue;
				}
				LOG.info("Extracting directory: " + entry.getName());
				File newFile = new File(file.getParent(), entry.getName());

				try (InputStream in = zipFile.getInputStream(entry);
						OutputStream out = new BufferedOutputStream(new FileOutputStream(newFile)))
				{
					StreamUtils.copy(in, out);
				}

				unzippedFiles.add(newFile);
			}
		}
		return unzippedFiles;
	}
}