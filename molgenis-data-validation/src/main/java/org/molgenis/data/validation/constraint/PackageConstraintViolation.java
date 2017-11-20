package org.molgenis.data.validation.constraint;

import org.molgenis.data.meta.model.Package;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class PackageConstraintViolation implements ConstraintViolation
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

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PackageConstraintViolation that = (PackageConstraintViolation) o;
		return packageConstraint == that.packageConstraint && Objects.equals(aPackage.getId(), that.aPackage.getId());
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(packageConstraint, aPackage.getId());
	}
}
