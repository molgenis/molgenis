package org.molgenis.data.file.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.apache.commons.io.FilenameUtils.getBaseName;

public class FileExtensionUtils
{
	public static String findExtensionFromPossibilities(String fileName, Set<String> fileExtensions)
	{
		String name = fileName.toLowerCase();
		List<String> possibleExtensions = new ArrayList<>();
		for (String extension : fileExtensions)
		{
			if (name.endsWith('.' + extension))
			{
				possibleExtensions.add(extension);
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

	public static String getFileNameWithoutExtension(String filename)
	{
		return getBaseName(filename);
	}
}
