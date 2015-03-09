package org.molgenis.util;

import static org.testng.Assert.assertEquals;

import java.util.Set;

import org.molgenis.data.support.GenericImporterExtensions;
import org.testng.annotations.Test;

public class FileExtensionUtilsTest
{

	@Test
	public void findExtensionFromSetForGenericImporter()
	{
		Set<String> extensions = GenericImporterExtensions.getAll();
		for(String extention :extensions){
			assertEquals(FileExtensionUtils.findExtensionFromPossibilities("molgenis.test." + extention,
					GenericImporterExtensions.getAll()),
					GenericImporterExtensions.valueOf(extention.toUpperCase().replace('.', '_'))
					.toString());
		}
	}
}
