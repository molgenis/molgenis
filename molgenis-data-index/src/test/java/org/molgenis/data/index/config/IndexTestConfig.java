package org.molgenis.data.index.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.index.meta.IndexActionFactory;
import org.molgenis.data.index.meta.IndexActionGroupFactory;
import org.molgenis.data.index.meta.IndexActionGroupMetadata;
import org.molgenis.data.index.meta.IndexActionMetadata;
import org.molgenis.data.index.meta.IndexPackage;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
  EntityBaseTestConfig.class,
  IndexActionMetadata.class,
  IndexActionFactory.class,
  IndexActionGroupMetadata.class,
  IndexActionGroupFactory.class,
  IndexPackage.class
})
public class IndexTestConfig {}
