package org.molgenis.io.excel;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.molgenis.io.TableReader;
import org.molgenis.io.TupleReader;
import org.molgenis.io.processor.CellProcessor;

public class ExcelReader implements TableReader, Closeable
{
	private final Workbook workbook;
	private final InputStream is;
	private final boolean hasHeader;

	/** process cells after reading */
	private List<CellProcessor> cellProcessors;

	public ExcelReader(InputStream is) throws IOException
	{
		this(is, true);
	}

	public ExcelReader(InputStream is, boolean hasHeader) throws IOException
	{
		if (is == null) throw new IllegalArgumentException("InputStream is null");
		this.is = is;
		this.hasHeader = hasHeader;
		try
		{
			this.workbook = WorkbookFactory.create(is);
		}
		catch (InvalidFormatException e)
		{
			throw new IOException(e);
		}
	}

	public ExcelReader(File file) throws IOException
	{
		this(new FileInputStream(file));
	}

	public ExcelReader(File file, boolean hasHeader) throws IOException
	{
		this(new FileInputStream(file), hasHeader);
	}

	public int getNumberOfSheets()
	{
		return this.workbook.getNumberOfSheets();
	}

	public String getSheetName(int i)
	{
		return this.workbook.getSheetName(i);
	}

	@SuppressWarnings("resource")
	public ExcelSheetReader getSheet(int i)
	{
		org.apache.poi.ss.usermodel.Sheet poiSheet = this.workbook.getSheetAt(i);
		return poiSheet != null ? new ExcelSheetReader(poiSheet, hasHeader, this.cellProcessors) : null;
	}

	@SuppressWarnings("resource")
	public ExcelSheetReader getSheet(String sheetName)
	{
		org.apache.poi.ss.usermodel.Sheet poiSheet = this.workbook.getSheet(sheetName);
		return poiSheet != null ? new ExcelSheetReader(poiSheet, hasHeader, this.cellProcessors) : null;
	}

	@Override
	public Iterator<TupleReader> iterator()
	{
		return new Iterator<TupleReader>()
		{
			private int i = 0;
			private int nrSheets = getNumberOfSheets();

			@Override
			public boolean hasNext()
			{
				return i < nrSheets;
			}

			@Override
			public TupleReader next()
			{
				return getSheet(i++);
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	public void addCellProcessor(CellProcessor cellProcessor)
	{
		if (cellProcessors == null) cellProcessors = new ArrayList<CellProcessor>();
		cellProcessors.add(cellProcessor);
	}

	@SuppressWarnings("resource")
	@Override
	public TupleReader getTupleReader(String tableName) throws IOException
	{
		org.apache.poi.ss.usermodel.Sheet poiSheet = this.workbook.getSheet(tableName);
		return poiSheet != null ? new ExcelSheetReader(poiSheet, hasHeader, this.cellProcessors) : null;
	}

	@Override
	public Iterable<String> getTableNames() throws IOException
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
	public void close() throws IOException
	{
		this.is.close();
	}
}
