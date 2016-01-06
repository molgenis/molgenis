package org.molgenis.data.i18n;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.testng.annotations.Test;

public class I18nStringMetaDataTest
{
	@Test
	public void addLanguage()
	{
		boolean add = I18nStringMetaData.INSTANCE.addLanguage("nl");
		assertTrue(add);

		add = I18nStringMetaData.INSTANCE.addLanguage("nl");
		assertFalse(add);

		AttributeMetaData attr = I18nStringMetaData.INSTANCE.getAttribute("nl");
		assertNotNull(attr);
		assertEquals(attr.getName(), "nl");
		assertEquals(attr.getDataType(), MolgenisFieldTypes.TEXT);

	}

	@Test
	public void removeLanguage()
	{
		I18nStringMetaData.INSTANCE.addLanguage("nl");
		I18nStringMetaData.INSTANCE.removeLanguage("nl");
		assertNull(I18nStringMetaData.INSTANCE.getAttribute("nl"));
	}
}
