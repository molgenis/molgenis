package org.molgenis.data.postgresql;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.testng.Assert.assertEquals;

public class PostgreSqlQueryGeneratorTest
{
	@Test
	public void getSqlCreateForeignKey()
	{

		AttributeMetaData refIdAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("refIdAttr").getMock();
		EntityMetaData refEntityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("refEntity").getMock();
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);

		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData refAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(refAttr.getDataType()).thenReturn(XREF);
		when(refAttr.getRefEntity()).thenReturn(refEntityMeta);

		String expectedSql = "ALTER TABLE \"entity\" ADD FOREIGN KEY (\"attr\") REFERENCES \"refEntity\"(\"refIdAttr\")";
		assertEquals(PostgreSqlQueryGenerator.getSqlCreateForeignKey(entityMeta, refAttr), expectedSql);
	}

	@Test
	public void getSqlCreateForeignKeySelfReferencing()
	{
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("idAttr").getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);

		AttributeMetaData refAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(refAttr.getDataType()).thenReturn(XREF);
		when(refAttr.getRefEntity()).thenReturn(entityMeta);

		String expectedSql = "ALTER TABLE \"entity\" ADD FOREIGN KEY (\"attr\") REFERENCES \"entity\"(\"idAttr\") DEFERRABLE INITIALLY DEFERRED";
		assertEquals(PostgreSqlQueryGenerator.getSqlCreateForeignKey(entityMeta, refAttr), expectedSql);
	}

	@Test
	public void getSqlCreateJunctionTable()
	{
		AttributeMetaData refIdAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("refIdAttr").getMock();
		when(refIdAttr.getDataType()).thenReturn(STRING);
		EntityMetaData refEntityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("refEntity").getMock();
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);

		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("idAttr").getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(attr.getDataType()).thenReturn(MREF);
		when(attr.getRefEntity()).thenReturn(refEntityMeta);

		String expectedSql = "CREATE TABLE IF NOT EXISTS \"entity_attr\" (\"order\" INT,\"idAttr\" character varying(255) NOT NULL, \"attr\" character varying(255) NOT NULL, FOREIGN KEY (\"idAttr\") REFERENCES \"entity\"(\"idAttr\") ON DELETE CASCADE, FOREIGN KEY (\"attr\") REFERENCES \"refEntity\"(\"refIdAttr\") ON DELETE CASCADE, UNIQUE (\"attr\",\"idAttr\"), UNIQUE (\"order\",\"idAttr\"))";
		assertEquals(PostgreSqlQueryGenerator.getSqlCreateJunctionTable(entityMeta, attr), expectedSql);
	}

	@Test
	public void getSqlCreateJunctionTableSelfReferencing()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("idAttr").getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(attr.getDataType()).thenReturn(MREF);
		when(attr.getRefEntity()).thenReturn(entityMeta);

		String expectedSql = " CREATE TABLE IF NOT EXISTS \"entity_attr\" (\"order\" INT,\"idAttr\" character varying(255) NOT NULL, \"attr\" character varying(255) NOT NULL, FOREIGN KEY (\"idAttr\") REFERENCES \"entity\"(\"idAttr\") ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED, FOREIGN KEY (\"attr\") REFERENCES \"entity\"(\"idAttr\") ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED, UNIQUE (\"attr\",\"idAttr\"), UNIQUE (\"order\",\"idAttr\"))";
		assertEquals(PostgreSqlQueryGenerator.getSqlCreateJunctionTable(entityMeta, attr), expectedSql);
	}

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
		assertEquals(sqlSelect, "SELECT this.\"masterId\", "
				+ "(SELECT array_agg(DISTINCT ARRAY[\"mref1\".\"order\"::TEXT,\"mref1\".\"mref1\"::TEXT]) "
				+ "FROM \"org_molgenis_MasterEntity_mref1\" AS \"mref1\" "
				+ "WHERE this.\"masterId\" = \"mref1\".\"masterId\") AS \"mref1\", "
				+ "(SELECT array_agg(DISTINCT ARRAY[\"mref2\".\"order\"::TEXT,\"mref2\".\"mref2\"::TEXT]) "
				+ "FROM \"org_molgenis_MasterEntity_mref2\" AS \"mref2\" "
				+ "WHERE this.\"masterId\" = \"mref2\".\"masterId\") AS \"mref2\" "
				+ "FROM \"org_molgenis_MasterEntity\" AS this");
	}

}