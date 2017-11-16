package org.molgenis.data.validation.constraint;

public enum TagConstraint implements Constraint
{
	UNKNOWN_RELATION_IRI;

	public ConstraintType getType()
	{
		return ConstraintType.TAG;
	}
}
