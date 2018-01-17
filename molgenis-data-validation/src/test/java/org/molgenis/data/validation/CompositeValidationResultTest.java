package org.molgenis.data.validation;

import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class CompositeValidationResultTest
{
	@Test
	public void testHasConstraintViolationsTrue()
	{
		ValidationResult validationResult0 = mock(ValidationResult.class);
		ValidationResult validationResult1 = mock(ValidationResult.class);
		when(validationResult0.hasConstraintViolations()).thenReturn(false);
		when(validationResult1.hasConstraintViolations()).thenReturn(true);
		CompositeValidationResult compositeValidationResult = new CompositeValidationResult();
		compositeValidationResult.addValidationResult(validationResult0);
		compositeValidationResult.addValidationResult(validationResult1);
		assertTrue(compositeValidationResult.hasConstraintViolations());
	}

	@Test
	public void testHasConstraintViolationsFalse()
	{
		ValidationResult validationResult = mock(ValidationResult.class);
		when(validationResult.hasConstraintViolations()).thenReturn(false);
		CompositeValidationResult compositeValidationResult = new CompositeValidationResult();
		compositeValidationResult.addValidationResult(validationResult);
		assertFalse(compositeValidationResult.hasConstraintViolations());
	}

	@Test
	public void testAccept() throws Exception
	{
		ValidationResult validationResult0 = mock(ValidationResult.class);
		ValidationResult validationResult1 = mock(ValidationResult.class);
		CompositeValidationResult compositeValidationResult = new CompositeValidationResult();
		compositeValidationResult.addValidationResult(validationResult0);
		compositeValidationResult.addValidationResult(validationResult1);
		ValidationResultVisitor validationResultVisitor = mock(ValidationResultVisitor.class);
		compositeValidationResult.accept(validationResultVisitor);
		verify(validationResult0).accept(validationResultVisitor);
		verify(validationResult1).accept(validationResultVisitor);
	}
}