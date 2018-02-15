package org.molgenis.data.security.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.security.auth.GroupFactory;
import org.molgenis.data.security.auth.GroupMetaData;
import org.molgenis.data.security.auth.SecurityPackage;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ EntityBaseTestConfig.class, GroupMetaData.class, GroupFactory.class, SecurityPackage.class })
public class GroupTestConfig
{
}
