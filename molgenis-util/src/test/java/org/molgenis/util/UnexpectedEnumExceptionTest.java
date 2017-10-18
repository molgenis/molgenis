package org.molgenis.util;

import org.testng.annotations.Test;

import static org.molgenis.util.UnexpectedEnumExceptionTest.MyEnum.MY_ENUM_CONSTANT;
import static org.testng.Assert.assertEquals;

public class UnexpectedEnumExceptionTest
{
	enum MyEnum
	{
		MY_ENUM_CONSTANT
	}

	@Test
	public void UnexpectedEnumException()
	{
		assertEquals(new UnexpectedEnumException(MY_ENUM_CONSTANT).getMessage(),
				"Unexpected enum constant 'MY_ENUM_CONSTANT' for type 'MyEnum'");
	}
}