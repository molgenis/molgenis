package org.molgenis.data.rsql;

import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.testng.Assert.assertEquals;

public class RSQLValueParserTest
{
	private RSQLValueParser rSqlValueParser;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		rSqlValueParser = new RSQLValueParser();
	}

	@DataProvider(name = "parseProvider")
	public static Iterator<Object[]> parseProvider()
	{
		return newArrayList(new Object[] { ONE_TO_MANY, INT, 1 }, new Object[] { ONE_TO_MANY, STRING, "1" },
				new Object[] { XREF, INT, 1 }, new Object[] { XREF, STRING, "1" }).iterator();
	}

	@Test(dataProvider = "parseProvider")
	public void parse(AttributeType attrType, AttributeType refIdAttrType, Object parsedValue)
	{
		AttributeMetaData oneToManyAttr = mock(AttributeMetaData.class);
		when(oneToManyAttr.getDataType()).thenReturn(attrType);
		EntityMetaData refEntity = mock(EntityMetaData.class);
		AttributeMetaData refIdAttr = mock(AttributeMetaData.class);
		when(refIdAttr.getDataType()).thenReturn(refIdAttrType);
		when(refEntity.getIdAttribute()).thenReturn(refIdAttr);
		when(oneToManyAttr.getRefEntity()).thenReturn(refEntity);
		assertEquals(parsedValue, rSqlValueParser.parse("1", oneToManyAttr));
	}
}