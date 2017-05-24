package org.molgenis.data.matrix.impl;

import gnu.trove.TObjectIntHashMap;
import org.molgenis.data.MolgenisDataException;
import org.ujmp.core.Matrix;

import java.io.File;
import java.io.IOException;

class DoubleMatrix
{
	private final File file;
	private final char seperator;

	private TObjectIntHashMap columnMap = new TObjectIntHashMap();
	private TObjectIntHashMap rowMap = new TObjectIntHashMap();
	private Matrix matrix;

	private boolean inited = false;

	public DoubleMatrix(File file, char seperator)
	{
		this.file = file;
		this.seperator = seperator;
	}

	private void init()
	{
		try
		{
			matrix = org.ujmp.core.Matrix.Factory.linkTo().file(file.getAbsolutePath()).asDenseCSV(seperator);
			setRowIndicesMap();
			setColumnIndicesMap();
		}
		catch (IOException e)
		{
			throw new MolgenisDataException(e);
		}
		inited = true;
	}

	public double getValueByIndex(int row, int column)
	{
		if (!inited) init();
		if (row > matrix.getRowCount()) throw new IndexOutOfBoundsException(
				"Index [" + row + "] is greater than the number of columns in the matrix [" + matrix.getRowCount()
						+ "]");
		if (column > matrix.getColumnCount()) throw new IndexOutOfBoundsException(
				"Index [" + column + "] is greater than the number of columns in the matrix [" + matrix.getColumnCount()
						+ "]");
		return matrix.getAsDouble(row, column);
	}

	public double getValueByName(String row, String column)
	{
		if (!inited) init();
		Integer rowIndex = rowMap.get(row);
		Integer columnIndex = columnMap.get(column);
		if (rowIndex == null) throw new MolgenisDataException("Unknown row name [" + row + "]");
		if (columnIndex == null) throw new MolgenisDataException("Unknown column name [" + column + "]");
		return getValueByIndex(rowIndex, columnIndex);
	}

	private void setRowIndicesMap()
	{
		int i = 0;
		String gene;
		while (i < matrix.getRowCount())
		{
			gene = matrix.getAsString(i, 0);
			rowMap.put(gene, i);
			i++;
		}
	}

	private void setColumnIndicesMap()
	{
		int i = 0;
		String hpo;
		while (i < matrix.getColumnCount())
		{
			hpo = matrix.getAsString(0, i);
			columnMap.put(hpo, i);
			i++;
		}
	}
}
