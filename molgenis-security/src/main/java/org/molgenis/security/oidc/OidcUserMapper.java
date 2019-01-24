package org.molgenis.security.oidc;

import org.molgenis.data.security.auth.User;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public interface OidcUserMapper {
  /** Get {@link User} or create one is none exist. */
  User toUser(OidcUser oidcUser, OidcUserRequest userRequest);
}
