package org.molgenis.data;

import java.util.Arrays;
import java.util.List;

import org.molgenis.data.support.AbstractEntity;
import org.molgenis.data.support.EntityMetaDataCache;

@javax.persistence.Entity
public class Person extends AbstractEntity implements Entity
{
	private static final long serialVersionUID = 1L;

	@javax.persistence.Id
	private Integer id;
	private String firstName;
	private String lastName;

	public Person()
	{
	}

	public String getFirstName()
	{
		return firstName;
	}

	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	public String getLastName()
	{
		return lastName;
	}

	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	public Integer getId()
	{
		return id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		return Arrays.asList(new String[]
		{ "firstName", "lastName" });
	}

	@Override
	public Object get(String attributeName)
	{
		if ("id".equalsIgnoreCase(attributeName)) return getId();
		if ("firstName".equalsIgnoreCase(attributeName)) return getFirstName();
		if ("lastName".equalsIgnoreCase(attributeName)) return getLastName();
		return null;
	}

	@Override
	public void set(String attributeName, Object value)
	{
		if ("id".equalsIgnoreCase(attributeName)) setFirstName(DataConverter.toString(value));
		else if ("firstName".equalsIgnoreCase(attributeName)) setFirstName(DataConverter.toString(value));
		else if ("lastName".equalsIgnoreCase(attributeName)) setLastName(DataConverter.toString(value));
		else throw new RuntimeException("attribute '" + attributeName + "'+ not known");
	}

	@Override
	public void set(Entity values)
	{
		for (String attributeName : values.getAttributeNames())
		{
			this.set(attributeName, values.get(attributeName));
		}
	}

	@Override
	public Integer getIdValue()
	{
		return getId();
	}

	@Override
	public String getLabelValue()
	{
		return getFirstName() + " " + getLastName();
	}

	@Override
	public List<String> getLabelAttributeNames()
	{
		return Arrays.asList(new String[]
		{ "firstName", "lastName" });
	}

	@Override
	public void set(Entity entity, boolean strict)
	{
		this.set(entity);
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return (PersonMetaData) EntityMetaDataCache.get(Person.class.getSimpleName());
	}

	@Override
	public String toString()
	{
		return String.format("{id=%s firstName=%s lastName=%s}", getId(), getFirstName(), getLastName());
	}
}
