package org.molgenis.data.security.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.security.auth.SecurityPackage;
import org.molgenis.data.security.auth.UserFactory;
import org.molgenis.data.security.auth.UserMetadata;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({EntityBaseTestConfig.class, UserMetadata.class, UserFactory.class, SecurityPackage.class})
public class UserTestConfig {}
