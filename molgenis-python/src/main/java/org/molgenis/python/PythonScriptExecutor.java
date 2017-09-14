package org.molgenis.python;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Executes a Python script with the Python version installed on server executable in a new process.
 */
@Service
public class PythonScriptExecutor
{
	private static final Logger LOG = LoggerFactory.getLogger(PythonScriptExecutor.class);

	private final String pythonScriptExecutable;

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
	public void executeScript(String pythonScript, PythonOutputHandler outputHandler)
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

		Path tempFile = null;
		try
		{
			tempFile = Files.createTempFile(null, ".py");
			Files.write(tempFile, pythonScript.getBytes(UTF_8), StandardOpenOption.WRITE);
			String tempScriptFilePath = tempFile.toAbsolutePath().toString();

			// Create r process
			LOG.info("Running python script [" + tempScriptFilePath + "]");
			Process process = Runtime.getRuntime().exec(pythonScriptExecutable + " " + tempScriptFilePath);

			// Capture the error output
			final StringBuilder sb = new StringBuilder();
			PythonStreamHandler errorHandler = new PythonStreamHandler(process.getErrorStream(),
					output -> sb.append(output).append("\n"));
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
				throw new MolgenisPythonException("Error running [" + tempScriptFilePath + "]." + sb.toString());
			}

			LOG.info("Script [" + tempScriptFilePath + "] done");
		}
		catch (IOException e)
		{
			throw new MolgenisPythonException("Exception executing PythonScipt.", e);
		}
		catch (InterruptedException e)
		{
			throw new MolgenisPythonException("Exception waiting for PythonScipt to finish", e);
		}
		finally
		{
			if (tempFile != null)
			{
				try
				{
					Files.delete(tempFile);
				}
				catch (IOException e)
				{
					LOG.error("", e);
				}
			}
		}
	}
}
