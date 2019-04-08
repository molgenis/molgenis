package org.molgenis.data.security.auth;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.DATE_TIME;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.security.auth.SecurityPackage.PACKAGE_SECURITY;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetTokenMetadata extends SystemEntityType {
  private static final String SIMPLE_NAME = "PasswordResetToken";

  @SuppressWarnings("squid:S2068") // this is not a hardcoded password
  public static final String PASSWORD_RESET_TOKEN =
      PACKAGE_SECURITY + PACKAGE_SEPARATOR + SIMPLE_NAME;

  public static final String ID = "id";
  public static final String TOKEN = "token";
  public static final String USER = "user";
  static final String EXPIRATION_DATE = "expirationDate";

  private final SecurityPackage securityPackage;
  private final UserMetadata userMetadata;

  PasswordResetTokenMetadata(SecurityPackage securityPackage, UserMetadata userMetadata) {
    super(SIMPLE_NAME, PACKAGE_SECURITY);
    this.securityPackage = requireNonNull(securityPackage);
    this.userMetadata = requireNonNull(userMetadata);
  }

  @Override
  public void init() {
    setLabel("Password reset token");
    setPackage(securityPackage);

    addAttribute(ID, ROLE_ID).setLabel("Identifier").setAuto(true).setVisible(false);
    addAttribute(TOKEN, ROLE_LABEL).setLabel("Token").setUnique(true).setNillable(false);
    addAttribute(USER)
        .setLabel("User")
        .setDataType(XREF)
        .setRefEntity(userMetadata)
        .setUnique(true)
        .setReadOnly(true)
        .setNillable(false);
    addAttribute(EXPIRATION_DATE)
        .setLabel("Expiration date")
        .setDataType(DATE_TIME)
        .setNillable(false);
  }
}
