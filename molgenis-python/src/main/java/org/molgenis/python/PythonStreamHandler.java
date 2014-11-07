package org.molgenis.python;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class PythonStreamHandler implements Runnable
{
	private final InputStream in;
	private final PythonOutputHandler outputHandler;
	private final Thread thread = new Thread(this);

	public PythonStreamHandler(InputStream in, PythonOutputHandler outputHandler)
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
			BufferedReader br = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
			String line = null;
			while ((line = br.readLine()) != null)
			{
				outputHandler.outputReceived(line);
			}
		}
		catch (IOException e)
		{
			throw new MolgenisPythonException("Error reading python outputstream", e);
		}
	}

}
