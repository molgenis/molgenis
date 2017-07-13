package org.molgenis.security.permission;

import java.util.*;

public class Permissions
{
	private String userId;
	private String groupId;
	private Map<String, String> entityIds;
	private Map<String, List<Permission>> userPermissionMap;
	private Map<String, List<Permission>> groupPermissionMap;
	private Map<String, List<Permission>> hierarchyPermissionMap;

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public String getGroupId()
	{
		return groupId;
	}

	public void setGroupId(String groupId)
	{
		this.groupId = groupId;
	}

	public Map<String, String> getEntityIds()
	{
		return entityIds;
	}

	public void setEntityIds(Map<String, String> entityIds)
	{
		this.entityIds = entityIds;
	}

	public Map<String, List<Permission>> getUserPermissions()
	{
		return userPermissionMap != null ? userPermissionMap : Collections.<String, List<Permission>>emptyMap();
	}

	public void addUserPermission(String pluginId, Permission pluginPermission)
	{
		if (userPermissionMap == null) userPermissionMap = new HashMap<>();
		List<Permission> pluginPermissions = userPermissionMap.get(pluginId);
		if (pluginPermissions == null)
		{
			pluginPermissions = new ArrayList<>();
			userPermissionMap.put(pluginId, pluginPermissions);
		}
		pluginPermissions.add(pluginPermission);
	}

	public Map<String, List<Permission>> getGroupPermissions()
	{
		return groupPermissionMap != null ? groupPermissionMap : Collections.<String, List<Permission>>emptyMap();
	}

	public void addGroupPermission(String pluginId, Permission pluginPermission)
	{
		if (groupPermissionMap == null) groupPermissionMap = new HashMap<>();
		List<Permission> pluginPermissions = groupPermissionMap.get(pluginId);
		if (pluginPermissions == null)
		{
			pluginPermissions = new ArrayList<>();
			groupPermissionMap.put(pluginId, pluginPermissions);
		}
		pluginPermissions.add(pluginPermission);
	}

	public Map<String, List<Permission>> getHierarchyPermissionMap()
	{
		return hierarchyPermissionMap;
	}

	public void addHierarchyPermission(String pluginId, Permission pluginPermission)
	{
		if (hierarchyPermissionMap == null) hierarchyPermissionMap = new HashMap<>();
		List<Permission> pluginPermissions = hierarchyPermissionMap.get(pluginId);
		if (pluginPermissions == null)
		{
			pluginPermissions = new ArrayList<>();
			hierarchyPermissionMap.put(pluginId, pluginPermissions);
		}
		pluginPermissions.add(pluginPermission);
	}

	public void sort()
	{
		if (userPermissionMap != null)
		{
			for (List<Permission> pluginPermissions : userPermissionMap.values())
			{
				if (pluginPermissions.size() > 1)
				{
					Collections.sort(pluginPermissions, new Comparator<Permission>()
					{
						@Override
						public int compare(Permission o1, Permission o2)
						{
							String group1 = o1.getGroup();
							String group2 = o2.getGroup();
							if (group1 == null) return group2 == null ? 0 : -1;
							else return group2 == null ? 1 : group1.compareTo(group2);
						}
					});
				}
			}
		}
		if (groupPermissionMap != null)
		{
			for (List<Permission> pluginPermissions : groupPermissionMap.values())
			{
				if (pluginPermissions.size() > 1)
				{
					Collections.sort(pluginPermissions, new Comparator<Permission>()
					{
						@Override
						public int compare(Permission o1, Permission o2)
						{
							String group1 = o1.getGroup();
							String group2 = o2.getGroup();
							if (group1 == null) return group2 == null ? 0 : -1;
							else return group2 == null ? 1 : group1.compareTo(group2);
						}
					});
				}
			}
		}
	}
}