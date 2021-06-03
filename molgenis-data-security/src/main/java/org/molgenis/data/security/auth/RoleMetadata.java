package org.molgenis.data.security.auth;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;
import static org.molgenis.data.meta.AttributeType.MREF;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LOOKUP;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.security.auth.SecurityPackage.PACKAGE_SECURITY;
import static org.molgenis.data.util.AttributeUtils.getI18nAttributeName;
import static org.molgenis.util.i18n.LanguageService.getLanguageCodes;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

@Component
public class RoleMetadata extends SystemEntityType {
  private static final String SIMPLE_NAME = "Role";
  public static final String ROLE = PACKAGE_SECURITY + PACKAGE_SEPARATOR + SIMPLE_NAME;

  public static final String ID = "id";
  public static final String NAME = "name";
  public static final String LABEL = "label";
  public static final String DESCRIPTION = "description";
  public static final String GROUP = "group";
  public static final String INCLUDES = "includes";

  private final SecurityPackage securityPackage;

  private final GroupMetadata groupMetadata;

  RoleMetadata(SecurityPackage securityPackage, GroupMetadata groupMetadata) {
    super(SIMPLE_NAME, PACKAGE_SECURITY);
    this.securityPackage = requireNonNull(securityPackage);
    this.groupMetadata = requireNonNull(groupMetadata);
    groupMetadata.setRoleMetadata(this);
  }

  @Override
  public void init() {
    setLabel("Role");
    setPackage(securityPackage);

    addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false);
    addAttribute(NAME, ROLE_LOOKUP)
        .setLabel("Name")
        .setDescription("Name of the Role")
        .setUnique(true)
        .setNillable(false)
        .setValidationExpression(format("regex('%s',{name})", escapeJava(UNIFIED_IDENTIFIER_REGEX)))
        .setReadOnly(true);
    addAttribute(LABEL, ROLE_LABEL, ROLE_LOOKUP).setLabel("Label").setNillable(false);
    getLanguageCodes()
        .map(languageCode -> getI18nAttributeName(LABEL, languageCode))
        .forEach(this::addAttribute);
    addAttribute(DESCRIPTION).setLabel("Description");
    getLanguageCodes()
        .map(languageCode -> getI18nAttributeName(DESCRIPTION, languageCode))
        .map(this::addAttribute)
        .forEach(attribute -> attribute.setDataType(TEXT));
    addAttribute(GROUP)
        .setLabel("Group")
        .setDescription("Optional reference to the group in which this is a Role.")
        .setDataType(XREF)
        .setRefEntity(groupMetadata);
    addAttribute(INCLUDES)
        .setLabel("Includes")
        .setDescription("These roles are included with this role.")
        .setDataType(MREF)
        .setRefEntity(this)
        .setNillable(true);
  }
}
