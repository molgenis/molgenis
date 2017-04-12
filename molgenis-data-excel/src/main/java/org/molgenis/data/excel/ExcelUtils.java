package org.molgenis.data.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.molgenis.data.DataConverter;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.processor.AbstractCellProcessor;
import org.molgenis.data.processor.CellProcessor;

import java.util.List;

class ExcelUtils
{
	static String toValue(Cell cell)
	{
		return toValue(cell, null);
	}

	// Gets a cell value as String and process the value with the given cellProcessors
	static String toValue(Cell cell, List<CellProcessor> cellProcessors)
	{
		String value;
		switch (cell.getCellTypeEnum())
		{
			case BLANK:
				value = null;
				break;
			case STRING:
				value = cell.getStringCellValue();
				break;
			case NUMERIC:
				if (DateUtil.isCellDateFormatted(cell))
				{
					value = DataConverter.toString(cell.getDateCellValue());
				}
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
			case BOOLEAN:
				value = String.valueOf(cell.getBooleanCellValue());
				break;
			case FORMULA:
				// evaluate formula
				FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
				CellValue cellValue = evaluator.evaluate(cell);
				switch (cellValue.getCellTypeEnum())
				{
					case BOOLEAN:
						value = String.valueOf(cellValue.getBooleanValue());
						break;
					case NUMERIC:
						if (DateUtil.isCellDateFormatted(cell))
						{
							value = DataConverter.toString(DateUtil.getJavaDate(cellValue.getNumberValue(), false));
						}
						else
						{
							// excel stores integer values as double values
							// read an integer if the double value equals the
							// integer value
							double x = cellValue.getNumberValue();
							if (x == Math.rint(x) && !Double.isNaN(x) && !Double.isInfinite(x))
								value = String.valueOf((int) x);
							else value = String.valueOf(x);
						}
						break;
					case STRING:
						value = cellValue.getStringValue();
						break;
					case BLANK:
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
}