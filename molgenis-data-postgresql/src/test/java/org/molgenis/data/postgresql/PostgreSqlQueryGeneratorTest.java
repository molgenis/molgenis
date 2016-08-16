package org.molgenis.data.postgresql;

import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.testng.Assert.assertEquals;

public class PostgreSqlQueryGeneratorTest
{
	@Test
	public void getSqlSetNotNull()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(attr.isNillable()).thenReturn(true);
		assertEquals(PostgreSqlQueryGenerator.getSqlSetNotNull(entityMeta, attr),
				"ALTER TABLE \"entity\" ALTER COLUMN \"attr\" SET NOT NULL");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void getSqlSetNotNullAttrNotNillable()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(attr.isNillable()).thenReturn(false);
		PostgreSqlQueryGenerator.getSqlSetNotNull(entityMeta, attr);
	}

	@Test
	public void getSqlSetDataType()
	{

	}

	@Test
	public void getSqlDropNotNull()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(attr.isNillable()).thenReturn(false);
		assertEquals(PostgreSqlQueryGenerator.getSqlDropNotNull(entityMeta, attr),
				"ALTER TABLE \"entity\" ALTER COLUMN \"attr\" DROP NOT NULL");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void getSqlDropNotNullAttrNotNillable()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(attr.isNillable()).thenReturn(true);
		PostgreSqlQueryGenerator.getSqlDropNotNull(entityMeta, attr);
	}

	@Test
	public void getSqlCreateTable()
	{
		// ref entity with string id attribute
		AttributeMetaData refIdAttrStr = mock(AttributeMetaData.class);
		when(refIdAttrStr.getName()).thenReturn("refIdAttrStr");
		when(refIdAttrStr.getDataType()).thenReturn(STRING);
		EntityMetaData refEntityMetaString = mock(EntityMetaData.class);
		when(refEntityMetaString.getName()).thenReturn("refEntityStr");
		when(refEntityMetaString.getIdAttribute()).thenReturn(refIdAttrStr);

		// ref entity with int id attribute
		AttributeMetaData refIdAttrInt = mock(AttributeMetaData.class);
		when(refIdAttrInt.getName()).thenReturn("refIdAttrInt");
		when(refIdAttrInt.getDataType()).thenReturn(INT);
		EntityMetaData refEntityMetaInt = mock(EntityMetaData.class);
		when(refEntityMetaInt.getName()).thenReturn("refEntityInt");
		when(refEntityMetaInt.getIdAttribute()).thenReturn(refIdAttrInt);

		// entity with attributes of all types and flavors
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("id").getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);

		List<AttributeMetaData> atomicAttrs = Lists.newArrayList();
		atomicAttrs.add(idAttr);
		StringBuilder attrNameBuilder = new StringBuilder(16);
		for (boolean hasExpression : newArrayList(false, true))
		{
			for (boolean unique : newArrayList(false, true))
			{
				for (boolean nillable : newArrayList(false, true))
				{
					for (AttributeType attrType : AttributeType.values())
					{
						if (attrType != COMPOUND)
						{
							attrNameBuilder.setLength(0);
							attrNameBuilder.append(attrType.toString().toLowerCase());
							if (hasExpression)
							{
								attrNameBuilder.append("_expression");
							}
							if (unique)
							{
								attrNameBuilder.append("_unique");
							}
							if (nillable)
							{
								attrNameBuilder.append("_nillable");
							}

							AttributeMetaData attr = mock(AttributeMetaData.class);
							when(attr.getName()).thenReturn(attrNameBuilder.toString());
							when(attr.getDataType()).thenReturn(attrType);
							when(attr.getExpression()).thenReturn(hasExpression ? "expression" : null);
							when(attr.isUnique()).thenReturn(unique);
							when(attr.isNillable()).thenReturn(nillable);

							if (attrType == CATEGORICAL || attrType == CATEGORICAL_MREF)
							{
								when(attr.getRefEntity()).thenReturn(refEntityMetaString);
							}
							else if (attrType == FILE || attrType == XREF || attrType == MREF)
							{
								when(attr.getRefEntity()).thenReturn(refEntityMetaInt);
							}
							else if (attrType == ENUM)
							{
								when(attr.getEnumOptions()).thenReturn(newArrayList("enum0", "enum1"));
							}
							atomicAttrs.add(attr);
						}
					}
				}
			}
		}
		when(entityMeta.getAtomicAttributes()).thenReturn(atomicAttrs);

		assertEquals(PostgreSqlQueryGenerator.getSqlCreateTable(entityMeta),
				"CREATE TABLE \"entity\"(\"id\" character varying(255),\"bool\" boolean NOT NULL,\"categorical\" character varying(255) NOT NULL,\"date\" date NOT NULL,\"date_time\" timestamp NOT NULL,\"decimal\" double precision NOT NULL,\"email\" character varying(255) NOT NULL,\"enum\" character varying(255) NOT NULL,\"file\" integer NOT NULL,\"html\" text NOT NULL,\"hyperlink\" character varying(255) NOT NULL,\"int\" integer NOT NULL,\"long\" bigint NOT NULL,\"script\" text NOT NULL,\"string\" character varying(255) NOT NULL,\"text\" text NOT NULL,\"xref\" integer NOT NULL,\"bool_nillable\" boolean,\"categorical_nillable\" character varying(255),\"date_nillable\" date,\"date_time_nillable\" timestamp,\"decimal_nillable\" double precision,\"email_nillable\" character varying(255),\"enum_nillable\" character varying(255),\"file_nillable\" integer,\"html_nillable\" text,\"hyperlink_nillable\" character varying(255),\"int_nillable\" integer,\"long_nillable\" bigint,\"script_nillable\" text,\"string_nillable\" character varying(255),\"text_nillable\" text,\"xref_nillable\" integer,\"bool_unique\" boolean NOT NULL,\"categorical_unique\" character varying(255) NOT NULL,\"date_unique\" date NOT NULL,\"date_time_unique\" timestamp NOT NULL,\"decimal_unique\" double precision NOT NULL,\"email_unique\" character varying(255) NOT NULL,\"enum_unique\" character varying(255) NOT NULL,\"file_unique\" integer NOT NULL,\"html_unique\" text NOT NULL,\"hyperlink_unique\" character varying(255) NOT NULL,\"int_unique\" integer NOT NULL,\"long_unique\" bigint NOT NULL,\"script_unique\" text NOT NULL,\"string_unique\" character varying(255) NOT NULL,\"text_unique\" text NOT NULL,\"xref_unique\" integer NOT NULL,\"bool_unique_nillable\" boolean,\"categorical_unique_nillable\" character varying(255),\"date_unique_nillable\" date,\"date_time_unique_nillable\" timestamp,\"decimal_unique_nillable\" double precision,\"email_unique_nillable\" character varying(255),\"enum_unique_nillable\" character varying(255),\"file_unique_nillable\" integer,\"html_unique_nillable\" text,\"hyperlink_unique_nillable\" character varying(255),\"int_unique_nillable\" integer,\"long_unique_nillable\" bigint,\"script_unique_nillable\" text,\"string_unique_nillable\" character varying(255),\"text_unique_nillable\" text,\"xref_unique_nillable\" integer,CONSTRAINT \"entity_id_pkey\" PRIMARY KEY (\"id\"),CONSTRAINT \"entity_categorical_fkey\" FOREIGN KEY (\"categorical\") REFERENCES \"refEntityStr\"(\"refIdAttrStr\"),CONSTRAINT \"entity_enum_chk\" CHECK (\"enum\" IN ('enum0','enum1')),CONSTRAINT \"entity_file_fkey\" FOREIGN KEY (\"file\") REFERENCES \"refEntityInt\"(\"refIdAttrInt\"),CONSTRAINT \"entity_xref_fkey\" FOREIGN KEY (\"xref\") REFERENCES \"refEntityInt\"(\"refIdAttrInt\"),CONSTRAINT \"entity_categorical_nillable_fkey\" FOREIGN KEY (\"categorical_nillable\") REFERENCES \"refEntityStr\"(\"refIdAttrStr\"),CONSTRAINT \"entity_enum_nillable_chk\" CHECK (\"enum_nillable\" IN ('enum0','enum1')),CONSTRAINT \"entity_file_nillable_fkey\" FOREIGN KEY (\"file_nillable\") REFERENCES \"refEntityInt\"(\"refIdAttrInt\"),CONSTRAINT \"entity_xref_nillable_fkey\" FOREIGN KEY (\"xref_nillable\") REFERENCES \"refEntityInt\"(\"refIdAttrInt\"),CONSTRAINT \"entity_bool_unique_key\" UNIQUE (\"bool_unique\"),CONSTRAINT \"entity_categorical_unique_fkey\" FOREIGN KEY (\"categorical_unique\") REFERENCES \"refEntityStr\"(\"refIdAttrStr\"),CONSTRAINT \"entity_categorical_unique_key\" UNIQUE (\"categorical_unique\"),CONSTRAINT \"entity_date_unique_key\" UNIQUE (\"date_unique\"),CONSTRAINT \"entity_date_time_unique_key\" UNIQUE (\"date_time_unique\"),CONSTRAINT \"entity_decimal_unique_key\" UNIQUE (\"decimal_unique\"),CONSTRAINT \"entity_email_unique_key\" UNIQUE (\"email_unique\"),CONSTRAINT \"entity_enum_unique_key\" UNIQUE (\"enum_unique\"),CONSTRAINT \"entity_enum_unique_chk\" CHECK (\"enum_unique\" IN ('enum0','enum1')),CONSTRAINT \"entity_file_unique_fkey\" FOREIGN KEY (\"file_unique\") REFERENCES \"refEntityInt\"(\"refIdAttrInt\"),CONSTRAINT \"entity_file_unique_key\" UNIQUE (\"file_unique\"),CONSTRAINT \"entity_html_unique_key\" UNIQUE (\"html_unique\"),CONSTRAINT \"entity_hyperlink_unique_key\" UNIQUE (\"hyperlink_unique\"),CONSTRAINT \"entity_int_unique_key\" UNIQUE (\"int_unique\"),CONSTRAINT \"entity_long_unique_key\" UNIQUE (\"long_unique\"),CONSTRAINT \"entity_script_unique_key\" UNIQUE (\"script_unique\"),CONSTRAINT \"entity_string_unique_key\" UNIQUE (\"string_unique\"),CONSTRAINT \"entity_text_unique_key\" UNIQUE (\"text_unique\"),CONSTRAINT \"entity_xref_unique_fkey\" FOREIGN KEY (\"xref_unique\") REFERENCES \"refEntityInt\"(\"refIdAttrInt\"),CONSTRAINT \"entity_xref_unique_key\" UNIQUE (\"xref_unique\"),CONSTRAINT \"entity_bool_unique_nillable_key\" UNIQUE (\"bool_unique_nillable\"),CONSTRAINT \"entity_categorical_unique_nillable_fkey\" FOREIGN KEY (\"categorical_unique_nillable\") REFERENCES \"refEntityStr\"(\"refIdAttrStr\"),CONSTRAINT \"entity_categorical_unique_nillable_key\" UNIQUE (\"categorical_unique_nillable\"),CONSTRAINT \"entity_date_unique_nillable_key\" UNIQUE (\"date_unique_nillable\"),CONSTRAINT \"entity_date_time_unique_nillable_key\" UNIQUE (\"date_time_unique_nillable\"),CONSTRAINT \"entity_decimal_unique_nillable_key\" UNIQUE (\"decimal_unique_nillable\"),CONSTRAINT \"entity_email_unique_nillable_key\" UNIQUE (\"email_unique_nillable\"),CONSTRAINT \"entity_enum_unique_nillable_key\" UNIQUE (\"enum_unique_nillable\"),CONSTRAINT \"entity_enum_unique_nillable_chk\" CHECK (\"enum_unique_nillable\" IN ('enum0','enum1')),CONSTRAINT \"entity_file_unique_nillable_fkey\" FOREIGN KEY (\"file_unique_nillable\") REFERENCES \"refEntityInt\"(\"refIdAttrInt\"),CONSTRAINT \"entity_file_unique_nillable_key\" UNIQUE (\"file_unique_nillable\"),CONSTRAINT \"entity_html_unique_nillable_key\" UNIQUE (\"html_unique_nillable\"),CONSTRAINT \"entity_hyperlink_unique_nillable_key\" UNIQUE (\"hyperlink_unique_nillable\"),CONSTRAINT \"entity_int_unique_nillable_key\" UNIQUE (\"int_unique_nillable\"),CONSTRAINT \"entity_long_unique_nillable_key\" UNIQUE (\"long_unique_nillable\"),CONSTRAINT \"entity_script_unique_nillable_key\" UNIQUE (\"script_unique_nillable\"),CONSTRAINT \"entity_string_unique_nillable_key\" UNIQUE (\"string_unique_nillable\"),CONSTRAINT \"entity_text_unique_nillable_key\" UNIQUE (\"text_unique_nillable\"),CONSTRAINT \"entity_xref_unique_nillable_fkey\" FOREIGN KEY (\"xref_unique_nillable\") REFERENCES \"refEntityInt\"(\"refIdAttrInt\"),CONSTRAINT \"entity_xref_unique_nillable_key\" UNIQUE (\"xref_unique_nillable\"))");
	}

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

		String expectedSql = "ALTER TABLE \"entity\" ADD CONSTRAINT \"entity_attr_fkey\" FOREIGN KEY (\"attr\") REFERENCES \"refEntity\"(\"refIdAttr\")";
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

		String expectedSql = "ALTER TABLE \"entity\" ADD CONSTRAINT \"entity_attr_fkey\" FOREIGN KEY (\"attr\") REFERENCES \"entity\"(\"idAttr\") DEFERRABLE INITIALLY DEFERRED";
		assertEquals(PostgreSqlQueryGenerator.getSqlCreateForeignKey(entityMeta, refAttr), expectedSql);
	}

	@Test
	public void getSqlDropForeignKey()
	{
		AttributeMetaData refIdAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("refIdAttr").getMock();
		EntityMetaData refEntityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("refEntity").getMock();
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);

		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData refAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(refAttr.getDataType()).thenReturn(XREF);
		when(refAttr.getRefEntity()).thenReturn(refEntityMeta);

		String expectedSql = "ALTER TABLE \"entity\" DROP CONSTRAINT \"entity_attr_fkey\"";
		assertEquals(PostgreSqlQueryGenerator.getSqlDropForeignKey(entityMeta, refAttr), expectedSql);
	}

	@Test
	public void getSqlCreateUniqueKey()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(attr.getDataType()).thenReturn(STRING);

		String expectedSql = "ALTER TABLE \"entity\" ADD CONSTRAINT \"entity_attr_key\" UNIQUE (\"attr\")";
		assertEquals(PostgreSqlQueryGenerator.getSqlCreateUniqueKey(entityMeta, attr), expectedSql);
	}

	@Test
	public void getSqlDropUniqueKey()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(attr.getDataType()).thenReturn(STRING);

		String expectedSql = "ALTER TABLE \"entity\" DROP CONSTRAINT \"entity_attr_key\"";
		assertEquals(PostgreSqlQueryGenerator.getSqlDropUniqueKey(entityMeta, attr), expectedSql);
	}

	@Test
	public void getSqlCreateCheckConstraint()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(attr.getDataType()).thenReturn(ENUM);
		when(attr.getEnumOptions()).thenReturn(newArrayList("enum0", "enum1", "enum2"));
		assertEquals(PostgreSqlQueryGenerator.getSqlCreateCheckConstraint(entityMeta, attr),
				"ALTER TABLE \"entity\" ADD CONSTRAINT \"entity_attr_chk\" CHECK (\"attr\" IN ('enum0','enum1','enum2'))");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void getSqlCreateCheckConstraintWrongDataType()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(attr.getDataType()).thenReturn(STRING);
		PostgreSqlQueryGenerator.getSqlCreateCheckConstraint(entityMeta, attr);
	}

	@Test
	public void getSqlDropCheckConstraint()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(attr.getDataType()).thenReturn(ENUM);
		when(attr.getEnumOptions()).thenReturn(newArrayList("enum0", "enum1", "enum2"));
		assertEquals(PostgreSqlQueryGenerator.getSqlDropCheckConstraint(entityMeta, attr),
				"ALTER TABLE \"entity\" DROP CONSTRAINT \"entity_attr_chk\"");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void getSqlDropCheckConstraintWrongDataType()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(attr.getDataType()).thenReturn(STRING);
		PostgreSqlQueryGenerator.getSqlDropCheckConstraint(entityMeta, attr);
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

		String expectedSql = "CREATE TABLE IF NOT EXISTS \"entity_attr\" (\"order\" INT,\"idAttr\" character varying(255) NOT NULL, \"attr\" character varying(255) NOT NULL, FOREIGN KEY (\"idAttr\") REFERENCES \"entity\"(\"idAttr\") ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED, FOREIGN KEY (\"attr\") REFERENCES \"entity\"(\"idAttr\") ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED, UNIQUE (\"attr\",\"idAttr\"), UNIQUE (\"order\",\"idAttr\"))";
		assertEquals(PostgreSqlQueryGenerator.getSqlCreateJunctionTable(entityMeta, attr), expectedSql);
	}

	@Test
	public void getSqlSelectMref() throws Exception
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

	@DataProvider(name = "getSqlAddColumnProvider")
	public static Iterator<Object[]> getSqlAddColumnProvider()
	{
		// ref entity with string id attribute
		AttributeMetaData refIdAttrStr = mock(AttributeMetaData.class);
		when(refIdAttrStr.getName()).thenReturn("refIdAttrStr");
		when(refIdAttrStr.getDataType()).thenReturn(STRING);
		EntityMetaData refEntityMetaString = mock(EntityMetaData.class);
		when(refEntityMetaString.toString()).thenReturn("refEntityStr");
		when(refEntityMetaString.getName()).thenReturn("refEntityStr");
		when(refEntityMetaString.getIdAttribute()).thenReturn(refIdAttrStr);

		// ref entity with int id attribute
		AttributeMetaData refIdAttrInt = mock(AttributeMetaData.class);
		when(refIdAttrInt.getName()).thenReturn("refIdAttrInt");
		when(refIdAttrInt.getDataType()).thenReturn(INT);
		EntityMetaData refEntityMetaInt = mock(EntityMetaData.class);
		when(refEntityMetaInt.toString()).thenReturn("refEntityInt");
		when(refEntityMetaInt.getName()).thenReturn("refEntityInt");
		when(refEntityMetaInt.getIdAttribute()).thenReturn(refIdAttrInt);

		return Arrays.asList(new Object[] { BOOL, true, null, "ALTER TABLE \"entity\" ADD \"attr\" boolean" },
				new Object[] { CATEGORICAL, true, refEntityMetaInt,
						"ALTER TABLE \"entity\" ADD \"attr\" integer,ADD CONSTRAINT \"entity_attr_fkey\" FOREIGN KEY (\"attr\") REFERENCES \"refEntityInt\"(\"refIdAttrInt\")" },
				new Object[] { DATE, true, null, "ALTER TABLE \"entity\" ADD \"attr\" date" },
				new Object[] { DATE_TIME, true, null, "ALTER TABLE \"entity\" ADD \"attr\" timestamp" },
				new Object[] { DECIMAL, true, null, "ALTER TABLE \"entity\" ADD \"attr\" double precision" },
				new Object[] { EMAIL, true, null, "ALTER TABLE \"entity\" ADD \"attr\" character varying(255)" },
				new Object[] { ENUM, true, null,
						"ALTER TABLE \"entity\" ADD \"attr\" character varying(255),ADD CONSTRAINT \"entity_attr_chk\" CHECK (\"attr\" IN ('enum0, enum1'))" },
				new Object[] { FILE, true, refEntityMetaString,
						"ALTER TABLE \"entity\" ADD \"attr\" character varying(255),ADD CONSTRAINT \"entity_attr_fkey\" FOREIGN KEY (\"attr\") REFERENCES \"refEntityStr\"(\"refIdAttrStr\")" },
				new Object[] { HTML, true, null, "ALTER TABLE \"entity\" ADD \"attr\" text" },
				new Object[] { HYPERLINK, true, null, "ALTER TABLE \"entity\" ADD \"attr\" character varying(255)" },
				new Object[] { INT, true, null, "ALTER TABLE \"entity\" ADD \"attr\" integer" },
				new Object[] { LONG, true, null, "ALTER TABLE \"entity\" ADD \"attr\" bigint" },
				new Object[] { SCRIPT, true, null, "ALTER TABLE \"entity\" ADD \"attr\" text" },
				new Object[] { STRING, true, null, "ALTER TABLE \"entity\" ADD \"attr\" character varying(255)" },
				new Object[] { TEXT, true, null, "ALTER TABLE \"entity\" ADD \"attr\" text" },
				new Object[] { XREF, true, refEntityMetaString,
						"ALTER TABLE \"entity\" ADD \"attr\" character varying(255),ADD CONSTRAINT \"entity_attr_fkey\" FOREIGN KEY (\"attr\") REFERENCES \"refEntityStr\"(\"refIdAttrStr\")" },
				new Object[] { BOOL, false, null, "ALTER TABLE \"entity\" ADD \"attr\" boolean NOT NULL" },
				new Object[] { CATEGORICAL, false, refEntityMetaInt,
						"ALTER TABLE \"entity\" ADD \"attr\" integer NOT NULL,ADD CONSTRAINT \"entity_attr_fkey\" FOREIGN KEY (\"attr\") REFERENCES \"refEntityInt\"(\"refIdAttrInt\")" },
				new Object[] { DATE, false, null, "ALTER TABLE \"entity\" ADD \"attr\" date NOT NULL" },
				new Object[] { DATE_TIME, false, null, "ALTER TABLE \"entity\" ADD \"attr\" timestamp NOT NULL" },
				new Object[] { DECIMAL, false, null, "ALTER TABLE \"entity\" ADD \"attr\" double precision NOT NULL" },
				new Object[] { EMAIL, false, null,
						"ALTER TABLE \"entity\" ADD \"attr\" character varying(255) NOT NULL" },
				new Object[] { ENUM, false, null,
						"ALTER TABLE \"entity\" ADD \"attr\" character varying(255) NOT NULL,ADD CONSTRAINT \"entity_attr_chk\" CHECK (\"attr\" IN ('enum0, enum1'))" },
				new Object[] { FILE, false, refEntityMetaString,
						"ALTER TABLE \"entity\" ADD \"attr\" character varying(255) NOT NULL,ADD CONSTRAINT \"entity_attr_fkey\" FOREIGN KEY (\"attr\") REFERENCES \"refEntityStr\"(\"refIdAttrStr\")" },
				new Object[] { HTML, false, null, "ALTER TABLE \"entity\" ADD \"attr\" text NOT NULL" },
				new Object[] { HYPERLINK, false, null,
						"ALTER TABLE \"entity\" ADD \"attr\" character varying(255) NOT NULL" },
				new Object[] { INT, false, null, "ALTER TABLE \"entity\" ADD \"attr\" integer NOT NULL" },
				new Object[] { LONG, false, null, "ALTER TABLE \"entity\" ADD \"attr\" bigint NOT NULL" },
				new Object[] { SCRIPT, false, null, "ALTER TABLE \"entity\" ADD \"attr\" text NOT NULL" },
				new Object[] { STRING, false, null,
						"ALTER TABLE \"entity\" ADD \"attr\" character varying(255) NOT NULL" },
				new Object[] { TEXT, false, null, "ALTER TABLE \"entity\" ADD \"attr\" text NOT NULL" },
				new Object[] { XREF, false, refEntityMetaString,
						"ALTER TABLE \"entity\" ADD \"attr\" character varying(255) NOT NULL,ADD CONSTRAINT \"entity_attr_fkey\" FOREIGN KEY (\"attr\") REFERENCES \"refEntityStr\"(\"refIdAttrStr\")" })
				.iterator();
	}

	@Test(dataProvider = "getSqlAddColumnProvider")
	public void getSqlAddColumn(AttributeType attrType, boolean nillable, EntityMetaData refEntityMeta, String sql)
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("id").getMock();
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(attr.getDataType()).thenReturn(attrType);
		when(attr.isNillable()).thenReturn(nillable);
		when(attr.getRefEntity()).thenReturn(refEntityMeta);
		when(attr.getEnumOptions())
				.thenReturn(attrType == ENUM ? newArrayList("enum0, enum1") : Collections.emptyList());
		assertEquals(PostgreSqlQueryGenerator.getSqlAddColumn(entityMeta, attr), sql);
	}

	@DataProvider(name = "getSqlAddColumnInvalidType")
	public static Iterator<Object[]> getSqlAddColumnInvalidTypeProvider()
	{
		return Arrays.asList(new Object[] { COMPOUND }, new Object[] { CATEGORICAL_MREF }, new Object[] { MREF })
				.iterator();
	}

	@Test(dataProvider = "getSqlAddColumnInvalidType", expectedExceptions = RuntimeException.class)
	public void getSqlAddColumnInvalidType(AttributeType attrType)
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(attr.getDataType()).thenReturn(attrType);
		PostgreSqlQueryGenerator.getSqlAddColumn(entityMeta, attr);
	}

	@Test
	public void getSqlDropColumn()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		assertEquals(PostgreSqlQueryGenerator.getSqlDropColumn(entityMeta, attr),
				"ALTER TABLE \"entity\" DROP COLUMN \"attr\"");
	}
}