package org.molgenis.data.mapper.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.config.UserEntityTestConfig;
import org.molgenis.data.mapper.meta.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ EntityBaseTestConfig.class, MappingProjectMetaData.class, MappingTargetMetaData.class,
		EntityMappingMetaData.class, AttributeMappingMetaData.class, MapperPackage.class, UserEntityTestConfig.class })
public class MapperTestConfig
{
}
