package org.molgenis.js.magma;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import jdk.nashorn.api.scripting.JSObject;
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

import javax.script.Bindings;
import javax.script.SimpleBindings;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.stream.Collectors.toList;
import static org.molgenis.util.ResourceUtils.getString;

/**
 * JavaScript script evaluator using the Nashorn script engine.
 */
@Component
public class JsMagmaScriptEvaluator
{
	private static final Logger LOG = LoggerFactory.getLogger(JsMagmaScriptEvaluator.class);
	private static final int ENTITY_REFERENCE_DEFAULT_FETCHING_DEPTH = 1;
	private static final String KEY_IS_NULL = "_isNull";
	private static final String KEY_NEW_VALUE = "newValue";
	private static final String KEY_DOLLAR = "$";
	private static final String KEY_MAGMA_SCRIPT = "MagmaScript";
	private static final String BIND = "bind";
	public static final String KEY_ID_VALUE = "_idValue";

	private final NashornScriptEngine jsScriptEngine;
	private final Bindings magmaBindings = new SimpleBindings();

	private static final List<String> RESOURCE_NAMES;

	static
	{
		RESOURCE_NAMES = asList("/js/es6-shims.js", "/js/math.min.js", "/js/script-evaluator.js");
	}

	public JsMagmaScriptEvaluator(NashornScriptEngine jsScriptEngine) throws javax.script.ScriptException, IOException
	{
		this.jsScriptEngine = requireNonNull(jsScriptEngine);
		for (String resourceName : RESOURCE_NAMES)
		{
			loadScript(resourceName);
		}
	}

	private void loadScript(String resourceName) throws javax.script.ScriptException, IOException
	{
		String string = getString(getClass(), resourceName);
		jsScriptEngine.eval(magmaBindings, string);
	}

	/**
	 * Evaluates multiple expressions for a single entity instance.
	 *
	 * @param expressions {@link Collection} containing the expression {@link String}s
	 * @param entity      the entity to bind the magmascript $ function to
	 * @return Collection containing the expression result {@link Object}s
	 */
	public Collection<Object> eval(Collection<String> expressions, Entity entity)
	{
		Stopwatch stopwatch = null;
		if (LOG.isTraceEnabled())
		{
			stopwatch = Stopwatch.createStarted();
		}
		Bindings bindings = createBindings(entity, ENTITY_REFERENCE_DEFAULT_FETCHING_DEPTH);
		List<Object> result = expressions.stream().map(expression -> eval(bindings, expression)).collect(toList());
		if (stopwatch != null)
		{
			stopwatch.stop();
			LOG.trace("Script evaluation took {} µs", stopwatch.elapsed(MICROSECONDS));
		}
		return result;
	}

	public Object eval(String expression, Entity entity)
	{
		return eval(expression, entity, ENTITY_REFERENCE_DEFAULT_FETCHING_DEPTH);
	}

	/**
	 * Evaluate a expression for a given entity.
	 *
	 * @param expression JavaScript expression
	 * @param entity     entity
	 * @return evaluated expression result, return type depends on the expression.
	 */
	public Object eval(String expression, Entity entity, int depth)
	{
		return eval(createBindings(entity, depth), expression);
	}

	/**
	 * Evaluates an expression with the given bindings.
	 *
	 * @param bindings Bindings to use as engine scope
	 * @param expression  JavaScript expression to evaluate
	 * @return evaluated expression result, return type depends on the expression.
	 */
	private Object eval(Bindings bindings, String expression)
	{
		try
		{
			return jsScriptEngine.eval(bindings, expression);
		}
		catch (javax.script.ScriptException t)
		{
			return new ScriptException(t.getCause());
		}
		catch (Exception t)
		{
			return new ScriptException(t);
		}
	}

	/**
	 * Creates magmascript bindings for a given Entity.
	 *
	 * @param entity the entity to bind to the magmascript $ function
	 * @param depth  maximum depth to follow references when creating the entity value map
	 * @return Bindings with $ function bound to the entity
	 */
	private Bindings createBindings(Entity entity, int depth)
	{
		Bindings bindings = new SimpleBindings();
		JSObject global = (JSObject) magmaBindings.get("nashorn.global");
		JSObject magmaScript = (JSObject) global.getMember(KEY_MAGMA_SCRIPT);
		JSObject dollarFunction = (JSObject) magmaScript.getMember(KEY_DOLLAR);
		JSObject bindFunction = (JSObject) dollarFunction.getMember(BIND);
		Object boundDollar = bindFunction.call(dollarFunction, toScriptEngineValueMap(entity, depth));
		bindings.put(KEY_DOLLAR, boundDollar);
		bindings.put(KEY_NEW_VALUE, magmaScript.getMember(KEY_NEW_VALUE));
		bindings.put(KEY_IS_NULL, magmaScript.getMember(KEY_IS_NULL));
		return bindings;
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
				map.put(KEY_ID_VALUE, idValue);
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
