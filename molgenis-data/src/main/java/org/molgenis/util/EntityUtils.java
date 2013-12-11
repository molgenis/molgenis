package org.molgenis.util;

import org.molgenis.data.Entity;

public class EntityUtils
{
	/**
	 * Checks if an entity contains data or not
	 * 
	 * @param entity
	 */
	public static boolean isEmpty(Entity entity)
	{
		for (String attr : entity.getAttributeNames())
		{
			if (entity.get(attr) != null)
			{
				return false;
			}
		}

		return true;
	}
}
