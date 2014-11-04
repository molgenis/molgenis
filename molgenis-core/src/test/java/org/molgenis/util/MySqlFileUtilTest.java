package org.molgenis.util;

import static org.testng.Assert.assertEquals;

import java.io.IOException;

import org.testng.annotations.Test;

public class MySqlFileUtilTest
{
	@Test
	public void getMySqlQueryFromFileTest() throws IOException
	{
		String query = ResourceUtils.getString(getClass(), "/test_mysql_repo_util_query.sql");
		assertEquals(query, "SELECT * FROM `test` WHERE `test`='test';");
	}
}
