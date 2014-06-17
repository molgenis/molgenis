package org.molgenis.util;

import static org.testng.Assert.assertEquals;

import java.io.IOException;

import org.testng.annotations.Test;

public class MySqlFileUtilTest
{
	@Test
	public void getMySqlQueryFromFileTest() throws IOException
	{
		String pathname = "/test_mysql_repo_util_query.sql";
		String query = MySqlFileUtil.getMySqlQueryFromFile(getClass(), pathname);
		assertEquals(query, "SELECT * FROM `test` WHERE `test`='test';");
	}
}
