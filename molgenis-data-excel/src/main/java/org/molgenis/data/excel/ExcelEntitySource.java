package org.molgenis.data.excel;

import static org.molgenis.data.excel.ExcelEntitySourceFactory.EXCEL_ENTITYSOURCE_URL_PREFIX;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.molgenis.data.EntitySource;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.io.processor.CellProcessor;

/**
 * Excel file EntitySource. Is a wrapper around an excel workbook.
 * 
 * Each sheet is a Repository.
 * 
 * Close after use, it will close the InputStream
 * 
 * The url of an EntitySource is the file path prefixed with 'excel://'.
 * 
 * example: excel://Users/john/Documents/matrix.xls
 */
public class ExcelEntitySource implements EntitySource
{
	private final Workbook workbook;
	private final InputStream is;
	private final String url;

	/** process cells after reading */
	private List<CellProcessor> cellProcessors;

	protected ExcelEntitySource(InputStream is, String url, List<CellProcessor> cellProcessors)
	{
		if ((is == null) && (url == null))
		{
			throw new IllegalArgumentException("InputStream and url are null");
		}

		if ((url != null) && !url.startsWith(EXCEL_ENTITYSOURCE_URL_PREFIX))
		{
			throw new IllegalArgumentException("ExcelEntitySource urls should start with "
					+ EXCEL_ENTITYSOURCE_URL_PREFIX);
		}

		try
		{
			if (is == null)
			{
				is = new FileInputStream(new File(url.substring(EXCEL_ENTITYSOURCE_URL_PREFIX.length())));
			}

			workbook = WorkbookFactory.create(is);
		}
		catch (InvalidFormatException e)
		{
			throw new MolgenisDataException("Exception opening Excel datasource [" + url + "]", e);
		}
		catch (IOException e)
		{
			throw new MolgenisDataException("Exception opening Excel datasource [" + url + "]", e);
		}

		this.is = is;
		this.url = url;
		this.cellProcessors = cellProcessors;
	}

	protected ExcelEntitySource(File file, List<CellProcessor> cellProcessors) throws IOException
	{
		this(new FileInputStream(file), EXCEL_ENTITYSOURCE_URL_PREFIX + file.getAbsolutePath(), cellProcessors);
	}

	protected ExcelEntitySource(String url, List<CellProcessor> cellProcessors) throws IOException
	{
		this(null, url, cellProcessors);
	}

	public ExcelEntitySource(InputStream is, List<CellProcessor> cellProcessors)
	{
		this(is, null, cellProcessors);
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
		return poiSheet != null ? new ExcelRepository(poiSheet, cellProcessors) : null;
	}

	public ExcelRepository getSheet(String sheetName)
	{
		Sheet poiSheet = workbook.getSheet(sheetName);
		return poiSheet != null ? new ExcelRepository(poiSheet, cellProcessors) : null;
	}

	public void addCellProcessor(CellProcessor cellProcessor)
	{
		if (cellProcessors == null) cellProcessors = new ArrayList<CellProcessor>();
		cellProcessors.add(cellProcessor);
	}

	@Override
	public void close() throws IOException
	{
		is.close();
	}

	@Override
	public String getUrl()
	{
		return url;
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return new Iterable<String>()
		{
			@Override
			public Iterator<String> iterator()
			{
				return new Iterator<String>()
				{
					private final int nrSheets = getNumberOfSheets();
					private int i = 0;

					@Override
					public boolean hasNext()
					{
						return i < nrSheets;
					}

					@Override
					public String next()
					{
						return getSheetName(i++);
					}

					@Override
					public void remove()
					{
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}

	@Override
	public Repository<ExcelEntity> getRepositoryByEntityName(String entityName) throws UnknownEntityException
	{
		Sheet poiSheet = workbook.getSheet(entityName);
		if (poiSheet == null)
		{
			throw new UnknownEntityException("Unknown entity [" + entityName + "]");
		}

		return new ExcelRepository(poiSheet, cellProcessors);
	}
}
