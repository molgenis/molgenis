package org.molgenis.compute5;

import org.testng.annotations.Test;

public class ValidatorTest
{
	@Test(expectedExceptions = ComputeException.class)  
	public void testvalidateParameterName() throws ComputeException
	{
		Validator.validateParameterName(".");
	}
}
