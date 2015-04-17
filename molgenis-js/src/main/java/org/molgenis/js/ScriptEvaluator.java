package org.molgenis.js;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.springframework.util.FileCopyUtils;

/**
 * Evaluate a script with molgenis-script-evaluator.js
 */
public class ScriptEvaluator
{
	private static String JS_SCRIPT = null;

	public static Object eval(final String source, final Entity entity, final EntityMetaData entityMetaData)
	{
		if (JS_SCRIPT == null)
		{
			try
			{
				JS_SCRIPT = FileCopyUtils.copyToString(new InputStreamReader(ScriptEvaluator.class
						.getResourceAsStream("/js/molgenis-script-evaluator.js")));
			}
			catch (IOException e)
			{
				throw new UncheckedIOException(e);
			}
		}

		return ContextFactory.getGlobal().call(new ContextAction()
		{
			@Override
			public Object run(Context cx)
			{
				ScriptableObject scriptableObject = cx.initStandardObjects();
				cx.evaluateString(scriptableObject, JS_SCRIPT, null, 1, null);

				Scriptable scriptableEntity = cx.newObject(scriptableObject);
				scriptableEntity.setPrototype(scriptableObject);

				entityMetaData.getAtomicAttributes().forEach(
						attr -> {
							scriptableEntity.put(attr.getName(), scriptableEntity,
									javaToJS(entity.get(attr.getName()), cx, scriptableObject));
						});

				Function evalScript = (Function) scriptableObject.get("evalScript", scriptableObject);
				Object result = evalScript.call(cx, scriptableObject, scriptableObject, new Object[]
				{ source, scriptableEntity });

				scriptableObject.sealObject();

				return result;
			}
		});
	}

	private static Object javaToJS(Object value, Context cx, Scriptable scope)
	{
		if (value != null && value instanceof Date)
		{
			long dateLong = ((Date) value).getTime();
			Scriptable convertedValue = cx.newObject(scope, "Date", new Object[]
			{ dateLong });
			return convertedValue;
		}

		if (value instanceof Entity)
		{
			return javaToJS(((Entity) value).getIdValue(), cx, scope);
		}

		if (value instanceof Iterable)
		{
			List<Object> values = new ArrayList<>();
			for (Object val : (Iterable<?>) value)
			{
				values.add(javaToJS(val, cx, scope));
			}

			value = cx.newArray(scope, values.toArray(new Object[values.size()]));
		}

		return Context.javaToJS(value, scope);
	}
}
