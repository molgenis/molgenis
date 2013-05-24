package org.molgenis.compute5.generators;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CreateWorkflowGenerator
{

	public CreateWorkflowGenerator(String createWorkflowDir)
	{
		File target = new File(createWorkflowDir);

		String sourcePath = "workflows/myworkflow";
		File source = new File(sourcePath);

		if (!source.exists())
		{
			System.err.println(">> ERROR >> Directory '" + source.toString()
					+ "' not found. Please add molgenis-compute-core/");
			System.err.println("Exit with code 1.");
			System.exit(1);
		}
		else try
		{
			copyFolder(source, target);
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
				dest.mkdir();
				System.out.println("Directory copied from " + src + "  to " + dest);
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
			System.out.println("File copied from " + src + " to " + dest);
		}
	}
}
