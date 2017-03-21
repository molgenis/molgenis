package org.molgenis.data.config;

import org.molgenis.auth.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ EntityBaseTestConfig.class, GroupAuthorityMetaData.class, GroupAuthorityFactory.class, GroupMetaData.class,
		AuthorityMetaData.class, SecurityPackage.class })
public class GroupAuthorityTestConfig
{
}
