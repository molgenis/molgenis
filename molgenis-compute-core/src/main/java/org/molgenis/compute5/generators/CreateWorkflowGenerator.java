package org.molgenis.compute5.generators;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;

public class CreateWorkflowGenerator
{
	private static final Logger LOG = Logger.getLogger(CreateWorkflowGenerator.class);
	public static final String WORKFlOW_NAME = "workflows/myworkflow";


	public CreateWorkflowGenerator(String createWorkflowDir)
	{
		File target = new File(createWorkflowDir);
		File file = new File(Thread.currentThread().getContextClassLoader().getResource(WORKFlOW_NAME).getFile());

		try
		{
			copyFolder(file, target.getAbsoluteFile());
			LOG.info("... Basic workflow structure is created");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	public static void copyFolder(File src, File dest) throws IOException
	{
		if (src.isDirectory())
		{

			// if directory not exists, create it
			if (!dest.exists())
			{
				dest.mkdirs();
			}

			// list all the directory contents
			String files[] = src.list();

			for (String file : files)
			{
				// construct the src and dest file structure
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				// recursive copy
				copyFolder(srcFile, destFile);
			}

		}
		else
		{
			// if file, then copy it
			// Use bytes stream to support all file types
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest);

			byte[] buffer = new byte[1024];

			int length;
			// copy the file content in bytes
			while ((length = in.read(buffer)) > 0)
			{
				out.write(buffer, 0, length);
			}

			in.close();
			out.close();
		}
	}
}