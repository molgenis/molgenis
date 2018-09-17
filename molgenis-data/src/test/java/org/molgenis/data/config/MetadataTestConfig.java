package org.molgenis.data.config;

import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.MetaPackage;
import org.molgenis.data.meta.model.PackageFactory;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.meta.model.TagFactory;
import org.molgenis.data.meta.model.TagMetadata;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
  EntityBaseTestConfig.class,
  EntityTypeMetadata.class,
  EntityTypeFactory.class,
  AttributeMetadata.class,
  AttributeFactory.class,
  PackageMetadata.class,
  PackageFactory.class,
  TagMetadata.class,
  TagFactory.class,
  MetaPackage.class
})
public class MetadataTestConfig {}
