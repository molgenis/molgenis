package org.molgenis.data.file.processor;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class MissingValueProcessorTest
{
	@Test
	public void processNull()
	{
		assertEquals(new MissingValueProcessor("unknown", false).process(null), "unknown");
	}

	@Test
	public void processNull_processEmpty()
	{
		assertEquals(new MissingValueProcessor("unknown", true).process(null), "unknown");
	}

	@Test
	public void processEmpty()
	{
		assertEquals(new MissingValueProcessor("unknown", true).process(""), "unknown");
	}

	@Test
	public void processEmpty_processEmpty()
	{
		assertEquals(new MissingValueProcessor("unknown", false).process(""), "");
	}

	@Test
	public void process()
	{
		assertEquals(new MissingValueProcessor("unknown", true).process("value"), "value");
	}

	@Test
	public void process_processEmpty()
	{
		assertEquals(new MissingValueProcessor("unknown", true).process("value"), "value");
	}

	@Test
	public void equals()
	{
		assertEquals(new MissingValueProcessor("unknown", true), new MissingValueProcessor("unknown", true));
	}
}
