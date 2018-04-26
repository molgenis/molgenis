package org.molgenis.gavin.job.input;

import org.slf4j.Logger;

import java.io.*;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Writes lines to file.
 */
public class LineSink implements Consumer<String>, Closeable
{
	private final static Logger LOG = getLogger(LineSink.class);
	private final File file;
	private BufferedWriter writer;

	public LineSink(File file)
	{
		this.file = file;
	}

	@Override
	public void accept(String line)
	{
		try
		{
			if (writer == null)
			{
				writer = createWriter(file);
			}
			writer.write(line);
			writer.newLine();
		}
		catch (IOException ex)
		{
			LOG.error("Failed to write line to file {}.", file.getAbsolutePath(), ex);
		}
	}

	private BufferedWriter createWriter(File file) throws IOException
	{
		LOG.debug("Creating LineSink for {}.", file.getAbsolutePath());
		return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
	}

	@Override
	public void close() throws IOException
	{
		if (writer != null)
		{
			LOG.debug("Closing LineSink for {}.", file.getAbsolutePath());
			writer.close();
		}
	}
}
