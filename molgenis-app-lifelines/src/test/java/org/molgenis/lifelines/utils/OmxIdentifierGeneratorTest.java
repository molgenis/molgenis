package org.molgenis.lifelines.utils;

import static org.testng.Assert.assertEquals;

import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.Protocol;
import org.testng.annotations.Test;

public class OmxIdentifierGeneratorTest
{

	@Test
	public void fromClassextendsEntityString()
	{
		assertEquals(OmxIdentifierGenerator.from(Category.class, "123"), "Category_123");
	}

	@Test
	public void fromClassextendsEntityStringString()
	{
		assertEquals(OmxIdentifierGenerator.from(Protocol.class, "123", "456"), "Protocol_123.456");
	}
}
