package org.molgenis.util.plink.drivers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.molgenis.util.TextFileUtils;
import org.molgenis.util.plink.PlinkFileParser;
import org.molgenis.util.plink.datatypes.MapEntry;

/**
 * Driver to query MAP files. By default, each line of the MAP file describes a
 * single marker and must contain exactly 4 columns. Content of a MAP file:
 * chromosome, SNP, cM, base-position. See:
 * http://pngu.mgh.harvard.edu/~purcell/plink/data.shtml#map
 */
public class MapFileDriver implements PlinkFileParser, Iterable<MapEntry>
{
	private BufferedReader reader;
	private final File file;
	private final char separator;
	private long nrElements;

	/**
	 * Construct a MapFileDriver on this file
	 * 
	 * @param mapFile
	 * @throws Exception
	 */
	public MapFileDriver(File mapFile)
	{
		this(mapFile, DEFAULT_FIELD_SEPARATOR);
	}

	public MapFileDriver(File mapFile, char separator)
	{
		if (mapFile == null) throw new IllegalArgumentException("file is null");
		this.file = mapFile;
		this.separator = separator;
		this.nrElements = -1l;
	}

	/**
	 * Get a specific set of MAP file entries
	 * 
	 * @param from
	 *            = inclusive
	 * @param to
	 *            = exclusive
	 * @return
	 * @throws Exception
	 */
	public List<MapEntry> getEntries(final long from, final long to) throws IOException
	{
		reset();

		List<MapEntry> entryList = new ArrayList<MapEntry>();
		String line;
		for (int i = 0; (line = reader.readLine()) != null && i < to; ++i)
			if (i >= from) entryList.add(parseEntry(line, this.separator + ""));

		return entryList;
	}

	/**
	 * Get all MAP file entries
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<MapEntry> getAllEntries() throws IOException
	{
		reset();

		List<MapEntry> entryList = new ArrayList<MapEntry>();
		String line;
		while ((line = reader.readLine()) != null)
			entryList.add(parseEntry(line, this.separator + ""));

		return entryList;
	}

	public static MapEntry parseEntry(String line, String separator) throws IOException
	{
		StringTokenizer strTokenizer = new StringTokenizer(line, separator);
		try
		{
			String chromosome = strTokenizer.nextToken();
			String snp = strTokenizer.nextToken();
			double cM = Double.parseDouble(strTokenizer.nextToken());
			long bpPos = Long.parseLong(strTokenizer.nextToken());
			return new MapEntry(chromosome, snp, cM, bpPos);
		}
		catch (NoSuchElementException e)
		{
			throw new IOException("error in line: " + line, e);
		}
		catch (IndexOutOfBoundsException e)
		{
			throw new IOException("error in line: " + line, e);
		}
		catch (NumberFormatException e)
		{
			throw new IOException("error in line: " + line, e);
		}
	}

	public long getNrOfElements() throws IOException
	{
		if (nrElements == -1) nrElements = TextFileUtils.getNumberOfNonEmptyLines(file, FILE_ENCODING);
		return nrElements;
	}

	@Override
	public void close() throws IOException
	{
		if (this.reader != null) this.reader.close();
	}

	public void reset() throws IOException
	{
		if (this.reader != null) close();
		this.reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), FILE_ENCODING));
	}

	@Override
	public Iterator<MapEntry> iterator()
	{
		try
		{
			reset();
			return new MapFileIterator();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
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
				entry = parseEntry(line, separator + "");
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
