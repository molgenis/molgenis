package org.molgenis.model.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Entity
{
	@XmlAttribute
	private String name;

	@XmlAttribute
	private String label;

	// serializes attributes in reverse order of elements
	@XmlElement
	private String description;

	@XmlElement(name = "field")
	private List<Field> fields = new ArrayList<Field>();

	@XmlElement(name = "unique")
	private List<Unique> uniques = new ArrayList<Unique>();

	@XmlAttribute(name = "extends")
	private String _extends = null;

	@XmlAttribute(name = "implements")
	private String _implements = null;

	@XmlAttribute(name = "decorator")
	private String _decorator = null;

	@XmlAttribute(name = "abstract")
	private Boolean _abstract;

	// HELPER METHODS
	public Field getField(String name)
	{
		for (Field f : fields)
		{
			if (f.getName().trim().equals(name.trim())) return f;
		}
		return null;
	}

	// GETTERS and SETTERS
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	// added function addField for adding field to entity
	public void addField(Field e)
	{
		fields.add(e);
	}

	public void removeField(int index)
	{
		fields.remove(index);
	}

	public List<Field> getFields()
	{
		return fields;
	}

	public void setFields(List<Field> fields)
	{
		this.fields = fields;
	}

	public List<Unique> getUniques()
	{
		return uniques;
	}

	public void setUniques(List<Unique> uniques)
	{
		this.uniques = uniques;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public Boolean isAbstract()
	{
		return _abstract;
	}

	public void setAbstract(Boolean _abstract)
	{
		this._abstract = _abstract;
	}

	public String getExtends()
	{
		return _extends;
	}

	public void setExtends(String _extends)
	{
		this._extends = _extends;
	}

	public String getImplements()
	{
		return _implements;
	}

	public void setImplements(String _implements)
	{
		this._implements = _implements;
	}

	public String getDecorator()
	{
		return _decorator;
	}

	public void setDecorator(String _decorator)
	{
		this._decorator = _decorator;
	}
}
