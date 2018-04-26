package org.molgenis.data.validation;

import org.testng.annotations.Test;

import java.util.Arrays;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class ConstraintViolationTest
{
	@Test
	public void renumberRowIndexRowNumber()
	{
		ConstraintViolation constraintViolation = new ConstraintViolation("test", 1L);
		constraintViolation.renumberRowIndex(Arrays.asList(3, 2, 1));
		assertEquals(constraintViolation.getRowNr(), Long.valueOf(3L));
	}

	@Test
	public void renumberRowIndexNoRowNumber()
	{
		ConstraintViolation constraintViolation = new ConstraintViolation("test", null);
		constraintViolation.renumberRowIndex(Arrays.asList(3, 2, 1));
		assertNull(constraintViolation.getRowNr());
	}
}
