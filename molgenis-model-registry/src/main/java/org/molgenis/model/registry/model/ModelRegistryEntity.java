package org.molgenis.model.registry.model;

/**
 * @author sido
 */
public class ModelRegistryEntity
{

	private String name;
	private String label;
	private boolean abstr;

	public ModelRegistryEntity(String name, String label, boolean abstr)
	{
		super();
		this.name = name;
		this.label = label;
		this.abstr = abstr;
	}

	public String getName()
	{
		return name;
	}

	@SuppressWarnings("unused")
	public String getLabel()
	{
		return label;
	}

	public boolean isAbtract()
	{
		return abstr;
	}
}
