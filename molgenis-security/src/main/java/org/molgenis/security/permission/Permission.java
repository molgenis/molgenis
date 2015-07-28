package org.molgenis.security.permission;

public class Permission
{
	private String type;
	private String group;

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getGroup()
	{
		return group;
	}

	public void setGroup(String group)
	{
		this.group = group;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Permission other = (Permission) obj;
		if (group == null)
		{
			if (other.group != null) return false;
		}
		else if (!group.equals(other.group)) return false;
		if (type == null)
		{
			if (other.type != null) return false;
		}
		else if (!type.equals(other.type)) return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "Permission [type=" + type + ", group=" + group + "]";
	}
}