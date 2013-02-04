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
import org.molgenis.util.plink.datatypes.PedEntry;

/**
 * Driver to query PED files. A PED file contains family- and genotyping data
 * for an individual, plus a single phenotype. Basically it is a FAM file with
 * added genotyping (typically SNP) data. However, the example file is a bit
 * peculiar: it has 'null' columns because of additional spacing between some
 * data values. This makes parsing hard. Question: can all Plink files have
 * this? or just PED? See:
 * http://pngu.mgh.harvard.edu/~purcell/plink/data.shtml#ped
 */
public class PedFileDriver implements PlinkFileParser
{
	private BufferedReader reader;
	private File file;
	private char separator;
	private long nrElements;

	/**
	 * Construct a PedFileDriver on this file
	 * 
	 * @param bimFile
	 * @throws Exception
	 */
	public PedFileDriver(File pedFile)
	{
		this(pedFile, DEFAULT_FIELD_SEPARATOR);
	}

	public PedFileDriver(File pedFile, char separator)
	{
		if (pedFile == null) throw new IllegalArgumentException("file is null");
		this.file = pedFile;
		this.separator = separator;
		this.nrElements = -1l;
	}

	/**
	 * Get all PED file entries
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<PedEntry> getAllEntries() throws IOException
	{
		reset();

		List<PedEntry> entryList = new ArrayList<PedEntry>();
		String line;
		while ((line = reader.readLine()) != null)
			entryList.add(parseEntry(line));

		return entryList;
	}

	/**
	 * Get a specific set of PED file entries
	 * 
	 * @param from
	 *            = inclusive
	 * @param to
	 *            = exclusive
	 * @return
	 * @throws Exception
	 */
	public List<PedEntry> getEntries(final long from, final long to) throws IOException
	{
		reset();

		List<PedEntry> entryList = new ArrayList<PedEntry>();
		String line;
		for (int i = 0; (line = reader.readLine()) != null && i < to; ++i)
			if (i >= from) entryList.add(parseEntry(line));

		return entryList;
	}

	private PedEntry parseEntry(String line) throws IOException
	{
		StringTokenizer strTokenizer = new StringTokenizer(line, separator + "");
		try
		{
			String family = strTokenizer.nextToken();
			String individual = strTokenizer.nextToken();
			String father = strTokenizer.nextToken();
			String mother = strTokenizer.nextToken();
			byte sex = Byte.parseByte(strTokenizer.nextToken());
			double phenotype = Double.parseDouble(strTokenizer.nextToken());
			List<Biallele> bialleles = new ArrayList<Biallele>();
			while (strTokenizer.hasMoreTokens())
			{
				char allele1 = strTokenizer.nextToken().charAt(0);
				char allele2 = strTokenizer.nextToken().charAt(0);
				bialleles.add(Biallele.create(allele1, allele2));
			}
			return new PedEntry(family, individual, father, mother, sex, phenotype, bialleles);
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
