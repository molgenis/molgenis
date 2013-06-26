package org.molgenis.omx.harmonizationIndexer.config;

import org.molgenis.omx.harmonizationIndexer.plugin.AsyncHarmonizationIndexer;
import org.molgenis.omx.harmonizationIndexer.plugin.HarmonizationIndexer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class HarmonizationIndexerConfig
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
