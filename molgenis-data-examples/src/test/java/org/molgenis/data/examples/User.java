package org.molgenis.data.examples;

import org.molgenis.data.support.MapEntity;

/**
 * Created by mswertz on 09/05/14.
 */
public class User extends MapEntity
{
	private static final long serialVersionUID = 1L;

	public User(String username, boolean active)
	{
		this();
		setUsername(username);
		setActive(active);
	}

	public User()
	{
		super(UserMetaData.USERNAME);
	}

	public String getUsername()
	{
		return getString(UserMetaData.USERNAME);
	}

	public void setUsername(String username)
	{
		set(UserMetaData.USERNAME, username);
	}

	public Boolean isActive()
	{
		return getBoolean(UserMetaData.ACTIVE);
	}

	public void setActive(Boolean active)
	{
		set(UserMetaData.ACTIVE, active);
	}

	@Override
	public String toString()
	{
		return "User [username=" + getUsername() + ", active=" + isActive() + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getUsername() == null) ? 0 : getUsername().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		User other = (User) obj;
		if (getUsername() == null)
		{
			if (other.getUsername() != null) return false;
		}
		else if (!getUsername().equals(other.getUsername())) return false;
		return true;
	}

}
