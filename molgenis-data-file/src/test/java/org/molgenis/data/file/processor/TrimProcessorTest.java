package org.molgenis.data.file.processor;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TrimProcessorTest
{
	@Test
	public void process()
	{
		Assert.assertEquals(new TrimProcessor().process(" val "), "val");
	}

	@Test
	public void processNull()
	{
		Assert.assertNull(new TrimProcessor().process(null));
	}
}
