package org.molgenis.data.validation.constraint;

public enum DefaultValueReferenceConstraint implements Constraint
{
	REFERENCE_EXISTS;

	public ConstraintType getType()
	{
		return ConstraintType.ENTITY;
	}
}
