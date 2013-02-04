package org.molgenis.util.plink.drivers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.molgenis.util.TextFileUtils;
import org.molgenis.util.plink.PlinkFileParser;
import org.molgenis.util.plink.datatypes.Biallele;
import org.molgenis.util.plink.datatypes.TpedEntry;

/**
 * Driver to query TPED files.
 */
public class TpedFileDriver implements PlinkFileParser
{
	private BufferedReader reader;
	private File file;
	private char separator;
	private long nrElements;

	/**
	 * Construct a TpedFileDriver on this file
	 * 
	 * @param tpedFile
	 * @throws Exception
	 */
	public TpedFileDriver(File tpedFile)
	{
		this(tpedFile, DEFAULT_FIELD_SEPARATOR);
	}

	public TpedFileDriver(File tpedFile, char separator)
	{
		if (tpedFile == null) throw new IllegalArgumentException("file is null");
		this.file = tpedFile;
		this.separator = separator;
		this.nrElements = -1l;
	}

	/**
	 * Get a specific set of TPED file entries
	 * 
	 * @param from
	 *            = inclusive
	 * @param to
	 *            = exclusive
	 * @return
	 * @throws Exception
	 */
	public List<TpedEntry> getEntries(final long from, final long to) throws IOException
	{
		reset();

		List<TpedEntry> entryList = new ArrayList<TpedEntry>();
		String line;
		for (int i = 0; (line = reader.readLine()) != null && i < to; ++i)
			if (i >= from) entryList.add(parseEntry(line));

		return entryList;
	}

	/**
	 * Get all TPED file entries
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<TpedEntry> getAllEntries() throws IOException
	{
		reset();

		List<TpedEntry> entryList = new ArrayList<TpedEntry>();
		String line;
		while ((line = reader.readLine()) != null)
			entryList.add(parseEntry(line));

		return entryList;
	}

	private TpedEntry parseEntry(String line) throws IOException
	{
		StringTokenizer strTokenizer = new StringTokenizer(line, separator + "");
		try
		{
			String chromosome = strTokenizer.nextToken();
			String snp = strTokenizer.nextToken();
			double cM = Double.parseDouble(strTokenizer.nextToken());
			long bpPos = Long.parseLong(strTokenizer.nextToken());
			List<Biallele> bialleles = new ArrayList<Biallele>();
			while (strTokenizer.hasMoreTokens())
			{
				char allele1 = strTokenizer.nextToken().charAt(0);
				char allele2 = strTokenizer.nextToken().charAt(0);
				bialleles.add(Biallele.create(allele1, allele2));
			}
			return new TpedEntry(chromosome, snp, cM, bpPos, bialleles);
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
}
