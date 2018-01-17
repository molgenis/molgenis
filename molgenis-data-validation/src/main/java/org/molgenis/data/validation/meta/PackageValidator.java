package org.molgenis.data.validation.meta;

import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.MetaUtils;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.system.SystemPackageRegistry;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.validation.meta.PackageConstraint.NAME;
import static org.molgenis.data.validation.meta.PackageConstraint.SYSTEM_PACKAGE_READ_ONLY;

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

	public PackageValidationResult validate(Package aPackage)
	{
		EnumSet<PackageConstraint> constraintViolations = EnumSet.noneOf(PackageConstraint.class);

		if (!isValidSystemPackage(aPackage))
		{
			constraintViolations.add(SYSTEM_PACKAGE_READ_ONLY);
		}
		if (!isValidPackageIdentifier(aPackage))
		{
			constraintViolations.add(NAME);
		}

		return PackageValidationResult.create(aPackage, constraintViolations);
	}

	private boolean isValidSystemPackage(Package aPackage)
	{
		return !MetaUtils.isSystemPackage(aPackage) || systemPackageRegistry.containsPackage(aPackage);
	}

	private static boolean isValidPackageIdentifier(Package aPackage)
	{
		try
		{
			// TODO get rid of try-deprecated-exception-catch
			NameValidator.validatePackageId(aPackage.getId());
		}
		catch (MolgenisDataException e)
		{
			return false;
		}
		return true;
	}
}
