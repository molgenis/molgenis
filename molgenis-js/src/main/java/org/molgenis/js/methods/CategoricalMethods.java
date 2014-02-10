package org.molgenis.js.methods;

import java.util.HashMap;
import java.util.Map;

import org.molgenis.js.ScriptableValue;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;

/**
 * Arithmetic functions
 */
public class CategoricalMethods
{
	/**
	 * $('test').map({ 1 : 0, 0 : 1 })
	 * 
	 * @param ctx
	 * @param thisObj
	 * @param args
	 * @param funObj
	 * @return
	 */
	public static Scriptable map(Context ctx, Scriptable thisObj, Object[] args, Function funObj)
	{
		if (args.length != 1)
		{
			throw new IllegalArgumentException("div expects one argument. Example: $('gender').div('10':'1','20':'2')");
		}
		Map<String, String> mappings = new HashMap<String, String>();
		if (args[0] instanceof NativeObject)
		{
			NativeObject object = (NativeObject) args[0];
			for (Object id : object.getAllIds())
			{
				mappings.put(object.get(Integer.parseInt(id.toString()), thisObj).toString(), id.toString());
			}
		}
		StringBuilder result = new StringBuilder();
		String lhs = Context.toString(thisObj);
		if (mappings.containsKey(lhs)) result.append(mappings.get(lhs));
		else result.append(9999);
		return new ScriptableValue(thisObj, result.toString());
	}
}
