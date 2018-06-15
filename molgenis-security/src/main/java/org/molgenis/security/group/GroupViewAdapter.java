package org.molgenis.security.group;

import org.molgenis.data.security.auth.Group;

import java.util.Objects;

public class GroupViewAdapter
{
	private String name;
	private String summary;

	public GroupViewAdapter(String name, String summary)
	{
		this.name = name;
		this.summary = summary;
	}

	public String getName()
	{
		return name;
	}

	public String getSummary()
	{
		return summary;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GroupViewAdapter that = (GroupViewAdapter) o;
		return Objects.equals(name, that.name) && Objects.equals(summary, that.summary);
	}

	@Override
	public int hashCode()
	{

		return Objects.hash(name, summary);
	}

	static GroupViewAdapter fromEntity(Group groupEntity) {
		return new GroupViewAdapter(groupEntity.getName(), groupEntity.getDescription());
	}
}
