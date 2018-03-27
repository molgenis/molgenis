package org.molgenis.integrationtest.config;

import org.molgenis.data.cache.l1.L1Cache;
import org.molgenis.data.cache.l2.L2Cache;
import org.molgenis.data.cache.l3.L3Cache;
import org.molgenis.data.cache.utils.EntityHydration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ L1Cache.class, L2Cache.class, L3Cache.class, EntityHydration.class })
public class CacheTestConfig
{
}
