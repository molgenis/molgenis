package org.molgenis.compute5.generators;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.molgenis.generators.doc.DotDocGen;

public class GraphvizUtils
{
	private static final Logger logger = Logger.getLogger(DotDocGen.class);

	// need to add input and output file
	public final static String GRAPHVIZ_COMMAND_WINDOWS = "dot";

	public static void executeDot(File dotFile, String type, boolean wait) throws IOException
	{
		// write script to disc
		String command = "";
		// String error = "";
		String result = "";
		// String output = "";
		// File inputfile = null;
		// File outputfile = null;
		try
		{

			// execute the scripts
			// if
			// (System.getProperty("os.name").toLowerCase().indexOf("windows")
			// == -1)
			// {
			// // make tempfiles executable
			// // command = "chmod 777 "+inputfile.getCanonicalPath()+"\n";
			// // logger.debug("added chmod 777 on input file");
			// command += GRAPHVIZ_COMMAND_WINDOWS;
			// }
			// else
			// windows
			// command flags infile outfile
			command += "" + GRAPHVIZ_COMMAND_WINDOWS + " -T" + type + " -O \"" + dotFile.getAbsolutePath() + "\"";

			Process p;
			String os = System.getProperty("os.name").toLowerCase();

			if (os.indexOf("windows 9") > -1)
			{
				p = Runtime.getRuntime().exec(new String[]
				{ "command.com", "/c", command });
			}
			else if (os.indexOf("windows") > -1)
			{
				p = Runtime.getRuntime().exec(new String[]
				{ "cmd.exe", "/c", command });
			}
			else
			{
				p = Runtime.getRuntime().exec(new String[]
				{ "/bin/sh", "-c", command });
			}

			logger.debug("Executing: " + command);
			if (wait) p.waitFor();
			logger.debug("Data model image was generated succesfully.\nOutput:\n" + result);

			{
				// command flags infile outfile
				command = "" + GRAPHVIZ_COMMAND_WINDOWS + " -Tsvg" + " -O \"" + dotFile.getAbsolutePath() + "\"";
			}
			logger.debug("Executing: " + command);

			if (os.indexOf("windows 9") > -1)
			{
				p = Runtime.getRuntime().exec(new String[]
				{ "command.com", "/c", command });
			}
			else if (os.indexOf("windows") > -1)
			{
				p = Runtime.getRuntime().exec(new String[]
				{ "cmd.exe", "/c", command });
			}
			else
			{
				p = Runtime.getRuntime().exec(new String[]
				{ "/bin/sh", "-c", command });
			}
			if (wait) p.waitFor();
			
			logger.debug("Data model image was generated succesfully.\nOutput:\n" + result);

		}
		catch (IOException e)
		{
			throw new IOException("Generation of graphical documentation failed: return code " + e.getMessage()
					+ ". Install GraphViz and put dot.exe on your path.");
		}
		catch (InterruptedException e)
		{
			throw new IOException("Generation of graphical documentation failed: return code " + e.getMessage()
					+ ". Install GraphViz and put dot.exe on your path.");
		}
		finally
		{
			// inputfile.delete();
			// outputfile.delete();
		}
	}
}
