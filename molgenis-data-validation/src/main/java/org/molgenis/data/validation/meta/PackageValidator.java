package org.molgenis.data.validation.meta;

import org.molgenis.data.meta.MetaUtils;
import org.molgenis.data.meta.NameValidator;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.system.SystemPackageRegistry;
import org.molgenis.data.validation.ValidationException;
import org.molgenis.data.validation.constraint.PackageConstraint;
import org.molgenis.data.validation.constraint.PackageConstraintViolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * {@link Package} validator
 *
 * TODO change 'validate(Package aPackage)' return type from void to Set<PackageConstraintViolation>
 */
@Component
public class PackageValidator
{
	private final SystemPackageRegistry systemPackageRegistry;

	private final static Logger LOG = LoggerFactory.getLogger(PackageValidator.class);

	public PackageValidator(SystemPackageRegistry systemPackageRegistry)
	{
		this.systemPackageRegistry = requireNonNull(systemPackageRegistry);
	}

	public void validate(Package package_)
	{
		validatePackageAllowed(package_);
		validatePackageName(package_);
	}

	private void validatePackageAllowed(Package package_)
	{
		if (MetaUtils.isSystemPackage(package_) && !systemPackageRegistry.containsPackage(package_))
		{
			throw new ValidationException(
					new PackageConstraintViolation(PackageConstraint.SYSTEM_PACKAGE_READ_ONLY, package_));
		}
	}

	private static void validatePackageName(Package package_)
	{
		NameValidator.validatePackageId(package_.getId());
	}
}
