package org.molgenis.util;

import java.io.IOException;
import java.nio.charset.Charset;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
public class ResourceUtilsTest
{

	@Test
	public void getStringClassString() throws IOException
	{
		assertEquals("example resource", ResourceUtils.getString(getClass(), "/resource.txt"));
	}

	@Test
	public void getStringClassStringCharset() throws IOException
	{
		assertEquals("example resource", ResourceUtils.getString(getClass(), "/resource.txt", Charset.forName("UTF-8")));
	}
}
