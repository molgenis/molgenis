package org.molgenis.jobs.model;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

import org.molgenis.data.meta.SystemPackage;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.system.model.RootSystemPackage;
import org.springframework.stereotype.Component;

@Component
public class JobPackage extends SystemPackage {
  public static final String SIMPLE_NAME = "job";
  public static final String PACKAGE_JOB = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

  private final RootSystemPackage rootSystemPackage;

  public JobPackage(PackageMetadata packageMetadata, RootSystemPackage rootSystemPackage) {
    super(PACKAGE_JOB, packageMetadata);
    this.rootSystemPackage = requireNonNull(rootSystemPackage);
  }

  @Override
  protected void init() {
    setLabel("Jobs");
    setDescription("Package containing al job related entities");
    setParent(rootSystemPackage);
  }
}
