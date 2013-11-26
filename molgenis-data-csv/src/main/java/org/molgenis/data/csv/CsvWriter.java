package org.molgenis.data.csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.support.AbstractWritable;
import org.molgenis.io.processor.AbstractCellProcessor;
import org.molgenis.io.processor.CellProcessor;
import org.molgenis.util.ListEscapeUtils;

public class CsvWriter<E extends Entity> extends AbstractWritable<E>
{

	private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

	public static final char DEFAULT_SEPARATOR = ',';

	private final au.com.bytecode.opencsv.CSVWriter csvWriter;
	/** process cells before writing */
	private List<CellProcessor> cellProcessors;

	private List<String> cachedAttributeNames;

	public CsvWriter(Writer writer)
	{
		this(writer, ',');
	}

	public CsvWriter(Writer writer, char separator)
	{
		if (writer == null) throw new IllegalArgumentException("writer is null");
		this.csvWriter = new au.com.bytecode.opencsv.CSVWriter(writer, separator);
	}

	public CsvWriter(OutputStream os)
	{
		this(new OutputStreamWriter(os, DEFAULT_CHARSET));
	}

	public CsvWriter(OutputStream os, char separator)
	{
		this(new OutputStreamWriter(os, DEFAULT_CHARSET), separator);
	}

	public CsvWriter(File file) throws FileNotFoundException
	{
		this(new OutputStreamWriter(new FileOutputStream(file), DEFAULT_CHARSET), DEFAULT_SEPARATOR);
	}

	public CsvWriter(File file, char separator) throws FileNotFoundException
	{
		this(new OutputStreamWriter(new FileOutputStream(file), DEFAULT_CHARSET), separator);
	}

	public void addCellProcessor(CellProcessor cellProcessor)
	{
		if (cellProcessors == null) cellProcessors = new ArrayList<CellProcessor>();
		cellProcessors.add(cellProcessor);
	}

	@Override
	public void add(Entity entity)
	{
		if (cachedAttributeNames == null) throw new MolgenisDataException(
				"No attribute names defined call writeAttributeNames first");

		int i = 0;
		String[] values = new String[cachedAttributeNames.size()];
		for (String colName : cachedAttributeNames)
			values[i++] = toValue(entity.get(colName));

		csvWriter.writeNext(values);
		if (csvWriter.checkError()) throw new MolgenisDataException("An exception occured writing the csv file");

	}

	public void writeAttributeNames(Iterable<String> attributeNames) throws IOException
	{
		if (cachedAttributeNames == null)
		{
			List<String> processedAttributeNames = new ArrayList<String>();
			for (String colName : attributeNames)
			{
				// process column name
				String processedColName = AbstractCellProcessor.processCell(colName, true, this.cellProcessors);
				processedAttributeNames.add(processedColName);
			}

			// write column names
			this.csvWriter.writeNext(processedAttributeNames.toArray(new String[0]));
			if (this.csvWriter.checkError()) throw new IOException();

			// store filtered column names
			cachedAttributeNames = processedAttributeNames;
		}
	}

	@Override
	public void close() throws IOException
	{
		csvWriter.close();
	}

	private String toValue(Object obj)
	{
		String value;
		if (obj == null)
		{
			value = null;
		}
		else if (obj instanceof List<?>)
		{
			// TODO apply cell processors to list elements?
			value = ListEscapeUtils.toString((List<?>) obj);
		}
		else
		{
			value = obj.toString();
		}
		return AbstractCellProcessor.processCell(value, false, this.cellProcessors);
	}

	@Override
	public void flush()
	{
		try
		{
			csvWriter.flush();
		}
		catch (IOException e)
		{
			throw new MolgenisDataException("Error flushing csvwriter", e);
		}

	}

	@Override
	public void clearCache()
	{
		// Nothing
	}
}
