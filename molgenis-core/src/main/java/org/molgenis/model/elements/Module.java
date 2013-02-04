package org.molgenis.model.elements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Annotation classes. Multiple entities can refer to one module.
 * 
 * @author Morris Swertz
 * 
 */
public class Module implements Serializable
{
	private static final long serialVersionUID = 6755412239260236656L;
	String name;
	String label;
	String description;
	List<Entity> entities = new ArrayList<Entity>();

	public Module(String name, Model model)
	{
		this.setName(name);
		model.getModules().add(this);
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public List<Entity> getEntities()
	{
		return entities;
	}

	public void setEntities(List<Entity> entities)
	{
		this.entities = entities;
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}
}
