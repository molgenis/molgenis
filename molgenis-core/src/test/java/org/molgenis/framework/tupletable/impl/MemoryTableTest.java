package org.molgenis.framework.tupletable.impl;

import org.molgenis.framework.tupletable.TableException;
import org.molgenis.framework.tupletable.TupleTable;
import org.molgenis.util.tuple.Tuple;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MemoryTableTest
{

	@Test
	public void test1() throws TableException
	{
		TupleTable table = MemoryTableFactory.create(5, 5);

		// check columns
		Assert.assertEquals("col1", table.getColumns().get(0).getName());
		Assert.assertEquals("col2", table.getColumns().get(1).getName());

		// check rows
		Assert.assertEquals(5, table.getRows().size());

		// check iterator
		int i = 1;
		for (Tuple row : table)
		{
			Assert.assertEquals(5, row.getNrCols());
			Assert.assertEquals(row.get("col1"), "val1," + i);
			Assert.assertEquals(row.get("col2"), "val2," + i);
			i = i + 1;
		}
	}

	@Test
	public void testLimitOffset() throws TableException
	{
		TupleTable table = MemoryTableFactory.create(5, 5);

		table.setLimitOffset(2, 3);

		// limit == 2
		Assert.assertEquals(table.getRows().size(), 2);

		// offset = 3, so we skip first1-first3 and expect first4
		Assert.assertEquals(table.getRows().get(0).getString("col1"), "val1,4");

		// remove filters again
		table.setLimitOffset(0, 0);
	}

	@Test
	public void testColLimitOffset() throws TableException
	{
		TupleTable table = MemoryTableFactory.create(5, 5);

		table.setColLimit(2);
		table.setColOffset(1);

		// limit == 1
		int i = 1;
		for (Tuple row : table.getRows())
		{
			Assert.assertEquals(row.getNrCols(), 2);
			Assert.assertEquals(row.get("col2"), "val2," + i);
			Assert.assertEquals(row.get("col3"), "val3," + i);
			++i;
		}

		// remove filters again
		table.setColLimit(0);
		table.setColOffset(0);
	}
}
