package org.molgenis.data.support;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.molgenis.MolgenisFieldTypes;
import org.testng.annotations.Test;

public class AbstractEntityTest
{
	// Regression test for #3301
	@Test
	public void getLabelValue()
	{
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData("entity");
		entityMetaData.addAttribute("label").setLabelAttribute(true).setDataType(MolgenisFieldTypes.SCRIPT);
		AbstractEntity entity = mock(AbstractEntity.class);
		when(entity.getLabelValue()).thenCallRealMethod();
		when(entity.getEntityMetaData()).thenReturn(entityMetaData);
		when(entity.get("label")).thenReturn("label value");
		assertEquals(entity.getLabelValue(), "label value");
	}
}
