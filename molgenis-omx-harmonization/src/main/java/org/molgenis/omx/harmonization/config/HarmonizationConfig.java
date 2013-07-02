package org.molgenis.omx.harmonization.config;

import org.molgenis.omx.ontologyIndexer.plugin.AsyncHarmonizationIndexer;
import org.molgenis.omx.ontologyIndexer.plugin.HarmonizationIndexer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class HarmonizationConfig
{
	/**
	 * Get a reference to a HarmonizationIndexer.
	 * 
	 * @return HarmonizationIndexer
	 */
	@Bean
	public HarmonizationIndexer harmonizationIndexer()
	{
		return new AsyncHarmonizationIndexer();
	}
}
