package org.molgenis.oneclickimporter.service.Impl;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.molgenis.oneclickimporter.exceptions.EmptySheetException;
import org.molgenis.oneclickimporter.exceptions.NoDataException;
import org.molgenis.oneclickimporter.service.ExcelService;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@Component
public class ExcelServiceImpl implements ExcelService
{
	@Override
	public List<Sheet> buildExcelSheetsFromFile(File file)
			throws IOException, InvalidFormatException, NoDataException, EmptySheetException
	{
		Workbook workbook = WorkbookFactory.create(file);
		int numberOfSheets = workbook.getNumberOfSheets();

		List<Sheet> sheets = newArrayList();
		for (int index = 0; index < numberOfSheets; index++)
		{
			Sheet sheet = workbook.getSheetAt(index);
			if (sheet.getPhysicalNumberOfRows() == 0)
			{
				throw new EmptySheetException("Sheet [" + sheet.getSheetName() + "] is empty");
			}
			else if (sheet.getPhysicalNumberOfRows() == 1)
			{
				throw new NoDataException(
						"Header was found, but no data is present in sheet [" + sheet.getSheetName() + "]");
			}
			else
			{
				sheets.add(sheet);
			}
		}
		return sheets;
	}
}
