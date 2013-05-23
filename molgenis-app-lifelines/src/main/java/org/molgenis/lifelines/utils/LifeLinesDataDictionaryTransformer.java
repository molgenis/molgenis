package org.molgenis.lifelines.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.molgenis.io.TupleWriter;
import org.molgenis.io.excel.ExcelReader;
import org.molgenis.io.excel.ExcelSheetReader;
import org.molgenis.io.excel.ExcelWriter;
import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.Tuple;
import org.molgenis.util.tuple.WritableTuple;

public class LifeLinesDataDictionaryTransformer
{
	private static final Logger logger = Logger.getLogger(LifeLinesDataDictionaryTransformer.class);

	private static final Set<String> EXCLUDED_SHEETS;
	private static final Map<String, String> HEADER_MAP;

	private static final String FIELD_VALUE_LABELS_IN = "Value labels";
	private static final String FIELD_VALUE_LABELS_OUT = "Value";
	private static final String FIELD_VALUE_LABELS_DESCRIPTION_IN = "Value labels omschrijving";
	private static final String FIELD_VALUE_LABELS_DESCRIPTION_OUT = "NL Value Description";
	private static final String FIELD_CODE_IN = "Veldnaam";
	private static final String FIELD_CODE_OUT = "Code";
	private static final String FIELD_GROUP = "Group";

	static
	{
		EXCLUDED_SHEETS = new HashSet<String>();
		EXCLUDED_SHEETS.add("Voorblad");
		EXCLUDED_SHEETS.add("Tussenblad");
		EXCLUDED_SHEETS.add("DB INFO");
		EXCLUDED_SHEETS.add("Relaties");

		HEADER_MAP = new LinkedHashMap<String, String>();
		HEADER_MAP.put(FIELD_GROUP, null);
		HEADER_MAP.put(FIELD_CODE_OUT, FIELD_CODE_IN);
		HEADER_MAP.put("Group label", null);
		HEADER_MAP.put("Code label", null);
		HEADER_MAP.put("Cohort", null);
		HEADER_MAP.put("NL Description", "SPSS Omschrijving");
		HEADER_MAP.put("EN Description", null);
		HEADER_MAP.put(FIELD_VALUE_LABELS_OUT, FIELD_VALUE_LABELS_IN);
		HEADER_MAP.put(FIELD_VALUE_LABELS_DESCRIPTION_OUT, FIELD_VALUE_LABELS_DESCRIPTION_IN);
		HEADER_MAP.put("EN Value Description", null);
		HEADER_MAP.put("Unit", null);
	}

	public void transform(InputStream in, OutputStream out) throws IOException
	{
		ExcelReader excelReader = new ExcelReader(in);
		ExcelWriter excelWriter = new ExcelWriter(out);

		try
		{
			// write data
			int rowOffset = 1;
			final int nrSheets = excelReader.getNumberOfSheets();
			for (int i = 0; i < nrSheets; ++i)
			{
				ExcelSheetReader sheetIn = excelReader.getSheet(i);
				String name = sheetIn.getName();
				if (EXCLUDED_SHEETS.contains(name)) continue;

				// create new sheet with header
				TupleWriter sheetOut = excelWriter.createTupleWriter(name);
				sheetOut.writeColNames(HEADER_MAP.keySet());

				// convert sheet
				convertSheet(sheetIn, name, sheetOut, rowOffset);
			}
		}
		finally
		{
			excelWriter.close();
			excelReader.close();
		}
	}

	private void convertSheet(ExcelSheetReader sheet, String name, TupleWriter sheetOut, int rowOffset)
			throws IOException
	{
		logger.debug("converting sheet: " + name);

		// find header row
		Tuple headerRow = null;
		for (Tuple row : sheet)
		{
			if (row.getString(FIELD_CODE_IN) != null)
			{
				headerRow = row;
				break;
			}
		}
		if (headerRow == null) throw new RuntimeException("can't find header start in sheet: " + name);

		// parse data
		for (Tuple row : sheet)
		{
			WritableTuple tuple = new KeyValueTuple();

			// row containing only values related to a previous row
			boolean isValueRow = row.getString(FIELD_CODE_IN) == null;

			for (Entry<String, String> entry : HEADER_MAP.entrySet())
			{
				String headerOut = entry.getKey();
				String headerIn = entry.getValue();
				if (headerIn == null) continue;

				// write group name unless row contains additional values related to a previous row
				if (headerOut.equals(FIELD_GROUP) && !isValueRow) tuple.set(FIELD_GROUP, name);

				String contents = row.getString(headerIn);
				if (headerOut.equals(FIELD_VALUE_LABELS_OUT))
				{
					tuple.set(headerOut, contents);
				}
				else
				{
					// split in content key and value in two columns
					if (contents != null && !contents.trim().isEmpty())
					{
						int off = contents.indexOf('=');
						try
						{
							String contentsKey = contents.substring(0, off).trim();
							String contentsVal = contents.substring(off + 1).trim();
							tuple.set(FIELD_VALUE_LABELS_OUT, contentsKey);
							tuple.set(FIELD_VALUE_LABELS_DESCRIPTION_OUT, contentsVal);
						}
						catch (IndexOutOfBoundsException e)
						{
							logger.error("error splitting string: " + contents + " - sheet:" + sheet.getName());
						}
					}
				}
			}
			sheetOut.write(tuple);
		}
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
		if (outFile.exists()) throw new IOException("file already exists: " + outFile);

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