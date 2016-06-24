package org.molgenis.js;

import javax.annotation.PostConstruct;

import org.molgenis.js.sandbox.SandboxedContextFactory;
import org.mozilla.javascript.ContextFactory;
import org.springframework.context.annotation.Configuration;

/**
 * Molgenis js {@link Configuration} class.
 * 
 * Initializes the Global {@link ContextFactory}
 */
@Configuration
public class RhinoConfig
{
	@PostConstruct
	public void init()
	{
		if (!ContextFactory.hasExplicitGlobal())
		{
			ContextFactory.initGlobal(new SandboxedContextFactory());
		}
	}

}
