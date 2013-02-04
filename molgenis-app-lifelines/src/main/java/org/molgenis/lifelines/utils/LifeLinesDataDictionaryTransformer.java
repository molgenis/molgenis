package org.molgenis.lifelines.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class LifeLinesDataDictionaryTransformer
{
	private static final Logger logger = Logger.getLogger(LifeLinesDataDictionaryTransformer.class);
	private static final Set<String> excludedSheets;
	private static final Map<String, String> headerMap;

	private static final String FIELD_VALUE_LABELS = "Value labels";
	private static final String FIELD_GROUP = "Group";

	static
	{
		excludedSheets = new HashSet<String>();
		excludedSheets.add("Voorblad");
		excludedSheets.add("Tussenblad");
		excludedSheets.add("DB INFO");
		excludedSheets.add("Relaties");

		headerMap = new LinkedHashMap<String, String>();
		headerMap.put(FIELD_GROUP, null);
		headerMap.put("Code", "Veldnaam");
		headerMap.put("Group label", null);
		headerMap.put("Code label", null);
		headerMap.put("Cohort", null);
		headerMap.put("NL Description", "SPSS Omschrijving");
		headerMap.put("EN Description", null);
		headerMap.put("Value", FIELD_VALUE_LABELS);
		headerMap.put("NL Value Description", "Value labels omschrijving");
		headerMap.put("EN Value Description", null);
		headerMap.put("Unit", null);
	}

	public LifeLinesDataDictionaryTransformer()
	{
	}

	public void transform(InputStream in, OutputStream out) throws IOException
	{
		WorkbookSettings inSettings = new WorkbookSettings();
		inSettings.setLocale(new Locale("en", "EN"));
		inSettings.setEncoding("Cp1252");
		WorkbookSettings outSettings = new WorkbookSettings();
		outSettings.setLocale(new Locale("en", "EN"));
		outSettings.setEncoding("Cp1252");

		Workbook workbookIn = null;
		WritableWorkbook workbookOut = null;

		try
		{
			workbookIn = Workbook.getWorkbook(in, inSettings);
			workbookOut = Workbook.createWorkbook(out, outSettings);

			// write data
			int rowOffset = 1;
			final int nrSheets = workbookIn.getNumberOfSheets();
			for (int i = 0; i < nrSheets; ++i)
			{
				Sheet sheet = workbookIn.getSheet(i);
				if (excludedSheets.contains(sheet.getName())) continue;

				// create new sheet with header
				WritableSheet sheetOut = workbookOut.createSheet(sheet.getName(), 0);
				int colOut = 0;
				for (String header : headerMap.keySet())
					sheetOut.addCell(new Label(colOut++, 0, header));

				// convert sheet
				convertSheet(sheet, sheetOut, rowOffset);
			}
			workbookOut.write();
		}
		catch (BiffException e)
		{
			throw new IOException(e);
		}
		catch (RowsExceededException e)
		{
			throw new IOException(e);
		}
		catch (WriteException e)
		{
			throw new IOException(e);
		}
		finally
		{
			try
			{
				workbookOut.close();
			}
			catch (WriteException e)
			{
				throw new IOException(e);
			}
			finally
			{
				workbookIn.close();
			}
		}
	}

	private int convertSheet(Sheet sheet, WritableSheet sheetOut, int rowOffset) throws IOException,
			RowsExceededException, WriteException
	{
		String name = sheet.getName();
		logger.debug("converting sheet: " + name);

		// find header row
		final int nrRows = sheet.getRows();
		Cell startCell = sheet.findCell("Veldnaam", 0, 0, 0, nrRows, false);
		if (startCell == null) throw new RuntimeException("can't find header start in sheet: " + name);
		final int nrHeaderRow = startCell.getRow();

		// parse header row
		Cell[] headerCells = sheet.getRow(nrHeaderRow);
		Map<String, Integer> headerIndex = new HashMap<String, Integer>();
		for (int i = 0, j = 0; i < headerCells.length; ++i, ++j)
		{
			String headerContents = headerCells[i].getContents();
			if (headerContents.equals(FIELD_VALUE_LABELS)) headerIndex.put(headerContents, j++);
			else headerIndex.put(headerContents, j);
		}

		// parse data
		int j = 0;
		for (int i = nrHeaderRow + 1; i < nrRows; ++i, ++j)
		{

			int colOut = 0;
			for (Entry<String, String> entry : headerMap.entrySet())
			{
				String key = entry.getKey();
				String val = entry.getValue();
				if (key.equals(FIELD_GROUP) && !sheet.getCell(0, i).getContents().isEmpty())
				{
					sheetOut.addCell(new Label(0, rowOffset + j, name));
				}
				else if (val != null)
				{
					Integer idx = headerIndex.get(val);
					if (idx != null)
					{
						Cell cell = sheet.getCell(idx, i);
						if (val != FIELD_VALUE_LABELS)
						{
							sheetOut.addCell(new Label(colOut, rowOffset + j, cell.getContents()));
						}
						else
						{
							String contents = cell.getContents();
							if (contents != null && !contents.trim().isEmpty())
							{
								int off = contents.indexOf('=');
								try
								{
									sheetOut.addCell(new Label(colOut, rowOffset + j, contents.substring(0, off).trim()));
									sheetOut.addCell(new Label(colOut + 1, rowOffset + j, contents.substring(off + 1)
											.trim()));
								}
								catch (IndexOutOfBoundsException e)
								{
									System.out.println("error splitting string: " + contents + " - sheet:"
											+ sheet.getName());
								}
								++colOut;
							}
						}
					}
				}
				++colOut;
			}
		}

		return j;
	}

	public static void main(String[] args) throws IOException
	{
		BasicConfigurator.configure();

		if (args.length != 2)
		{
			System.err.println("usage: <input file> <output file>");
			return;
		}

		File inFile = new File(args[0]);
		File outFile = new File(args[1]);
		// if (outFile.exists()) throw new IOException("file already exists: " +
		// outFile);

		FileInputStream fis = new FileInputStream(inFile);
		FileOutputStream fos = new FileOutputStream(outFile);
		try
		{
			new LifeLinesDataDictionaryTransformer().transform(fis, fos);
		}
		finally
		{
			IOUtils.closeQuietly(fos);
			IOUtils.closeQuietly(fis);
		}
	}
}