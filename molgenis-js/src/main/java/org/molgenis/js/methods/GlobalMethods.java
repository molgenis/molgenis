package org.molgenis.js.methods;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

import org.molgenis.data.Entity;
import org.molgenis.js.MolgenisContext;
import org.molgenis.js.ScriptableValue;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.UniqueTag;

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

		// convert java Date to javascript Date
		if (value != null && value instanceof Date)
		{
			long dateLong = ((Date) value).getTime();
			Scriptable convertedValue = ctx.newObject(mctx.getSharedScope(), "Date", new Object[]
			{ dateLong });
			return convertedValue;
		}

		return new ScriptableValue(thisObj, value);
	}

	/**
	 * age($('birthdate'))
	 * 
	 * Calculates an age given a date
	 * 
	 * @param ctx
	 * @param thisObj
	 * @param args
	 * @param funObj
	 * @return
	 */
	public static Scriptable age(Context ctx, Scriptable thisObj, Object[] args, Function funObj)
	{
		if (args.length != 1)
		{
			throw new IllegalArgumentException(
					"age() expects exactly one argument: a date name. Example: age($('birthdate'))");
		}

		if (args[0] == UniqueTag.NULL_VALUE) return null;
		if ((args[0] instanceof ScriptableValue) && ((ScriptableValue) args[0]).getValue() == null) return null;

		Date d = (Date) Context.jsToJava(args[0], Date.class);
		LocalDate today = LocalDate.now();
		LocalDate birthday = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		Period p = Period.between(birthday, today);

		return new ScriptableValue(thisObj, p.getYears());
	}
}
