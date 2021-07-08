package org.molgenis.security.oidc.model;

import static java.lang.Boolean.FALSE;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.COMPOUND;
import static org.molgenis.data.meta.AttributeType.ENUM;
import static org.molgenis.data.meta.AttributeType.HYPERLINK;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LOOKUP;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.security.oidc.model.OidcPackage.PACKAGE_OIDC;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.Attribute;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.stereotype.Component;

@Component
public class OidcClientMetadata extends SystemEntityType {
  private static final String SIMPLE_NAME = "OidcClient";
  public static final String OIDC_CLIENT = PACKAGE_OIDC + PACKAGE_SEPARATOR + SIMPLE_NAME;

  static final String REGISTRATION_ID = "registrationId";
  static final String CLIENT_ID = "clientId";
  static final String CLIENT_SECRET = "clientSecret";
  static final String CLIENT_NAME = "clientName";
  static final String CLIENT_AUTHENTICATION_METHOD = "clientAuthenticationMethod";
  static final String AUTHORIZATION_GRANT_TYPE = "authorizationGrantType";
  static final String ISSUER_URI = "issuerUri";
  static final String SCOPES = "scopes";

  private static final String PROVIDER_DETAILS = "providerDetails";
  public static final String DETAILS_VISIBLE_EXPRESSION = String.format("{%s} empty", ISSUER_URI);
  static final String AUTHORIZATION_URI = "authorizationUri";
  static final String TOKEN_URI = "tokenUri";
  static final String JWK_SET_URI = "jwkSetUri";
  static final String USER_INFO_URI = "userInfoUri";
  static final String USERNAME_ATTRIBUTE_NAME = "userNameAttributeName";
  static final String EMAIL_ATTRIBUTE_NAME = "emailAttributeName";
  public static final String CLAIMS_ROLE_PATH = "claimsRolePath";
  public static final String CLAIMS_VOGROUP_PATH = "claimsVOGroupPath";

  private final OidcPackage oidcPackage;

  public OidcClientMetadata(OidcPackage oidcPackage) {
    super(SIMPLE_NAME, PACKAGE_OIDC);
    this.oidcPackage = requireNonNull(oidcPackage);
  }

  @Override
  public void init() {
    setPackage(oidcPackage);

    setLabel("OIDC client");
    setDescription("OpenID Connect client registration");

    addAttribute(REGISTRATION_ID, ROLE_ID)
        .setLabel("Registration ID")
        .setDescription("Registration identifier");
    addAttribute(CLIENT_ID)
        .setLabel("Client ID")
        .setDescription("Client identifier")
        .setNillable(false)
        .setUnique(true);
    addAttribute(CLIENT_SECRET).setLabel("Client secret").setNillable(false);
    addAttribute(CLIENT_NAME, ROLE_LABEL, ROLE_LOOKUP)
        .setLabel("Client name")
        .setDescription("Client name to be presented to the end user")
        .setNillable(false)
        .setUnique(true);
    addAttribute(ISSUER_URI)
        .setLabel("Issuer URI")
        .setDescription(
            "If the provider supports OpenID Provider Configuration, specify the issuer URI here. "
                + "The details will be retrieved from <Issuer URI>/.well-known/openid-configuration")
        .setDataType(HYPERLINK)
        .setNillable(true);
    addAttribute(CLIENT_AUTHENTICATION_METHOD)
        .setLabel("Authentication")
        .setDescription("Client authentication method")
        .setNullableExpression(FALSE.toString())
        .setVisibleExpression(DETAILS_VISIBLE_EXPRESSION)
        .setDefaultValue("basic");
    addAttribute(AUTHORIZATION_GRANT_TYPE)
        .setLabel("Grant type")
        .setDescription("Authorization grant type")
        .setDataType(ENUM)
        .setEnumOptions(asList("authorization_code", "implicit", "refresh_token"))
        .setDefaultValue("authorization_code")
        .setNullableExpression(FALSE.toString())
        .setVisibleExpression(DETAILS_VISIBLE_EXPRESSION);
    addAttribute(SCOPES)
        .setLabel("Scopes")
        .setDescription("Comma-separated set of scopes")
        .setDefaultValue("openid,email,profile")
        .setNullableExpression(FALSE.toString())
        .setVisibleExpression(DETAILS_VISIBLE_EXPRESSION);
    Attribute providerDetailsAttribute =
        addAttribute(PROVIDER_DETAILS).setDataType(COMPOUND).setLabel("Provider details");
    addAttribute(AUTHORIZATION_URI)
        .setParent(providerDetailsAttribute)
        .setLabel("Authorization URI")
        .setDataType(HYPERLINK)
        .setVisibleExpression(DETAILS_VISIBLE_EXPRESSION)
        .setNullableExpression(FALSE.toString());
    addAttribute(TOKEN_URI)
        .setParent(providerDetailsAttribute)
        .setLabel("Token URI")
        .setDataType(HYPERLINK)
        .setVisibleExpression(DETAILS_VISIBLE_EXPRESSION)
        .setNullableExpression(FALSE.toString());
    addAttribute(JWK_SET_URI)
        .setParent(providerDetailsAttribute)
        .setLabel("JWKS URI")
        .setDescription("JSON Web Key Set URI")
        .setDataType(HYPERLINK)
        .setVisibleExpression(DETAILS_VISIBLE_EXPRESSION)
        .setNullableExpression(FALSE.toString());
    addAttribute(USER_INFO_URI)
        .setParent(providerDetailsAttribute)
        .setLabel("User info URI")
        .setDataType(HYPERLINK)
        .setVisibleExpression(DETAILS_VISIBLE_EXPRESSION)
        .setNullableExpression(FALSE.toString());
    addAttribute(CLAIMS_ROLE_PATH)
        .setLabel("Claims role path")
        .setDescription(
            "JSON path expression to evaluate on the joined ID token and user info claims to retrieve user roles")
        .setNillable(true);
    addAttribute(CLAIMS_VOGROUP_PATH)
        .setLabel("Claims VO Group path")
        .setDescription(
            "JSON path expression to evaluate on the joined ID token and user info claims to retrieve VO Groups")
        .setNillable(true);
    addAttribute(USERNAME_ATTRIBUTE_NAME)
        .setParent(providerDetailsAttribute)
        .setLabel("Username attribute")
        .setDescription("Name of the attribute used as username when registering a new user.")
        .setNillable(false)
        .setDefaultValue(StandardClaimNames.EMAIL);
    addAttribute(EMAIL_ATTRIBUTE_NAME)
        .setParent(providerDetailsAttribute)
        .setLabel("Email attribute")
        .setDescription("Email attribute name")
        .setNillable(false)
        .setDefaultValue(StandardClaimNames.EMAIL);
  }
}
