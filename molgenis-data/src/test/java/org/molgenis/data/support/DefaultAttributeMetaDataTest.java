package org.molgenis.data.support;

import static org.testng.Assert.assertEquals;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Range;
import org.testng.annotations.Test;

public class DefaultAttributeMetaDataTest
{
	@Test
	public void DefaultAttributeMetaDataAttributeMetaData()
	{
		DefaultAttributeMetaData attributeMetaData = new DefaultAttributeMetaData("attribute");
		attributeMetaData.setAggregateable(true);
		attributeMetaData.setAuto(true);
		attributeMetaData.setDataType(MolgenisFieldTypes.INT);
		attributeMetaData.setDefaultValue(null);
		attributeMetaData.setDescription("description");
		attributeMetaData.setIdAttribute(true);
		attributeMetaData.setLabel("label");
		attributeMetaData.setLabelAttribute(true);
		attributeMetaData.setLookupAttribute(true);
		attributeMetaData.setNillable(true);
		attributeMetaData.setRange(new Range(-1l, 1l));
		attributeMetaData.setReadOnly(true);
		attributeMetaData.setUnique(true);
		attributeMetaData.setVisible(true);
		assertEquals(new DefaultAttributeMetaData(attributeMetaData), attributeMetaData);
	}
}
