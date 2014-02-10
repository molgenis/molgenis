package org.molgenis.data.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.molgenis.data.Repository;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.support.FileRepositorySource;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * Read an excel file and iterate through the sheets.
 * 
 * A sheet is exposed as a {@link org.molgenis.data.Repository} with the sheetname as the Repository name
 */
public class ExcelRepositorySource extends FileRepositorySource
{
	public static final Set<String> EXTENSIONS = ImmutableSet.of("xls", "xlsx");
	private final String name;
	private final Workbook workbook;

	public ExcelRepositorySource(File file) throws InvalidFormatException, IOException
	{
		this(file, (CellProcessor[]) null);
	}

	public ExcelRepositorySource(File file, CellProcessor... cellProcessors) throws InvalidFormatException, IOException
	{
		this(file.getName(), new FileInputStream(file), cellProcessors);
	}

	public ExcelRepositorySource(String name, InputStream in, CellProcessor... cellProcessors)
			throws InvalidFormatException, IOException
	{
		super(EXTENSIONS, cellProcessors);
		this.name = name;
		workbook = WorkbookFactory.create(in);
	}

	@Override
	public List<Repository> getRepositories()
	{
		int count = getNumberOfSheets();
		List<Repository> repositories = Lists.newArrayListWithCapacity(count);

		for (int i = 0; i < count; i++)
		{
			repositories.add(getSheet(i));
		}

		return repositories;
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

	public ExcelRepository getSheet(String sheetName)
	{
		Sheet poiSheet = workbook.getSheet(sheetName);
		if (poiSheet == null)
		{
			return null;
		}

		return new ExcelRepository(name, poiSheet, cellProcessors);
	}

}
