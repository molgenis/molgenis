package org.molgenis.js;

import com.google.api.client.util.Maps;
import com.google.common.base.Stopwatch;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.molgenis.AttributeType;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.file.model.FileMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Map;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

/**
 * JavaScript script evaluator using the Nashorn script engine.
 * <p>
 * TODO Decide on the best way to reuse Nashorn script engine, see: http://stackoverflow.com/a/30159424, http://stackoverflow.com/a/27712812, https://blogs.oracle.com/nashorn/entry/nashorn_multi_threading_and_mt
 * TODO Evaluate if script engine class filter is sufficient sand boxing and check if normal JavaScript script runner still works
 * TODO Evaluate current date and datetime conversion solution
 * TODO Extend unit tests
 * TODO Remove Rhino based ScriptEvaluator
 * <p>
 * Creating and initializing a script engine is very expensive, ideally we have one instance system-wide
 */
@Component
public class JsScriptEvaluator
{
	private static final Logger LOG = LoggerFactory.getLogger(JsScriptEvaluator.class);

	private static final String SCRIPT_EVALUATOR_RESOURCE_NAME = "js/molgenis-script-evaluator-nashorn.js";

	private ScriptEngine scriptEngine;

	/**
	 * Evaluate a expression for the given entity.
	 *
	 * @param expression JavaScript expression
	 * @param entity     entity
	 * @return evaluated expression result, return type depends on the expression.
	 */
	public synchronized Object eval(String expression, Entity entity)
	{
		if (scriptEngine == null)
		{
			initScriptEngine();
		}

		Invocable invocable = (Invocable) scriptEngine;
		try
		{
			Stopwatch stopwatch = null;
			if (LOG.isTraceEnabled())
			{
				stopwatch = Stopwatch.createStarted();
			}

			Map<String, Object> scriptEngineValueMap = toScriptEngineValueMap(entity);
			Object nashornValue = invocable.invokeFunction("evalScript", expression, scriptEngineValueMap);
			Object value = toEntityValue(nashornValue);

			if (stopwatch != null)
			{
				stopwatch.stop();
				LOG.trace("Script evaluation took {} µs", stopwatch.elapsed(MICROSECONDS));
			}

			return value;
		}
		catch (ScriptException | NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static Map<String, Object> toScriptEngineValueMap(Entity entity)
	{
		Map<String, Object> map = Maps.newHashMap();
		entity.getEntityType().getAtomicAttributes()
				.forEach(attr -> map.put(attr.getName(), toScriptEngineValue(entity, attr)));
		return map;
	}

	private static Object toScriptEngineValue(Entity entity, Attribute attr)
	{
		Object value;

		String attrName = attr.getName();
		AttributeType attrType = attr.getDataType();
		switch (attrType)
		{
			case BOOL:
				value = entity.getBoolean(attrName);
				break;
			case CATEGORICAL:
			case XREF:
				Entity xrefEntity = entity.getEntity(attrName);
				value = xrefEntity != null ? toScriptEngineValue(xrefEntity,
						xrefEntity.getEntityType().getIdAttribute()) : null;
				break;
			case CATEGORICAL_MREF:
			case MREF:
			case ONE_TO_MANY:
				Iterable<Entity> mrefEntities = entity.getEntities(attrName);
				value = stream(mrefEntities.spliterator(), false)
						.map(mrefEntity -> toScriptEngineValue(mrefEntity, mrefEntity.getEntityType().getIdAttribute()))
						.collect(toList());
				break;
			case DATE:
				// convert to epoch
				Date date = entity.getDate(attrName);
				value = date != null ? date.getTime() : null;
				break;
			case DATE_TIME:
				// convert to epoch
				Timestamp timestamp = entity.getTimestamp(attrName);
				value = timestamp != null ? timestamp.getTime() : null;
				break;
			case DECIMAL:
				value = entity.getDouble(attrName);
				break;
			case EMAIL:
			case ENUM:
			case HTML:
			case HYPERLINK:
			case SCRIPT:
			case STRING:
			case TEXT:
				value = entity.getString(attrName);
				break;
			case FILE:
				FileMeta fileEntity = entity.getEntity(attrName, FileMeta.class);
				value = fileEntity != null ? toScriptEngineValue(fileEntity,
						fileEntity.getEntityType().getIdAttribute()) : null;
				break;
			case INT:
				value = entity.getInt(attrName);
				break;
			case LONG:
				value = entity.getLong(attrName);
				break;
			case COMPOUND:
				throw new RuntimeException(format("Illegal attribute type [%s]", attrType.toString()));
			default:
				throw new RuntimeException(format("Unknown attribute type [%s]", attrType.toString()));
		}
		return value;
	}

	private static Object toEntityValue(Object nashornValue)
	{
		if (nashornValue == null)
		{
			return null;
		}

		Object convertedValue;
		if (nashornValue instanceof ScriptObjectMirror)
		{
			ScriptObjectMirror scriptObjectMirror = (ScriptObjectMirror) nashornValue;
			if (scriptObjectMirror.isArray())
			{
				convertedValue = scriptObjectMirror.values();
			}
			else
			{
				throw new RuntimeException("Unable to convert [ScriptObjectMirror]");
			}
		}
		else
		{
			convertedValue = nashornValue;
		}
		return convertedValue;
	}

	private void initScriptEngine()
	{
		LOG.debug("Initializing Nashorn script engine ...");
		NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
		scriptEngine = factory.getScriptEngine(s -> false); // create engine with class filter exposing no classes
		try
		{
			scriptEngine.eval("load(\"classpath:" + SCRIPT_EVALUATOR_RESOURCE_NAME + "\")");
		}
		catch (ScriptException e)
		{
			throw new RuntimeException("", e);
		}
		LOG.debug("Initialized Nashorn script engine");
	}
}
