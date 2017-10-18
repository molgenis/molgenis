package org.molgenis.data.config;

import org.molgenis.data.security.model.SecurityPackage;
import org.molgenis.data.security.model.UserFactory;
import org.molgenis.data.security.model.UserMetadata;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ EntityBaseTestConfig.class, UserMetadata.class, UserFactory.class, SecurityPackage.class })
public class UserEntityTestConfig
{
}