package org.molgenis.gavin.job.input.model;

import org.testng.Assert;
import org.testng.annotations.Test;

public class VcfVariantTest
{
	@Test
	public void testToString()
	{
		Assert.assertEquals(VcfVariant.create("20", 14370, "rs6054257", "G", "A").toString(),
				"20\t14370\trs6054257\tG\tA\t.\t.\t.");
	}

	@Test
	public void testToStringDotId()
	{
		Assert.assertEquals(VcfVariant.create("20", 14370, ".", "G", "A").toString(), "20\t14370\t.\tG\tA\t.\t.\t.");
	}
}
