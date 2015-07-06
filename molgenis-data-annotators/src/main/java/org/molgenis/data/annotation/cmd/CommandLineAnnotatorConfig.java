package org.molgenis.data.annotation.cmd;

import org.molgenis.CommandLineOnlyConfiguration;
import org.molgenis.data.DataService;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.support.AnnotationServiceImpl;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.server.MolgenisSimpleSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Commandline-specific annotator configuration.
 */
@Configuration
@CommandLineOnlyConfiguration
public class CommandLineAnnotatorConfig
{
	@Bean
	MolgenisSettings settings()
	{
		return new MolgenisSimpleSettings();
	}
	
	@Bean
	DataService dataService()
	{
		return new DataServiceImpl();
	}
	
	@Bean
	AnnotationService annotationService()
	{
		return new AnnotationServiceImpl();
	}

}
