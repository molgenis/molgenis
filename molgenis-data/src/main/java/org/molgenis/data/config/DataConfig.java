package org.molgenis.data.config;

import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.support.DataServiceImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Convenience configuration that can be imported by other configurations that want to autowire a
 * {@link org.molgenis.data.DataService} or {@link org.molgenis.data.meta.MetaDataService}.
 */
@Import({DataServiceImpl.class, MetaDataServiceImpl.class})
@Configuration
public class DataConfig {}
