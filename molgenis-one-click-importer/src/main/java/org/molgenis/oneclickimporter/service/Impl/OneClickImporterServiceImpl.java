package org.molgenis.oneclickimporter.service.Impl;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.molgenis.oneclickimporter.model.Column;
import org.molgenis.oneclickimporter.model.DataCollection;
import org.molgenis.oneclickimporter.service.OneClickImporterService;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static org.apache.commons.lang3.math.NumberUtils.isNumber;
import static org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted;

@Component
public class OneClickImporterServiceImpl implements OneClickImporterService
{
	private static String CSV_SEPARATOR = ",";

	@Override
	public DataCollection buildDataCollection(String dataCollectionName, Sheet sheet)
	{
		List<Column> columns = newArrayList();

		Row headerRow = sheet.getRow(0);
		headerRow.cellIterator().forEachRemaining(cell -> columns.add(createColumnFromCell(sheet, cell)));

		return DataCollection.create(dataCollectionName, columns);
	}

	@Override
	public DataCollection buildDataCollection(String dataCollectionName, List<String> lines)
	{
		List<Column> columns = newArrayList();

		String[] headers = lines.get(0).split(CSV_SEPARATOR);
		lines.remove(0); // Remove the header

		int columnIndex = 0;
		for (String header : headers)
		{
			columns.add(createColumnFromLine(header, columnIndex, lines));
			columnIndex++;
		}

		return DataCollection.create(dataCollectionName, columns);
	}

	private Column createColumnFromCell(Sheet sheet, Cell cell)
	{
		return Column.create(cell.getStringCellValue(), cell.getColumnIndex(),
				getColumnDataFromSheet(sheet, cell.getColumnIndex()));
	}

	private Column createColumnFromLine(String header, int columnIndex, List<String> lines)
	{
		return Column.create(header, columnIndex, getColumnDataFromLines(lines, columnIndex));
	}

	private List<Object> getColumnDataFromSheet(Sheet sheet, int columnIndex)
	{
		List<Object> dataValues = newLinkedList();
		sheet.rowIterator().forEachRemaining(row -> dataValues.add(getCellValue(row.getCell(columnIndex))));
		dataValues.remove(0); // Remove the header value
		return dataValues;
	}

	private List<Object> getColumnDataFromLines(List<String> lines, int columnIndex)
	{
		List<Object> dataValues = newLinkedList();
		lines.forEach(line ->
		{
			String[] lineParts = line.split(CSV_SEPARATOR, -1);
			dataValues.add(getPartValue(lineParts[columnIndex]));
		});
		return dataValues;
	}

	private Object getPartValue(String part)
	{
		if (isNullOrEmpty(part))
		{
			return null;
		}

		if (part.equalsIgnoreCase("true") || part.equalsIgnoreCase("false"))
		{
			return parseBoolean(part);
		}

		if (isNumber(part))
		{
			return parseInt(part);
		}

		return part;
	}

	/**
	 * Retrieves the proper Java type instance based on the Excel CellTypeEnum
	 */
	private Object getCellValue(Cell cell)
	{
		Object value;

		// Empty cells are null, instead of BLANK
		if (cell == null)
		{
			return null;
		}

		switch (cell.getCellTypeEnum())
		{
			case STRING:
				value = cell.getStringCellValue();
				break;
			case NUMERIC:
				if (isCellDateFormatted(cell))
				{
					Date dateCellValue = cell.getDateCellValue();
					value = dateCellValue.toString();
				}
				else
				{
					value = cell.getNumericCellValue();
				}
				break;
			case BOOLEAN:
				value = cell.getBooleanCellValue();
				break;
			case FORMULA:
				value = getTypedFormulaValue(cell);
				break;
			default:
				value = null;
				break;
		}
		return value;
	}

	private Object getTypedFormulaValue(Cell cell)
	{
		Object value;
		switch (cell.getCachedFormulaResultTypeEnum())
		{
			case STRING:
				value = cell.getStringCellValue();
				break;
			case NUMERIC:
				value = cell.getNumericCellValue();
				break;
			case BOOLEAN:
				value = cell.getBooleanCellValue();
				break;
			case BLANK:
				value = null;
				break;
			case ERROR:
				value = "#ERROR";
				break;
			default:
				value = null;
				break;
		}
		return value;
	}
}
