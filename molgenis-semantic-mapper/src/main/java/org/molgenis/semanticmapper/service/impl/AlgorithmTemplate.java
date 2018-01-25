package org.molgenis.semanticmapper.service.impl;

import org.molgenis.script.core.Script;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Mapping algorithm template for JavaScript Magma scripts.
 * <p>
 * Example: $('weight').div($('height').pow(2)).value()<br>
 * Example rendered:<br>
 * Model: {weight: 'attr1', height: 'attr2'}<br>
 * Example: $('attr1').div($('attr2').pow(2)).value()
 * </p>
 */
public class AlgorithmTemplate
{
	private final Script script;
	private final Map<String, String> model;

	public AlgorithmTemplate(Script script, Map<String, String> model)
	{
		this.script = requireNonNull(script);
		this.model = requireNonNull(model);
	}

	public String render()
	{
		String content = script.getContent();
		for (Map.Entry<String, String> entry : model.entrySet())
		{
			content = content.replaceAll(String.format("\\$\\('%s'\\)", entry.getKey()),
					String.format("\\$\\('%s'\\)", entry.getValue()));
		}
		return content;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((model == null) ? 0 : model.hashCode());
		result = prime * result + ((script == null) ? 0 : script.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		AlgorithmTemplate other = (AlgorithmTemplate) obj;
		if (model == null)
		{
			if (other.model != null) return false;
		}
		else if (!model.equals(other.model)) return false;
		if (script == null)
		{
			if (other.script != null) return false;
		}
		else if (!script.equals(other.script)) return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "AlgorithmTemplate [script=" + script + ", model=" + model + "]";
	}
}
