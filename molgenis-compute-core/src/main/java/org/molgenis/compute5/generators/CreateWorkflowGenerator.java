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
	public static final String WORKFOW_NAME = "myworkflow";


	public CreateWorkflowGenerator(String createWorkflowDir)
	{
		File target = new File(createWorkflowDir);

		try
		{
			Enumeration<URL> en=getClass().getClassLoader().getResources("workflows");
			if (en.hasMoreElements()) {
				URL metaInf=en.nextElement();
				File fileMetaInf=new File(metaInf.toURI());

				File[] files=fileMetaInf.listFiles();
				//or
				String[] filenames=fileMetaInf.list();

				int index = -1;
				for (int i = 0; i < filenames.length; i++)
				{
					//System.out.println("file " + filenames[i]);
					if(filenames[i].equalsIgnoreCase(WORKFOW_NAME))
						index = i;
				}

				if(index >= 0)
				{
					copyFolder(files[index], target.getAbsoluteFile());
					LOG.info("... Basic workflow structure is created");

				}
				else
					throw new Exception("The original workflow is absent");

			}

		}
		catch (Exception e)
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
