package org.molgenis.data.validation.constraint;

import org.molgenis.data.meta.model.Package;

import static java.util.Objects.requireNonNull;

public final class PackageConstraintViolation implements ConstraintViolation
{
	private final PackageConstraint packageConstraint;
	private final Package aPackage;

	public PackageConstraintViolation(PackageConstraint packageConstraint, Package aPackage)
	{
		this.packageConstraint = requireNonNull(packageConstraint);
		this.aPackage = requireNonNull(aPackage);
	}

	public PackageConstraint getConstraint()
	{
		return packageConstraint;
	}

	public Package getPackage()
	{
		return aPackage;
	}

	@Override
	public void accept(ConstraintViolationVisitor constraintViolationVisitor)
	{
		constraintViolationVisitor.visit(this);
	}
}
