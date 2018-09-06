package org.molgenis.data.plugin.model;

import static org.molgenis.data.meta.model.EntityType.AttributeRole.*;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

@Component
public class PluginMetadata extends SystemEntityType {
  private static final String SIMPLE_NAME = "Plugin";
  public static final String PLUGIN = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

  public static final String ID = "id";
  public static final String LABEL = "label";
  public static final String PATH = "path";
  public static final String DESCRIPTION = "description";

  PluginMetadata() {
    super(SIMPLE_NAME, PACKAGE_SYSTEM);
  }

  @Override
  public void init() {
    setLabel(SIMPLE_NAME);

    addAttribute(ID, ROLE_ID).setLabel("Identifier");
    addAttribute(LABEL, ROLE_LABEL, ROLE_LOOKUP)
        .setNillable(false)
        .setUnique(true)
        .setLabel("Label");
    addAttribute(PATH)
        .setNillable(false)
        .setUnique(true)
        .setReadOnly(true)
        .setLabel("Path to the plugin");
    addAttribute(DESCRIPTION, ROLE_LOOKUP).setLabel("Description");
  }
}
