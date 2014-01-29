package org.molgenis.data.jpa;

import java.util.Arrays;
import java.util.List;

import org.molgenis.data.DataConverter;
import org.molgenis.data.Entity;
import org.molgenis.data.support.AbstractMetaDataEntity;

@javax.persistence.Entity
@javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
public class Person extends AbstractMetaDataEntity
{
	private static final long serialVersionUID = 1L;

	@javax.persistence.Id
	@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
	private Integer id;
	private String firstName;
	private String lastName;

	public Person()
	{
		super(new PersonMetaData());
	}

	public Person(String firstName, String lastName)
	{
		this();
		this.firstName = firstName;
		this.lastName = lastName;
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
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Person other = (Person) obj;
		if (firstName == null)
		{
			if (other.firstName != null) return false;
		}
		else if (!firstName.equals(other.firstName)) return false;
		if (id == null)
		{
			if (other.id != null) return false;
		}
		else if (!id.equals(other.id)) return false;
		if (lastName == null)
		{
			if (other.lastName != null) return false;
		}
		else if (!lastName.equals(other.lastName)) return false;
		return true;
	}

	@Override
	public String toString()
	{
		return String.format("{id=%s firstName=%s lastName=%s}", getId(), getFirstName(), getLastName());
	}
}
