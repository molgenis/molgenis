package org.molgenis.data.config;

import org.molgenis.data.meta.model.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ EntityBaseTestConfig.class, EntityTypeMetadata.class, EntityTypeFactory.class, AttributeMetadata.class,
		AttributeFactory.class, PackageMetadata.class, PackageFactory.class, TagMetadata.class, TagFactory.class,
		MetaPackage.class })
public class MetadataTestConfig
{
}
