package org.molgenis.data.security.auth;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.DATE_TIME;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.security.auth.SecurityPackage.PACKAGE_SECURITY;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

@Component
public class VOGroupRoleMembershipMetadata extends SystemEntityType {
  private static final String SIMPLE_NAME = "VOGroupRoleMembership";
  public static final String VOGROUP_ROLE_MEMBERSHIP =
      PACKAGE_SECURITY + PACKAGE_SEPARATOR + SIMPLE_NAME;

  public static final String ID = "id";
  public static final String VO_GROUP = "voGroup";
  public static final String FROM = "from";
  public static final String TO = "to";
  public static final String ROLE = "role";

  private final SecurityPackage securityPackage;
  private final VOGroupMetadata voGroupMetadata;
  private final RoleMetadata roleMetadata;

  public VOGroupRoleMembershipMetadata(
      SecurityPackage securityPackage, VOGroupMetadata voGroupMetadata, RoleMetadata roleMetadata) {
    super(SIMPLE_NAME, PACKAGE_SECURITY);
    this.securityPackage = requireNonNull(securityPackage);
    this.voGroupMetadata = requireNonNull(voGroupMetadata);
    this.roleMetadata = requireNonNull(roleMetadata);
  }

  @Override
  public void init() {
    setPackage(securityPackage);

    setLabel("VO Group Role Membership");
    setDescription(
        "Records the fact that VO Group members are members of a Role during an interval.");

    addAttribute(ID, ROLE_ID).setAuto(true).setLabel("Identifier");
    addAttribute(VO_GROUP)
        .setLabel("VO Group")
        .setDataType(XREF)
        .setRefEntity(voGroupMetadata)
        .setNillable(false);
    addAttribute(ROLE)
        .setLabel("Role")
        .setDataType(XREF)
        .setRefEntity(roleMetadata)
        .setNillable(false);
    addAttribute(FROM).setLabel("From").setDataType(DATE_TIME).setNillable(false);
    addAttribute(TO).setLabel("To").setDataType(DATE_TIME);
  }
}
