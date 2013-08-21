package org.molgenis.elasticsearch.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

public class MapperTypeSanitizerTest
{

	@Test
	public void sanitizeMapperType()
	{
		assertEquals(MapperTypeSanitizer.sanitizeMapperType("Hello dude"), "Hello dude");
		assertEquals(MapperTypeSanitizer.sanitizeMapperType("_Hello_dude"), "Hello_dude");
		assertEquals(MapperTypeSanitizer.sanitizeMapperType(",Hello,dude,"), "Hellodude");
		assertEquals(MapperTypeSanitizer.sanitizeMapperType("#Hello#dude#"), "Hellodude");
		assertEquals(MapperTypeSanitizer.sanitizeMapperType(".Hello.dude."), "Hellodude");
		assertEquals(MapperTypeSanitizer.sanitizeMapperType("_xx..xx#xx.##,xx"), "xxxxxxxx");
		assertNull(MapperTypeSanitizer.sanitizeMapperType(null));
	}
}
