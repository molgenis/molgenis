package org.molgenis.data.security.auth;

import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.*;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.security.auth.SecurityPackage.PACKAGE_SECURITY;
import static org.molgenis.data.util.AttributeUtils.getI18nAttributeName;
import static org.molgenis.i18n.LanguageService.getLanguageCodes;

import java.util.Set;
import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

@Component
public class GroupMetadata extends SystemEntityType {
  private static final String SIMPLE_NAME = "Group";
  public static final String GROUP = PACKAGE_SECURITY + PACKAGE_SEPARATOR + SIMPLE_NAME;

  public static final String ID = "id";
  public static final String NAME = "name";
  public static final String LABEL = "label";
  public static final String DESCRIPTION = "description";
  public static final String ROLES = "roles";
  public static final String PUBLIC = "public";
  public static final String ROOT_PACKAGE = "rootPackage";

  private final SecurityPackage securityPackage;
  private RoleMetadata roleMetadata;

  public GroupMetadata(SecurityPackage securityPackage) {
    super(SIMPLE_NAME, PACKAGE_SECURITY);
    this.securityPackage = requireNonNull(securityPackage);
  }

  @Override
  public void init() {
    setPackage(securityPackage);

    setLabel(SIMPLE_NAME);
    setDescription("A number of people that work together or share certain beliefs.");

    addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false).setLabel("Identifier");
    addAttribute(NAME, ROLE_LOOKUP)
        .setLabel("Name")
        .setDescription("Name of the group. Use kebab-case, e.g. my-group.")
        .setNillable(false)
        .setUnique(true)
        .setValidationExpression("$('name').matches(/^[a-z][a-z0-9]*(-[a-z0-9]+)*$/).value()");
    addAttribute(LABEL, ROLE_LABEL, ROLE_LOOKUP).setLabel("Label").setNillable(false);
    getLanguageCodes()
        .map(languageCode -> getI18nAttributeName(LABEL, languageCode))
        .forEach(this::addAttribute);
    addAttribute(DESCRIPTION).setLabel("Description").setDataType(TEXT);
    getLanguageCodes()
        .map(languageCode -> getI18nAttributeName(DESCRIPTION, languageCode))
        .map(this::addAttribute)
        .forEach(attribute -> attribute.setDataType(TEXT));
    addAttribute(PUBLIC)
        .setDataType(BOOL)
        .setLabel("Publicly visible")
        .setDescription("Indication if this group is publicly visible.")
        .setNillable(false)
        .setDefaultValue("true");
    addAttribute(ROLES)
        .setDataType(ONE_TO_MANY)
        .setRefEntity(roleMetadata)
        .setMappedBy(roleMetadata.getAttribute(RoleMetadata.GROUP))
        .setLabel("Roles")
        .setDescription("Roles a User can have within this Group")
        .setCascadeDelete(true);
    addAttribute(ROOT_PACKAGE)
        .setLabel("Root package")
        .setDescription("Id of the package where this Group's resources reside.")
        .setNillable(false);
  }

  public void setRoleMetadata(RoleMetadata roleMetadata) {
    this.roleMetadata = requireNonNull(roleMetadata);
  }

  @Override
  public Set<SystemEntityType> getDependencies() {
    return singleton(roleMetadata);
  }
}
