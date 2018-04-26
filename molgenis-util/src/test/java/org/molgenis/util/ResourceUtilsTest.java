package org.molgenis.util;

import org.testng.annotations.Test;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.testng.Assert.assertEquals;

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
		assertEquals(ResourceUtils.getString(getClass(), "/resource.txt", UTF_8), "example resource");
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
