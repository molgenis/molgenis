package org.molgenis.script.core;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.script.core.ScriptPackage.PACKAGE_SCRIPT;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

@Component
public class ScriptParameterMetadata extends SystemEntityType {
  private static final String SIMPLE_NAME = "ScriptParameter";
  public static final String SCRIPT_PARAMETER = PACKAGE_SCRIPT + PACKAGE_SEPARATOR + SIMPLE_NAME;

  public static final String NAME = "name";

  private final ScriptPackage scriptPackage;

  ScriptParameterMetadata(ScriptPackage scriptPackage) {
    super(SIMPLE_NAME, PACKAGE_SCRIPT);
    this.scriptPackage = requireNonNull(scriptPackage);
  }

  @Override
  public void init() {
    setLabel("Script parameter");
    setPackage(scriptPackage);

    addAttribute(NAME, ROLE_ID).setNillable(false).setLabel("Name label");
  }
}
