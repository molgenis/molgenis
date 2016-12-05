package org.molgenis.data.support;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Iterator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.meta.AttributeType.CATEGORICAL;
import static org.molgenis.data.meta.AttributeType.CATEGORICAL_MREF;
import static org.molgenis.data.meta.AttributeType.COMPOUND;
import static org.molgenis.data.meta.AttributeType.DATE;
import static org.molgenis.data.meta.AttributeType.DATE_TIME;
import static org.molgenis.data.meta.AttributeType.DECIMAL;
import static org.molgenis.data.meta.AttributeType.EMAIL;
import static org.molgenis.data.meta.AttributeType.ENUM;
import static org.molgenis.data.meta.AttributeType.FILE;
import static org.molgenis.data.meta.AttributeType.HTML;
import static org.molgenis.data.meta.AttributeType.HYPERLINK;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.LONG;
import static org.molgenis.data.meta.AttributeType.MREF;
import static org.molgenis.data.meta.AttributeType.SCRIPT;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.testng.Assert.assertEquals;

public class AttributeUtilsTest
{
	@DataProvider(name = "isIdAttributeTypeAllowedProvider")
	public static Iterator<Object[]> isIdAttributeTypeAllowedProvider()
	{
		return Arrays.asList(new Object[] { BOOL, false }, new Object[] { CATEGORICAL, false },
				new Object[] { CATEGORICAL_MREF, false }, new Object[] { COMPOUND, false },
				new Object[] { DATE, false }, new Object[] { DATE_TIME, false }, new Object[] { DECIMAL, false },
				new Object[] { EMAIL, true }, new Object[] { ENUM, false }, new Object[] { FILE, false },
				new Object[] { HTML, false }, new Object[] { HYPERLINK, true }, new Object[] { INT, true },
				new Object[] { LONG, true }, new Object[] { MREF, false }, new Object[] { SCRIPT, false },
				new Object[] { STRING, true }, new Object[] { TEXT, false }, new Object[] { XREF, false }).iterator();
	}

	@Test(dataProvider = "isIdAttributeTypeAllowedProvider")
	public void isIdAttributeTypeAllowed(AttributeType attrType, boolean validIdAttrType) throws Exception
	{
		Attribute attr = when(mock(Attribute.class).getDataType()).thenReturn(attrType).getMock();
		assertEquals(AttributeUtils.isIdAttributeTypeAllowed(attr), validIdAttrType);
	}
}