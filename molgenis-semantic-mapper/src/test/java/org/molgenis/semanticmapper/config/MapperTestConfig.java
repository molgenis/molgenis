package org.molgenis.semanticmapper.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.security.config.UserTestConfig;
import org.molgenis.semanticmapper.meta.AttributeMappingMetaData;
import org.molgenis.semanticmapper.meta.EntityMappingMetaData;
import org.molgenis.semanticmapper.meta.MapperPackage;
import org.molgenis.semanticmapper.meta.MappingProjectMetaData;
import org.molgenis.semanticmapper.meta.MappingTargetMetaData;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
  EntityBaseTestConfig.class,
  MappingProjectMetaData.class,
  MappingTargetMetaData.class,
  EntityMappingMetaData.class,
  AttributeMappingMetaData.class,
  UserTestConfig.class,
  MapperPackage.class
})
public class MapperTestConfig {}
