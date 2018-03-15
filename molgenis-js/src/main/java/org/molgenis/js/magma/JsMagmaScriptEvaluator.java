package org.molgenis.js.magma;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
import java.util.Set;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

/**
 * JavaScript script evaluator using the Nashorn script engine.
 */
@Component
public class JsMagmaScriptEvaluator
{
	private static final Logger LOG = LoggerFactory.getLogger(JsMagmaScriptEvaluator.class);

	private final NashornScriptEngine jsScriptEngine;

	/**
	 * Set containing the attribute types that can be used in combination the the 'dot' operator to resolve the reference
	 * value from the referenced entity.
	 */
	private static Set<AttributeType> referenceTypes = Sets.newHashSet(AttributeType.CATEGORICAL,
			AttributeType.CATEGORICAL_MREF, AttributeType.XREF, AttributeType.MREF);

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

		Map<String, Object> scriptEngineValueMap = toScriptEngineValueMap(expression, entity);
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

	private static Map<String, Object> toScriptEngineValueMap(String expression, Entity entity)
	{
		Map<String, Object> map = Maps.newHashMap();
		entity.getEntityType()
			  .getAtomicAttributes()
			  .forEach(attr -> {

			  	map.put(attr.getName(), toScriptEngineValue(expression, entity, attr));

				  AttributeType attributeType = attr.getDataType();
				  if(attributeType == AttributeType.CATEGORICAL || attributeType == AttributeType.XREF) {
					  Entity refEntity = entity.getEntity(attr.getName());
					  if(refEntity != null) {
						  entity.getEntityType().getAtomicAttributes().forEach(refAttr -> {
							  map.put(attr.getName() + "." + refAttr.getName(), toScriptEngineValue(expression, entity, attr));
						  });
					  }
				  }
			  });
		return map;
	}

	private static boolean isReferenceTypeAttr(AttributeType attrType)
	{
		return referenceTypes.contains(attrType);
	}

	private static Object toScriptEngineValue(String expression, Entity entity, Attribute attr)
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

				if(xrefEntity != null) {
					Attribute idAttribute = xrefEntity.getEntityType().getIdAttribute();
					value = toScriptEngineValue(expression, xrefEntity, idAttribute);
				}

				break;
			case CATEGORICAL_MREF:
			case MREF:
			case ONE_TO_MANY:
				Iterable<Entity> mrefEntities = entity.getEntities(attrName);
				value = stream(mrefEntities.spliterator(), false).map(
						mrefEntity -> toScriptEngineValue(expression, mrefEntity, mrefEntity.getEntityType().getIdAttribute()))
																 .collect(toList());
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
