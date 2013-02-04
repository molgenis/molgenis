package org.molgenis.util.tuple;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AbstractTupleTest
{
	private AbstractTuple tuple;

	@BeforeMethod
	public void setUp()
	{
		tuple = new AbstractTuple()
		{
			private static final long serialVersionUID = 1L;

			private final List<String> header = Arrays.<String> asList("col1", "col2", "col3", "col4", "col5", "col6",
					"col7", "col8", "col9", "col10", "col11");
			private final List<Object> values = Arrays.<Object> asList("str", 1, true, 1.23, null, "2000-10-20",
					"2000-10-20 10:11:12", "1,2,3", Arrays.asList("1", "2", "3"), Date.valueOf("2000-10-20"),
					Timestamp.valueOf("2000-10-20 10:11:12"));

			@Override
			public int getNrCols()
			{
				return header.size();
			}

			@Override
			public Iterable<String> getColNames()
			{
				return header;
			}

			@Override
			public Object get(int col)
			{
				return values.get(col);
			}

			@Override
			public Object get(String colName)
			{
				for (int i = 0; i < header.size(); ++i)
					if (header.get(i).equals(colName)) return values.get(i);
				return null;
			}
		};
	}

	@Test
	public void hasColNames()
	{
		assertTrue(tuple.hasColNames());
	}

	@Test
	public void isNull_colName()
	{
		assertFalse(tuple.isNull("col1"));
		assertTrue(tuple.isNull("col_unknown"));
	}

	@Test
	public void isNull_colIndex()
	{
		assertFalse(tuple.isNull(0));
	}

	@Test
	public void getString_colName()
	{
		assertEquals(tuple.getString("col1"), "str");
		assertEquals(tuple.getString("col_unknown"), null);
	}

	@Test
	public void getString_colIndex()
	{
		assertEquals(tuple.getString(0), "str");
	}

	@Test
	public void getInt_colName()
	{
		assertEquals(tuple.getInt("col2"), Integer.valueOf(1));
		assertEquals(tuple.getInt("col_unknown"), null);
	}

	@Test
	public void getInt_colIndex()
	{
		assertEquals(tuple.getInt(1), Integer.valueOf(1));
	}

	@Test
	public void getLong_colName()
	{
		assertEquals(tuple.getLong("col2"), Long.valueOf(1));
		assertEquals(tuple.getLong("col_unknown"), null);
	}

	@Test
	public void getLong_colIndex()
	{
		assertEquals(tuple.getLong(1), Long.valueOf(1));
	}

	@Test
	public void getBoolean_colName()
	{
		assertEquals(tuple.getBoolean("col3"), Boolean.valueOf(true));
		assertEquals(tuple.getBoolean("col_unknown"), null);
	}

	@Test
	public void getBoolean_colIndex()
	{
		assertEquals(tuple.getBoolean(2), Boolean.TRUE);
	}

	@Test
	public void getDouble_colName()
	{
		assertEquals(tuple.getDouble("col4"), Double.valueOf(1.23));
		assertEquals(tuple.getDouble("col_unknown"), null);
	}

	@Test
	public void getDouble_colIndex()
	{
		assertEquals(tuple.getDouble(3), 1.23, 1E-6);
	}

	@Test
	public void getDate_colName_Object()
	{
		assertEquals(tuple.getDate("col10"), Date.valueOf("2000-10-20"));
		assertEquals(tuple.getDate("col_unknown"), null);
	}

	@Test
	public void getDate_colName()
	{
		assertEquals(tuple.getDate("col6"), Date.valueOf("2000-10-20"));
		assertEquals(tuple.getDate("col_unknown"), null);
	}

	@Test
	public void getDate_colIndex()
	{
		assertEquals(tuple.getDate(5), Date.valueOf("2000-10-20"));
	}

	@Test
	public void getTimestamp_colName_Object()
	{
		assertEquals(tuple.getTimestamp("col11"), Timestamp.valueOf("2000-10-20 10:11:12"));
		assertEquals(tuple.getTimestamp("col_unknown"), null);
	}

	@Test
	public void getTimestamp_colName()
	{
		assertEquals(tuple.getTimestamp("col7"), Timestamp.valueOf("2000-10-20 10:11:12"));
		assertEquals(tuple.getTimestamp("col_unknown"), null);
	}

	@Test
	public void getTimestamp_colIndex()
	{
		assertEquals(tuple.getTimestamp(6), Timestamp.valueOf("2000-10-20 10:11:12"));
	}

	@Test
	public void getList_colName()
	{
		assertEquals(tuple.getList("col8"), Arrays.asList("1", "2", "3"));
		assertEquals(tuple.getList("col_unknown"), null);
	}

	@Test
	public void getList_colName_Object()
	{
		List<String> list = tuple.getList("col9");
		assertEquals(list, Arrays.asList("1", "2", "3"));
		assertEquals(tuple.getList("col_unknown"), null);
	}

	@Test
	public void getList_colIndex()
	{
		assertEquals(tuple.getList(7), Arrays.asList("1", "2", "3"));
	}
}
