package org.molgenis.js.methods;

import org.molgenis.data.Entity;
import org.molgenis.js.MolgenisContext;
import org.molgenis.js.ScriptableValue;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

/**
 * Methods in the global context
 */
public class GlobalMethods
{
	/**
	 * Attribute value lookup.
	 * 
	 * Javascript example:
	 * 
	 * $('firstName')
	 * 
	 * @param ctx
	 * @param thisObj
	 * @param args
	 * @param funObj
	 * @return ScriptableValue
	 */
	public static Scriptable $(Context ctx, Scriptable thisObj, Object[] args, Function funObj)
	{
		if (args.length != 1)
		{
			throw new IllegalArgumentException(
					"$() expects exactly one argument: an attribute name. Example: $('firstName')");
		}

		String attributeName = (String) args[0];
		MolgenisContext mctx = MolgenisContext.asMolgenisContext(ctx);
		Entity entity = mctx.getEntity();
		Object value = entity.get(attributeName);

		return new ScriptableValue(thisObj, value);
	}

}
