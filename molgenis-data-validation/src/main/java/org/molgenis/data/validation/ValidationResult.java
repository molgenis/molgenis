package org.molgenis.data.validation;

/**
 * @see org.molgenis.data.validation.meta.AttributeValidationResult
 * @see org.molgenis.data.validation.meta.EntityTypeValidationResult
 * @see org.molgenis.data.validation.meta.PackageValidationResult
 * @see CompositeValidationResult
 */
public interface ValidationResult
{
	boolean hasConstraintViolations();

	void accept(ValidationResultVisitor validationResultVisitor);
}
