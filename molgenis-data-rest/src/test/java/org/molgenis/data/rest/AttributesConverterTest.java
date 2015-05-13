package org.molgenis.data.rest;

import org.testng.annotations.Test;

public class AttributesConverterTest
{

	@Test
	public void convert()
	{
		System.out.println(new AttributesConverter().convert("attr0"));
		System.out.println(new AttributesConverter().convert("attr0,attr1"));
		System.out.println(new AttributesConverter().convert("attr0[attrnested0]"));
		System.out.println(new AttributesConverter().convert("attr0[attrnested0],attr1"));
		System.out.println(new AttributesConverter().convert("attr0[attrnested0,attrnested1]"));
		System.out.println(new AttributesConverter().convert("attr0[attrnested0],attr1"));
		System.out.println(new AttributesConverter().convert("attr0[attrnested0[attrdeepnested0],attr1"));
	}
}
