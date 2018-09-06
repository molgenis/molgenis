package org.molgenis.data.validation.meta;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.system.SystemPackageRegistry;
import org.molgenis.data.util.MetaUtils;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.validation.ConstraintViolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** {@link Package} validator */
@Component
public class PackageValidator {
  private final SystemPackageRegistry systemPackageRegistry;

  private static final Logger LOG = LoggerFactory.getLogger(PackageValidator.class);

  public PackageValidator(SystemPackageRegistry systemPackageRegistry) {
    this.systemPackageRegistry = requireNonNull(systemPackageRegistry);
  }

  public void validate(Package aPackage) {
    validatePackageAllowed(aPackage);
    validatePackageName(aPackage);
  }

  private void validatePackageAllowed(Package aPackage) {
    if (MetaUtils.isSystemPackage(aPackage) && !systemPackageRegistry.containsPackage(aPackage)) {
      LOG.error(
          "validatePackageAllowed, the system package registry does not contain package with id {} and label {}",
          aPackage.getId(),
          aPackage.getLabel());
      throw new MolgenisValidationException(
          new ConstraintViolation("Modifying system packages is not allowed"));
    }
  }

  private static void validatePackageName(Package aPackage) {
    NameValidator.validatePackageId(aPackage.getId());
  }
}
