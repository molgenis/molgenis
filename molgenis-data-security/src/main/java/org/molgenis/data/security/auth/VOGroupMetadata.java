package org.molgenis.data.security.auth;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LOOKUP;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.security.auth.SecurityPackage.PACKAGE_SECURITY;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

@Component
public class VOGroupMetadata extends SystemEntityType {
  private static final String SIMPLE_NAME = "VOGroup";
  public static final String VO_GROUP = PACKAGE_SECURITY + PACKAGE_SEPARATOR + SIMPLE_NAME;

  public static final String ID = "id";
  public static final String NAME = "name";

  private final SecurityPackage securityPackage;

  public VOGroupMetadata(SecurityPackage securityPackage) {
    super(SIMPLE_NAME, PACKAGE_SECURITY);
    this.securityPackage = requireNonNull(securityPackage);
  }

  @Override
  public void init() {
    setPackage(securityPackage);

    setLabel("VO Group");
    setDescription("A group that is managed remotely in a virtual organisation (VO).");

    addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false).setLabel("Identifier");
    addAttribute(NAME, ROLE_LOOKUP, ROLE_LABEL)
        .setLabel("Name")
        .setDescription("Name of the group")
        .setNillable(false)
        .setUnique(true);
  }
}
