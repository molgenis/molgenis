package org.molgenis.data.omx2emx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.WritableFactory;
import org.molgenis.data.excel.ExcelRepositoryCollection;
import org.molgenis.data.excel.ExcelWriter;
import org.molgenis.data.excel.ExcelWriter.FileFormat;
import org.springframework.util.StringUtils;

public class Omx2EmxExcelConverter
{
	public static void main(String[] args)
	{
		try
		{
			convert(new File(args[0]), new File(args[1]));
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (InvalidFormatException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void convert(File omxInputFile, File emxOutputFile) throws InvalidFormatException, IOException
	{

		RepositoryCollection repositoryCollection = new ExcelRepositoryCollection(omxInputFile);

		FileFormat fileFormat = StringUtils.getFilenameExtension(emxOutputFile.getName()).equalsIgnoreCase("xls") ? FileFormat.XLS : FileFormat.XLSX;
		WritableFactory writableFactory = new ExcelWriter(emxOutputFile, fileFormat);

		try
		{
			new Omx2EmxConverter(repositoryCollection).convert(writableFactory);
		}
		finally
		{
			writableFactory.close();
		}
	}
}
