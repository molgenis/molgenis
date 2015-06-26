package org.molgenis.js;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Evaluate a script with molgenis-script-evaluator.js
 */
public class ScriptEvaluator
{
	private static final Logger LOG = LoggerFactory.getLogger(ScriptEvaluator.class);

	private static String JS_SCRIPT = null;

	/**
	 * Evaluates a script for a single entity.
	 * 
	 * @param source
	 *            {@link String} containing the script to evaluate
	 * @param entity
	 *            the {@link Entity} to evaluate the script on
	 * @param entityMetaData
	 *            {@link EntityMetaData} for the entity
	 * @return result of the evaluation, or a {@link RuntimeException} if one was thrown
	 * @throws EcmaError
	 *             if there's a syntax error in the script
	 */
	public static Object eval(final String source, final Entity entity, final EntityMetaData entityMetaData)
	{

		Object result = Iterables.get(eval(source, Collections.singleton(entity), entityMetaData), 0);
		if (result instanceof RuntimeException)
		{
			throw (RuntimeException) result;
		}
		return result;
	}

	/**
	 * Evaluates a script on a batch of entities
	 * 
	 * @param source
	 *            the source of the script to run
	 * @param entities
	 *            {@link Iterable} of {@link Entity}s to evaluate it for
	 * @param entityMetaData
	 * @return {@link List} of {@link Object} that contain the results of the evaluation, or the
	 *         {@link RuntimeException} if one was thrown
	 * @throws EcmaError
	 *             if there's a syntax error in the script
	 */
	@SuppressWarnings("unchecked")
	public static List<Object> eval(final String source, final Iterable<Entity> entities,
			final EntityMetaData entityMetaData)
	{
		if (JS_SCRIPT == null)
		{
			try
			{
				JS_SCRIPT = FileCopyUtils.copyToString(new InputStreamReader(ScriptEvaluator.class
						.getResourceAsStream("/js/molgenis-script-evaluator.js"), "UTF-8"));
			}
			catch (IOException e)
			{
				throw new UncheckedIOException(e);
			}
		}

		Object result = ContextFactory.getGlobal().call(new ContextAction()
		{
			@Override
			public Object run(Context cx)
			{
				ScriptableObject scriptableObject = cx.initStandardObjects();
				cx.evaluateString(scriptableObject, JS_SCRIPT, null, 1, null);
				Function evalScript = (Function) scriptableObject.get("evalScript", scriptableObject);

				List<Object> result = Lists.newArrayList();

				for (Entity entity : entities)
				{
					try
					{
						Scriptable scriptableEntity = mapEntity(entity, entityMetaData, cx, scriptableObject);
						result.add(evalScript.call(cx, scriptableObject, scriptableObject, new Object[]
						{ source, scriptableEntity }));
					}
					catch (EcmaError error)
					{
						if ("SyntaxError".equals(error.getName()))
						{
							throw error;
						}
						else
						{
							LOG.warn("EcmaError evaluating script, but it isn't a syntax error.", error);
							result.add(error);
						}
					}
					catch (RuntimeException ex)
					{
						result.add(ex);
					}
				}

				scriptableObject.sealObject();

				return result;
			}

			protected Scriptable mapEntity(final Entity entity, final EntityMetaData entityMetaData, Context cx,
					ScriptableObject scriptableObject)
			{
				Scriptable scriptableEntity = cx.newObject(scriptableObject);
				scriptableEntity.setPrototype(scriptableObject);
				entityMetaData.getAtomicAttributes().forEach(
						attr -> {
							scriptableEntity.put(attr.getName(), scriptableEntity,
									javaToJS(entity.get(attr.getName()), cx, scriptableObject));
						});
				return scriptableEntity;
			}
		});
		return (List<Object>) result;
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
