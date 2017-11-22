package org.molgenis.data.validation.meta;

import com.google.auto.value.AutoValue;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.validation.ValidationResult;
import org.molgenis.data.validation.ValidationResultVisitor;

import java.util.EnumSet;
import java.util.Set;

@AutoValue
public abstract class PackageValidationResult implements ValidationResult
{
	public abstract Package getPackage();

	public abstract Set<PackageConstraint> getConstraintViolations();

	public static PackageValidationResult create(Package newPackage)
	{
		return create(newPackage, EnumSet.noneOf(PackageConstraint.class));
	}

	public static PackageValidationResult create(Package newPackage, Set<PackageConstraint> newConstraints)
	{
		return new AutoValue_PackageValidationResult(newPackage, newConstraints);
	}

	@Override
	public boolean hasConstraintViolations()
	{
		return !getConstraintViolations().isEmpty();
	}

	@Override
	public void accept(ValidationResultVisitor validationResultVisitor)
	{
		validationResultVisitor.visit(this);
	}
}
