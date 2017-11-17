package org.molgenis.data.validation.constraint;

public interface ConstraintViolation
{
	void accept(ConstraintViolationVisitor constraintViolationVisitor);
}
