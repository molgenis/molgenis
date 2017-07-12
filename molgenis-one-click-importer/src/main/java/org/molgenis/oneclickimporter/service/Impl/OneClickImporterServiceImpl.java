package org.molgenis.oneclickimporter.service.Impl;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.molgenis.oneclickimporter.model.Column;
import org.molgenis.oneclickimporter.model.DataCollection;
import org.molgenis.oneclickimporter.service.OneClickImporterService;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;

@Component
public class OneClickImporterServiceImpl implements OneClickImporterService
{
	@Override
	public DataCollection buildDataCollection(Sheet sheet)
	{
		Row headerRow = sheet.getRow(0);

		List<Column> columns = newArrayList();
		headerRow.cellIterator().forEachRemaining(cell -> columns.add(createColumnFromCell(sheet, cell)));

		return DataCollection.create(sheet.getSheetName(), columns);
	}

	private Column createColumnFromCell(Sheet sheet, Cell cell)
	{
		return Column.create(cell.getStringCellValue(), cell.getColumnIndex(),
				getColumnData(sheet, cell.getColumnIndex()));
	}

	private List<Object> getColumnData(Sheet sheet, int columnIndex)
	{
		List<Object> dataValues = newLinkedList();
		sheet.rowIterator().forEachRemaining(row -> dataValues.add(row.getCell(columnIndex)));
		dataValues.remove(0); // Remove the header value
		return dataValues;
	}
}
