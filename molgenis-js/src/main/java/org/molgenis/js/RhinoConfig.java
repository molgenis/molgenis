package org.molgenis.js;

import org.molgenis.js.sandbox.SandboxedContextFactory;
import org.mozilla.javascript.ContextFactory;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Molgenis js {@link Configuration} class.
 * <p>
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
