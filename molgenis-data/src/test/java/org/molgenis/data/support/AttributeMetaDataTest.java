package org.molgenis.data.support;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Range;
import org.molgenis.data.meta.AttributeMetaData;
import org.testng.annotations.Test;

public class AttributeMetaDataTest
{
	@Test
	public void AttributeMetaDataAttributeMetaData()
	{
		AttributeMetaData attr = new AttributeMetaData("attribute");
		attr.setAggregatable(true);
		attr.setAuto(true);
		attr.setDataType(MolgenisFieldTypes.INT);
		attr.setDefaultValue(null);
		attr.setDescription("description");
		attr.setLabel("label");
		attr.setNillable(true);
		attr.setRange(new Range(-1L, 1L));
		attr.setReadOnly(true);
		attr.setUnique(true);
		attr.setVisible(true);
		assertEquals(AttributeMetaData.newInstance(attr), attr);
	}

	@Test
	public void AttributeMetaDataUnknownLabelUseName()
	{
		assertEquals(new AttributeMetaData("attribute").getLabel(), "attribute");
	}

	@Test
	public void getAttributePart()
	{
		AttributeMetaData attr = new AttributeMetaData("attribute");
		String attrName = "Attr";
		assertNull(attr.getAttributePart(attrName));
		AttributeMetaData attrPart = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		attr.addAttributePart(attrPart);
		assertEquals(attrPart, attr.getAttributePart(attrName));
		assertEquals(attrPart, attr.getAttributePart(attrName.toLowerCase())); // case insensitive
	}
}
