package org.molgenis.io.excel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.molgenis.io.TupleReader;
import org.molgenis.io.processor.AbstractCellProcessor;
import org.molgenis.io.processor.CellProcessor;
import org.molgenis.util.tuple.AbstractTuple;
import org.molgenis.util.tuple.Tuple;

public class ExcelSheetReader implements TupleReader
{
	private final org.apache.poi.ss.usermodel.Sheet sheet;
	private final boolean hasHeader;

	/** process cells after reading */
	private List<CellProcessor> cellProcessors;
	/** column names index */
	private Map<String, Integer> colNamesMap;

	ExcelSheetReader(org.apache.poi.ss.usermodel.Sheet sheet, boolean hasHeader, List<CellProcessor> cellProcessors)
	{
		if (sheet == null) throw new IllegalArgumentException("sheet is null");
		this.sheet = sheet;
		this.hasHeader = hasHeader;
		this.cellProcessors = cellProcessors;
	}

	public String getName()
	{
		return sheet.getSheetName();
	}

	public int getNrRows()
	{
		return sheet.getLastRowNum() + 1; // getLastRowNum is 0-based
	}

	@Override
	public boolean hasColNames()
	{
		return hasHeader;
	}

	@Override
	public Iterator<String> colNamesIterator() throws IOException
	{
		if (!hasHeader) return null;

		Iterator<Row> it = sheet.iterator();
		if (!it.hasNext()) return null;

		if (colNamesMap == null) colNamesMap = toColNamesMap(it.next());
		return colNamesMap != null ? colNamesMap.keySet().iterator() : null;
	}

	@Override
	public Iterator<Tuple> iterator()
	{
		final Iterator<Row> it = sheet.iterator();
		if (!it.hasNext()) return Collections.<Tuple> emptyList().iterator();

		// create column header index once and reuse
		final Map<String, Integer> colNamesMap = hasHeader ? (this.colNamesMap == null ? toColNamesMap(it.next()) : this.colNamesMap) : null;

		return new Iterator<Tuple>()
		{
			@Override
			public boolean hasNext()
			{
				return it.hasNext();
			}

			@Override
			public Tuple next()
			{
				if (colNamesMap == null) return new RowTuple(it.next(), cellProcessors);
				else return new RowIndexTuple(it.next(), colNamesMap, cellProcessors);
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public void addCellProcessor(CellProcessor cellProcessor)
	{
		if (cellProcessors == null) cellProcessors = new ArrayList<CellProcessor>();
		cellProcessors.add(cellProcessor);
	}

	@Override
	public void close() throws IOException
	{
		// noop
	}

	private Map<String, Integer> toColNamesMap(Row headerRow)
	{
		if (headerRow == null) return null;

		Map<String, Integer> columnIdx = new LinkedHashMap<String, Integer>();
		int i = 0;
		for (Iterator<Cell> it = headerRow.cellIterator(); it.hasNext();)
		{
			String header = AbstractCellProcessor.processCell(it.next().getStringCellValue(), true, cellProcessors);
			columnIdx.put(header, i++);
		}
		return columnIdx;
	}

	private static String toValue(Cell cell, List<CellProcessor> cellProcessors)
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
						throw new RuntimeException("unsupported cell type: " + cellValue.getCellType());
				}
				break;
			default:
				throw new RuntimeException("unsupported cell type: " + cell.getCellType());
		}
		return AbstractCellProcessor.processCell(value, false, cellProcessors);
	}

	private static class RowTuple extends AbstractTuple
	{
		private static final long serialVersionUID = 1L;

		private final Row row;
		private final List<CellProcessor> cellProcessors;

		public RowTuple(Row row, List<CellProcessor> cellProcessors)
		{
			if (row == null) throw new IllegalArgumentException("row is null");
			this.row = row;
			this.cellProcessors = cellProcessors;
		}

		@Override
		public int getNrCols()
		{
			return row.getLastCellNum();
		}

		@Override
		public boolean hasColNames()
		{
			return false;
		}

		@Override
		public Iterable<String> getColNames()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Object get(String colName)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Object get(int col)
		{
			Cell cell = row.getCell(col);
			return cell != null ? toValue(cell, cellProcessors) : null;
		}
	}

	private static class RowIndexTuple extends AbstractTuple
	{
		private static final long serialVersionUID = 1L;

		private final transient Row row;
		private final Map<String, Integer> colNamesMap;
		private final List<CellProcessor> cellProcessors;

		public RowIndexTuple(Row row, Map<String, Integer> colNamesMap, List<CellProcessor> cellProcessors)
		{
			if (row == null) throw new IllegalArgumentException("row is null");
			if (colNamesMap == null) throw new IllegalArgumentException("column names map is null");
			this.row = row;
			this.colNamesMap = colNamesMap;
			this.cellProcessors = cellProcessors;
		}

		@Override
		public int getNrCols()
		{
			return colNamesMap.size();
		}

		@Override
		public boolean hasColNames()
		{
			return true;
		}

		@Override
		public Iterable<String> getColNames()
		{
			return Collections.unmodifiableSet(colNamesMap.keySet());
		}

		@Override
		public Object get(String colName)
		{
			Integer col = colNamesMap.get(colName);
			return col != null ? get(col) : null;
		}

		@Override
		public Object get(int col)
		{
			Cell cell = row.getCell(col);
			return cell != null ? toValue(cell, cellProcessors) : null;
		}
	}
}
