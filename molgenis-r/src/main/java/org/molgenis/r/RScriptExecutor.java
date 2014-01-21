package org.molgenis.r;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.apache.log4j.Logger;
import org.springframework.util.FileCopyUtils;

/**
 * Executes a r script with the RScript executable in a new process.
 * 
 */
public class RScriptExecutor
{
	private static final Logger logger = Logger.getLogger(RScriptExecutor.class);
	private final String rScriptExecutable;

	public RScriptExecutor(String rScriptExecutable)
	{
		if (rScriptExecutable == null)
		{
			throw new IllegalArgumentException("rExecutable is null");
		}

		this.rScriptExecutable = rScriptExecutable;
	}

	public void executeScript(String script, ROutputHandler outputHandler)
	{
		File file;
		try
		{
			file = File.createTempFile("molgenis", "r");
		}
		catch (IOException e)
		{
			throw new MolgenisRException("Exception creating temp file", e);
		}

		try
		{
			FileCopyUtils.copy(script, new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8")));
		}
		catch (IOException e)
		{
			throw new MolgenisRException("Exception writing r script to temp file [" + file + "]", e);
		}

		executeScript(file, outputHandler);
	}

	/**
	 * Execute a r script and wait for it to finish
	 */
	public void executeScript(File script, ROutputHandler outputHandler)
	{
		// Check if r is installed
		File file = new File(rScriptExecutable);
		if (!file.exists())
		{
			throw new MolgenisRException("File [" + rScriptExecutable + "] does not exist");
		}

		// Check if r has execution rights
		if (!file.canExecute())
		{
			throw new MolgenisRException("Can not execute [" + rScriptExecutable
					+ "]. Does it have executable permissions?");
		}

		// Check if the r script exists
		if (!script.exists())
		{
			throw new MolgenisRException("File [" + script + "] does not exist");
		}

		try
		{
			// Create r process
			logger.info("Running r script [" + script.getAbsolutePath() + "]");
			Process process = Runtime.getRuntime().exec(rScriptExecutable + " " + script.getAbsolutePath());

			// Capture the error output
			final StringBuilder sb = new StringBuilder();
			RStreamHandler errorHandler = new RStreamHandler(process.getErrorStream(), new ROutputHandler()
			{
				@Override
				public void outputReceived(String output)
				{
					sb.append(output).append("\n");
				}
			});
			errorHandler.start();

			// Capture r output if an r output handler is defined
			if (outputHandler != null)
			{
				RStreamHandler streamHandler = new RStreamHandler(process.getInputStream(), outputHandler);
				streamHandler.start();
			}

			// Wait until script is finished
			process.waitFor();

			// Check for errors
			if (process.exitValue() > 0)
			{
				throw new MolgenisRException("Error running [" + script.getAbsolutePath() + "]." + sb.toString());
			}

			logger.info("Script [" + script.getAbsolutePath() + "] done");
		}
		catch (IOException e)
		{
			throw new MolgenisRException("Exception executing RScipt.", e);
		}
		catch (InterruptedException e)
		{
			throw new MolgenisRException("Exception waiting for RScipt to finish", e);
		}
	}
}
