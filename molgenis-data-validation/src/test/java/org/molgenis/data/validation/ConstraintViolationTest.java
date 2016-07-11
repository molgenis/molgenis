package org.molgenis.data.validation;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.Arrays;

import org.testng.annotations.Test;

public class ConstraintViolationTest
{
	@Test
	public void renumberRowIndexRowNumber()
	{
		ConstraintViolation constraintViolation = new ConstraintViolation("test", 1l);
		constraintViolation.renumberRowIndex(Arrays.asList(3, 2, 1));
		assertEquals(constraintViolation.getRownr(), Long.valueOf(3l));
	}

	@Test
	public void renumberRowIndexNoRowNumber()
	{
		ConstraintViolation constraintViolation = new ConstraintViolation("test", null);
		constraintViolation.renumberRowIndex(Arrays.asList(3, 2, 1));
		assertNull(constraintViolation.getRownr());
	}
}
