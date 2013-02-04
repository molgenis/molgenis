package org.molgenis.framework.server.async;

import java.util.HashMap;
import java.util.UUID;

public class LoadingScreenFactory
{

	public static class LoadingScreen
	{
		public LoadingScreen(String a, String b)
		{
			service = a;
			output = b;
		}

		public String service;
		public String output;
		// public Response
	}

	public HashMap<UUID, LoadingScreen> ids = new HashMap<UUID, LoadingScreen>();

	public boolean isActiveLoadingScreenId(UUID id)
	{
		if (ids.keySet().contains(id))
		{
			return true;
		}
		return false;
	}

	public void addLoadingId(UUID id, String service)
	{
		ids.put(id, new LoadingScreen(service, ""));
	}

	public LoadingScreen getLoadinScreen(UUID id)
	{
		return ids.get(id);
	}

	public LoadingScreen doneLoadingId(UUID id)
	{
		LoadingScreen s = ids.get(id);
		ids.remove(id);
		return s;
	}

}
