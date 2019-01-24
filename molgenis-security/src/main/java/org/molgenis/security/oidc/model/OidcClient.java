package org.molgenis.security.oidc.model;

import static org.molgenis.security.oidc.model.OidcClientMetadata.AUTHORIZATION_GRANT_TYPE;
import static org.molgenis.security.oidc.model.OidcClientMetadata.AUTHORIZATION_URI;
import static org.molgenis.security.oidc.model.OidcClientMetadata.CLIENT_AUTHENTICATION_METHOD;
import static org.molgenis.security.oidc.model.OidcClientMetadata.CLIENT_ID;
import static org.molgenis.security.oidc.model.OidcClientMetadata.CLIENT_NAME;
import static org.molgenis.security.oidc.model.OidcClientMetadata.CLIENT_SECRET;
import static org.molgenis.security.oidc.model.OidcClientMetadata.JWK_SET_URI;
import static org.molgenis.security.oidc.model.OidcClientMetadata.REGISTRATION_ID;
import static org.molgenis.security.oidc.model.OidcClientMetadata.SCOPES;
import static org.molgenis.security.oidc.model.OidcClientMetadata.TOKEN_URI;
import static org.molgenis.security.oidc.model.OidcClientMetadata.USERNAME_ATTRIBUTE_NAME;
import static org.molgenis.security.oidc.model.OidcClientMetadata.USER_INFO_URI;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

/** A representation of a client registration with an OpenID Connect 1.0 Provider. */
public class OidcClient extends StaticEntity {
  @SuppressWarnings("unused")
  public OidcClient(Entity entity) {
    super(entity);
  }

  @SuppressWarnings("unused")
  public OidcClient(EntityType entityType) {
    super(entityType);
  }

  @SuppressWarnings("unused")
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

  @SuppressWarnings("unused")
  public void setClientId(String clientId) {
    set(CLIENT_ID, clientId);
  }

  public String getClientId() {
    return getString(CLIENT_ID);
  }

  @SuppressWarnings("unused")
  public void setClientSecret(String clientSecret) {
    set(CLIENT_SECRET, clientSecret);
  }

  public String getClientSecret() {
    return getString(CLIENT_SECRET);
  }

  @SuppressWarnings("unused")
  public void setClientName(String clientName) {
    set(CLIENT_NAME, clientName);
  }

  public String getClientName() {
    return getString(CLIENT_NAME);
  }

  @SuppressWarnings("unused")
  public void setClientAuthenticationMethod(String clientAuthenticationMethod) {
    set(CLIENT_AUTHENTICATION_METHOD, clientAuthenticationMethod);
  }

  public String getClientAuthenticationMethod() {
    return getString(CLIENT_AUTHENTICATION_METHOD);
  }

  @SuppressWarnings("unused")
  public void setAuthorizationGrantType(String authorizationGrantType) {
    set(AUTHORIZATION_GRANT_TYPE, authorizationGrantType);
  }

  public String getAuthorizationGrantType() {
    return getString(AUTHORIZATION_GRANT_TYPE);
  }

  @SuppressWarnings("unused")
  public void setAuthorizationUri(String authorizationUri) {
    set(AUTHORIZATION_URI, authorizationUri);
  }

  public String getAuthorizationUri() {
    return getString(AUTHORIZATION_URI);
  }

  @SuppressWarnings("unused")
  public void setTokenUri(String tokenUri) {
    set(TOKEN_URI, tokenUri);
  }

  public String getTokenUri() {
    return getString(TOKEN_URI);
  }

  @SuppressWarnings("unused")
  public void setJwkSetUri(String jwkSetUri) {
    set(JWK_SET_URI, jwkSetUri);
  }

  public String getJwkSetUri() {
    return getString(JWK_SET_URI);
  }

  @SuppressWarnings("unused")
  public void setScopes(String[] scopes) {
    set(SCOPES, scopes != null ? String.join(",", scopes) : null);
  }

  public String[] getScopes() {
    String scopeStr = getString(SCOPES);
    return scopeStr != null ? scopeStr.split(",") : new String[0];
  }

  @SuppressWarnings("unused")
  public void setUserInfoUri(String userInfoUri) {
    set(USER_INFO_URI, userInfoUri);
  }

  public String getUserInfoUri() {
    return getString(USER_INFO_URI);
  }

  @SuppressWarnings("unused")
  public void setUsernameAttributeName(String usernameAttributeName) {
    set(USERNAME_ATTRIBUTE_NAME, usernameAttributeName);
  }

  public String getUsernameAttributeName() {
    return getString(USERNAME_ATTRIBUTE_NAME);
  }
}
