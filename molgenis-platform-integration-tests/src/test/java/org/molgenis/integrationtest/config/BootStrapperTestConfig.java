package org.molgenis.integrationtest.config;

import org.molgenis.data.TestPackage;
import org.molgenis.data.platform.bootstrap.SystemEntityTypeBootstrapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ SystemEntityTypeBootstrapper.class, TestPackage.class })
public class BootStrapperTestConfig
{
}