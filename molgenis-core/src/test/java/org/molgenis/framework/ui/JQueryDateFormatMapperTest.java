package org.molgenis.framework.ui;

import org.testng.Assert;
import org.testng.annotations.Test;

public class JQueryDateFormatMapperTest
{

	@Test
	public void toJQueryDateFormatString()
	{
		String actual = JQueryDateFormatMapper.toJQueryDateFormat("yyyy-MM-dd");

		Assert.assertEquals(actual, "yy-mm-dd");
	}

	@Test
	public void toJQueryDateFormatStringString()
	{
		String actual = JQueryDateFormatMapper.toJQueryDateFormat("yyyy-MM-dd", "yyyy-MM-dd");
		Assert.assertEquals(actual, "yy-mm-dd");
	}

	@Test
	public void toJavaDateFormatString()
	{
		String actual = JQueryDateFormatMapper.toJavaDateFormat("yy-mm-dd");

		Assert.assertEquals(actual, "yyyy-MM-dd");
	}

	@Test
	public void toJavaDateFormatStringString()
	{
		String actual = JQueryDateFormatMapper.toJavaDateFormat("yy-mm-dd", "yy-mm-dd");
		Assert.assertEquals(actual, "yyyy-MM-dd");
	}
}
