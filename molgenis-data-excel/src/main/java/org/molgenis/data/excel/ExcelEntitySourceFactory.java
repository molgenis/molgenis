package org.molgenis.data.excel;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.molgenis.data.EntitySource;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.processor.TrimProcessor;
import org.molgenis.data.support.AbstractFileBasedEntitySourceFactory;
import org.springframework.stereotype.Component;

/**
 * EntitySourceFactory that creates a ExcelReader EntitySource
 * 
 * The url of a ExcelReader EntitySource is the file path prefixed with 'excel://'.
 * 
 * example: excel://Users/john/Documents/matrix.xls
 * 
 */
@Component
public class ExcelEntitySourceFactory extends AbstractFileBasedEntitySourceFactory
{
	public static final String EXCEL_ENTITYSOURCE_URL_PREFIX = "excel://";
	public static final List<String> FILE_EXTENSIONS = Arrays.asList("xls", "xlsx");
	private static final List<CellProcessor> CELLPROCESSORS = Arrays.<CellProcessor> asList(new TrimProcessor());

	public ExcelEntitySourceFactory()
	{
		super(EXCEL_ENTITYSOURCE_URL_PREFIX, FILE_EXTENSIONS, CELLPROCESSORS);
	}

	@Override
	protected EntitySource createInternal(String url, List<CellProcessor> cellProcessors) throws IOException
	{
		return new ExcelEntitySource(url, cellProcessors);
	}

	@Override
	protected EntitySource createInternal(File file, List<CellProcessor> cellProcessors) throws IOException
	{
		return new ExcelEntitySource(file, cellProcessors);
	}

}
