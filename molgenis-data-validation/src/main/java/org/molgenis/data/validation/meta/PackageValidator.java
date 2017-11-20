package org.molgenis.data.validation.meta;

import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.MetaUtils;
import org.molgenis.data.meta.NameValidator;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.system.SystemPackageRegistry;
import org.molgenis.data.validation.constraint.PackageConstraint;
import org.molgenis.data.validation.constraint.PackageConstraintViolation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * {@link Package} validator
 */
@Component
public class PackageValidator
{
	private final SystemPackageRegistry systemPackageRegistry;

	PackageValidator(SystemPackageRegistry systemPackageRegistry)
	{
		this.systemPackageRegistry = requireNonNull(systemPackageRegistry);
	}

	public Collection<PackageConstraintViolation> validate(Package aPackage)
	{
		List<PackageConstraintViolation> constraintViolations = new ArrayList<>();
		validatePackageAllowed(aPackage).ifPresent(constraintViolations::add);
		validatePackageName(aPackage).ifPresent(constraintViolations::add);
		return constraintViolations;
	}

	private Optional<PackageConstraintViolation> validatePackageAllowed(Package aPackage)
	{
		if (MetaUtils.isSystemPackage(aPackage) && !systemPackageRegistry.containsPackage(aPackage))
		{
			return Optional.of(new PackageConstraintViolation(PackageConstraint.SYSTEM_PACKAGE_READ_ONLY, aPackage));
		}
		return Optional.empty();
	}

	private static Optional<PackageConstraintViolation> validatePackageName(Package aPackage)
	{
		try
		{
			// TODO get rid of try-deprecated-exception-catch
			NameValidator.validatePackageId(aPackage.getId());
		}
		catch (MolgenisDataException e)
		{
			return Optional.of(new PackageConstraintViolation(PackageConstraint.NAME, aPackage));
		}
		return Optional.empty();
	}
}
