package org.molgenis.data.postgresql;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.testng.Assert.assertEquals;

public class PostgreSqlQueryUtilsTest
{
	@Test
	public void getJunctionTableName() throws Exception
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		assertEquals(PostgreSqlQueryUtils.getJunctionTableName(entityMeta, attr), "\"entity_attr\"");
	}

	@DataProvider(name = "getPersistedAttributesProvider")
	public static Iterator<Object[]> getPersistedAttributesProvider()
	{
		List<Object[]> dataList = newArrayList();
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
		assertEquals(PostgreSqlQueryUtils.getPersistedAttributes(entityMeta).collect(toList()), persistedAttrs);
	}

	@Test
	public void getJunctionTableAttributes() throws Exception
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData stringAttr = mock(AttributeMetaData.class);
		when(stringAttr.getDataType()).thenReturn(STRING);
		AttributeMetaData mrefAttr = mock(AttributeMetaData.class);
		when(mrefAttr.getDataType()).thenReturn(MREF);
		AttributeMetaData mrefAttrWithExpression = mock(AttributeMetaData.class);
		when(mrefAttrWithExpression.getDataType()).thenReturn(MREF);
		when(mrefAttrWithExpression.getExpression()).thenReturn("expression");
		AttributeMetaData xrefAttr = mock(AttributeMetaData.class);
		when(xrefAttr.getDataType()).thenReturn(XREF);
		AttributeMetaData xrefAttrInversedBy = mock(AttributeMetaData.class);
		when(xrefAttrInversedBy.getDataType()).thenReturn(XREF);
		when(xrefAttrInversedBy.isInversedBy()).thenReturn(true);
		AttributeMetaData refAttr = mock(AttributeMetaData.class);
		when(refAttr.getDataType()).thenReturn(ONE_TO_MANY);
		when(xrefAttrInversedBy.getInversedBy()).thenReturn(refAttr);
		when(entityMeta.getAtomicAttributes())
				.thenReturn(newArrayList(stringAttr, mrefAttr, mrefAttrWithExpression, xrefAttr, xrefAttrInversedBy));
		List<AttributeMetaData> junctionTableAttrs = newArrayList(mrefAttr);
		assertEquals(PostgreSqlQueryUtils.getJunctionTableAttributes(entityMeta).collect(toList()), junctionTableAttrs);
	}

	@Test
	public void getTableAttributes() throws Exception
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData stringAttr = mock(AttributeMetaData.class);
		when(stringAttr.getDataType()).thenReturn(STRING);
		AttributeMetaData mrefAttr = mock(AttributeMetaData.class);
		when(mrefAttr.getDataType()).thenReturn(MREF);
		AttributeMetaData mrefAttrWithExpression = mock(AttributeMetaData.class);
		when(mrefAttrWithExpression.getDataType()).thenReturn(MREF);
		when(mrefAttrWithExpression.getExpression()).thenReturn("expression");
		AttributeMetaData xrefAttr = mock(AttributeMetaData.class);
		when(xrefAttr.getDataType()).thenReturn(XREF);
		AttributeMetaData xrefAttrInversedBy = mock(AttributeMetaData.class);
		when(xrefAttrInversedBy.getDataType()).thenReturn(XREF);
		when(xrefAttrInversedBy.isInversedBy()).thenReturn(true);
		AttributeMetaData refAttr = mock(AttributeMetaData.class);
		when(refAttr.getDataType()).thenReturn(ONE_TO_MANY);
		when(xrefAttrInversedBy.getInversedBy()).thenReturn(refAttr);
		when(entityMeta.getAtomicAttributes())
				.thenReturn(newArrayList(stringAttr, mrefAttr, mrefAttrWithExpression, xrefAttr, xrefAttrInversedBy));
		List<AttributeMetaData> junctionTableAttrs = newArrayList(stringAttr, xrefAttr, xrefAttrInversedBy);
		assertEquals(PostgreSqlQueryUtils.getTableAttributes(entityMeta).collect(toList()), junctionTableAttrs);
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