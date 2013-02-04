package org.molgenis.framework.tupletable.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.DataFormatException;

import org.apache.commons.io.IOUtils;
import org.molgenis.framework.tupletable.AbstractTupleTable;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.io.csv.CsvReader;
import org.molgenis.model.elements.Field;
import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.Tuple;

/**
 * Wrap a CSV file into a TupleTable
 */
public class CsvTable extends AbstractTupleTable
{
	private CsvReader csvReader;
	private InputStream countStream;
	private List<Field> columns = new ArrayList<Field>();

	File csvFile;
	String csvString;

	/**
	 * Read table from a csv file
	 * 
	 * @param csvFile
	 * @throws Exception
	 */
	public CsvTable(File csvFile) throws Exception
	{
		if (csvFile == null) throw new NullPointerException("Creation of CsvTable failed: csvFile == null");
		if (!csvFile.exists()) throw new IllegalArgumentException("Creation of CsvTable failed: csvFile does not exist");

		this.csvFile = csvFile;
		this.resetStreams();
		loadColumns();
	}

	/**
	 * Read table from a csv string
	 * 
	 * @param csvString
	 * @throws Exception
	 */
	public CsvTable(String csvString) throws TableException
	{
		if (csvString == null) throw new NullPointerException("Creation of CsvTable failed: csvString == null");

		this.csvString = csvString;
		try
		{
			resetStreams();
			loadColumns();
		}
		catch (Exception e)
		{
			throw new TableException(e);
		}
	}

	int rowCount = -1;

	/**
	 * Count rows (not including header of csv file)
	 */
	@Override
	public int getCount() throws TableException
	{
		if (rowCount == -1)
		{
			LineNumberReader lineReader = new LineNumberReader(new InputStreamReader(countStream,
					Charset.forName("UTF-8")));
			try
			{
				String line = null;
				while ((line = lineReader.readLine()) != null)
				{
					line = line.trim();
				}

				// substract 1 because of header
				rowCount = lineReader.getLineNumber() - 1;
			}
			catch (Exception e)
			{
				throw new TableException(e);
			}
			finally
			{
				IOUtils.closeQuietly(lineReader);
			}
		}
		return rowCount;
	}

	/**
	 * Helper method to load the Field metadata
	 * 
	 * @throws Exception
	 */
	private void loadColumns() throws Exception
	{
		for (Iterator<String> it = csvReader.colNamesIterator(); it.hasNext();)
		{
			Field f = new Field(it.next());
			columns.add(f);
		}
	}

	@Override
	public List<Field> getAllColumns()
	{
		return columns;
	}

	@Override
	public List<Tuple> getRows()
	{
		List<Tuple> result = new ArrayList<Tuple>();
		for (Tuple row : this)
		{
			result.add(row);
		}

		return result;
	}

	@Override
	public Iterator<Tuple> iterator()
	{

		try
		{
			this.resetStreams();
		}
		catch (Exception e)
		{
			// should not happen as this is second load
			e.printStackTrace();
		}

		if (getLimit() > 0 || getOffset() > 0 || getColOffset() > 0 || getColLimit() > 0)
		{
			// offset + 1 to skip the header
			return new TupleIterator(csvReader.iterator(), getLimit(), getOffset(), getColLimit(), getColOffset());
		}
		return csvReader.iterator();
	}

	@Override
	public void close() throws TableException
	{
		IOUtils.closeQuietly(csvReader);
		IOUtils.closeQuietly(countStream);
	}

	private void resetStreams() throws FileNotFoundException, IOException, DataFormatException
	{
		IOUtils.closeQuietly(csvReader);
		if (csvFile != null)
		{
			csvReader = new CsvReader(csvFile);
			countStream = new FileInputStream(csvFile);
		}
		else
		{
			csvReader = new CsvReader(new StringReader(csvString));
			countStream = new ByteArrayInputStream(csvString.getBytes(Charset.forName("UTF-8")));
		}
	}

	private static class TupleIterator implements Iterator<Tuple>
	{
		Iterator<Tuple> it;
		Tuple next;
		int limit = 0;
		int offset = 0;
		int index = 0;
		int count = 0;

		int colLimit = 0;
		int colOffset = 0;

		public TupleIterator(Iterator<Tuple> it, int limit, int offset, int colLimit, int colOffset)
		{
			this.it = it;
			this.limit = limit;
			this.offset = offset;
			this.colLimit = colLimit;
			this.colOffset = colOffset;
		}

		@Override
		public boolean hasNext()
		{
			while (index++ < offset)
			{
				it.next();
			}

			if (limit != 0 && count++ >= limit) return false;

			if (colLimit > 0 || colOffset > 0)
			{
				next = it.next();
				if (next != null)
				{
					KeyValueTuple tuple = new KeyValueTuple();
					int colIndex = 1;
					int colCount = 0;
					for (String f : next.getColNames())
					{
						if (colOffset == 0 || colIndex > colOffset)
						{
							tuple.set(f, next.get(f));
							colCount++;
							if (colLimit != 0 && colCount > colLimit) break;
						}
						colIndex++;
					}
					next = tuple;
				}
			}
			else
			{
				next = it.next();
			}
			if (next != null) return true;
			return false;
		}

		@Override
		public Tuple next()
		{
			if (next != null || hasNext()) return next;
			else
				return null;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

	}

}
