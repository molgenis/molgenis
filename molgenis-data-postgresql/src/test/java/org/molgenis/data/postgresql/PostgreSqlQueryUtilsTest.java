package org.molgenis.data.postgresql;

import com.google.common.collect.Lists;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static freemarker.template.utility.Collections12.singletonList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class PostgreSqlQueryUtilsTest
{
	@DataProvider(name = "getPersistedAttributesProvider")
	public static Iterator<Object[]> getPersistedAttributesProvider()
	{
		List<Object[]> dataList = Lists.newArrayList();
		for (MolgenisFieldTypes.AttributeType attrType : MolgenisFieldTypes.AttributeType.values())
		{
			AttributeMetaData attr = mock(AttributeMetaData.class);
			when(attr.getDataType()).thenReturn(attrType);
			when(attr.toString()).thenReturn("attr_" + attrType.toString());
			dataList.add(new Object[] { attr, singletonList(attr) });

			AttributeMetaData attrWithExpression = mock(AttributeMetaData.class);
			when(attrWithExpression.getDataType()).thenReturn(attrType);
			when(attrWithExpression.getExpression()).thenReturn("expression");
			when(attrWithExpression.toString()).thenReturn("attrWithExpression_" + attrType.toString());
			dataList.add(new Object[] { attrWithExpression, emptyList() });
		}
		return dataList.iterator();
	}

	@Test(dataProvider = "getPersistedAttributesProvider")
	public void getPersistedAttributes(AttributeMetaData attr, List<AttributeMetaData> persistedAttrs)
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		when(entityMeta.getAtomicAttributes()).thenReturn(singletonList(attr));
		assertEquals(PostgreSqlQueryUtils.getPersistedAttributes(entityMeta).collect(Collectors.toList()),
				persistedAttrs);
	}

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