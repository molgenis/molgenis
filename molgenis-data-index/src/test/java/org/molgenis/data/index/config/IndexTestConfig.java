package org.molgenis.data.index.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.index.meta.IndexActionFactory;
import org.molgenis.data.index.meta.IndexActionGroupFactory;
import org.molgenis.data.index.meta.IndexActionGroupMetaData;
import org.molgenis.data.index.meta.IndexActionMetaData;
import org.molgenis.data.index.meta.IndexPackage;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
  EntityBaseTestConfig.class,
  IndexActionMetaData.class,
  IndexActionFactory.class,
  IndexActionGroupMetaData.class,
  IndexActionGroupFactory.class,
  IndexPackage.class
})
public class IndexTestConfig {}
