package org.molgenis.js.sandbox;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

/**
 * A sandboxed {@link Context} that prevents access to native java classes
 * 
 */
public class SandboxedContext extends Context
{
	public SandboxedContext(ContextFactory factory)
	{
		super(factory);

		setWrapFactory(new SandboxWrapFactory());
		setClassShutter(new SandboxClassShutter());
	}

}
