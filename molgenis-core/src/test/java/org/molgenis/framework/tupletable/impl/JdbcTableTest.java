package org.molgenis.framework.tupletable.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Arrays;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.framework.tupletable.TupleTable;
import org.molgenis.util.tuple.Tuple;
import org.testng.Assert;
import org.testng.annotations.Test;

public class JdbcTableTest
{
	@Test
	public void testJDBCTable() throws SQLException, DatabaseException, TableException
	{
		Tuple tuple0 = mock(Tuple.class);
		when(tuple0.getColNames()).thenReturn(Arrays.asList("Name", "Continent")).getMock();
		when(tuple0.getString("Name")).thenReturn("Netherlands");

		Database db = mock(Database.class);
		String query = "SELECT Name, Continent FROM Country";
		QueryRule queryRule = new QueryRule("Code", Operator.EQUALS, "NLD");
		when(db.sql(query, queryRule)).thenReturn(Arrays.asList(tuple0));
		when(db.sql("SELECT COUNT(*) FROM Country", queryRule)).thenReturn(Arrays.asList(tuple0));

		TupleTable jdbcTable = new JdbcTable(db, query, Arrays.asList(queryRule));
		try
		{
			// check columns
			Assert.assertEquals(jdbcTable.getColumns().get(0).getName(), "Name");
			Assert.assertEquals(jdbcTable.getColumns().get(1).getName(), "Continent");

			// check rows
			int i = 1;
			for (Tuple row : jdbcTable)
			{
				Assert.assertEquals(row.getString("Name"), "Netherlands");

				i = i + 1;
			}
		}
		finally
		{
			jdbcTable.close();
		}
	}
}
