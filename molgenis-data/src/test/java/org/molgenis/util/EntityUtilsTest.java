package org.molgenis.util;

import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.DynamicEntity;
import org.testng.annotations.Test;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Collections.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.AttributeType.STRING;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class EntityUtilsTest
{
	@Test
	public void isEmptyNoAttributes()
	{
		EntityType entityType = when(mock(EntityType.class).getName()).thenReturn("entity").getMock();
		when(entityType.getAtomicAttributes()).thenReturn(emptyList());
		assertTrue(EntityUtils.isEmpty(new DynamicEntity(entityType)));
	}

	@Test
	public void isEmptyAttributeValuesNull()
	{
		EntityType entityType = when(mock(EntityType.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(attr.getDataType()).thenReturn(STRING);
		when(entityType.getAtomicAttributes()).thenReturn(singletonList(attr));
		when(entityType.getAttribute("attr")).thenReturn(attr);
		assertTrue(EntityUtils.isEmpty(new DynamicEntity(entityType, singletonMap("attr", null))));
	}

	@Test
	public void isEmptyAttributeValuesNotNull()
	{
		EntityType entityType = when(mock(EntityType.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(attr.getDataType()).thenReturn(STRING);
		when(entityType.getAtomicAttributes()).thenReturn(singletonList(attr));
		when(entityType.getAttribute("attr")).thenReturn(attr);
		assertFalse(EntityUtils.isEmpty(new DynamicEntity(entityType, of("attr", "val"))));
	}

	@Test
	public void doesExtend()
	{
		EntityType grandfather = when(mock(EntityType.class).getName()).thenReturn("grandfather").getMock();
		assertFalse(EntityUtils.doesExtend(grandfather, "grandfather"));

		EntityType father = when(mock(EntityType.class).getName()).thenReturn("father").getMock();
		when(father.getExtends()).thenReturn(grandfather);
		assertTrue(EntityUtils.doesExtend(father, "grandfather"));

		EntityType child = when(mock(EntityType.class).getName()).thenReturn("child").getMock();
		when(child.getExtends()).thenReturn(father);
		assertTrue(EntityUtils.doesExtend(child, "grandfather"));
	}
}
