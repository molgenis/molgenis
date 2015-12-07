package org.molgenis.data.support;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.molgenis.data.AttributeMetaData;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

public class EntityMetaDataUtilsTest
{
	@Test
	public void getAttributeNames()
	{
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
		AttributeMetaData attr1 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1").getMock();
		assertEquals(Lists.newArrayList(EntityMetaDataUtils.getAttributeNames(Arrays.asList(attr0, attr1))),
				Arrays.asList("attr0", "attr1"));
	}
}
