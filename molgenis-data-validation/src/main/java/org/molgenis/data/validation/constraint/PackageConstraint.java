package org.molgenis.data.validation.constraint;

public enum PackageConstraint implements Constraint
{
	SYSTEM_PACKAGE_READ_ONLY;

	public ConstraintType getType()
	{
		return ConstraintType.PACKAGE;
	}
}
