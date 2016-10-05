package org.molgenis.data.postgresql;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.meta.model.Attribute;
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
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		assertEquals(PostgreSqlQueryUtils.getJunctionTableName(entityMeta, attr), "\"entity_attr\"");
	}

	@Test
	public void getJunctionTableNameMappedBy() throws Exception
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.isMappedBy()).thenReturn(true);
		EntityMetaData refEntityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("refEntity").getMock();
		when(attr.getRefEntity()).thenReturn(refEntityMeta);
		Attribute refAttr = when(mock(Attribute.class).getName()).thenReturn("refAttr").getMock();
		when(attr.getMappedBy()).thenReturn(refAttr);
		assertEquals(PostgreSqlQueryUtils.getJunctionTableName(entityMeta, attr), "\"refEntity_refAttr\"");
	}

	@DataProvider(name = "getPersistedAttributesProvider")
	public static Iterator<Object[]> getPersistedAttributesProvider()
	{
		List<Object[]> dataList = newArrayList();
		for (MolgenisFieldTypes.AttributeType attrType : MolgenisFieldTypes.AttributeType.values())
		{
			Attribute attr = mock(Attribute.class);
			when(attr.getDataType()).thenReturn(attrType);
			when(attr.toString()).thenReturn("attr_" + attrType.toString());
			dataList.add(new Object[] { attr, singletonList(attr) });

			Attribute attrWithExpression = mock(Attribute.class);
			when(attrWithExpression.getDataType()).thenReturn(attrType);
			when(attrWithExpression.getExpression()).thenReturn("expression");
			when(attrWithExpression.toString()).thenReturn("attrWithExpression_" + attrType.toString());
			dataList.add(new Object[] { attrWithExpression, emptyList() });
		}
		return dataList.iterator();
	}

	@Test(dataProvider = "getPersistedAttributesProvider")
	public void getPersistedAttributes(Attribute attr, List<Attribute> persistedAttrs)
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		when(entityMeta.getAtomicAttributes()).thenReturn(singletonList(attr));
		assertEquals(PostgreSqlQueryUtils.getPersistedAttributes(entityMeta).collect(toList()), persistedAttrs);
	}

	@Test
	public void getJunctionTableAttributes() throws Exception
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		Attribute stringAttr = mock(Attribute.class);
		when(stringAttr.getDataType()).thenReturn(STRING);
		Attribute mrefAttr = mock(Attribute.class);
		when(mrefAttr.getDataType()).thenReturn(MREF);
		Attribute mrefAttrWithExpression = mock(Attribute.class);
		when(mrefAttrWithExpression.getDataType()).thenReturn(MREF);
		when(mrefAttrWithExpression.getExpression()).thenReturn("expression");
		Attribute xrefAttr = mock(Attribute.class);
		when(xrefAttr.getDataType()).thenReturn(XREF);
		Attribute xrefAttrInversedBy = mock(Attribute.class);
		when(xrefAttrInversedBy.getDataType()).thenReturn(XREF);
		when(xrefAttrInversedBy.isInversedBy()).thenReturn(true);
		Attribute refAttr = mock(Attribute.class);
		when(refAttr.getDataType()).thenReturn(ONE_TO_MANY);
		when(xrefAttrInversedBy.getInversedBy()).thenReturn(refAttr);
		when(entityMeta.getAtomicAttributes())
				.thenReturn(newArrayList(stringAttr, mrefAttr, mrefAttrWithExpression, xrefAttr, xrefAttrInversedBy));
		List<Attribute> junctionTableAttrs = newArrayList(mrefAttr, xrefAttrInversedBy);
		assertEquals(PostgreSqlQueryUtils.getJunctionTableAttributes(entityMeta).collect(toList()), junctionTableAttrs);
	}

	@Test
	public void getTableAttributes() throws Exception
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		Attribute stringAttr = mock(Attribute.class);
		when(stringAttr.getDataType()).thenReturn(STRING);
		Attribute mrefAttr = mock(Attribute.class);
		when(mrefAttr.getDataType()).thenReturn(MREF);
		Attribute mrefAttrWithExpression = mock(Attribute.class);
		when(mrefAttrWithExpression.getDataType()).thenReturn(MREF);
		when(mrefAttrWithExpression.getExpression()).thenReturn("expression");
		Attribute xrefAttr = mock(Attribute.class);
		when(xrefAttr.getDataType()).thenReturn(XREF);
		Attribute xrefAttrInversedBy = mock(Attribute.class);
		when(xrefAttrInversedBy.getDataType()).thenReturn(XREF);
		when(xrefAttrInversedBy.isInversedBy()).thenReturn(true);
		Attribute refAttr = mock(Attribute.class);
		when(refAttr.getDataType()).thenReturn(ONE_TO_MANY);
		when(xrefAttrInversedBy.getInversedBy()).thenReturn(refAttr);
		when(entityMeta.getAtomicAttributes())
				.thenReturn(newArrayList(stringAttr, mrefAttr, mrefAttrWithExpression, xrefAttr, xrefAttrInversedBy));
		List<Attribute> junctionTableAttrs = newArrayList(stringAttr, xrefAttr);
		assertEquals(PostgreSqlQueryUtils.getTableAttributes(entityMeta).collect(toList()), junctionTableAttrs);
	}

	@Test
	public void getJunctionTableIndexName() throws Exception
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		Attribute idxAttr = when(mock(Attribute.class).getName()).thenReturn("idxAttr").getMock();
		assertEquals(PostgreSqlQueryUtils.getJunctionTableIndexName(entityMeta, attr, idxAttr),
				"\"entity_attr_idxAttr_idx\"");
	}
}