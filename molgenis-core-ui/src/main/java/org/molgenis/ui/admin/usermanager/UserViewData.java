package org.molgenis.ui.admin.usermanager;

import org.molgenis.auth.Group;
import org.molgenis.auth.User;

import java.util.ArrayList;
import java.util.List;

public class UserViewData
{
	private final String id;
	private final String username;
	private String firstName;
	private String middleName;
	private String lastName;
	private String fullName; // first, middle, last name
	private Boolean active;
	private Boolean superuser;
	private final List<String> groupList = new ArrayList<>();

	UserViewData(User mu, List<Group> groups)
	{
		this(mu.getId(), mu.getUsername());
		firstName = (null == mu.getFirstName() ? "" : mu.getFirstName());
		middleName = (null == mu.getMiddleNames() ? "" : mu.getMiddleNames());
		lastName = (null == mu.getLastName() ? "" : mu.getLastName());

		fullName = firstName + ' ' + middleName + ' ' + lastName;

		this.active = mu.isActive();
		this.superuser = mu.isSuperuser();

		for (Group mg : groups)
		{
			this.groupList.add(mg.getId());
		}
	}

	UserViewData(String id, final String username)
	{
		if (null == id)
		{
			throw new IllegalArgumentException("id is null");
		}
		if (null == username)
		{
			throw new IllegalArgumentException("username is null");
		}
		this.id = id;
		this.username = username;
	}

	public String getId()
	{
		return id;
	}

	public String getUsername()
	{
		return username;
	}

	public String getFullName()
	{
		return fullName;
	}

	public Boolean isActive()
	{
		return this.active;
	}

	public Boolean isSuperuser()
	{
		return this.superuser;
	}

	public Boolean isGroupMember(String id)
	{
		return groupList.contains(id);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((active == null) ? 0 : active.hashCode());
		result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
		result = prime * result + ((fullName == null) ? 0 : fullName.hashCode());
		result = prime * result + ((groupList == null) ? 0 : groupList.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
		result = prime * result + ((middleName == null) ? 0 : middleName.hashCode());
		result = prime * result + ((superuser == null) ? 0 : superuser.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		UserViewData other = (UserViewData) obj;
		if (active == null)
		{
			if (other.active != null) return false;
		}
		else if (!active.equals(other.active)) return false;
		if (firstName == null)
		{
			if (other.firstName != null) return false;
		}
		else if (!firstName.equals(other.firstName)) return false;
		if (fullName == null)
		{
			if (other.fullName != null) return false;
		}
		else if (!fullName.equals(other.fullName)) return false;
		if (groupList == null)
		{
			if (other.groupList != null) return false;
		}
		else if (!groupList.equals(other.groupList)) return false;
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
		if (middleName == null)
		{
			if (other.middleName != null) return false;
		}
		else if (!middleName.equals(other.middleName)) return false;
		if (superuser == null)
		{
			if (other.superuser != null) return false;
		}
		else if (!superuser.equals(other.superuser)) return false;
		if (username == null)
		{
			if (other.username != null) return false;
		}
		else if (!username.equals(other.username)) return false;
		return true;
	}
}
