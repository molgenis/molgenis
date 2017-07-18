package org.molgenis.oneclickimporter.service.Impl;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.oneclickimporter.model.Column;
import org.molgenis.oneclickimporter.model.DataCollection;
import org.molgenis.oneclickimporter.service.OneClickImporterService;
import org.molgenis.util.MolgenisDateFormat;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static java.time.ZoneOffset.UTC;
import static org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted;
import static org.apache.poi.util.LocaleUtil.*;
import static org.molgenis.data.meta.AttributeType.*;

@Component
public class OneClickImporterServiceImpl implements OneClickImporterService
{

	public static final int MAX_STRING_LENGTH = 255;

	@Override
	public DataCollection buildDataCollection(String dataCollectionName, Sheet sheet)
	{
		Row headerRow = sheet.getRow(0);
		List<Column> columns = newArrayList();
		headerRow.cellIterator().forEachRemaining(cell -> columns.add(createColumnFromCell(sheet, cell)));

		return DataCollection.create(dataCollectionName, columns);
	}

	@Override
	public AttributeType guessAttributeType(List<Object> dataValues)
	{
		boolean guessCompleted = false;
		int rowCount = dataValues.size();
		int currentRowIndex = 0;

		AttributeType guess = getBasicAttributeType(dataValues.get(0));
		while (currentRowIndex < rowCount && !guessCompleted)
		{
			Object value = dataValues.get(currentRowIndex);
			AttributeType basicType = getBasicAttributeType(value);

			guess = getCommonType(guess, basicType);
			guess = getEnrichedType(guess, value);

			if (guess.equals(STRING) || guess.equals(TEXT))
			{
				guessCompleted = true;
			}
			currentRowIndex++;
		}

		return guess;
	}

	/**
	 * Returns an enriched AttributeType for when the value meets certain criteria
	 * i.e. if a string value is longer dan 255 characters, the type should be TEXT
	 */
	private AttributeType getEnrichedType(AttributeType guess, Object value)
	{
		if (guess.equals(STRING))
		{
			String stringValue = value.toString();
			if (stringValue.length() > MAX_STRING_LENGTH)
			{
				return TEXT;
			}

			try
			{
				// If parseInstant() succeeds, return DATE
				MolgenisDateFormat.parseInstant(stringValue);
				return DATE;
			}
			catch (DateTimeParseException e)
			{
				return guess;
			}
		}
		return guess;
	}

	/**
	 * Returns the AttributeType shared by both types
	 */
	private AttributeType getCommonType(AttributeType existingGuess, AttributeType newGuess)
	{
		if (existingGuess.equals(newGuess))
		{
			return existingGuess;
		}

		switch (existingGuess)
		{
			case INT:
				//noinspection Duplicates
				if (newGuess.equals(DECIMAL))
				{
					return DECIMAL;
				}
				else if (newGuess.equals(LONG))
				{
					return LONG;
				}
				else
				{
					return STRING;
				}
			case DECIMAL:
				if (newGuess.equals(INT) || newGuess.equals(LONG))
				{
					return DECIMAL;
				}
				else
				{
					return STRING;
				}
			case LONG:
				//noinspection Duplicates
				if (newGuess.equals(INT))
				{
					return LONG;
				}
				else if (newGuess.equals(DECIMAL))
				{
					return DECIMAL;
				}
				else
				{
					return STRING;
				}
			case BOOL:
				if (!newGuess.equals(BOOL))
				{
					return STRING;
				}
			case DATE:
				if (!newGuess.equals(DATE))
				{
					return STRING;
				}
			default:
				return STRING;
		}
	}

	/**
	 * Sets the basic type based on instance of the value Object
	 */
	private AttributeType getBasicAttributeType(Object value)
	{
		if (value == null)
		{
			return STRING;
		}
		if (value instanceof Integer)
		{
			return INT;
		}
		else if (value instanceof Double || value instanceof Float)
		{
			return DECIMAL;
		}
		else if (value instanceof Long)
		{
			return LONG;
		}
		else if (value instanceof Boolean)
		{
			return BOOL;
		}
		else
		{
			return STRING;
		}
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
