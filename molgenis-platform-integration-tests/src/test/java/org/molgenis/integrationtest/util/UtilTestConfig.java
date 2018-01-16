package org.molgenis.integrationtest.util;

import org.molgenis.data.util.GenericDependencyResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(GenericDependencyResolver.class)
public class UtilTestConfig
{
}
