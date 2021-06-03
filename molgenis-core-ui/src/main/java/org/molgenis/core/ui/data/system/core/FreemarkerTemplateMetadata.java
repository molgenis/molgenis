package org.molgenis.core.ui.data.system.core;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;
import static org.molgenis.data.meta.AttributeType.SCRIPT;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.system.model.RootSystemPackage;
import org.springframework.stereotype.Component;

@Component
public class FreemarkerTemplateMetadata extends SystemEntityType {
  private static final String SIMPLE_NAME = "FreemarkerTemplate";
  public static final String FREEMARKER_TEMPLATE = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

  private static final String REGEX_NAME = "^view-.*\\.ftl$";

  public static final String ID = "id";
  public static final String NAME = "Name";
  public static final String VALUE = "Value";
  private final RootSystemPackage systemPackage;

  FreemarkerTemplateMetadata(RootSystemPackage systemPackage) {
    super(SIMPLE_NAME, PACKAGE_SYSTEM);
    this.systemPackage = requireNonNull(systemPackage);
  }

  @Override
  public void init() {
    setLabel("Freemarker template");
    setPackage(systemPackage);

    addAttribute(ID, ROLE_ID).setLabel("Id").setAuto(true).setVisible(false);
    addAttribute(NAME, ROLE_LABEL)
        .setLabel("Name")
        .setDescription("Template name (must start with 'view-' and end with '.ftl')")
        .setNillable(false)
        .setUnique(true)
        .setValidationExpression(String.format("regex('%s', {%s})", escapeJava(REGEX_NAME), NAME));
    addAttribute(VALUE).setLabel(VALUE).setDataType(SCRIPT).setNillable(false);
  }
}
