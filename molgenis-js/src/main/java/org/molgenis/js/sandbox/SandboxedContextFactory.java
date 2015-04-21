package org.molgenis.js.sandbox;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

/**
 * {@link ContextFactory} for Molgenis js.
 * 
 * Creates Contexts that are sandboxed.
 * 
 */
public class SandboxedContextFactory extends ContextFactory
{
	@Override
	protected Context makeContext()
	{
		return new SandboxedContext(this);
	}

}
