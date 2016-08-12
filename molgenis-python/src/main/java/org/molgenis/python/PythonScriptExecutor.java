package org.molgenis.python;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * Executes a Python script with the Python version installed on server executable in a new process.
 */
@Service
public class PythonScriptExecutor
{
	private static final Logger LOG = LoggerFactory.getLogger(PythonScriptExecutor.class);

	private final String pythonScriptExecutable;

	@Autowired
	public PythonScriptExecutor(@Value("${python_script_executable:/usr/bin/python}") String pythonScriptExecutable)
	{
		if (pythonScriptExecutable == null)
		{
			throw new IllegalArgumentException("pythonExecutable is null");
		}

		this.pythonScriptExecutable = pythonScriptExecutable;
	}

	/**
	 * Execute a python script and wait for it to finish
	 */
	public void executeScript(File script, PythonOutputHandler outputHandler)
	{
		// Check if Python is installed
		File file = new File(pythonScriptExecutable);
		if (!file.exists())
		{
			throw new MolgenisPythonException("File [" + pythonScriptExecutable + "] does not exist");
		}

		// Check if Python has execution rights
		if (!file.canExecute())
		{
			throw new MolgenisPythonException(
					"Can not execute [" + pythonScriptExecutable + "]. Does it have executable permissions?");
		}

		// Check if the Pyhton script exists
		if (!script.exists())
		{
			throw new MolgenisPythonException("File [" + script + "] does not exist");
		}

		try
		{
			// Create r process
			LOG.info("Running python script [" + script.getAbsolutePath() + "]");
			Process process = Runtime.getRuntime().exec(pythonScriptExecutable + " " + script.getAbsolutePath());

			// Capture the error output
			final StringBuilder sb = new StringBuilder();
			PythonStreamHandler errorHandler = new PythonStreamHandler(process.getErrorStream(),
					new PythonOutputHandler()
					{
						@Override
						public void outputReceived(String output)
						{
							sb.append(output).append("\n");
						}
					});
			errorHandler.start();

			// Capture r output if an Python output handler is defined
			if (outputHandler != null)
			{
				PythonStreamHandler streamHandler = new PythonStreamHandler(process.getInputStream(), outputHandler);
				streamHandler.start();
			}

			// Wait until script is finished
			process.waitFor();

			// Check for errors
			if (process.exitValue() > 0)
			{
				throw new MolgenisPythonException("Error running [" + script.getAbsolutePath() + "]." + sb.toString());
			}

			LOG.info("Script [" + script.getAbsolutePath() + "] done");
		}
		catch (IOException e)
		{
			throw new MolgenisPythonException("Exception executing PythonScipt.", e);
		}
		catch (InterruptedException e)
		{
			throw new MolgenisPythonException("Exception waiting for PythonScipt to finish", e);
		}
	}
}
