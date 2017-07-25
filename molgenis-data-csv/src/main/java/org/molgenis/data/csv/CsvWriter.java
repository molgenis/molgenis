package org.molgenis.data.csv;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.processor.AbstractCellProcessor;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.support.AbstractWritable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class CsvWriter extends AbstractWritable
{
	public static final char DEFAULT_SEPARATOR = ',';

	private final au.com.bytecode.opencsv.CSVWriter csvWriter;
	/**
	 * process cells before writing
	 */
	private List<CellProcessor> cellProcessors;

	private List<String> cachedAttributeNames;

	public CsvWriter(Writer writer)
	{
		this(writer, ',');
	}

	public CsvWriter(Writer writer, List<String> attributeNames) throws IOException
	{
		this(writer);
		writeAttributeNames(attributeNames);
	}

	public CsvWriter(Writer writer, char separator)
	{
		this(writer, separator, false);
	}

	public CsvWriter(Writer writer, char separator, boolean noQuotes)
	{
		if (writer == null) throw new IllegalArgumentException("writer is null");
		if (noQuotes)
		{
			this.csvWriter = new au.com.bytecode.opencsv.CSVWriter(writer, separator,
					au.com.bytecode.opencsv.CSVWriter.NO_QUOTE_CHARACTER);
		}
		else
		{
			this.csvWriter = new au.com.bytecode.opencsv.CSVWriter(writer, separator);
		}
	}

	public CsvWriter(OutputStream os)
	{
		this(new OutputStreamWriter(os, UTF_8));
	}

	public CsvWriter(OutputStream os, char separator)
	{
		this(new OutputStreamWriter(os, UTF_8), separator);
	}

	public CsvWriter(OutputStream os, char separator, boolean noQuotes)
	{
		this(new OutputStreamWriter(os, UTF_8), separator, noQuotes);
	}

	public CsvWriter(File file) throws FileNotFoundException
	{
		this(new OutputStreamWriter(new FileOutputStream(file), UTF_8), DEFAULT_SEPARATOR);
	}

	public CsvWriter(File file, char separator) throws FileNotFoundException
	{
		this(new OutputStreamWriter(new FileOutputStream(file), UTF_8), separator);
	}

	public void addCellProcessor(CellProcessor cellProcessor)
	{
		if (cellProcessors == null) cellProcessors = new ArrayList<>();
		cellProcessors.add(cellProcessor);
	}

	@Override
	public void add(Entity entity)
	{
		if (cachedAttributeNames == null)
			throw new MolgenisDataException("No attribute names defined call writeAttributeNames first");

		int i = 0;
		String[] values = new String[cachedAttributeNames.size()];
		for (String colName : cachedAttributeNames)
		{
			values[i++] = toValue(entity.get(colName));
		}

		csvWriter.writeNext(values);
		if (csvWriter.checkError()) throw new MolgenisDataException("An exception occured writing the csv file");
	}

	public void writeAttributeNames(Iterable<String> attributeNames) throws IOException
	{
		writeAttributes(attributeNames, attributeNames);
	}

	/**
	 * Use attribute labels as column names
	 *
	 * @param attributes
	 * @throws IOException
	 */
	public void writeAttributes(Iterable<Attribute> attributes) throws IOException
	{
		List<String> attributeNames = Lists.newArrayList();
		List<String> attributeLabels = Lists.newArrayList();

		for (Attribute attr : attributes)
		{
			attributeNames.add(attr.getName());
			if (attr.getLabel() != null)
			{
				attributeLabels.add(attr.getLabel());
			}
			else
			{
				attributeLabels.add(attr.getName());
			}
		}

		writeAttributes(attributeNames, attributeLabels);
	}

	public void writeAttributes(Iterable<String> attributeNames, Iterable<String> attributeLabels) throws IOException
	{
		if (cachedAttributeNames == null)
		{
			List<String> processedAttributeNames = new ArrayList<>();
			for (String colName : attributeNames)
			{
				// process column name
				String processedColName = AbstractCellProcessor.processCell(colName, true, this.cellProcessors);
				processedAttributeNames.add(processedColName);
			}

			// store filtered column names
			cachedAttributeNames = processedAttributeNames;

			// write column labels
			this.csvWriter.writeNext(Iterables.toArray(attributeLabels, String.class));
			if (this.csvWriter.checkError()) throw new IOException();

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
		else if (obj instanceof Entity)
		{
			if (getEntityWriteMode() != null)
			{
				switch (getEntityWriteMode())
				{
					case ENTITY_IDS:
						value = ((Entity) obj).getIdValue().toString();
						break;
					case ENTITY_LABELS:
						Object labelValue = ((Entity) obj).getLabelValue();
						value = labelValue != null ? labelValue.toString() : null;
						break;
					default:
						throw new RuntimeException("Unknown write mode [" + getEntityWriteMode() + "]");
				}
			}
			else
			{
				Object labelValue = ((Entity) obj).getLabelValue();
				value = labelValue != null ? labelValue.toString() : null;
			}
		}
		else if (obj instanceof Iterable<?>)
		{
			StringBuilder strBuilder = new StringBuilder();
			for (Object listItem : (Iterable<?>) obj)
			{
				if (strBuilder.length() > 0) strBuilder.append(',');
				strBuilder.append(toValue(listItem));
			}
			// TODO apply cell processors to list elements?
			value = strBuilder.toString();
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
