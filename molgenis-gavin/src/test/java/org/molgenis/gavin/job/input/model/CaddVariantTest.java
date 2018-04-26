package org.molgenis.gavin.job.input.model;

import org.testng.Assert;
import org.testng.annotations.Test;

public class CaddVariantTest
{
	@Test
	public void testToString()
	{
		Assert.assertEquals(CaddVariant.create("20", 14370, "G", "A", 4.901031, 24.9).toString(),
				"20\t14370\t.\tG\tA\t.\t.\tCADD=4.901031;CADD_SCALED=24.9");
	}
}
