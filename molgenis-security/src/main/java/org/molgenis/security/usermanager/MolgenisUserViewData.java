package org.molgenis.security.usermanager;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.auth.MolgenisGroup;
import org.molgenis.auth.MolgenisUser;

public class MolgenisUserViewData
{
	private final Integer id;
	private final String username;
	private String firstName;
	private String middleName;
	private String lastName;
	private String fullName; // first, middle, last name
	private Boolean active;
	private Boolean superuser;
	private final List<Integer> groupList = new ArrayList<Integer>();

	MolgenisUserViewData(MolgenisUser mu, List<MolgenisGroup> molgenisGroups)
	{
		this(mu.getId(), mu.getUsername());
		firstName = (null == mu.getFirstName() ? "" : mu.getFirstName());
		middleName = (null == mu.getMiddleNames() ? "" : mu.getMiddleNames());
		lastName = (null == mu.getLastName() ? "" : mu.getLastName());

		fullName = firstName + ' ' + middleName + ' ' + lastName;

		this.active = mu.getActive();
		this.superuser = mu.getSuperuser();

		for (MolgenisGroup mg : molgenisGroups)
		{
			this.groupList.add(mg.getId());
		}
	}

	MolgenisUserViewData(final Integer id, final String username)
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

	public Integer getId()
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

	public Boolean isGroupMember(Integer id)
	{
		return groupList.contains(id);
	}
}
