package org.molgenis.data.meta;

import static org.testng.Assert.assertEquals;

import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DefaultEntityMetaDataTest
{
	@Test
	public void testCopyConstructorPreservesIdAttribute()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("name");
		emd.addAttribute("id").setIdAttribute(true);

		DefaultEntityMetaData emdCopy = new DefaultEntityMetaData(emd);
		Assert.assertEquals(emdCopy.getIdAttribute().getName(), "id");
	}

	// regression test for https://github.com/molgenis/molgenis/issues/3665
	@Test
	public void testExtendsEntityMetaDataMissingIdAttribute()
	{
		DefaultEntityMetaData extendsEntityMeta = new DefaultEntityMetaData("entity");
		extendsEntityMeta.addAttribute("attr");

		DefaultEntityMetaData entityMeta = new DefaultEntityMetaData("entity");
		entityMeta.setExtends(extendsEntityMeta);
		DefaultAttributeMetaData idAttr = entityMeta.addAttribute("id").setIdAttribute(true);
		assertEquals(idAttr, entityMeta.getIdAttribute());
	}
}
