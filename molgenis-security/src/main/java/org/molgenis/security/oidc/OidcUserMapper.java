package org.molgenis.security.oidc;

import org.molgenis.security.oidc.model.OidcClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public interface OidcUserMapper {
  /** Get MOLGENIS username for {@link OidcUser}, creating a new one if one doesn't exist */
  String toUser(OidcUser oidcUser, OidcClient oidcClient);
}
