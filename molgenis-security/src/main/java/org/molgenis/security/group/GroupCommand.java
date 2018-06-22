package org.molgenis.security.group;

import java.util.Objects;

public class GroupCommand
{
	/**
	 * Unique url-save name for group
	 */
	private String name;
	/**
	 * Used as human readable name describing the group
	 */
	private String label;

	public GroupCommand(String name, String label)
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
		GroupCommand that = (GroupCommand) o;
		return Objects.equals(name, that.name) && Objects.equals(label, that.label);
	}

	@Override
	public int hashCode()
	{

		return Objects.hash(name, label);
	}
}
