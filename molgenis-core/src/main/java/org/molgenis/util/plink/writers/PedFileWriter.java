package org.molgenis.util.plink.writers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.molgenis.util.plink.PlinkFileParser;
import org.molgenis.util.plink.datatypes.Biallele;
import org.molgenis.util.plink.datatypes.PedEntry;

/**
 * Write MAP file entries to a selected location.
 */
public class PedFileWriter implements PlinkFileParser
{
	private BufferedWriter writer;
	private char separator;

	public PedFileWriter(File pedFile) throws IOException
	{
		this(pedFile, DEFAULT_FIELD_SEPARATOR);
	}

	public PedFileWriter(File pedFile, char separator) throws IOException
	{
		if (pedFile == null) throw new IllegalArgumentException("ped file is null");
		this.writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pedFile), FILE_ENCODING));
		this.separator = separator;
	}

	/**
	 * Write a single entry.
	 * 
	 * @throws IOException
	 */
	public void write(PedEntry ped) throws IOException
	{
		writer.write(ped.getFamily());
		writer.write(separator);
		writer.write(ped.getIndividual());
		writer.write(separator);
		writer.write(ped.getFather());
		writer.write(separator);
		writer.write(ped.getMother());
		writer.write(separator);
		writer.write(Byte.toString(ped.getSex()));
		writer.write(separator);
		writer.write(Double.toString(ped.getPhenotype()));
		for (Biallele biallele : ped.getBialleles())
		{
			writer.write(separator);
			writer.write(biallele.getAllele1());
			writer.write(separator);
			writer.write(biallele.getAllele2());
		}
		writer.write(LINE_SEPARATOR);
	}

	/**
	 * Write multiple entries in order.
	 * 
	 * @throws IOException
	 */
	public void write(Iterable<PedEntry> peds) throws IOException
	{
		for (PedEntry ped : peds)
			write(ped);
	}

	/**
	 * Close the underlying writer.
	 * 
	 * @throws IOException
	 */
	@Override
	public void close() throws IOException
	{
		if (writer != null) writer.close();
	}
}
