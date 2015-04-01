package org.molgenis.rdf;

import org.molgenis.rdf.fair.FairService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RdfConfig
{
	@Bean
	public RdfService rdfService()
	{
		return new RdfService();
	}

	@Bean
	public FairService fairService()
	{
		return new FairService();
	}
}
