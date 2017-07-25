package org.molgenis.oneclickimporter.service.Impl;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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
	public List<Sheet> buildExcelSheetsFromFile(File file) throws IOException, InvalidFormatException
	{
		Workbook workbook = WorkbookFactory.create(file);
		int numberOfSheets = workbook.getNumberOfSheets();

		List<Sheet> sheets = newArrayList();
		for (int index = 0; index < numberOfSheets; index++)
		{
			sheets.add(workbook.getSheetAt(index));
		}
		return sheets;
	}
}
