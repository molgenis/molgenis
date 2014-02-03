package org.molgenis.js.methods;

import java.math.BigDecimal;
import java.math.MathContext;

import org.molgenis.js.ScriptableValue;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

/**
 * Arithmetic functions
 */
public class NumericMethods
{
	/**
	 * $('test').div(5)
	 * 
	 * @param ctx
	 * @param thisObj
	 * @param args
	 * @param funObj
	 * @return
	 */
	public static Scriptable div(Context ctx, Scriptable thisObj, Object[] args, Function funObj)
	{
		if (args.length != 1)
		{
			throw new IllegalArgumentException("div expects one argument. Example: $('weight').div(10)");
		}

		BigDecimal lhs = new BigDecimal(Context.toNumber(thisObj));
		BigDecimal rhs = new BigDecimal(Context.toNumber(args[0]));
		BigDecimal result = lhs.divide(rhs, MathContext.DECIMAL128);

		return new ScriptableValue(thisObj, result.doubleValue());
	}

	/**
	 * $('test').pow(2)
	 * 
	 * @param ctx
	 * @param thisObj
	 * @param args
	 * @param funObj
	 * @return
	 */
	public static Scriptable pow(Context ctx, Scriptable thisObj, Object[] args, Function funObj)
	{
		if (args.length != 1)
		{
			throw new IllegalArgumentException("pow expects one argument. Example: $('weight').pow(10)");
		}

		BigDecimal lhs = new BigDecimal(Context.toNumber(thisObj));
		int rhs = (int) Context.toNumber(args[0]);
		BigDecimal result = lhs.pow(rhs, MathContext.DECIMAL128);

		return new ScriptableValue(thisObj, result.doubleValue());
	}
}
