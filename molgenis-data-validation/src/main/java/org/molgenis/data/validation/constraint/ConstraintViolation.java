package org.molgenis.data.validation.constraint;

public interface ConstraintViolation
{
	Constraint getConstraint();

	void accept(ConstraintViolationVisitor constraintViolationVisitor);
}
