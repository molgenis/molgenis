package org.molgenis.util;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.Charset;

import org.testng.annotations.Test;

public class ResourceUtilsTest
{

	@Test
	public void getStringClassString() throws IOException
	{
		assertEquals(ResourceUtils.getString(getClass(), "/resource.txt"), "example resource");
	}

	@Test
	public void getStringClassStringCharset() throws IOException
	{
		assertEquals(ResourceUtils.getString(getClass(), "/resource.txt", Charset.forName("UTF-8")),
				"example resource");
	}

	@Test
	public void getBytes() throws IOException
	{
		assertEquals(ResourceUtils.getBytes(getClass(), "/resource.txt").length, 16);
	}

	@Test
	public void getMySqlQueryFromFileTest() throws IOException
	{
		String query = ResourceUtils.getString(getClass(), "/test_mysql_repo_util_query.sql");
		assertEquals(query, "SELECT * FROM `test` WHERE `test`='test';");
	}
}
