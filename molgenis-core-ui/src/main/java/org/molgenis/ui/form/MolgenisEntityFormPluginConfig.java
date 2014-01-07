package org.molgenis.ui.form;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.MappedInterceptor;

/**
 * MolgenisEntityFormPlugin configuration
 */
@Configuration
public class MolgenisEntityFormPluginConfig
{
	/**
	 * Map MolgenisEntityFormPluginInterceptor on MolgenisEntityFormPluginController
	 */
	@Bean
	public MappedInterceptor molgenisEntityFormPluginMappedInterceptor()
	{
		return new MappedInterceptor(new String[]
		{ MolgenisEntityFormPluginController.URI + "**/**" }, new MolgenisEntityFormPluginInterceptor());
	}
}
