package org.molgenis.r;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static java.nio.charset.StandardCharsets.UTF_8;

public class RStreamHandler implements Runnable
{
	private final InputStream in;
	private final ROutputHandler outputHandler;
	private final Thread thread = new Thread(this);

	public RStreamHandler(InputStream in, ROutputHandler outputHandler)
	{
		this.in = in;
		this.outputHandler = outputHandler;
	}

	public void start()
	{
		thread.start();
	}

	@Override
	public void run()
	{
		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(in, UTF_8));
			String line = null;
			while ((line = br.readLine()) != null)
			{
				outputHandler.outputReceived(line);
			}
		}
		catch (IOException e)
		{
			throw new MolgenisRException("Error reading R outputstream", e);
		}
	}

}
