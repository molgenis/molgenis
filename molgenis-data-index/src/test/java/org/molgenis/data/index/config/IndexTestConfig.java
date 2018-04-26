package org.molgenis.data.index.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.index.meta.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ EntityBaseTestConfig.class, IndexActionMetaData.class, IndexActionFactory.class,
		IndexActionGroupMetaData.class, IndexActionGroupFactory.class, IndexPackage.class })
public class IndexTestConfig
{
}
