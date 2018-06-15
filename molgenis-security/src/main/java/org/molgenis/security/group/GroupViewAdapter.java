package org.molgenis.security.group;

import org.molgenis.data.security.auth.Group;

import java.util.Objects;

public class GroupViewAdapter
{
	/**
	 * Used as unique url-identifier for name in group
 	 */
	private String name;
	/**
	 * Used as human readable name describing the group
	 */
	private String label;

	public GroupViewAdapter(String name, String label)
	{
		this.name = name;
		this.label = label;
	}

	public String getName()
	{
		return name;
	}

	public String getLabel()
	{
		return label;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GroupViewAdapter that = (GroupViewAdapter) o;
		return Objects.equals(name, that.name) && Objects.equals(label, that.label);
	}

	@Override
	public int hashCode()
	{

		return Objects.hash(name, label);
	}

	static GroupViewAdapter fromEntity(Group groupEntity) {
		return new GroupViewAdapter(groupEntity.getName(), groupEntity.getLabel());
	}
}
