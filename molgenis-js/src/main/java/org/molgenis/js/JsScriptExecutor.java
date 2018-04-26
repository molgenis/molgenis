package org.molgenis.js;

import org.molgenis.js.nashorn.NashornScriptEngine;
import org.springframework.stereotype.Service;

import javax.script.ScriptException;

import static java.util.Objects.requireNonNull;

/**
 * Executes a JavaScript
 */
@Service
class JsScriptExecutor
{
	private final NashornScriptEngine jsScriptEngine;

	public JsScriptExecutor(NashornScriptEngine jsScriptEngine)
	{
		this.jsScriptEngine = requireNonNull(jsScriptEngine);
	}

	/**
	 * Executes the given JavaScript, e.g. 'var product = 2 * 3; return product;'
	 *
	 * @param jsScript JavaScript
	 * @return value of which the type depends on the JavaScript type of the returned variable
	 */
	Object executeScript(String jsScript)
	{
		String jsScriptWithFunction = "(function (){" + jsScript + "})();";
		try
		{
			return jsScriptEngine.eval(jsScriptWithFunction);
		}
		catch (ScriptException e)
		{
			throw new org.molgenis.script.core.ScriptException(e);
		}
	}
}
