package org.molgenis.util;

import org.molgenis.data.support.GenericImporterExtensions;
import org.testng.annotations.Test;

import java.util.Set;

import static org.testng.Assert.assertEquals;

public class FileExtensionUtilsTest
{

	@Test
	public void findExtensionFromSetForGenericImporter()
	{
		Set<String> extensions = GenericImporterExtensions.getAll();
		for (String extention : extensions)
		{
			assertEquals(FileExtensionUtils
							.findExtensionFromPossibilities("molgenis.test." + extention, GenericImporterExtensions.getAll()),
					GenericImporterExtensions.valueOf(extention.toUpperCase().replace('.', '_')).toString());
		}
	}
}
