package org.molgenis.model.elements;

import org.molgenis.util.SimpleTree;

@Deprecated
public class PRSchema extends SimpleTree<PRSchema>
{
	public PRSchema(String name, PRSchema parent)
	{
		super(name, parent);
	}

	private static final long serialVersionUID = 2513991729164752297L;
}
