package org.molgenis.js;

import org.molgenis.data.Entity;
import org.mozilla.javascript.ContextFactory;

/**
 * Run a script with local scope
 */
public class ScriptEvaluator
{
	public static Object eval(final String source)
	{
		return eval(source, null);
	}

	public static Object eval(final String source, final Entity entity)
	{
		return ContextFactory.getGlobal().call(new MolgenisContextAction()
		{
			@Override
			public Object run(MolgenisContext ctx)
			{
				ctx.setEntity(entity);
				return ctx.evaluateString(ctx.newLocalScope(), source, null, 1, null);
			}

		});
	}

}
