package org.molgenis.data.config;

import org.molgenis.data.security.model.SecurityMetadataConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ EntityBaseTestConfig.class, SecurityMetadataConfig.class })
public class UserEntityTestConfig
{
}