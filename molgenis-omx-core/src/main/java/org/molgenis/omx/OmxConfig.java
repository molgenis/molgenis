package org.molgenis.omx;

import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.util.ShoppingCart;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;

@Configuration
public class OmxConfig
{
	@Bean
	@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS, value = WebApplicationContext.SCOPE_SESSION)
	public ShoppingCart shoppingCart()
	{
		return new ShoppingCart();
	}

	@Bean
	public MolgenisSettings molgenisSettings()
	{
		return new MolgenisDbSettings();
	}
}
