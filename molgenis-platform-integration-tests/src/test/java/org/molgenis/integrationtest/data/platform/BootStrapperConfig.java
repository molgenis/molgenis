package org.molgenis.integrationtest.data.platform;

import org.molgenis.data.platform.bootstrap.SystemEntityTypeBootstrapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ SystemEntityTypeBootstrapper.class })
public class BootStrapperConfig
{
}
