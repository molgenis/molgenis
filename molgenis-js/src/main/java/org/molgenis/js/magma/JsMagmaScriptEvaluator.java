package org.molgenis.js.magma;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
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
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.collect.Streams.stream;
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

	private final NashornScriptEngine jsScriptEngine;

	public JsMagmaScriptEvaluator(NashornScriptEngine jsScriptEngine)
	{
		this.jsScriptEngine = requireNonNull(jsScriptEngine);
	}

	/**
	 * Evaluate a expression for the given entity.
	 *
	 * @param expression JavaScript expression
	 * @param entity     entity
	 * @return evaluated expression result, return type depends on the expression.
	 */
	public Object eval(String expression, Entity entity)
	{
		Stopwatch stopwatch = null;
		if (LOG.isTraceEnabled())
		{
			stopwatch = Stopwatch.createStarted();
		}

		Map<String, Object> scriptEngineValueMap = toScriptEngineValueMap(entity,
				expressionIsMapExpression(expression));

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

	private boolean expressionIsMapExpression(String expression)
	{
		return expression.contains("').map({");
	}

	private static Map<String, Object> toScriptEngineValueMap(Entity entity, boolean isMapExpression)
	{
		Map<String, Object> map = Maps.newHashMap();
		entity.getEntityType()
			  .getAtomicAttributes()
			  .forEach(attr -> map.put(attr.getName(), toScriptEngineValue(entity, attr, isMapExpression)));

		return map;
	}

	private static Object toScriptEngineValue(Entity entity, Attribute attr, boolean isMapExpression)
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
				if (isMapExpression)
				{
					value = xrefEntity == null ? null : toScriptEngineValue(xrefEntity,
							xrefEntity.getEntityType().getIdAttribute(), isMapExpression);
				}
				else
				{
					value = xrefEntity;
				}
				break;
			case CATEGORICAL_MREF:
			case MREF:
			case ONE_TO_MANY:
				Iterable<Entity> mrefEntities = entity.getEntities(attrName);
				if (isMapExpression)
				{
					value = stream(mrefEntities).map(
							mrefEntity -> toScriptEngineValue(mrefEntity, mrefEntity.getEntityType().getIdAttribute(),
									isMapExpression)).collect(Collectors.toList());
				}
				else
				{
					value = stream(mrefEntities).collect(Collectors.toList());
				}
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
