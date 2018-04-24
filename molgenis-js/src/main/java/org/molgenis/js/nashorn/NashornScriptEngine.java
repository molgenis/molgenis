package org.molgenis.js.nashorn;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.molgenis.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.script.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static javax.script.ScriptContext.ENGINE_SCOPE;

@Component
public class NashornScriptEngine
{
	private static final Logger LOG = LoggerFactory.getLogger(NashornScriptEngine.class);

	private static final List<String> RESOURCE_NAMES;
	private static final int MAX_COMPILED_EXPRESSIONS_CACHE = 10000;

	static
	{
		RESOURCE_NAMES = asList("/js/es6-shims.js", "/js/math.min.js", "/js/script-evaluator.js");
	}

	private ScriptEngine scriptEngine;
	private LoadingCache<String, CompiledScript> expressions;

	public NashornScriptEngine()
	{
		initScriptEngine();
	}

	public Object eval(String script) throws ScriptException
	{
		return convertNashornValue(scriptEngine.eval(script, copyEngineBindings()));
	}

	/**
	 * Binds magmascript $ function to an entity value map.
	 *
	 * @param entityValueMap the map to bind the $ functions to
	 * @return Bindings object where the $ function is bound to the entity value map
	 */
	public Bindings createBindings(Object entityValueMap)
	{
		Bindings bindings = copyEngineBindings();
		JSObject magmaScript = (JSObject) bindings.get("MagmaScript");
		JSObject dollarFunction = (JSObject) magmaScript.getMember("$");
		JSObject bindFunction = (JSObject) dollarFunction.getMember("bind");
		Object boundDollar = bindFunction.call(dollarFunction, entityValueMap);
		bindings.put("$", boundDollar);
		bindings.put("newValue", magmaScript.getMember("newValue"));
		bindings.put("_isNull", magmaScript.getMember("_isNull"));
		return bindings;
	}

	private Bindings copyEngineBindings()
	{
		Bindings bindings = new SimpleBindings();
		bindings.putAll(scriptEngine.getBindings(ENGINE_SCOPE));
		return bindings;
	}

	/**
	 * Evaluates an expression using given bindings.
	 *
	 * @param bindings   the Bindings to use as ENGINE_SCOPE
	 * @param expression the expression to evaluate
	 * @return result of the evaluation
	 * @throws ScriptException if the evaluation fails
	 */
	public Object eval(Bindings bindings, String expression) throws ScriptException
	{
		CompiledScript compiledExpression = expressions.get(expression);
		Object returnValue = compiledExpression.eval(bindings);
		return convertNashornValue(returnValue);
	}

	public ScriptObjectMirror newJSArray()
	{
		return (ScriptObjectMirror) ((JSObject) scriptEngine.get("Array")).newObject();
	}

	private void initScriptEngine()
	{
		LOG.debug("Initializing Nashorn script engine ...");
		NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
		scriptEngine = factory.getScriptEngine(s -> false); // create engine with class filter exposing no classes

		// construct common JavaScript content string from defined resources
		String commonJs = RESOURCE_NAMES.stream().map(this::loadScript).collect(joining("\n"));

		try
		{
			scriptEngine.eval(commonJs);
		}
		catch (ScriptException e)
		{
			throw new IllegalStateException("Failed to load default resources.", e);
		}
		copyMagmaScriptFunctions();

		expressions = Caffeine.newBuilder().maximumSize(MAX_COMPILED_EXPRESSIONS_CACHE).build(((Compilable) this.scriptEngine)::compile);

		LOG.debug("Initialized Nashorn script engine");
	}

	/**
	 * Copies static MagmaScript functions to engine scope
	 */
	private void copyMagmaScriptFunctions()
	{
		// make MagmaScript functions available on global scope
		JSObject magmaScript = (JSObject) scriptEngine.get("MagmaScript");
		scriptEngine.put("$", magmaScript.getMember("$"));
		scriptEngine.put("newValue", magmaScript.getMember("newValue"));
		scriptEngine.put("_isNull", magmaScript.getMember("_isNull"));
	}

	private String loadScript(String resourceName)
	{
		try
		{
			return ResourceUtils.getString(getClass(), resourceName);
		}
		catch (IOException e)
		{
			throw new IllegalStateException("Failed to load resource file: " + resourceName);
		}
	}

	private Object convertNashornValue(Object nashornValue)
	{
		if (nashornValue == null)
		{
			return null;
		}

		Object convertedValue;
		String idValueKey = "_idValue";
		if (nashornValue instanceof ScriptObjectMirror)
		{
			ScriptObjectMirror scriptObjectMirror = (ScriptObjectMirror) nashornValue;
			if (scriptObjectMirror.isArray())
			{
				convertedValue = scriptObjectMirror.values()
												   .stream()
												   .map(this::convertNashornValue)
												   .collect(Collectors.toList());
			}
			else
			{
				if ("Date".equals(scriptObjectMirror.getClassName()))
				{
					// convert to Java Interface
					JsDate jsDate = ((Invocable) scriptEngine).getInterface(scriptObjectMirror, JsDate.class);
					return jsDate.getTime();
				}
				else if (((ScriptObjectMirror) nashornValue).containsKey(idValueKey))
				{
					// entity object returned from script
					return ((ScriptObjectMirror) nashornValue).get(idValueKey);
				}
				else
				{
					throw new RuntimeException("Unable to convert [ScriptObjectMirror]");
				}
			}
		}
		else if (nashornValue instanceof Map)
		{
			Map mapValue = (Map) (nashornValue);
			if (mapValue.get(idValueKey) != null)
			{
				convertedValue = mapValue.get(idValueKey);
			}
			else
			{
				convertedValue = nashornValue;
			}
		}
		else
		{
			convertedValue = nashornValue;
		}
		return convertedValue;
	}
}
