package org.molgenis.data.postgresql;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
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
import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.*;

public class PostgreSqlQueryUtilsTest
{
	@DataProvider(name = "getPersistedAttributesProvider")
	public static Iterator<Object[]> getPersistedAttributesProvider()
	{
		List<Object[]> dataList = newArrayList();
		for (AttributeType attrType : AttributeType.values())
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
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(entityType.getAtomicAttributes()).thenReturn(singletonList(attr));
		assertEquals(PostgreSqlQueryUtils.getPersistedAttributes(entityType).collect(toList()), persistedAttrs);
	}

	@Test
	public void getJunctionTableAttributes() throws Exception
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
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
		when(entityType.getAtomicAttributes()).thenReturn(
				newArrayList(stringAttr, mrefAttr, mrefAttrWithExpression, xrefAttr, xrefAttrInversedBy));
		List<Attribute> junctionTableAttrs = newArrayList(mrefAttr);
		assertEquals(PostgreSqlQueryUtils.getJunctionTableAttributes(entityType).collect(toList()), junctionTableAttrs);
	}

	@Test
	public void getTableAttributes() throws Exception
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
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
		when(entityType.getAtomicAttributes()).thenReturn(
				newArrayList(stringAttr, mrefAttr, mrefAttrWithExpression, xrefAttr, xrefAttrInversedBy));
		List<Attribute> junctionTableAttrs = newArrayList(stringAttr, xrefAttr, xrefAttrInversedBy);
		assertEquals(PostgreSqlQueryUtils.getTableAttributes(entityType).collect(toList()), junctionTableAttrs);
	}

	@Test
	public void isTableAttributeStringAttr() throws Exception
	{
		Attribute attr = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
		assertTrue(PostgreSqlQueryUtils.isTableAttribute(attr));
	}

	@Test
	public void isTableAttributeMrefAttr() throws Exception
	{
		Attribute attr = when(mock(Attribute.class).getDataType()).thenReturn(MREF).getMock();
		assertFalse(PostgreSqlQueryUtils.isTableAttribute(attr));
	}
}