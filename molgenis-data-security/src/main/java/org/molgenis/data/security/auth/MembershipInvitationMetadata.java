package org.molgenis.data.security.auth;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.security.auth.MembershipInvitationMetadata.Status.PENDING;
import static org.molgenis.data.security.auth.SecurityPackage.PACKAGE_SECURITY;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

@Component
public class MembershipInvitationMetadata extends SystemEntityType {
  public enum Status {
    PENDING,
    ACCEPTED,
    REVOKED,
    EXPIRED,
    DECLINED
  }

  private static final String SIMPLE_NAME = "MembershipInvitation";
  public static final String MEMBERSHIP_INVITATION =
      PACKAGE_SECURITY + PACKAGE_SEPARATOR + SIMPLE_NAME;

  public static final String ID = "id";
  public static final String TOKEN = "token";
  public static final String EMAIL = "email";
  public static final String FROM = "from";
  public static final String TO = "to";
  public static final String ROLE = "role";
  public static final String INVITED_BY = "invitedBy";
  public static final String ISSUED = "issued";
  public static final String LAST_UPDATE = "lastUpdate";
  public static final String INVITATION_TEXT = "invitationText";
  public static final String DECLINE_REASON = "declineReason";
  public static final String STATUS = "status";

  private final SecurityPackage securityPackage;
  private final UserMetaData userMetadata;
  private final RoleMetadata roleMetadata;

  public MembershipInvitationMetadata(
      SecurityPackage securityPackage, UserMetaData userMetadata, RoleMetadata roleMetadata) {
    super(SIMPLE_NAME, PACKAGE_SECURITY);
    this.securityPackage = requireNonNull(securityPackage);
    this.userMetadata = requireNonNull(userMetadata);
    this.roleMetadata = requireNonNull(roleMetadata);
  }

  @Override
  public void init() {
    setPackage(securityPackage);

    setLabel(SIMPLE_NAME);
    setDescription("An Invitation for a User to join a Group in a certain Role.");

    addAttribute(ID, ROLE_ID).setAuto(true).setLabel("Identifier");
    addAttribute(TOKEN)
        .setNillable(false)
        .setLabel("Token")
        .setDescription("The token used when responding to this invitation.");
    addAttribute(EMAIL)
        .setNillable(false)
        .setLabel("Email")
        .setDescription("Email address of the invited user.")
        .setDataType(AttributeType.EMAIL);
    addAttribute(FROM)
        .setNillable(false)
        .setLabel("From")
        .setDescription("The proposed start date of the membership.")
        .setDataType(DATE_TIME);
    addAttribute(TO)
        .setLabel("To")
        .setDescription("The proposed end date of the membership.")
        .setDataType(DATE_TIME);
    addAttribute(ROLE)
        .setNillable(false)
        .setLabel("Role")
        .setDescription("The role that the user will join the group in.")
        .setDataType(XREF)
        .setRefEntity(roleMetadata);
    addAttribute(INVITED_BY)
        .setNillable(false)
        .setLabel("Invited by")
        .setDescription("The group manager who created this invitation.")
        .setDataType(XREF)
        .setRefEntity(userMetadata)
        .setAggregatable(true);
    addAttribute(ISSUED)
        .setNillable(false)
        .setAuto(true)
        .setLabel("Issued")
        .setDescription("The moment this invitation was issued.")
        .setDataType(DATE_TIME);
    addAttribute(LAST_UPDATE)
        .setNillable(false)
        .setLabel("Last update")
        .setDescription("The moment this invitation was last updated.")
        .setDataType(DATE_TIME);
    addAttribute(INVITATION_TEXT)
        .setNillable(true)
        .setLabel("Invitation text")
        .setDescription("Custom text used to invite the user.")
        .setDataType(TEXT);
    addAttribute(DECLINE_REASON)
        .setNillable(true)
        .setLabel("Decline reason")
        .setDescription("Reason provided by the invited User on why they declined the invitation.")
        .setDataType(TEXT);
    addAttribute(STATUS)
        .setDataType(ENUM)
        .setEnumOptions(Status.class)
        .setNillable(false)
        .setDefaultValue(PENDING.toString())
        .setAggregatable(true);
  }
}
