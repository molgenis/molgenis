package org.molgenis.dataexplorer.controller;

public class ModuleConfig
{
	private final String id;
	private final String label;
	private final String icon;

	public ModuleConfig(String id, String label, String icon)
	{
		if (id == null) throw new IllegalArgumentException("id is null");
		if (label == null) throw new IllegalArgumentException("label is null");
		if (icon == null) throw new IllegalArgumentException("icon is null");
		this.id = id;
		this.label = label;
		this.icon = icon;
	}

	public String getId()
	{
		return id;
	}

	public String getLabel()
	{
		return label;
	}

	public String getIcon()
	{
		return icon;
	}
}
