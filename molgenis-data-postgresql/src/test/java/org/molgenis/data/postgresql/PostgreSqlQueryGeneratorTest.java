package org.molgenis.data.postgresql;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.STRING;

import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.support.QueryImpl;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

public class PostgreSqlQueryGeneratorTest
{
	@Test
	public void testGetSqlSelectMref() throws Exception
	{
		Package package_ = when(mock(Package.class).getName()).thenReturn("org_molgenis").getMock();

		AttributeMetaData ref1IdAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("ref1Id").getMock();
		EntityMetaData ref1Meta = when(mock(EntityMetaData.class).getName()).thenReturn("Ref1").getMock();
		when(ref1Meta.getIdAttribute()).thenReturn(ref1IdAttr);

		AttributeMetaData ref2IdAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("ref2Id").getMock();
		EntityMetaData ref2Meta = when(mock(EntityMetaData.class).getName()).thenReturn("Ref2").getMock();
		when(ref2Meta.getIdAttribute()).thenReturn(ref2IdAttr);

		AttributeMetaData masterIdAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("masterId").getMock();
		when(masterIdAttr.getDataType()).thenReturn(STRING);
		AttributeMetaData mref1Attr = when(mock(AttributeMetaData.class).getName()).thenReturn("mref1").getMock();
		when(mref1Attr.getDataType()).thenReturn(MREF);
		when(mref1Attr.getRefEntity()).thenReturn(ref1Meta);
		AttributeMetaData mref2Attr = when(mock(AttributeMetaData.class).getName()).thenReturn("mref2").getMock();
		when(mref2Attr.getDataType()).thenReturn(MREF);
		when(mref2Attr.getRefEntity()).thenReturn(ref2Meta);

		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("org_molgenis_MasterEntity")
				.getMock();
		when(entityMeta.getPackage()).thenReturn(package_);
		when(entityMeta.getIdAttribute()).thenReturn(masterIdAttr);
		when(entityMeta.getAttribute("masterId")).thenReturn(masterIdAttr);
		when(entityMeta.getAttribute("mref1")).thenReturn(mref1Attr);
		when(entityMeta.getAttribute("mref2")).thenReturn(mref2Attr);
		when(entityMeta.getAtomicAttributes()).thenReturn(asList(masterIdAttr, mref1Attr, mref2Attr));

		QueryImpl<Entity> q = new QueryImpl<>();

		List<Object> parameters = Lists.newArrayList();

		String sqlSelect = PostgreSqlQueryGenerator.getSqlSelect(entityMeta, q, parameters, true);
		Assert.assertEquals(sqlSelect, "SELECT this.\"masterId\", "
				+ "(SELECT array_agg(DISTINCT ARRAY[\"mref1\".\"order\"::TEXT,\"mref1\".\"mref1\"::TEXT]) "
				+ "FROM \"org_molgenis_MasterEntity_mref1\" AS \"mref1\" "
				+ "WHERE this.\"masterId\" = \"mref1\".\"masterId\") AS \"mref1\", "
				+ "(SELECT array_agg(DISTINCT ARRAY[\"mref2\".\"order\"::TEXT,\"mref2\".\"mref2\"::TEXT]) "
				+ "FROM \"org_molgenis_MasterEntity_mref2\" AS \"mref2\" "
				+ "WHERE this.\"masterId\" = \"mref2\".\"masterId\") AS \"mref2\" "
				+ "FROM \"org_molgenis_MasterEntity\" AS this");
	}

}