package org.molgenis.gids.tools.convertor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConvertGidsMain
{

	public static void main(String[] args) throws IOException
	{
		final String DIRECTORY = args[0];
		final String OUTPUTDIR = DIRECTORY + args[1];
		final String PROJECTNAME = args[2];
		SampleConverter sample = new SampleConverter();
		InputStream is = new FileInputStream(DIRECTORY + PROJECTNAME + ".xls");

		File theDir = new File(OUTPUTDIR + PROJECTNAME);

		// if the directory does not exist, create it
		if (!theDir.exists())
		{
			boolean success = theDir.mkdir();

			if (success)
			{
				System.out.println("Directory: " + theDir + " created");
			}
			else
			{
				System.out.println("FAIL to make the directory");
			}
		}
		String newOutput = theDir + "/";
		OutputStream os = new FileOutputStream(newOutput + PROJECTNAME + "_Output.csv");
		sample.convert(is, os, newOutput, PROJECTNAME);
		System.out.println("Program is finished");

	}

}