package org.molgenis.data.validation.constraint;

public enum AttributeValueConstraint implements Constraint
{
	EMAIL, ENTITY_REFERENCE, ENUM, EXPRESSION, HYPERLINK, MAX_LENGTH, NOT_NULL, RANGE, READ_ONLY, TYPE, UNIQUE;

	public ConstraintType getType()
	{
		return ConstraintType.ATTRIBUTE_VALUE;
	}
}
