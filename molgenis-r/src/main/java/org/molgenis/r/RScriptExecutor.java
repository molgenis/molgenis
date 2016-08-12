package org.molgenis.r;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

import static java.util.Objects.requireNonNull;

/**
 * Executes a r script with the RScript executable in a new process.
 */
@Service
public class RScriptExecutor
{
	private static final Logger LOG = LoggerFactory.getLogger(RScriptExecutor.class);

	private final String rScriptExecutable;
	/**
	 * Path to R libraries
	 */
	private final String rLibs;

	@Autowired
	public RScriptExecutor(@Value("${r_script_executable:/usr/bin/Rscript}") String rScriptExecutable,
			@Value("${r_libs:@null}") String rLibs)
	{
		this.rScriptExecutable = requireNonNull(rScriptExecutable);
		if (rLibs == null)
		{
			this.rLibs = System.getProperty("user.home") + File.separator + "r-packages";
		}
		else
		{
			this.rLibs = rLibs;
		}
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
			throw new MolgenisRException(
					"Can not execute [" + rScriptExecutable + "]. Does it have executable permissions?");
		}

		// Check if the r script exists
		if (!script.exists())
		{
			throw new MolgenisRException("File [" + script + "] does not exist");
		}

		try
		{
			// Create r process
			LOG.info("Running r script [" + script.getAbsolutePath() + "]");
			ProcessBuilder processBuilder = new ProcessBuilder(rScriptExecutable, script.getAbsolutePath());
			processBuilder.environment().put("R_LIBS", rLibs);
			Process process = processBuilder.start();

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

			LOG.info("Script [" + script.getAbsolutePath() + "] done");
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
