package org.molgenis.security.usermanager;

public class MolgenisUserViewData
{
	private final Integer id;
	private final String username;

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
}
