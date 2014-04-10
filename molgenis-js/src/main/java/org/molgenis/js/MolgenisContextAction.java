package org.molgenis.js;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;

/**
 * A {@link ContextAction} that casts the Context to a MolgenisContext
 */
public abstract class MolgenisContextAction implements ContextAction
{

	@Override
	public final Object run(Context ctx)
	{
		return run(MolgenisContext.asMolgenisContext(ctx));
	}

	protected abstract Object run(MolgenisContext ctx);
}
