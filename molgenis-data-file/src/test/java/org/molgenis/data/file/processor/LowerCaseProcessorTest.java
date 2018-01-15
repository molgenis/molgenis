package org.molgenis.data.file.processor;

import org.testng.Assert;
import org.testng.annotations.Test;

public class LowerCaseProcessorTest
{
	@Test
	public void process()
	{
		Assert.assertEquals(new LowerCaseProcessor().process("A"), "a");
	}

	@Test
	public void processNull()
	{
		Assert.assertNull(new LowerCaseProcessor().process(null));
	}
}
