package org.molgenis.js.magma;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.js.nashorn.NashornScriptEngine;
import org.molgenis.script.core.ScriptException;
import org.molgenis.util.UnexpectedEnumException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MICROSECONDS;

/**
 * JavaScript script evaluator using the Nashorn script engine.
 */
@Component
public class JsMagmaScriptEvaluator
{
	private static final Logger LOG = LoggerFactory.getLogger(JsMagmaScriptEvaluator.class);
	private static final int ENTITY_REFERENCE_DEFAULT_FETCHING_DEPTH = 1;

	private final NashornScriptEngine jsScriptEngine;

	public JsMagmaScriptEvaluator(NashornScriptEngine jsScriptEngine)
	{
		this.jsScriptEngine = requireNonNull(jsScriptEngine);
	}

	public Object eval(String expression, Entity entity)
	{
		return eval(expression, entity, ENTITY_REFERENCE_DEFAULT_FETCHING_DEPTH);
	}

	/**
	 * Evaluate a expression for the given entity.
	 *
	 * @param expression JavaScript expression
	 * @param entity     entity
	 * @return evaluated expression result, return type depends on the expression.
	 */
	public Object eval(String expression, Entity entity, int depth)
	{
		Stopwatch stopwatch = null;
		if (LOG.isTraceEnabled())
		{
			stopwatch = Stopwatch.createStarted();
		}

		Object scriptEngineValueMap = toScriptEngineValueMap(entity, depth);
		Object value;
		try
		{
			value = jsScriptEngine.invokeFunction("evalScript", expression, scriptEngineValueMap);
		}
		catch (Throwable t)
		{
			return new ScriptException(t);
		}

		if (stopwatch != null)
		{
			stopwatch.stop();
			LOG.trace("Script evaluation took {} µs", stopwatch.elapsed(MICROSECONDS));
		}

		return value;
	}

	/**
	 * Convert entity to a JavaScript object.
	 * Adds "_idValue" as a special key to every level for quick access to the id value of an entity.
	 *
	 * @param entity The entity to be flattened, should start with non null entity
	 * @param depth  Represents the number of reference levels being added to the JavaScript object
	 * @return A JavaScript object in Tree form, containing entities and there references
	 */
	private Object toScriptEngineValueMap(Entity entity, int depth)
	{
		if (entity != null)
		{
			Object idValue = toScriptEngineValue(entity, entity.getEntityType().getIdAttribute(), 0);
			if (depth == 0)
			{
				return idValue;
			}
			else
			{
				Map<String, Object> map = Maps.newHashMap();
				entity.getEntityType()
					  .getAtomicAttributes()
					  .forEach(attr -> map.put(attr.getName(), toScriptEngineValue(entity, attr, depth)));
				map.put("_idValue", idValue);
				return map;
			}
		}
		else
		{
			return null;
		}
	}

	private Object toScriptEngineValue(Entity entity, Attribute attr, int depth)
	{
		Object value = null;

		String attrName = attr.getName();
		AttributeType attrType = attr.getDataType();
		switch (attrType)
		{
			case BOOL:
				value = entity.getBoolean(attrName);
				break;
			case CATEGORICAL:
			case FILE:
			case XREF:
				Entity xrefEntity = entity.getEntity(attrName);
				value = toScriptEngineValueMap(xrefEntity, depth - 1);
				break;
			case CATEGORICAL_MREF:
			case MREF:
			case ONE_TO_MANY:
				ScriptObjectMirror jsArray = jsScriptEngine.newJSArray();
				@SuppressWarnings("unchecked")
				List<Object> mrefValues = jsArray.to(List.class);
				entity.getEntities(attrName)
					  .forEach(mrefEntity -> mrefValues.add(toScriptEngineValueMap(mrefEntity, depth - 1)));
				value = jsArray;
				break;
			case DATE:
				LocalDate localDate = entity.getLocalDate(attrName);
				if (localDate != null)
				{
					value = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
				}
				break;
			case DATE_TIME:
				Instant instant = entity.getInstant(attrName);
				if (instant != null)
				{
					value = instant.toEpochMilli();
				}
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
			case INT:
				value = entity.getInt(attrName);
				break;
			case LONG:
				value = entity.getLong(attrName);
				break;
			case COMPOUND:
				throw new RuntimeException(format("Illegal attribute type [%s]", attrType.toString()));
			default:
				throw new UnexpectedEnumException(attrType);
		}
		return value;
	}
}
