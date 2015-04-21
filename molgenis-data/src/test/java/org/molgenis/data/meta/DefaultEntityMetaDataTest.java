package org.molgenis.data.meta;

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

}
