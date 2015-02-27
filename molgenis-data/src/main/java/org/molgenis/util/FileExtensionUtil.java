package org.molgenis.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FileExtensionUtil
{
	/**
	 * Checks if an entity contains data or not
	 * 
	 * @param entity
	 */
	public static String findTheClosestFileExtansionFromSet(String fileName, Set<String> fileExtensions)
	{
		String name = fileName.toLowerCase();
		List<String> possibleExtensions = new ArrayList<String>();
		for (String extention : fileExtensions)
		{
			if (name.endsWith('.' + extention))
			{
				possibleExtensions.add(extention);
			}
		}

		String longestExtention = null;
		for (String possibleExtension : possibleExtensions)
		{
			if (null == longestExtention){
				longestExtention = possibleExtension;
				continue;
			}
			else
			{
				if (longestExtention.length() < possibleExtension.length()) longestExtention = possibleExtension;
			}
		}

		return longestExtention;
	}
}
