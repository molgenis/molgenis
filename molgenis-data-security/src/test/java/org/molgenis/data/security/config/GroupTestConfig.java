package org.molgenis.data.security.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.security.auth.RoleFactory;
import org.molgenis.data.security.auth.RoleMetadata;
import org.molgenis.data.security.auth.SecurityPackage;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({EntityBaseTestConfig.class, RoleMetadata.class, RoleFactory.class, SecurityPackage.class})
public class GroupTestConfig {}
