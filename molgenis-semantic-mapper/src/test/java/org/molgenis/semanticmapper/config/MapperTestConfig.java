package org.molgenis.semanticmapper.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.security.config.UserTestConfig;
import org.molgenis.semanticmapper.meta.AttributeMappingMetadata;
import org.molgenis.semanticmapper.meta.EntityMappingMetadata;
import org.molgenis.semanticmapper.meta.MapperPackage;
import org.molgenis.semanticmapper.meta.MappingProjectMetadata;
import org.molgenis.semanticmapper.meta.MappingTargetMetadata;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
  EntityBaseTestConfig.class,
  MappingProjectMetadata.class,
  MappingTargetMetadata.class,
  EntityMappingMetadata.class,
  AttributeMappingMetadata.class,
  UserTestConfig.class,
  MapperPackage.class
})
public class MapperTestConfig {}
