package org.molgenis.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.util.FileCopyUtils;

public class MySqlFileUtil
{
	private static final Logger logger = Logger.getLogger(MySqlFileUtil.class);

	public static String getMySqlQueryFromFile(Class<?> clazzPath, String nameResource)
	{
		final String pathname = clazzPath.getResource(nameResource).getFile();
		try
		{
			final File f = new File(pathname);
			BufferedReader bufferedReader = new BufferedReader(new FileReader(f.toString()));
			return FileCopyUtils.copyToString(bufferedReader);
		}
		catch (IOException e)
		{
			logger.error("A problem reading the query from path: " + pathname, e);
		}
		return null;
	}
}
