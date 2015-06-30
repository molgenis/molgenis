package org.molgenis.data.excel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.convert.DateToStringConverter;
import org.molgenis.data.processor.AbstractCellProcessor;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.support.AbstractWritable;

/**
 * Writable implementation for an excel sheet
 */
public class ExcelSheetWriter extends AbstractWritable
{
	private final Sheet sheet;
	private int row;

	/** process cells before writing */
	private List<CellProcessor> cellProcessors;

	private List<String> cachedAttributeNames;

	ExcelSheetWriter(Sheet sheet, List<String> attributeNames, List<CellProcessor> cellProcessors)
	{
		if (sheet == null) throw new IllegalArgumentException("sheet is null");
		this.sheet = sheet;
		this.cellProcessors = cellProcessors;
		this.row = 0;

		if (attributeNames != null)
		{
			writeAttributeNames(attributeNames);
		}
	}

	/**
	 * Add a new row to the sheet
	 */
	@Override
	public void add(Entity entity)
	{
		if (entity == null) throw new IllegalArgumentException("Entity cannot be null");
		if (cachedAttributeNames == null) throw new MolgenisDataException(
				"The attribute names are not defined, call writeAttributeNames first");

		int i = 0;
		Row poiRow = sheet.createRow(row++);
		for (String attributeName : cachedAttributeNames)
		{
			Cell cell = poiRow.createCell(i++, Cell.CELL_TYPE_STRING);
			cell.setCellValue(toValue(entity.get(attributeName)));
		}

		entity.getIdValue();
	}

	/**
	 * Write sheet column headers
	 */
	public void writeAttributeNames(Iterable<String> attributeNames)
	{
		if (attributeNames == null) throw new IllegalArgumentException("AttributeNames cannot be null");

		if (cachedAttributeNames == null)
		{

			Row poiRow = sheet.createRow(row++);

			// write header
			int i = 0;
			List<String> processedAttributeNames = new ArrayList<String>();
			for (String attributeName : attributeNames)
			{
				// process column name
				Cell cell = poiRow.createCell(i++, Cell.CELL_TYPE_STRING);
				cell.setCellValue(AbstractCellProcessor.processCell(attributeName, true, cellProcessors));
				processedAttributeNames.add(attributeName);
			}

			// store header
			this.cachedAttributeNames = processedAttributeNames;
		}
	}

	public void addCellProcessor(CellProcessor cellProcessor)
	{
		if (cellProcessors == null) cellProcessors = new ArrayList<CellProcessor>();
		cellProcessors.add(cellProcessor);
	}

	private String toValue(Object obj)
	{
		String value;
		if (obj == null)
		{
			value = null;
		}
		else if (obj instanceof java.util.Date)
		{
			value = new DateToStringConverter().convert((java.util.Date) obj);
		}
		else if (obj instanceof Entity)
		{
			if (getWriteMode() != null)
			{
				switch (getWriteMode())
				{
					case ENTITY_IDS:
						value = ((Entity) obj).getIdValue().toString();
						break;
					case ENTITY_LABELS:
						value = ((Entity) obj).getLabelValue();
						break;
					default:
						throw new RuntimeException("Unknown write mode [" + getWriteMode() + "]");
				}
			}
			else
			{
				value = ((Entity) obj).getLabelValue();
			}
		}
		else if (obj instanceof Iterable<?>)
		{
			StringBuilder strBuilder = new StringBuilder();
			for (Object listItem : (Iterable<?>) obj)
			{
				if (strBuilder.length() > 0) strBuilder.append(',');
				strBuilder.append(toValue(listItem));
			}
			// TODO apply cell processors to list elements?
			value = strBuilder.toString();
		}
		else
		{
			value = obj.toString();
		}

		return AbstractCellProcessor.processCell(value, false, cellProcessors);
	}

	@Override
	public void close() throws IOException
	{
		// Nothing
	}

	@Override
	public void flush()
	{
		// Nothing
	}

	@Override
	public void clearCache()
	{
		// Nothing
	}

}
