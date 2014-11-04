package org.molgenis.omx.utils;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

public class ValueCellTest
{

	@Test
	public void ValueCell()
	{
		ValueCell<String> valueCell = new ValueCell<String>("value");
		assertNull(valueCell.getId());
		assertNull(valueCell.getKey());
		assertEquals(valueCell.getValue(), "value");
	}

	@Test
	public void ValueCellStringString()
	{
		ValueCell<String> valueCell = new ValueCell<String>("key", "value");
		assertNull(valueCell.getId());
		assertEquals(valueCell.getKey(), "key");
		assertEquals(valueCell.getValue(), "value");
	}

	@Test
	public void ValueCellIntegerStringString()
	{
		ValueCell<String> valueCell = new ValueCell<String>(1, "key", "value");
		assertEquals(valueCell.getId(), Integer.valueOf(1));
		assertEquals(valueCell.getKey(), "key");
		assertEquals(valueCell.getValue(), "value");
	}

	@Test
	public void ValueCelltoString()
	{
		ValueCell<String> valueCell = new ValueCell<String>(1, "key", "value");
		assertEquals(valueCell.toString(), "value");
	}
}
