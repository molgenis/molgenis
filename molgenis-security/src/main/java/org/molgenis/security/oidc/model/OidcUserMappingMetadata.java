package org.molgenis.security.oidc.model;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LOOKUP;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.security.oidc.model.OidcPackage.PACKAGE_OIDC;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.security.auth.UserMetaData;
import org.springframework.stereotype.Component;

@Component
public class OidcUserMappingMetadata extends SystemEntityType {
  private static final String SIMPLE_NAME = "OidcUserMapping";
  public static final String OIDC_USER_MAPPING = PACKAGE_OIDC + PACKAGE_SEPARATOR + SIMPLE_NAME;

  public static final String ID = "id";
  public static final String LABEL = "label";
  public static final String OIDC_CLIENT = "oidcClient";
  public static final String OIDC_USERNAME = "oidcUsername";
  public static final String USER = "user";

  private final OidcPackage oidcPackage;
  private final OidcClientMetadata oidcClientMetadata;
  private final UserMetaData userMetaData;

  public OidcUserMappingMetadata(
      OidcPackage oidcPackage, OidcClientMetadata oidcClientMetadata, UserMetaData userMetaData) {
    super(SIMPLE_NAME, PACKAGE_OIDC);
    this.oidcPackage = requireNonNull(oidcPackage);
    this.oidcClientMetadata = requireNonNull(oidcClientMetadata);
    this.userMetaData = requireNonNull(userMetaData);
  }

  @Override
  public void init() {
    setPackage(oidcPackage);

    setLabel("OIDC user mapping");
    setDescription("Mapping of OpenID Connect users to MOLGENIS users");

    addAttribute(ID, ROLE_ID).setLabel("Identifier").setAuto(true).setVisible(false);
    addAttribute(LABEL, ROLE_LABEL, ROLE_LOOKUP)
        .setLabel("Label")
        .setNillable(false)
        .setUnique(true);
    addAttribute(OIDC_CLIENT)
        .setLabel("OIDC client")
        .setDescription("OpenID Connect client")
        .setDataType(XREF)
        .setRefEntity(oidcClientMetadata)
        .setNillable(false);
    addAttribute(OIDC_USERNAME)
        .setLabel("OIDC username")
        .setDescription("OpenID Connect username")
        .setNillable(false);
    addAttribute(USER)
        .setLabel("User")
        .setDescription("MOLGENIS user")
        .setDataType(XREF)
        .setRefEntity(userMetaData)
        .setNillable(false);

    // TODO decorator with unique constraint on [OIDC_CLIENT, OIDC_USERNAME]
  }
}
