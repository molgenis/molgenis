package org.molgenis.js.nashorn;

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

import static java.util.Arrays.asList;

@Component
public class NashornScriptEngine
{
	private static final Logger LOG = LoggerFactory.getLogger(NashornScriptEngine.class);

	private static final List<String> RESOURCE_NAMES;

	static
	{
		RESOURCE_NAMES = asList("/js/es6-shims.js", "/js/math.min.js", "/js/script-evaluator.js");
	}

	private ScriptEngine scriptEngine;
	private ThreadLocal<Bindings> bindingsThreadLocal;

	public NashornScriptEngine()
	{
		initScriptEngine();
	}

	public Object invokeFunction(String functionName, Object... args)
	{
		Bindings bindings = bindingsThreadLocal.get();
		Object returnValue = ((JSObject) bindings.get(functionName)).call(this, args);
		return convertNashornValue(returnValue);
	}

	public Object eval(String script) throws ScriptException
	{
		Bindings bindings = bindingsThreadLocal.get();
		return scriptEngine.eval(script, bindings);
	}

	private void initScriptEngine()
	{
		LOG.debug("Initializing Nashorn script engine ...");
		NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
		scriptEngine = factory.getScriptEngine(s -> false); // create engine with class filter exposing no classes

		// construct common JavaScript content string from defined resources
		StringBuilder commonJs = new StringBuilder(1000000);
		RESOURCE_NAMES.forEach(resourceName ->
		{
			try
			{
				commonJs.append(ResourceUtils.getString(getClass(), resourceName)).append('\n');
			}
			catch (IOException e)
			{
				throw new RuntimeException("", e);
			}
		});

		// pre-compile common JavaScript
		CompiledScript compiledScript;
		try
		{
			compiledScript = ((Compilable) scriptEngine).compile(commonJs.toString());
		}
		catch (ScriptException e)
		{
			throw new RuntimeException("", e);
		}

		// create bindings per thread resulting in a JavaScript global per thread
		bindingsThreadLocal = ThreadLocal.withInitial(() ->
		{
			Bindings bindings = scriptEngine.createBindings();
			try
			{
				// evaluate pre-compiled common JavaScript
				compiledScript.eval(bindings);
			}
			catch (ScriptException e)
			{
				throw new RuntimeException("", e);
			}
			return bindings;
		});

		LOG.debug("Initialized Nashorn script engine");
	}

	private static Object convertNashornValue(Object nashornValue)
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
}
