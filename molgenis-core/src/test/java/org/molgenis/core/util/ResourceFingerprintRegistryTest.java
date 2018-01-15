package org.molgenis.core.util;

import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.assertEquals;

public class ResourceFingerprintRegistryTest
{
	@Test
	public void getFingerprint() throws IOException
	{
		assertEquals(new ResourceFingerprintRegistry().getFingerprint(getClass(), "/resource.txt"), "czpzLA");
	}
}
