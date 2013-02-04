package org.molgenis.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.molgenis.util.ValueLabel;

public class SelectInput extends HtmlInput<SelectInput, ValueLabel>
{
	List<ValueLabel> options = new ArrayList<ValueLabel>();

	public SelectInput(String id, ValueLabel value)
	{
		super(id, value);
	}

	public SelectInput(String id, String value)
	{
		super(id, new ValueLabel(value, value));
	}

	public SelectInput setOptions(Map<String, String> options)
	{
		this.options.clear();
		for (Entry<String, String> e : options.entrySet())
		{
			this.options.add(new ValueLabel(e.getKey(), e.getValue()));
		}
		return this;
	}

	public SelectInput options(String... options)
	{
		this.options.clear();
		for (String s : options)
		{
			this.options.add(new ValueLabel(s, s));
		}
		return this;
	}

	public void clearOptions()
	{
		options.clear();
	}

	public List<ValueLabel> getOptions()
	{
		return options;
	}
}
