package org.molgenis.io.excel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.molgenis.io.TableWriter;
import org.molgenis.io.TupleWriter;
import org.molgenis.io.processor.CellProcessor;

public class ExcelWriter implements TableWriter
{
	private final Workbook workbook;
	private final OutputStream os;

	/** process cells after reading */
	private List<CellProcessor> cellProcessors;

	public enum FileFormat
	{
		XLS, XLSX
	}

	public ExcelWriter(OutputStream os)
	{
		this(os, FileFormat.XLS);
	}

	public ExcelWriter(OutputStream os, FileFormat format)
	{
		if (os == null) throw new IllegalArgumentException("output stream is null");
		if (format == null) throw new IllegalArgumentException("format is null");
		this.os = os;
		this.workbook = format == FileFormat.XLS ? new HSSFWorkbook() : new XSSFWorkbook();
	}

	public ExcelWriter(File file) throws FileNotFoundException
	{
		this(new FileOutputStream(file), FileFormat.XLS);
	}

	public ExcelWriter(File file, FileFormat format) throws FileNotFoundException
	{
		this(new FileOutputStream(file), format);
	}

	@Override
	public TupleWriter createTupleWriter(String tableName) throws IOException
	{
		org.apache.poi.ss.usermodel.Sheet poiSheet = this.workbook.createSheet(tableName);
		return new ExcelSheetWriter(poiSheet, cellProcessors);
	}

	public void addCellProcessor(CellProcessor cellProcessor)
	{
		if (cellProcessors == null) cellProcessors = new ArrayList<CellProcessor>();
		cellProcessors.add(cellProcessor);
	}

	@Override
	public void close() throws IOException
	{
		try
		{
			this.workbook.write(os);
		}
		finally
		{
			this.os.close();
		}
	}
}
