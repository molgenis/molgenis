package org.molgenis.oneclickimporter.service.Impl;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.molgenis.oneclickimporter.model.Column;
import org.molgenis.oneclickimporter.model.DataCollection;
import org.molgenis.oneclickimporter.service.OneClickImporterService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static java.time.ZoneOffset.UTC;
import static org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted;
import static org.apache.poi.util.LocaleUtil.*;

@Component
public class OneClickImporterServiceImpl implements OneClickImporterService
{
	@Override
	public DataCollection buildDataCollection(String dataCollectionName, Sheet sheet)
	{
		List<Column> columns = newArrayList();

		Row headerRow = sheet.getRow(0);
		headerRow.cellIterator().forEachRemaining(cell -> columns.add(createColumnFromCell(sheet, cell)));
		int numberOfRows = columns.get(0).getDataValues().size();

		return DataCollection.create(dataCollectionName, columns, numberOfRows);
	}

	private Column createColumnFromCell(Sheet sheet, Cell cell)
	{
		return Column.create(cell.getStringCellValue(), cell.getColumnIndex(),
				getColumnData(sheet, cell.getColumnIndex()));
	}

	private List<Object> getColumnData(Sheet sheet, int columnIndex)
	{
		List<Object> dataValues = newLinkedList();
		sheet.rowIterator().forEachRemaining(row -> dataValues.add(getCellValue(row.getCell(columnIndex))));
		dataValues.remove(0); // Remove the header value
		return dataValues;
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
					try
					{
						setUserTimeZone(TIMEZONE_UTC);
						Date dateCellValue = cell.getDateCellValue();
						value = formatUTCDateAsLocalDateTime(dateCellValue);
					}
					finally
					{
						resetUserTimeZone();
					}
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

	/**
	 * Formats parsed Date as LocalDateTime string at zone UTC to express that we don't know the timezone.
	 *
	 * @param javaDate Parsed Date representing start of day in UTC
	 * @return Formatted {@link LocalDateTime} string of the java.util.Date
	 */
	private String formatUTCDateAsLocalDateTime(Date javaDate)
	{
		String value;// Now back from start of day in UTC to LocalDateTime to express that we don't know the timezone.
		LocalDateTime localDateTime = javaDate.toInstant().atZone(UTC).toLocalDateTime();
		// And format to string
		value = localDateTime.toString();
		return value;
	}
}
