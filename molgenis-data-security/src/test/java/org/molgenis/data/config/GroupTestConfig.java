package org.molgenis.data.config;

import org.molgenis.auth.AuthorityMetaData;
import org.molgenis.auth.GroupFactory;
import org.molgenis.auth.GroupMetaData;
import org.molgenis.auth.SecurityPackage;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ EntityBaseTestConfig.class, GroupMetaData.class, GroupFactory.class, AuthorityMetaData.class,
		SecurityPackage.class })
public class GroupTestConfig
{
}
