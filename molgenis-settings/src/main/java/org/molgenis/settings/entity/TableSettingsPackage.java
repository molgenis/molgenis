package org.molgenis.settings.entity;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

import org.molgenis.data.meta.SystemPackage;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.system.model.RootSystemPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TableSettingsPackage extends SystemPackage {
  public static final String SIMPLE_NAME = "ts";
  public static final String PACKAGE_ENTITY_SETTINGS =
      PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

  private RootSystemPackage rootSystemPackage;

  protected TableSettingsPackage(PackageMetadata packageMetadata) {
    super(PACKAGE_ENTITY_SETTINGS, packageMetadata);
  }

  @Override
  protected void init() {
    setLabel("Entity Settings");
    setDescription("Settings entities for per entity config");
    setParent(rootSystemPackage);
  }

  // setter injection instead of constructor injection to avoid unresolvable circular dependencies
  @Autowired
  public void setRootSystemPackage(RootSystemPackage rootSystemPackage) {
    this.rootSystemPackage = requireNonNull(rootSystemPackage);
  }
}
