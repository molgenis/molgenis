package org.molgenis.util.plink.readers;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Iterator;

import org.molgenis.util.plink.datatypes.MapEntry;
import org.molgenis.util.plink.drivers.MapFileDriver;

public class MapFileReader implements Closeable, Iterable<MapEntry>
{
	private static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");
	private final BufferedReader reader;
	private final char separator;

	public MapFileReader(InputStream in, char separator)
	{
		reader = new BufferedReader(new InputStreamReader(in, CHARSET_UTF8));
		this.separator = separator;
	}

	@Override
	public void close() throws IOException
	{
		reader.close();
	}

	@Override
	public Iterator<MapEntry> iterator()
	{
		return new MapFileIterator();
	}

	private class MapFileIterator implements Iterator<MapEntry>
	{
		private String line;

		public MapFileIterator()
		{
			try
			{
				line = reader.readLine();
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		public boolean hasNext()
		{
			return line != null;
		}

		@Override
		public MapEntry next()
		{
			MapEntry entry;
			try
			{
				entry = MapFileDriver.parseEntry(line, separator + "");
				line = reader.readLine();
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}

			return entry;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

	}

}
