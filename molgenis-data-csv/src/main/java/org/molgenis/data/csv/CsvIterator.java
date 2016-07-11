package org.molgenis.data.csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.processor.AbstractCellProcessor;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.GenericImporterExtensions;
import org.molgenis.util.CloseableIterator;
import org.springframework.util.StringUtils;

import au.com.bytecode.opencsv.CSVReader;

public class CsvIterator implements CloseableIterator<Entity>
{
	private static final Charset CHARSET = Charset.forName("UTF-8");
	private final String repositoryName;
	private final EntityMetaData entityMeta;
	private ZipFile zipFile;
	private CSVReader csvReader;
	private final List<CellProcessor> cellProcessors;
	private final Map<String, Integer> colNamesMap; // column names index
	private Entity next;
	private boolean getNext = true;
	private Character separator = null;

	public CsvIterator(File file, String repositoryName, List<CellProcessor> cellProcessors, Character separator)
	{
		this(file, repositoryName, cellProcessors, separator, null);
	}

	public CsvIterator(File file, String repositoryName, List<CellProcessor> cellProcessors, Character separator,
			EntityMetaData entityMeta)
	{
		this.repositoryName = repositoryName;
		this.cellProcessors = cellProcessors;
		this.separator = separator;
		this.entityMeta = entityMeta;

		try
		{
			if (StringUtils.getFilenameExtension(file.getName()).equalsIgnoreCase(
					GenericImporterExtensions.ZIP.toString()))
			{
				zipFile = new ZipFile(file.getAbsolutePath());
				for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements();)
				{
					ZipEntry entry = e.nextElement();
					if (StringUtils.stripFilenameExtension(entry.getName()).equalsIgnoreCase(repositoryName))
					{
						csvReader = createCSVReader(entry.getName(), zipFile.getInputStream(entry));
						break;
					}
				}

			}
			else if (file.getName().toLowerCase().startsWith(repositoryName.toLowerCase()))
			{
				csvReader = createCSVReader(file.getName(), new FileInputStream(file));
			}

			if (csvReader == null)
			{
				throw new UnknownEntityException("Unknown entity [" + repositoryName + "] ");
			}

			colNamesMap = toColNamesMap(csvReader.readNext());
		}
		catch (IOException e)
		{
			throw new MolgenisDataException("Exception reading [" + file.getAbsolutePath() + "]", e);
		}
	}


	public Map<String, Integer> getColNamesMap()
	{
		return colNamesMap;
	}

	@Override
	public boolean hasNext()
	{
		boolean next = get() != null;
		if (!next)
		{
			close();
		}

		return next;
	}

	@Override
	public Entity next()
	{
		Entity entity = get();
		getNext = true;
		return entity;
	}

	private Entity get()
	{
		if (getNext)
		{
			try
			{
				String[] values = csvReader.readNext();

				if ((values != null) && (values.length >= colNamesMap.size()))
				{
					List<String> valueList = Arrays.asList(values);
					for (int i = 0; i < values.length; ++i)
					{
						// subsequent separators indicate
						// null
						// values instead of empty strings
						String value = values[i].isEmpty() ? null : values[i];
						values[i] = processCell(value, false);
					}

					next = new DynamicEntity(entityMeta);

					for (String name : colNamesMap.keySet())
					{
						next.set(name, valueList.get(colNamesMap.get(name)));
					}
				}
				else
				{
					next = null;
				}

				getNext = false;
			}
			catch (IOException e)
			{
				throw new MolgenisDataException("Exception reading line of csv file [" + repositoryName + "]", e);
			}
		}

		return next;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void close()
	{
		IOUtils.closeQuietly(csvReader);

		if (zipFile != null)
		{
			IOUtils.closeQuietly(zipFile);
		}
	}

	private CSVReader createCSVReader(String fileName, InputStream in)
	{
		Reader reader = new InputStreamReader(in, CHARSET);

		if (null == separator)
		{
			if (fileName.toLowerCase().endsWith('.' + GenericImporterExtensions.CSV.toString()) || fileName
					.toLowerCase().endsWith('.' + GenericImporterExtensions.TXT.toString()))
			{
				return new CSVReader(reader);
			}

			if (fileName.toLowerCase().endsWith('.' + GenericImporterExtensions.TSV.toString()))
			{
				return new CSVReader(reader, '\t');
			}

			throw new MolgenisDataException("Unknown file type: [" + fileName + "] for csv repository");
		}

		return new CSVReader(reader, this.separator);
	}

	private Map<String, Integer> toColNamesMap(String[] headers)
	{
		if ((headers == null) || (headers.length == 0)) return Collections.emptyMap();

		int capacity = (int) (headers.length / 0.75) + 1;
		Map<String, Integer> columnIdx = new LinkedHashMap<>(capacity);
		for (int i = 0; i < headers.length; ++i)
		{
			String header = processCell(headers[i], true);
			columnIdx.put(header, i);
		}

		return columnIdx;
	}

	private String processCell(String value, boolean isHeader)
	{
		return AbstractCellProcessor.processCell(value, isHeader, cellProcessors);
	}

}
