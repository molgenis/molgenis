package org.molgenis.omx;

import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OmxConfig
{
	@Bean
	public MolgenisSettings molgenisSettings()
	{
		return new MolgenisDbSettings();
	}
}
