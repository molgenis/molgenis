package org.molgenis.data.config;

import org.molgenis.auth.SecurityPackage;
import org.molgenis.auth.UserFactory;
import org.molgenis.auth.UserMetaData;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ EntityBaseTestConfig.class, UserMetaData.class, UserFactory.class, SecurityPackage.class })
public class UserTestConfig
{
}
