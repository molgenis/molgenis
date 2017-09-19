package org.molgenis.oneclickimporter.service.impl;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.oneclickimporter.exceptions.EmptySheetException;
import org.molgenis.oneclickimporter.service.ExcelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@Component
public class ExcelServiceImpl implements ExcelService
{
	private static final Logger LOG = LoggerFactory.getLogger(ExcelServiceImpl.class);

	@Override
	public List<Sheet> buildExcelSheetsFromFile(File file)
			throws IOException, InvalidFormatException, EmptySheetException
	{
		List<Sheet> sheets = newArrayList();
		try (Workbook workbook = WorkbookFactory.create(file))
		{
			int numberOfSheets = workbook.getNumberOfSheets();

			for (int index = 0; index < numberOfSheets; index++)
			{
				Sheet sheet = workbook.getSheetAt(index);
				if (sheet.getPhysicalNumberOfRows() == 0)
				{
					throw new EmptySheetException("Sheet [" + sheet.getSheetName() + "] is empty");
				}
				else if (sheet.getPhysicalNumberOfRows() == 1)
				{
					throw new MolgenisDataException(
							"Header was found, but no data is present in sheet [" + sheet.getSheetName() + "]");
				}
				else
				{
					sheets.add(sheet);
				}
			}

		}
		catch (IOException | InvalidFormatException | EncryptedDocumentException ex)
		{
			LOG.error(ex.getLocalizedMessage());
			throw new MolgenisDataException("Could not create excel workbook from file");
		}
		return sheets;

	}
}
