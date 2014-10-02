package org.molgenis.data.jpa;

import java.util.Arrays;
import java.util.List;

import javax.persistence.GeneratedValue;

import org.eclipse.persistence.annotations.UuidGenerator;
import org.molgenis.data.DataConverter;
import org.molgenis.data.Entity;
import org.molgenis.data.support.AbstractMetaDataEntity;

import com.google.common.collect.Lists;

@javax.persistence.Entity
@javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
@UuidGenerator(name = "PERSON_ID_GEN")
public class Person extends AbstractMetaDataEntity
{
	private static final long serialVersionUID = 1L;

	@javax.persistence.Id
	@GeneratedValue(generator = "PERSON_ID_GEN")
	private String id;
	private String firstName;
	private String lastName;
	private Integer age;

	@javax.persistence.ManyToOne
	private Person father;

	@javax.persistence.ManyToMany
	private List<Person> children = Lists.newArrayList();

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

	public Person(String firstName, String lastName, Integer age)
	{
		this();
		this.firstName = firstName;
		this.lastName = lastName;
		this.age = age;
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

	public Integer getAge()
	{
		return age;
	}

	public void setAge(Integer age)
	{
		this.age = age;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public Person getFather()
	{
		return father;
	}

	public void setFather(Person father)
	{
		this.father = father;
	}

	public List<Person> getChildren()
	{
		return children;
	}

	public void setChildren(List<Person> children)
	{
		this.children = children;
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		return Arrays.asList(new String[]
		{ "id", "firstName", "lastName", "age", "father", "children" });
	}

	@Override
	public Object get(String attributeName)
	{
		if ("id".equalsIgnoreCase(attributeName)) return getId();
		if ("firstName".equalsIgnoreCase(attributeName)) return getFirstName();
		if ("lastName".equalsIgnoreCase(attributeName)) return getLastName();
		if ("age".equalsIgnoreCase(attributeName)) return getAge();
		if ("father".equalsIgnoreCase(attributeName)) return getFather();
		if ("children".equalsIgnoreCase(attributeName)) return getChildren();
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void set(String attributeName, Object value)
	{
		if ("id".equalsIgnoreCase(attributeName)) setId(DataConverter.toString(value));
		else if ("firstName".equalsIgnoreCase(attributeName)) setFirstName(DataConverter.toString(value));
		else if ("lastName".equalsIgnoreCase(attributeName)) setLastName(DataConverter.toString(value));
		else if ("age".equalsIgnoreCase(attributeName)) setAge(DataConverter.toInt(value));
		else if ("father".equalsIgnoreCase(attributeName)) setFather((Person) value);
		else if ("children".equalsIgnoreCase(attributeName)) setChildren((List<Person>) value);
	}

	@Override
	public void set(Entity values, boolean strict)
	{
		for (String attributeName : values.getAttributeNames())
		{
			this.set(attributeName, values.get(attributeName));
		}
	}

	@Override
	public Object getIdValue()
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
		{ "firstName" });
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
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
