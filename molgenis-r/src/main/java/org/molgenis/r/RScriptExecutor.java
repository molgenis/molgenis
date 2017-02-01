package org.molgenis.r;

import org.molgenis.script.ScriptException;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

/**
 * Executes an R script using an Rserve
 */
@Service
public class RScriptExecutor
{
	private static final Logger LOG = LoggerFactory.getLogger(RScriptExecutor.class);

	private final RSettings rSettings;

	@Autowired
	public RScriptExecutor(RSettings rSettings)
	{
		this.rSettings = requireNonNull(rSettings);
	}

	String executeScript(String script, String outputFile)
	{
		RConnection rConnection = null;
		try
		{
			rConnection = new RConnection(rSettings.getHost(), rSettings.getPort());
			return executeScript(script, outputFile, rConnection);
		}
		catch (RserveException e)
		{
			throw new ScriptException(e);
		}
		finally
		{
			if (rConnection != null)
			{
				rConnection.close();
			}
		}
	}

	private String executeScript(String script, String outputFile, RConnection rConnection)
	{
		if (outputFile != null)
		{
			String randomFileName = generateRandomString();
			script = script.replace(outputFile, randomFileName); // TODO implement more robust solution
			if (!script.endsWith("\n"))
			{
				script += '\n';
			}
			script += "r=readBin('" + randomFileName + "','raw',1024*1024); unlink('" + randomFileName + "'); r";
		}

		REXP rExp = executeScript(script, rConnection);

		if (outputFile != null)
		{
			createFile(rExp, outputFile);
			return null;
		}
		else
		{
			try
			{
				return rExp.asString();
			}
			catch (REXPMismatchException e)
			{
				throw new ScriptException(e);
			}
		}
	}

	private REXP executeScript(String script, RConnection rConnection)
	{
		REXP rResponseObject;
		try
		{
			// see https://www.rforge.net/Rserve/faq.html#errors
			rConnection.assign(".tmp.", script);
			rResponseObject = rConnection.parseAndEval("try(eval(parse(text=.tmp.)),silent=TRUE)");
			if (rResponseObject.inherits("try-error"))
			{
				String prefix = "Error in parse(text = .tmp.) : <text>:";
				String errorMessage = rResponseObject.asString();
				if (errorMessage.startsWith(prefix))
				{
					errorMessage = errorMessage.substring(prefix.length());
				}
				throw new ScriptException(errorMessage);
			}
			return rResponseObject;
		}
		catch (REngineException e)
		{
			throw new ScriptException(e.getMessage());
		}
		catch (REXPMismatchException e)
		{
			throw new RuntimeException(e);
		}
	}

	private void createFile(REXP rExp, String outputFile)
	{
		byte[] bytes;
		try
		{
			bytes = rExp.asBytes();
		}
		catch (REXPMismatchException e)
		{
			throw new RuntimeException(e);
		}
		try
		{
			Files.write(new File(outputFile).toPath(), bytes);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private String generateRandomString()
	{
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
}
