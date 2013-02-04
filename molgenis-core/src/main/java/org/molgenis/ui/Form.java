package org.molgenis.ui;

import java.util.ArrayList;
import java.util.List;

public class Form extends MolgenisComponent<Form>
{
	List<MolgenisComponent<?>> components = new ArrayList<MolgenisComponent<?>>();

	String legend;

	public static enum FormType
	{
		VERTICAL, INLINE, HORIZONTAL
	};

	private FormType type = FormType.VERTICAL;

	public Form()
	{
		super();
	}

	public Form(FormType type)
	{
		assert type != null;
		this.type = type;
	}

	public Form add(MolgenisComponent<?> component)
	{
		assert component != null;
		this.components.add(component);

		return this;
	}

	public List<MolgenisComponent<?>> getComponents()
	{
		return components;
	}

	public FormType getType()
	{
		return type;
	}

	public Form setType(FormType type)
	{
		this.type = type;
		return this;
	}

	public String getLegend()
	{
		return legend;
	}

	public Form setLegend(String legend)
	{
		this.legend = legend;
		return this;
	}
}
