package org.molgenis.data.csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.processor.AbstractCellProcessor;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.springframework.util.StringUtils;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Repository implementation for csv files.
 * 
 * The filename without the extension is considered to be the entityname
 */
public class CsvRepository extends AbstractRepository
{
	private static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");
	public static final char DEFAULT_SEPARATOR = ',';

	private final CSVReader csvReader;

	/**
	 * process cells after reading
	 */
	private List<CellProcessor> cellProcessors;

	/**
	 * column names index
	 */
	private Map<String, Integer> colNamesMap;
	private DefaultEntityMetaData entityMetaData;
	private final String entityName;

	public CsvRepository(Reader reader, String entityName, List<CellProcessor> cellProcessors)
	{
		this(reader, DEFAULT_SEPARATOR, entityName, cellProcessors);
	}

	public CsvRepository(Reader reader, char separator, String entityName, @Nullable
	List<CellProcessor> cellProcessors)
	{
		if (reader == null) throw new IllegalArgumentException("reader is null");
		if (entityName == null) throw new IllegalArgumentException("entityName is null");
		this.csvReader = new CSVReader(reader, separator);
		this.entityName = entityName;
		this.cellProcessors = cellProcessors;
	}

	public CsvRepository(File file, List<CellProcessor> cellProcessors) throws FileNotFoundException
	{
		this(new InputStreamReader(new FileInputStream(file), CHARSET_UTF8), StringUtils.stripFilenameExtension(file
				.getName()), cellProcessors);
	}

	public CsvRepository(File file) throws FileNotFoundException
	{
		this(file, CsvEntitySourceFactory.CELLPROCESSORS);
	}

	public CsvRepository(File file, char separator, List<CellProcessor> cellProcessors) throws FileNotFoundException
	{
		this(new InputStreamReader(new FileInputStream(file), CHARSET_UTF8), separator, StringUtils
				.stripFilenameExtension(file.getName()), cellProcessors);
	}

	@Override
	public Iterator<Entity> iterator()
	{
		try
		{
			final Map<String, Integer> colNamesMap = this.colNamesMap == null ? toColNamesMap(csvReader.readNext()) : this.colNamesMap;

			return new Iterator<Entity>()
			{
				private MapEntity next;
				private boolean getNext = true;

				@Override
				public boolean hasNext()
				{
					return get() != null;
				}

				@Override
				public MapEntity next()
				{
					MapEntity entity = get();
					getNext = true;
					return entity;
				}

				private MapEntity get()
				{
					if (getNext)
					{
						try
						{
							String[] values = csvReader.readNext();

							if (values != null)
							{
								for (int i = 0; i < values.length; ++i)
								{
									// subsequent separators indicate
									// null
									// values instead of empty strings
									String value = values[i].isEmpty() ? null : values[i];
									values[i] = processCell(value, false);
								}

								if (colNamesMap != null)
								{
									next = new MapEntity();

									List<String> valueList = Arrays.asList(values);
									for (String name : colNamesMap.keySet())
									{
										next.set(name, valueList.get(colNamesMap.get(name)));
									}
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
							throw new MolgenisDataException("Exception reading line of csv file [" + entityName + "]",
									e);
						}
					}

					return next;
				}

				@Override
				public void remove()
				{
					throw new UnsupportedOperationException();
				}
			};

		}
		catch (IOException e)
		{
			throw new MolgenisDataException("Exception getting iterator for csv file [" + entityName + "]", e);
		}

	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		try
		{
			if (colNamesMap == null)
			{
				colNamesMap = toColNamesMap(csvReader.readNext());
			}

			if (entityMetaData == null)
			{
				entityMetaData = new DefaultEntityMetaData(entityName);

				for (String attrName : colNamesMap.keySet())
				{
					AttributeMetaData attr = new DefaultAttributeMetaData(attrName,
							MolgenisFieldTypes.FieldTypeEnum.STRING);
					entityMetaData.addAttributeMetaData(attr);
				}
			}

			return entityMetaData;
		}
		catch (IOException e)
		{
			throw new MolgenisDataException("Exception getting EntityMetaData for csv file [" + entityName + "]", e);
		}
	}

	private Map<String, Integer> toColNamesMap(String[] headers)
	{
		if (headers == null) return null;
		if (headers.length == 0) return Collections.emptyMap();

		int capacity = (int) (headers.length / 0.75) + 1;
		Map<String, Integer> columnIdx = new LinkedHashMap<String, Integer>(capacity);
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

	public void addCellProcessor(CellProcessor cellProcessor)
	{
		if (cellProcessors == null) cellProcessors = new ArrayList<CellProcessor>();
		cellProcessors.add(cellProcessor);
	}

	@Override
	public void close() throws IOException
	{
		csvReader.close();
	}

	@Override
	public Class<? extends Entity> getEntityClass()
	{
		return MapEntity.class;
	}

}
