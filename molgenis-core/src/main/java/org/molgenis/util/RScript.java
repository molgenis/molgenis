package org.molgenis.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.apache.log4j.Logger;

/**
 * This class provides an handle to an R script
 * <ul>
 * <li>it has a static path to the R command
 * <li>it can start the script
 * <li>it can create a temp file</li>
 * </ul>
 * 
 * @author P219083
 * 
 */
public class RScript
{
	/** command to run R */
	public String R_COMMAND = "R CMD BATCH --vanilla --slave";

	/** logger */
	private Logger logger = Logger.getLogger(RScript.class.getSimpleName());

	/** buffer containing the script */
	private StringBuffer script = new StringBuffer();

	/** here the error messages will be stored after execution */
	private String error;

	/** here the output messages will be stored after execution */
	private String output;

	/** here the result of the script */
	private String result;

	public String getErrors()
	{
		return error;
	}

	public String getOutput()
	{
		return output;
	}

	public String getResult()
	{
		return result;
	}

	public String getR_COMMAND()
	{
		return R_COMMAND;
	}

	public void setR_COMMAND(String r_COMMAND)
	{
		R_COMMAND = r_COMMAND;
	}

	/** Construct an R script object */
	public RScript()
	{
		// add some basic commands to configure output of the script.
		// this.append("options(echo=FALSE)");
		// this.append("options(warn=-1)");
	}

	/**
	 * Add a command to the r script.
	 * 
	 * @param command
	 */
	public void append(String command)
	{
		script.append(command + System.getProperty("line.separator"));
	}

	/**
	 * Add a command to the r script as a template
	 * 
	 * @param command
	 * @param args
	 *            a variable list of arguments
	 */
	public void append(String command, Object... args)
	{
		script.append(String.format(command, args) + System.getProperty("line.separator"));
	}

	/**
	 * Execute the R script.
	 * 
	 * @return the output of the script
	 * @throws RScriptException
	 */
	public String execute() throws RScriptException
	{
		return execute(null);
	}

	/**
	 * Execute the R script. Extra parameter for specifying file name.
	 * 
	 * @return the output of the script
	 * @throws RScriptException
	 */
	public String execute(String scriptPathName) throws RScriptException
	{
		// add end to script script
		this.append("q(\"no\",status=0, FALSE)");

		String scriptCode = this.script.toString();

		// write script to disc
		String command = "";
		File inputfile = null;
		File outputfile = null;
		try
		{
			// create tempfiles
			if (scriptPathName == null)
			{
				inputfile = File.createTempFile("run", ".R");
			}
			else
			{
				inputfile = new File(scriptPathName);
			}
			outputfile = File.createTempFile("run", ".output");

			Writer fw = new OutputStreamWriter(new FileOutputStream(inputfile), Charset.forName("UTF-8"));
			try
			{
				fw.write(scriptCode);
			}
			finally
			{
				fw.close();
			}
			System.out.println("wrote script to file " + inputfile);

			// execute the scripts
			if (System.getProperty("os.name").toLowerCase().indexOf("windows") == -1)
			{
				// make tempfiles executable
				// command = "chmod 777 "+inputfile.getCanonicalPath()+"\n";
				// logger.debug("added chmod 777 on input file");
				command += R_COMMAND + " " + inputfile.getCanonicalPath() + " " + outputfile.getCanonicalPath();
			}
			else
			// windows
			{
				command += R_COMMAND + " \"" + inputfile.getCanonicalPath() + "\" \"" + outputfile.getCanonicalPath()
						+ "\"";
			}
			logger.debug("Executing: " + command);
			Process process = Runtime.getRuntime().exec(command);
			process.waitFor();

			// get error messages
			error = this.streamToString(process.getErrorStream());
			if (error.length() > 0)
			{
				logger.error("R script printed errors: " + error);
			}

			// get output messages
			output = this.streamToString(process.getInputStream());
			if (error.length() > 0)
			{
				logger.debug("R script printed messages: " + output);
			}

			result = this.streamToString(new FileInputStream(outputfile));

			// check exit value
			if (process.exitValue() > 0)
			{
				throw new Exception("" + process.exitValue());
			}
		}
		catch (Exception e)
		{
			logger.debug("Script failed: return code " + e.getMessage() + "\nScript:\n" + scriptCode + "\nOutput:\n"
					+ result);
			// throw new RScriptException(result + "\n\nScript:\n" +
			// scriptCode);
			throw new RScriptException(result);
		}
		finally
		{
			// inputfile.delete();
			// outputfile.delete();
		}
		logger.debug("Script completed succesfully.\nScript:\n" + scriptCode + "\nOutput:\n" + result);

		return result;
	}

	/** Helper function to copy a file */
	public void copyFile(String fromPath, String toPath) throws Exception
	{
		FileInputStream fis = new FileInputStream(fromPath);
		try
		{
			FileOutputStream fos = new FileOutputStream(toPath);
			try
			{
				byte[] buf = new byte[1024];
				int i = 0;
				while ((i = fis.read(buf)) != -1)
				{
					fos.write(buf, 0, i);
				}
			}
			finally
			{
				fos.close();
			}
		}
		finally
		{
			fis.close();
		}
	}

	/** Helper function to delete a file */
	public void deleteFile(String path) throws Exception
	{
		try
		{
			File toDelete = new File(path);
			toDelete.delete();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Show the contents of the R script that has been created
	 */
	@Override
	public String toString()
	{
		return this.script.toString();
	}

	/** Helper function to translate streams to strings */
	private String streamToString(InputStream inputStream) throws IOException
	{
		StringBuffer fileContents = new StringBuffer();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));

		String line;
		while ((line = reader.readLine()) != null)
		{
			fileContents.append(line + "\n");
		}
		reader.close();
		inputStream.close();
		return fileContents.toString();
	}
}
