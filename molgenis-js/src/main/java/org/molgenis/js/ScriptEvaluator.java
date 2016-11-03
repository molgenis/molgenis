package org.molgenis.js;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.mozilla.javascript.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.*;

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
	 * @param source     {@link String} containing the script to evaluate
	 * @param entity     the {@link Entity} to evaluate the script on
	 * @param entityType {@link EntityType} for the entity
	 * @return result of the evaluation, or a {@link RuntimeException} if one was thrown
	 * @throws EcmaError if there's a syntax error in the script
	 */
	public static Object eval(final String source, final Entity entity, final EntityType entityType)
	{
		Object result = Iterables
				.get(eval(Collections.singletonList(source), Collections.singleton(entity), entityType), 0);
		if (result instanceof RuntimeException)
		{
			throw (RuntimeException) result;
		}
		return result;
	}

	public static List<Object> eval(final String source, final Iterable<Entity> entities, final EntityType entityType)
	{
		List<Object> results = eval(Arrays.asList(source), entities, entityType);

		if (results.get(0) instanceof RuntimeException)
		{
			throw (RuntimeException) results.get(0);
		}
		return results;
	}

	public static List<Object> eval(final List<String> sources, final Entity entity, final EntityType entityType)
	{
		List<Object> results = eval(sources, Collections.singleton(entity), entityType);

		if (results.get(0) instanceof RuntimeException)
		{
			throw (RuntimeException) results.get(0);
		}
		return results;
	}

	/**
	 * Evaluates a script on a batch of entities
	 *
	 * @param sources    the source of the script to run
	 * @param entities   {@link Iterable} of {@link Entity}s to evaluate it for
	 * @param entityType
	 * @return {@link List} of {@link Object} that contain the results of the evaluation, or the
	 * {@link RuntimeException} if one was thrown
	 * @throws EcmaError if there's a syntax error in the script
	 */
	@SuppressWarnings("unchecked")
	protected static List<Object> eval(final List<String> sources, final Iterable<Entity> entities,
			final EntityType entityType)
	{
		if (JS_SCRIPT == null)
		{
			try
			{
				JS_SCRIPT = FileCopyUtils.copyToString(new InputStreamReader(
						ScriptEvaluator.class.getResourceAsStream("/js/molgenis-script-evaluator.js"), "UTF-8"));
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
						Scriptable scriptableEntity = mapEntity(entity, entityType, cx, scriptableObject);

						for (String source : sources)
						{
							try
							{
								result.add(evalScript.call(cx, scriptableObject, scriptableObject,
										new Object[] { source, scriptableEntity }));
							}
							catch (EcmaError e)
							{
								LOG.error("Failing script: " + source);
								throw e;
							}
						}
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

			protected Scriptable mapEntity(final Entity entity, final EntityType EntityType, Context cx,
					ScriptableObject scriptableObject)
			{
				Scriptable scriptableEntity = cx.newObject(scriptableObject);
				scriptableEntity.setPrototype(scriptableObject);
				EntityType.getAtomicAttributes().forEach(attr ->
				{
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
			Scriptable convertedValue = cx.newObject(scope, "Date", new Object[] { dateLong });
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
