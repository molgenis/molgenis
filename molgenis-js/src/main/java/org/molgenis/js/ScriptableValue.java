package org.molgenis.js;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * {@link ScriptableObject} that holds the value of an interpreted js function.
 * 
 * TODO Null values
 */
public class ScriptableValue extends ScriptableObject
{
	private static final long serialVersionUID = 277471335110754837L;
	private static final String CLASS_NAME = "Value";
	private Object value;

	public ScriptableValue()
	{
	}

	public ScriptableValue(Scriptable scope, Object value)
	{
		super(scope, ScriptableObject.getClassPrototype(scope, CLASS_NAME));

		if (value == null)
		{
			throw new IllegalArgumentException("Value is null");// TODO How to deal with null values?
		}

		this.value = value;
	}

	@Override
	public String getClassName()
	{
		return CLASS_NAME;
	}

	public Object getValue()
	{
		return value;
	}

	@Override
	public Object getDefaultValue(Class<?> typeHint)
	{
		return value;
	}

	@Override
	public String toString()
	{
		return value.toString();
	}

}
