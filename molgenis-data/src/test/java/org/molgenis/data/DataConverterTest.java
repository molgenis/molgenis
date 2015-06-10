package org.molgenis.data;

import static org.testng.Assert.assertEquals;

import java.text.ParseException;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.util.MolgenisDateFormat;
import org.testng.annotations.Test;

public class DataConverterTest
{
	@Test
	public void convert()
	{
		assertEquals(DataConverter.convert("test", String.class), "test");
		assertEquals(DataConverter.convert(5L, Number.class).longValue(), 5L);
	}

	@Test
	public void convertDate() throws ParseException
	{
		AttributeMetaData attr = new DefaultAttributeMetaData("attr").setDataType(MolgenisFieldTypes.DATE);
		assertEquals(DataConverter.convert("2015-06-04", attr), MolgenisDateFormat.getDateFormat().parse("2015-06-04"));
	}

	@Test
	public void convertDateTime() throws ParseException
	{
		AttributeMetaData attr = new DefaultAttributeMetaData("attr").setDataType(MolgenisFieldTypes.DATETIME);
		assertEquals(DataConverter.convert("2015-05-22T11:12:13+0500", attr), MolgenisDateFormat.getDateTimeFormat()
				.parse("2015-05-22T11:12:13+0500"));
	}
}
