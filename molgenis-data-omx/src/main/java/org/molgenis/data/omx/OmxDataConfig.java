package org.molgenis.data.omx;

import org.molgenis.data.DataService;
import org.molgenis.data.EntitySource;
import org.molgenis.data.EntitySourceFactory;
import org.molgenis.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OmxDataConfig
{
	@Autowired
	private DataService dataService;

	@Autowired
	private SearchService searchService;

	@Bean
	public EntitySource omxEntitySource()
	{
		return new OmxEntitySource(dataService, searchService);
	}

	@Bean
	public EntitySourceFactory omxEntitySourceFactory()
	{
		return new OmxEntitySourceFactory(omxEntitySource());
	}

	@Bean
	public OmxEntitySourceRegistrator omxEntitySourceRegistrator()
	{
		return new OmxEntitySourceRegistrator(dataService);
	}
}
