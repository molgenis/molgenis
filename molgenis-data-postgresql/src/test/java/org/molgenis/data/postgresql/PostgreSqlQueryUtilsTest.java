package org.molgenis.data.postgresql;

import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class PostgreSqlQueryUtilsTest
{
	@Test
	public void getJunctionTableIndexName() throws Exception
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		AttributeMetaData idxAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("idxAttr").getMock();
		assertEquals(PostgreSqlQueryUtils.getJunctionTableIndexName(entityMeta, attr, idxAttr),
				"\"entity_attr_idxAttr_idx\"");
	}
}