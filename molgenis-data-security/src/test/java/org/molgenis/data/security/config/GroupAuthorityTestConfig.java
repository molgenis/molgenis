package org.molgenis.data.security.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.security.auth.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ EntityBaseTestConfig.class, GroupAuthorityMetaData.class, GroupAuthorityFactory.class, GroupMetaData.class,
		AuthorityMetaData.class, SecurityPackage.class })
public class GroupAuthorityTestConfig
{
}
