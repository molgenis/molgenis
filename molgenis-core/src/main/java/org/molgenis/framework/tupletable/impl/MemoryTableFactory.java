package org.molgenis.framework.tupletable.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.framework.tupletable.TupleTable;
import org.molgenis.util.tuple.Tuple;
import org.molgenis.util.tuple.ValueIndexTuple;

// TODO move to test folder
public class MemoryTableFactory
{
	public static TupleTable create()
	{
		return create(5, 5);
	}

	public static TupleTable create(int nrows, int ncols)
	{
		Map<String, Integer> headerIndex = new LinkedHashMap<String, Integer>(ncols);
		for (int j = 1; j <= ncols; j++)
			headerIndex.put("col" + j, j - 1);

		List<Tuple> tuples = new ArrayList<Tuple>(nrows);
		for (int i = 1; i <= nrows; i++)
		{

			List<String> values = new ArrayList<String>(ncols);
			for (int j = 1; j <= ncols; j++)
				values.add("val" + j + "," + i);
			tuples.add(new ValueIndexTuple(headerIndex, values));
		}

		return new MemoryTable(tuples);
	}
}
