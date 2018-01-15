package org.molgenis.core.ui.admin.usermanager;

import org.molgenis.data.security.auth.Group;
import org.molgenis.data.security.auth.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserViewData that = (UserViewData) o;
		return Objects.equals(id, that.id) && Objects.equals(username, that.username) && Objects.equals(firstName,
				that.firstName) && Objects.equals(middleName, that.middleName) && Objects.equals(lastName,
				that.lastName) && Objects.equals(fullName, that.fullName) && Objects.equals(active, that.active)
				&& Objects.equals(superuser, that.superuser) && Objects.equals(groupList, that.groupList);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id, username, firstName, middleName, lastName, fullName, active, superuser, groupList);
	}
}
