package org.molgenis.data.validation.meta;

import org.molgenis.data.meta.MetaUtils;
import org.molgenis.data.meta.NameValidator;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.system.SystemPackageRegistry;
import org.molgenis.data.validation.ConstraintViolation;
import org.molgenis.data.validation.MolgenisValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * {@link Package} validator
 */
@Component
public class PackageValidator
{
	private final SystemPackageRegistry systemPackageRegistry;

	private final static Logger LOG = LoggerFactory.getLogger(PackageValidator.class);

	@Autowired
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
			LOG.error("validatePackageAllowed, the system package registry does not contain package: " + package_.getFullyQualifiedName());
			throw new MolgenisValidationException(new ConstraintViolation("Modifying system packages is not allowed"));
		}
	}

	private static void validatePackageName(Package package_)
	{
		NameValidator.validatePackageId(package_.getId());
	}
}
