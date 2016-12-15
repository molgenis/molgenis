package org.molgenis.fair.controller.config;

import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FairConfig
{
	@Bean
	public SimpleValueFactory simpleValueFactory()
	{
		return SimpleValueFactory.getInstance();
	}
}
