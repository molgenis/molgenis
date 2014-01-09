package org.molgenis.data.excel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.WritableFactory;
import org.molgenis.data.processor.CellProcessor;

/**
 * Creates new Excel sheets
 */
public class ExcelWriter<E extends Entity> implements WritableFactory<E>
{
	private final Workbook workbook;
	private final OutputStream os;
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

	public void addCellProcessor(CellProcessor cellProcessor)
	{
		if (cellProcessors == null) cellProcessors = new ArrayList<CellProcessor>();
		cellProcessors.add(cellProcessor);
	}

	@Override
	public ExcelSheetWriter<E> createWritable(String entityName, List<String> attributeNames)
	{
		Sheet poiSheet = workbook.createSheet(entityName);
		return new ExcelSheetWriter<E>(poiSheet, attributeNames, cellProcessors);
	}

	@Override
	public void close() throws IOException
	{
		try
		{
			workbook.write(os);
		}
		catch (IOException e)
		{
			throw new MolgenisDataException("Exception writing to excel file", e);
		}
		os.close();
	}

}
