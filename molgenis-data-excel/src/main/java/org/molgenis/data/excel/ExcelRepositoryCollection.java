package org.molgenis.data.excel;

import com.google.common.collect.Lists;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.processor.TrimProcessor;
import org.molgenis.data.support.AbstractWritable.AttributeWriteMode;
import org.molgenis.data.support.FileRepositoryCollection;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Read an excel file and iterate through the sheets.
 * <p>
 * A sheet is exposed as a {@link org.molgenis.data.Repository} with the sheetname as the Repository name
 */
public class ExcelRepositoryCollection extends FileRepositoryCollection
{
	public static final String NAME = "EXCEL";

	private final String name;
	private final Workbook workbook;

	private EntityTypeFactory entityTypeFactory;
	private AttributeFactory attributeFactory;

	public ExcelRepositoryCollection(File file) throws IOException, MolgenisInvalidFormatException
	{
		this(file, new TrimProcessor());
	}

	public ExcelRepositoryCollection(File file, CellProcessor... cellProcessors)
			throws IOException, MolgenisInvalidFormatException
	{
		this(file.getName(), new FileInputStream(file), cellProcessors);
	}

	public ExcelRepositoryCollection(String name, InputStream in, CellProcessor... cellProcessors)
			throws IOException, MolgenisInvalidFormatException
	{
		super(ExcelFileExtensions.getExcel(), cellProcessors);
		this.name = name;
		try
		{
			workbook = WorkbookFactory.create(in);
		}
		catch (InvalidFormatException e)
		{
			throw new MolgenisInvalidFormatException(e.getMessage());
		}
	}

	@Override
	public void init() throws IOException
	{
		// no operation
	}

	@Override
	public Iterable<String> getEntityTypeIds()
	{
		int count = getNumberOfSheets();
		List<String> sheetNames = Lists.newArrayListWithCapacity(count);

		for (int i = 0; i < count; i++)
		{
			sheetNames.add(getSheetName(i));
		}

		return sheetNames;
	}

	@Override
	public Repository<Entity> getRepository(String name)
	{
		Sheet poiSheet = workbook.getSheet(name);
		if (poiSheet == null)
		{
			return null;
		}

		return new ExcelRepository(name, poiSheet, entityTypeFactory, attributeFactory, cellProcessors);
	}

	public int getNumberOfSheets()
	{
		return workbook.getNumberOfSheets();
	}

	public String getSheetName(int i)
	{
		return workbook.getSheetName(i);
	}

	public ExcelRepository getSheet(int i)
	{
		Sheet poiSheet = workbook.getSheetAt(i);
		if (poiSheet == null)
		{
			return null;
		}

		return new ExcelRepository(name, poiSheet, entityTypeFactory, attributeFactory, cellProcessors);
	}

	public ExcelSheetWriter createWritable(String entityTypeId, List<Attribute> attributes,
			AttributeWriteMode attributeWriteMode)
	{
		Sheet sheet = workbook.createSheet(entityTypeId);
		return new ExcelSheetWriter(sheet, attributes, attributeWriteMode, cellProcessors);
	}

	public ExcelSheetWriter createWritable(String entityTypeId, List<String> attributeNames)
	{
		List<Attribute> attributes = attributeNames != null ? attributeNames.stream().map(
				attrName -> attributeFactory.create().setName(attrName)).collect(Collectors.toList()) : null;

		return createWritable(entityTypeId, attributes, AttributeWriteMode.ATTRIBUTE_NAMES);
	}

	public void save(OutputStream out) throws IOException
	{
		workbook.write(out);
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public Iterator<Repository<Entity>> iterator()
	{
		return new Iterator<Repository<Entity>>()
		{
			Iterator<String> it = getEntityTypeIds().iterator();

			@Override
			public boolean hasNext()
			{
				return it.hasNext();
			}

			@Override
			public Repository<Entity> next()
			{
				return getRepository(it.next());
			}

		};
	}

	@Override
	public boolean hasRepository(String name)
	{
		if (null == name) return false;
		for (String s : getEntityTypeIds())
		{
			if (s.equals(name)) return true;
		}
		return false;
	}

	@Override
	public boolean hasRepository(EntityType entityType)
	{
		return hasRepository(entityType.getId());
	}

	@Autowired
	public void setEntityTypeFactory(EntityTypeFactory entityTypeFactory)
	{
		this.entityTypeFactory = entityTypeFactory;
	}

	@Autowired
	public void setAttributeFactory(AttributeFactory attributeFactory)
	{
		this.attributeFactory = attributeFactory;
	}
}
