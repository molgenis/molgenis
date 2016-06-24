package org.molgenis.model.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Module
{
	@XmlAttribute
	private String name;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@XmlElement(name = "entity")
	private List<Entity> entities = new ArrayList<Entity>();

	public synchronized List<Entity> getEntities()
	{
		return entities;
	}

	public synchronized void setEntities(List<Entity> entities)
	{
		this.entities = entities;
	}

	public void addEntity(Entity e)
	{
		this.entities.add(e);
	}

	public Entity getEntity(String name)
	{
		for (Entity entity : entities)
		{
			if (entity.getName().toLowerCase().equals(name.toLowerCase())) return entity;
		}
		return null;
	}
}
