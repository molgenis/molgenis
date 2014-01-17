package org.molgenis.omx;

import org.molgenis.data.DataService;
import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OmxConfig
{
	@Autowired
	private DataService dataService;

	@Bean
	public MolgenisSettings molgenisSettings()
	{
		return new MolgenisDbSettings(dataService);
	}
}
