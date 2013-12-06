package org.molgenis.omx.biobankconnect.wizard;

import java.util.HashMap;
import java.util.Map;

public class CurrentUserStatus
{
	private final Map<String, Boolean> userStatus;

	public CurrentUserStatus()
	{
		this.userStatus = new HashMap<String, Boolean>();
	}

	public Boolean getUserstatus(String userName)
	{
		return userStatus.containsKey(userName) && userStatus.get(userName);
	}

	public void setUserStatus(String userName, Boolean status)
	{
		if (status) userStatus.put(userName, status);
		else if (userStatus.containsKey(userName)) userStatus.remove(userName);
	}
}
