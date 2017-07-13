package org.molgenis.oneclickimporter.service.Impl;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.molgenis.oneclickimporter.service.ExcelService;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class ExcelServiceImpl implements ExcelService
{
	@Override
	public Sheet buildExcelSheetFromFile(File file) throws IOException, InvalidFormatException
	{
		Workbook workbook = WorkbookFactory.create(file);
		return workbook.getSheetAt(0);
	}
}
