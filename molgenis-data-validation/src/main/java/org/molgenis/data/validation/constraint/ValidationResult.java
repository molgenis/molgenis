package org.molgenis.data.validation.constraint;

public interface ValidationResult
{
	boolean hasConstraintViolations();

	void accept(ValidationResultVisitor validationResultVisitor);
}
