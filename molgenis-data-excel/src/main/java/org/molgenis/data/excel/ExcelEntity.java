package org.molgenis.data.excel;

import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.processor.AbstractCellProcessor;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.support.AbstractMetaDataEntity;

/**
 * Entity implementation for Excel.
 * 
 * All attributes are of type String, values are processed using the given CellProcessors
 * 
 */
public class ExcelEntity extends AbstractMetaDataEntity
{
	private static final long serialVersionUID = 8928375571009145452L;
	private final transient Row row;
	private final Map<String, Integer> colNamesMap;
	private final List<CellProcessor> cellProcessors;

	public ExcelEntity(Row row, Map<String, Integer> colNamesMap, List<CellProcessor> cellProcessors,
			EntityMetaData entityMetaData)
	{
		super(entityMetaData);

		if (row == null) throw new IllegalArgumentException("row is null");
		if (colNamesMap == null) throw new IllegalArgumentException("column names map is null");

		this.row = row;
		this.colNamesMap = colNamesMap;
		this.cellProcessors = cellProcessors;
	}

	/**
	 * Gets an Attribute (Cell value).
	 * 
	 * All values are retrieved as String, returns null if the attributeName is unknown
	 */
	@Override
	public Object get(String attributeName)
	{
		Integer col = colNamesMap.get(attributeName);
		if (col != null)
		{
			Cell cell = row.getCell(col);
			if (cell != null)
			{
				return toValue(cell, cellProcessors);
			}
		}

		return null;
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public void set(String attributeName, Object value)
	{
		throw new UnsupportedOperationException();
	}

	// Gets a cell value as String and process the value with the given cellProcessors
	private String toValue(Cell cell, List<CellProcessor> cellProcessors)
	{
		String value;
		switch (cell.getCellType())
		{
			case Cell.CELL_TYPE_BLANK:
				value = null;
				break;
			case Cell.CELL_TYPE_STRING:
				value = cell.getStringCellValue();
				break;
			case Cell.CELL_TYPE_NUMERIC:
				if (DateUtil.isCellDateFormatted(cell)) value = cell.getDateCellValue().toString();
				else
				{
					// excel stores integer values as double values
					// read an integer if the double value equals the
					// integer value
					double x = cell.getNumericCellValue();
					if (x == Math.rint(x) && !Double.isNaN(x) && !Double.isInfinite(x)) value = String.valueOf((int) x);
					else value = String.valueOf(x);
				}
				break;
			case Cell.CELL_TYPE_BOOLEAN:
				value = String.valueOf(cell.getBooleanCellValue());
				break;
			case Cell.CELL_TYPE_FORMULA:
				// evaluate formula
				FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
				CellValue cellValue = evaluator.evaluate(cell);
				switch (cellValue.getCellType())
				{
					case Cell.CELL_TYPE_BOOLEAN:
						value = String.valueOf(cellValue.getBooleanValue());
						break;
					case Cell.CELL_TYPE_NUMERIC:
						// excel stores integer values as double values
						// read an integer if the double value equals the
						// integer value
						double x = cellValue.getNumberValue();
						if (x == Math.rint(x) && !Double.isNaN(x) && !Double.isInfinite(x)) value = String
								.valueOf((int) x);
						else value = String.valueOf(x);
						break;
					case Cell.CELL_TYPE_STRING:
						value = cellValue.getStringValue();
						break;
					case Cell.CELL_TYPE_BLANK:
						value = null;
						break;
					default:
						throw new MolgenisDataException("unsupported cell type: " + cellValue.getCellType());
				}
				break;
			default:
				throw new MolgenisDataException("unsupported cell type: " + cell.getCellType());
		}

		return AbstractCellProcessor.processCell(value, false, cellProcessors);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append("[");
		boolean first = true;
		for (String attr : colNamesMap.keySet())
		{
			if (!first)
			{
				sb.append(",");
			}
			sb.append(attr).append("=").append(get(attr));
			first = false;
		}
		sb.append("]");

		return sb.toString();
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		return colNamesMap.keySet();
	}

}
