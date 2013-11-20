package org.molgenis.r;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.springframework.util.FileCopyUtils;

/**
 * Executes a r script with the RScript executable in a new process.
 * 
 */
public class RScriptExecutor
{
	private final String rScriptExecutable;

	public RScriptExecutor(String rScriptExecutable)
	{
		if (rScriptExecutable == null)
		{
			throw new IllegalArgumentException("rExecutable is null");
		}

		File file = new File(rScriptExecutable);
		if (!file.exists())
		{
			throw new MolgenisRException("File [" + rScriptExecutable + "] does not exist");
		}

		if (!file.canExecute())
		{
			throw new MolgenisRException("Can not execute [" + rScriptExecutable
					+ "]. Does it have executable permissions?");
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
			FileCopyUtils.copy(script, new FileWriter(file));
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
		if (!script.exists())
		{
			throw new MolgenisRException("File [" + script + "] does not exist");
		}

		try
		{
			Process process = Runtime.getRuntime().exec(rScriptExecutable + " " + script.getAbsolutePath());

			if (outputHandler != null)
			{
				RStreamHandler streamHandler = new RStreamHandler(process.getInputStream(), outputHandler);
				streamHandler.start();
			}

			process.waitFor();
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

	public static void main(String[] args)
	{
		RScriptExecutor r = new RScriptExecutor("/usr/bin/Rscript");

		r.executeScript(new File("/Users/erwin/projects/molgenis/molgenis-r/src/main/resources/heatmap.r"),
				new ROutputHandler()
				{
					@Override
					public void outputReceived(String output)
					{
						System.out.println(output);
					}

				});
	}

}
