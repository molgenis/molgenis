package org.molgenis.data.excel;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.molgenis.data.EntitySource;
import org.molgenis.data.FileBasedEntitySourceFactory;
import org.molgenis.data.MolgenisDataException;

/**
 * EntitySourceFactory that creates a ExcelReader EntitySource
 * 
 * The url of a ExcelReader EntitySource is the file path prefixed with 'excel://'.
 * 
 * example: excel://Users/john/Documents/matrix.xls
 * 
 */
public class ExcelEntitySourceFactory implements FileBasedEntitySourceFactory
{
	public static final String EXCEL_ENTITYSOURCE_URL_PREFIX = "excel://";
	public static final List<String> FILE_EXTENSIONS = Arrays.asList("xls", "xlsx");

	@Override
	public String getUrlPrefix()
	{
		return "excel";
	}

	/**
	 * Creates an ExcelReader
	 */
	@Override
	public EntitySource create(String url)
	{
		try
		{
			return new ExcelEntitySource(url);
		}
		catch (IOException e)
		{
			throw new MolgenisDataException("Exception creating excel datasource with url [" + url + "]", e);
		}
	}

	@Override
	public List<String> getFileExtensions()
	{
		return FILE_EXTENSIONS;
	}

	@Override
	public EntitySource create(File file) throws IOException
	{
		return new ExcelEntitySource(file);
	}

}
