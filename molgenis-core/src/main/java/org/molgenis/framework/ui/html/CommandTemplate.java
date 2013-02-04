package org.molgenis.framework.ui.html;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;

import org.molgenis.util.tuple.Tuple;

/**
 * This class is used by commands as template. It must be replaced by
 * 'container' at some point.
 */
public class CommandTemplate extends LinkedHashMap<String, HtmlInput<?>>
{
	private static final long serialVersionUID = -8565170009471766957L;

	public void add(HtmlInput<?> i)
	{
		this.put(i.getName().toLowerCase(), i);
	}

	public void addAll(List<HtmlInput<?>> inputs)
	{
		for (HtmlInput<?> i : inputs)
			this.add(i);
	}

	public void addAll(Vector<HtmlInput<?>> inputs)
	{
		for (HtmlInput<?> i : inputs)
			this.add(i);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public HtmlInput get(Object key)
	{
		if (key instanceof String) return super.get(((String) key).toLowerCase());
		return super.get(key);
	}

	@SuppressWarnings("unchecked")
	public void setAll(Tuple t)
	{
		for (String key : t.getColNames())
		{
			// only sets known fields!
			if (this.containsKey(key)) this.get(key).setValue(t.get(key));
		}
	}

	public List<HtmlInput<?>> getInputs()
	{
		List<HtmlInput<?>> result = new ArrayList<HtmlInput<?>>();
		for (String key : this.keySet())
		{
			result.add((HtmlInput<?>) this.get(key));
		}
		return result;
	}
}
