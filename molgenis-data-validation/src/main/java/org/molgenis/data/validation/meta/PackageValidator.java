package org.molgenis.data.validation.meta;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;

import java.util.HashSet;
import java.util.Set;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.system.SystemPackageRegistry;
import org.molgenis.data.util.PackageUtils;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.validation.ConstraintViolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** {@link Package} validator */
@Component
public class PackageValidator {
  private static final Logger LOG = LoggerFactory.getLogger(PackageValidator.class);

  private final SystemPackageRegistry systemPackageRegistry;

  PackageValidator(SystemPackageRegistry systemPackageRegistry) {
    this.systemPackageRegistry = requireNonNull(systemPackageRegistry);
  }

  public void validate(Package aPackage) {
    validatePackageAllowed(aPackage);
    validatePackageName(aPackage);
    runAsSystem(() -> validatePackageParent(aPackage));
  }

  private void validatePackageAllowed(Package aPackage) {
    if (PackageUtils.isSystemPackage(aPackage)
        && !systemPackageRegistry.containsPackage(aPackage)) {
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

  private static void validatePackageParent(Package aPackage) {
    Set<String> packageIds = new HashSet<>();
    for (Package currentPackage = aPackage;
        currentPackage != null;
        currentPackage = currentPackage.getParent()) {
      boolean isNewPackageId = packageIds.add(currentPackage.getId());
      if (!isNewPackageId) {
        throw new MolgenisValidationException(
            new ConstraintViolation(
                format(
                    "Package '%s' with id '%s' parent contains cycles",
                    aPackage.getLabel(), aPackage.getId())));
      }
    }
  }
}
