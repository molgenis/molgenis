package org.molgenis.gavin.job.input;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

/**
 * Files methods, adapted to also work for {@link GZIPInputStream}.
 */
public class Files
{
	public static Stream<String> getLines(Path path, Charset cs) throws IOException
	{
		BufferedReader br = createBufferedReader(path, cs);
		try
		{
			return br.lines().onClose(asUncheckedRunnable(br));
		}
		catch (Error | RuntimeException e)
		{
			try
			{
				br.close();
			}
			catch (IOException ex)
			{
				try
				{
					e.addSuppressed(ex);
				}
				catch (Throwable ignore)
				{
				}
			}
			throw e;
		}
	}

	/**
	 * Convert a Closeable to a Runnable by converting checked IOException
	 * to UncheckedIOException
	 */
	private static Runnable asUncheckedRunnable(Closeable c)
	{
		return () ->
		{
			try
			{
				c.close();
			}
			catch (IOException e)
			{
				throw new UncheckedIOException(e);
			}
		};
	}

	/**
	 * Creates a {@link BufferedReader} for an input file with specified encoding.
	 * Unzips it if the file name ends with "gz".
	 *
	 * @param path the input {@link Path}
	 * @return the {@link BufferedReader}
	 * @throws IOException if the file cannot be read
	 */
	private static BufferedReader createBufferedReader(Path path, Charset cs) throws IOException
	{
		InputStream in = java.nio.file.Files.newInputStream(path);
		if (path.toString().toLowerCase().endsWith("gz"))
		{
			in = new GZIPInputStream(in);
		}
		return new BufferedReader(new InputStreamReader(in, cs));
	}
}
