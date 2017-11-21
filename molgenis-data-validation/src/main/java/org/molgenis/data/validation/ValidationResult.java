package org.molgenis.data.validation;

public interface ValidationResult
{
	boolean hasConstraintViolations();

	void accept(ValidationResultVisitor validationResultVisitor);
}
