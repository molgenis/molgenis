package org.molgenis.compute5;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ValidatorTest
{
	@Test(expectedExceptions = ComputeException.class)  
	public void testvalidateParameterName() throws ComputeException
	{
		Validator.validateParameterName(".");
	}

	@Test
	public void testCypher1()
	{
		try
		{
			Validator.validateParameterName("d7b");
		}
		catch (Exception e)
		{
			Assert.fail("incorrect validation");
		}
	}

	@Test(expectedExceptions = Exception.class)
	public void testCypher2()
	{
		Validator.validateParameterName("7b");
	}

	@Test
	public void testUnderscore()
	{
		try
		{
			Validator.validateParameterName("out_out");
		}
		catch (Exception e)
		{
			Assert.fail("incorrect validation");
		}
	}

}
