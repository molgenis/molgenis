package org.molgenis.data.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.file.processor.AbstractCellProcessor;
import org.molgenis.data.file.processor.CellProcessor;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.support.AbstractWritable;
import org.molgenis.util.UnexpectedEnumException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Writable implementation for an excel sheet
 */
public class ExcelSheetWriter extends AbstractWritable
{
	private final Sheet sheet;
	private int row;

	/**
	 * process cells before writing
	 */
	private List<CellProcessor> cellProcessors;
	private Iterable<Attribute> cachedAttributes;

	ExcelSheetWriter(Sheet sheet, Iterable<Attribute> attributes, AttributeWriteMode attributeWriteMode,
			List<CellProcessor> cellProcessors)
	{
		if (sheet == null) throw new IllegalArgumentException("sheet is null");
		this.sheet = sheet;
		this.cellProcessors = cellProcessors;
		this.row = 0;

		if (attributes != null)
		{
			writeAttributeHeaders(attributes, attributeWriteMode);
		}
	}

	/**
	 * Add a new row to the sheet
	 */
	@Override
	public void add(Entity entity)
	{
		if (entity == null) throw new IllegalArgumentException("Entity cannot be null");
		if (cachedAttributes == null)
			throw new MolgenisDataException("The attribute names are not defined, call writeAttributeNames first");

		int i = 0;
		Row poiRow = sheet.createRow(row++);
		for (Attribute attribute : cachedAttributes)
		{
			Cell cell = poiRow.createCell(i++, CellType.STRING);
			cell.setCellValue(toValue(entity.get(attribute.getName())));
		}

		entity.getIdValue();
	}

	/**
	 * Write sheet column headers
	 */
	public void writeAttributeHeaders(Iterable<Attribute> attributes, AttributeWriteMode attributeWriteMode)
	{
		if (attributes == null) throw new IllegalArgumentException("Attributes cannot be null");
		if (attributeWriteMode == null) throw new IllegalArgumentException("AttributeWriteMode cannot be null");

		if (cachedAttributes == null)
		{
			Row poiRow = sheet.createRow(row++);

			// write header
			int i = 0;
			for (Attribute attribute : attributes)
			{
				Cell cell = poiRow.createCell(i++, CellType.STRING);

				switch (attributeWriteMode)
				{
					case ATTRIBUTE_LABELS:
						cell.setCellValue(
								AbstractCellProcessor.processCell(attribute.getLabel(), true, cellProcessors));
						break;
					case ATTRIBUTE_NAMES:
						cell.setCellValue(AbstractCellProcessor.processCell(attribute.getName(), true, cellProcessors));
						break;
					default:
						throw new UnexpectedEnumException(attributeWriteMode);
				}
			}

			// store header
			this.cachedAttributes = attributes;
		}
	}

	public void addCellProcessor(CellProcessor cellProcessor)
	{
		if (cellProcessors == null) cellProcessors = new ArrayList<>();
		cellProcessors.add(cellProcessor);
	}

	private String toValue(Object obj)
	{
		String value;
		if (obj == null)
		{
			value = null;
		}
		else if (obj instanceof Entity)
		{
			if (getEntityWriteMode() != null)
			{
				switch (getEntityWriteMode())
				{
					case ENTITY_IDS:
						value = ((Entity) obj).getIdValue().toString();
						break;
					case ENTITY_LABELS:
						Object labelValue = ((Entity) obj).getLabelValue();
						value = labelValue != null ? labelValue.toString() : null;
						break;
					default:
						throw new UnexpectedEnumException(getEntityWriteMode());
				}
			}
			else
			{
				Object labelValue = ((Entity) obj).getLabelValue();
				value = labelValue != null ? labelValue.toString() : null;
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
