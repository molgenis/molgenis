package org.molgenis.data.csv;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.file.processor.AbstractCellProcessor;
import org.molgenis.data.file.processor.CellProcessor;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.util.CloseableIterator;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.molgenis.data.csv.CsvRepositoryCollection.MAC_ZIP;

public class CsvIterator implements CloseableIterator<Entity>
{
	private final String repositoryName;
	private final EntityType entityType;
	private ZipFile zipFile;
	private CSVReader csvReader;
	private final List<CellProcessor> cellProcessors;
	private final Map<String, Integer> colNamesMap; // column names index
	private Entity next;
	private boolean getNext = true;
	private Character separator = null;

	CsvIterator(File file, String repositoryName, List<CellProcessor> cellProcessors, Character separator)
	{
		this(file, repositoryName, cellProcessors, separator, null);
	}

	CsvIterator(File file, String repositoryName, List<CellProcessor> cellProcessors, Character separator,
			EntityType entityType)
	{
		this.repositoryName = repositoryName;
		this.cellProcessors = cellProcessors;
		this.separator = separator;
		this.entityType = entityType;

		try
		{
			if (StringUtils.getFilenameExtension(file.getName()).equalsIgnoreCase("zip"))
			{
				zipFile = new ZipFile(file.getAbsolutePath());
				for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements(); )
				{
					ZipEntry entry = e.nextElement();
					if (!entry.getName().contains(MAC_ZIP) && !entry.isDirectory())
					{
						String fileRepositoryName = FilenameUtils.getBaseName(entry.getName());
						if (fileRepositoryName.equalsIgnoreCase(repositoryName))
						{
							csvReader = createCSVReader(entry.getName(),
									removeByteOrderMark(zipFile.getInputStream(entry)));
							break;
						}
					}
				}
			}
			else if (file.getName().toLowerCase().startsWith(repositoryName.toLowerCase()))
			{
				csvReader = createCSVReader(file.getName(), removeByteOrderMark(new FileInputStream(file)));
			}

			if (csvReader == null)
			{
				throw new UnknownEntityException(format("Unknown entity [%s]", repositoryName));
			}

			colNamesMap = toColNamesMap(csvReader.readNext());
		}
		catch (IOException e)
		{
			throw new MolgenisDataException(format("Exception reading [%s]", file.getAbsolutePath()), e);
		}
	}

	/**
	 * <p>Convert the inputstreams that can be generated by the CsvIterator and check on BOM-attachements./p>
	 *
	 * @param inputStream from zipfile or normal files
	 * @return inputStream without ByteOrderMark (always)
	 */
	private InputStream removeByteOrderMark(InputStream inputStream)
	{
		return new BOMInputStream(inputStream, false);
	}

	Map<String, Integer> getColNamesMap()
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

				if (values != null && values.length == colNamesMap.size())
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

					next = new DynamicEntity(entityType);

					for (String name : colNamesMap.keySet())
					{
						next.set(name, valueList.get(colNamesMap.get(name)));
					}
				}
				else if (values != null && (values.length > 1 || (values.length == 1 && values[0].length() > 0))
						&& values.length < colNamesMap.size())
				{
					throw new MolgenisDataException(
							format("Number of values (%d) doesn't match the number of headers (%d): [%s]",
									values.length, colNamesMap.size(), stream(values).collect(joining(","))));
				}
				else
				{
					next = null;
				}

				getNext = false;
			}
			catch (IOException e)
			{
				throw new MolgenisDataException(format("Exception reading line of csv file [%s]", repositoryName), e);
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
		Reader reader = new InputStreamReader(in, UTF_8);

		if (null == separator)
		{
			if (fileName.toLowerCase().endsWith('.' + CsvFileExtensions.CSV.toString()) || fileName.toLowerCase()
																								   .endsWith('.'
																										   + CsvFileExtensions.TXT
																										   .toString()))
			{
				return new CSVReader(reader);
			}

			if (fileName.toLowerCase().endsWith('.' + CsvFileExtensions.TSV.toString()))
			{
				return new CSVReader(reader, '\t');
			}

			throw new MolgenisDataException(format("Unknown file type: [%s] for csv repository", fileName));
		}

		return new CSVReader(reader, this.separator);
	}

	private Map<String, Integer> toColNamesMap(String[] headers)
	{
		if ((headers == null) || (headers.length == 0))
		{
			return Collections.emptyMap();
		}

		int capacity = (int) (headers.length / 0.75) + 1;
		Map<String, Integer> columnIdx = new LinkedHashMap<>(capacity);
		for (int i = 0; i < headers.length; ++i)
		{
			String header = processCell(headers[i], true);
			if (columnIdx.containsKey(header))
			{
				throw new MolgenisDataException(format("Duplicate column header '%s' not allowed", header));
			}
			columnIdx.put(header, i);
		}

		return columnIdx;
	}

	private String processCell(String value, boolean isHeader)
	{
		return AbstractCellProcessor.processCell(value, isHeader, cellProcessors);
	}

}
