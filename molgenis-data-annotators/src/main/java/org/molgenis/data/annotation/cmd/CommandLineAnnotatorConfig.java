package org.molgenis.data.annotation.cmd;

import org.molgenis.CommandLineOnlyConfiguration;
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
		System.out.println("Instantiating commandline-only simple settings");
		return new MolgenisSimpleSettings();
	}

}
