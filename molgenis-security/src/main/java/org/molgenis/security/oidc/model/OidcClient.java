package org.molgenis.security.oidc.model;

import static org.molgenis.security.oidc.model.OidcClientMetadata.AUTHORIZATION_URI;
import static org.molgenis.security.oidc.model.OidcClientMetadata.CLAIMS_ROLE_PATH;
import static org.molgenis.security.oidc.model.OidcClientMetadata.CLAIMS_VOGROUP_PATH;
import static org.molgenis.security.oidc.model.OidcClientMetadata.CLIENT_AUTHENTICATION_METHOD;
import static org.molgenis.security.oidc.model.OidcClientMetadata.CLIENT_ID;
import static org.molgenis.security.oidc.model.OidcClientMetadata.CLIENT_NAME;
import static org.molgenis.security.oidc.model.OidcClientMetadata.CLIENT_SECRET;
import static org.molgenis.security.oidc.model.OidcClientMetadata.EMAIL_ATTRIBUTE_NAME;
import static org.molgenis.security.oidc.model.OidcClientMetadata.ISSUER_URI;
import static org.molgenis.security.oidc.model.OidcClientMetadata.JWK_SET_URI;
import static org.molgenis.security.oidc.model.OidcClientMetadata.REGISTRATION_ID;
import static org.molgenis.security.oidc.model.OidcClientMetadata.SCOPES;
import static org.molgenis.security.oidc.model.OidcClientMetadata.TOKEN_URI;
import static org.molgenis.security.oidc.model.OidcClientMetadata.USERNAME_ATTRIBUTE_NAME;
import static org.molgenis.security.oidc.model.OidcClientMetadata.USER_INFO_URI;

import java.util.Optional;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

/** A representation of a client registration with an OpenID Connect 1.0 Provider. */
@SuppressWarnings("unused")
public class OidcClient extends StaticEntity {
  public OidcClient(Entity entity) {
    super(entity);
  }

  public OidcClient(EntityType entityType) {
    super(entityType);
  }

  public OidcClient(String id, EntityType entityType) {
    super(entityType);
    setRegistrationId(id);
  }

  private void setRegistrationId(String registrationId) {
    set(REGISTRATION_ID, registrationId);
  }

  public String getRegistrationId() {
    return getString(REGISTRATION_ID);
  }

  public void setClientId(String clientId) {
    set(CLIENT_ID, clientId);
  }

  public String getClientId() {
    return getString(CLIENT_ID);
  }

  public void setClientSecret(String clientSecret) {
    set(CLIENT_SECRET, clientSecret);
  }

  public String getClientSecret() {
    return getString(CLIENT_SECRET);
  }

  public void setClientName(String clientName) {
    set(CLIENT_NAME, clientName);
  }

  public String getClientName() {
    return getString(CLIENT_NAME);
  }

  public void setClientAuthenticationMethod(String clientAuthenticationMethod) {
    set(CLIENT_AUTHENTICATION_METHOD, clientAuthenticationMethod);
  }

  public String getClientAuthenticationMethod() {
    return getString(CLIENT_AUTHENTICATION_METHOD);
  }

  public void setAuthorizationUri(String authorizationUri) {
    set(AUTHORIZATION_URI, authorizationUri);
  }

  public String getAuthorizationUri() {
    return getString(AUTHORIZATION_URI);
  }

  public void setTokenUri(String tokenUri) {
    set(TOKEN_URI, tokenUri);
  }

  public String getTokenUri() {
    return getString(TOKEN_URI);
  }

  public void setJwkSetUri(String jwkSetUri) {
    set(JWK_SET_URI, jwkSetUri);
  }

  public String getJwkSetUri() {
    return getString(JWK_SET_URI);
  }

  public void setScopes(String[] scopes) {
    set(SCOPES, scopes != null ? String.join(",", scopes) : null);
  }

  public String[] getScopes() {
    String scopeStr = getString(SCOPES);
    return scopeStr != null ? scopeStr.split(",") : new String[0];
  }

  public String getIssuerUri() {
    return getString(ISSUER_URI);
  }

  public void setUserInfoUri(String userInfoUri) {
    set(USER_INFO_URI, userInfoUri);
  }

  public String getUserInfoUri() {
    return getString(USER_INFO_URI);
  }

  public void setUsernameAttributeName(String usernameAttributeName) {
    set(USERNAME_ATTRIBUTE_NAME, usernameAttributeName);
  }

  public String getUsernameAttributeName() {
    return getString(USERNAME_ATTRIBUTE_NAME);
  }

  public void setEmailAttributeName(String emailAttributeName) {
    set(EMAIL_ATTRIBUTE_NAME, emailAttributeName);
  }

  public String getEmailAttributeName() {
    return getString(EMAIL_ATTRIBUTE_NAME);
  }

  public Optional<String> getClaimsRolePath() {
    return Optional.ofNullable(getString(CLAIMS_ROLE_PATH));
  }

  public Optional<String> getClaimsVOGroupPath() {
    return Optional.ofNullable(getString(CLAIMS_VOGROUP_PATH));
  }
}
