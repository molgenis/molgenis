package org.molgenis.data.config;

import org.molgenis.data.index.meta.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ EntityBaseTestConfig.class, IndexActionMetaData.class, IndexActionFactory.class,
		IndexActionGroupMetaData.class, IndexActionGroupFactory.class, IndexPackage.class })
public class IndexTestConfig
{
}
