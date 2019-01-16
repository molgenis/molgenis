package org.molgenis.security.oidc.model;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.security.auth.SecurityPackage;
import org.molgenis.data.security.auth.UserMetadata;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
  EntityBaseTestConfig.class,
  SecurityPackage.class,
  OidcClientMetadata.class,
  UserMetadata.class
})
public class OidcTestConfig {}
