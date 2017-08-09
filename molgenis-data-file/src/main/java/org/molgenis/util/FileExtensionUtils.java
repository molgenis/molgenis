package org.molgenis.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FileExtensionUtils
{
	public static String findExtensionFromPossibilities(String fileName, Set<String> fileExtensions)
	{
		String name = fileName.toLowerCase();
		List<String> possibleExtensions = new ArrayList<>();
		for (String extention : fileExtensions)
		{
			if (name.endsWith('.' + extention))
			{
				possibleExtensions.add(extention);
			}
		}

		String longestExtension = null;
		for (String possibleExtension : possibleExtensions)
		{
			if (null == longestExtension)
			{
				longestExtension = possibleExtension;
				continue;
			}
			else
			{
				if (longestExtension.length() < possibleExtension.length()) longestExtension = possibleExtension;
			}
		}

		return longestExtension;
	}
}
