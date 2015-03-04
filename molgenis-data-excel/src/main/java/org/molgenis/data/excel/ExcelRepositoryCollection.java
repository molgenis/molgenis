package org.molgenis.data.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.Writable;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.processor.TrimProcessor;
import org.molgenis.data.support.FileRepositoryCollection;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * Read an excel file and iterate through the sheets.
 * 
 * A sheet is exposed as a {@link org.molgenis.data.Repository} with the sheetname as the Repository name
 */
public class ExcelRepositoryCollection extends FileRepositoryCollection
{
	public static final String NAME = "EXCEL";
	public static final Set<String> EXTENSIONS = ImmutableSet.of("xls", "xlsx");
	private final String name;
	private final Workbook workbook;

	public ExcelRepositoryCollection(File file) throws InvalidFormatException, IOException
	{
		this(file, new TrimProcessor());
	}

	public ExcelRepositoryCollection(File file, CellProcessor... cellProcessors) throws InvalidFormatException,
			IOException
	{
		this(file.getName(), new FileInputStream(file), cellProcessors);
	}

	public ExcelRepositoryCollection(String name, InputStream in, CellProcessor... cellProcessors)
			throws InvalidFormatException, IOException
	{
		super(EXTENSIONS, cellProcessors);
		this.name = name;
		workbook = WorkbookFactory.create(in);
	}

	@Override
	public Iterable<String> getEntityNames()
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
	public Repository getRepository(String name)
	{
		Sheet poiSheet = workbook.getSheet(name);
		if (poiSheet == null)
		{
			return null;
		}

		return new ExcelRepository(name, poiSheet, cellProcessors);
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

		return new ExcelRepository(name, poiSheet, cellProcessors);
	}

	public Writable createWritable(String entityName, List<String> attributeNames)
	{
		Sheet sheet = workbook.createSheet(entityName);
		return new ExcelSheetWriter(sheet, attributeNames, cellProcessors);
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
	public Repository addEntityMeta(EntityMetaData entityMeta)
	{
		return getRepository(entityMeta.getName());
	}

	@Override
	public Iterator<Repository> iterator()
	{
		return new Iterator<Repository>()
		{
			Iterator<String> it = getEntityNames().iterator();

			@Override
			public boolean hasNext()
			{
				return it.hasNext();
			}

			@Override
			public Repository next()
			{
				return getRepository(it.next());
			}

		};
	}

}
