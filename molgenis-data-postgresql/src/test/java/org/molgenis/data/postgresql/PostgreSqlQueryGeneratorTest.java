package org.molgenis.data.postgresql;

import org.molgenis.data.*;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.postgresql.PostgreSqlQueryGenerator.ColumnMode;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.QueryRule.Operator.*;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.postgresql.PostgreSqlQueryGenerator.ColumnMode.EXCLUDE_DEFAULT_CONSTRAINT;
import static org.molgenis.data.postgresql.PostgreSqlQueryGenerator.ColumnMode.INCLUDE_DEFAULT_CONSTRAINT;
import static org.testng.Assert.assertEquals;

public class PostgreSqlQueryGeneratorTest
{
	@Test
	public void getSqlSetNotNull()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(entityType.getId()).thenReturn("entityTypeId");
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		when(attr.isNillable()).thenReturn(true);
		assertEquals(PostgreSqlQueryGenerator.getSqlSetNotNull(entityType, attr),
				"ALTER TABLE \"entityTypeId#c34894ba\" ALTER COLUMN \"attr\" SET NOT NULL");
	}

	@Test
	public void getSqlDropNotNull()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(entityType.getId()).thenReturn("entityTypeId");
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		when(attr.isNillable()).thenReturn(false);
		assertEquals(PostgreSqlQueryGenerator.getSqlDropNotNull(entityType, attr),
				"ALTER TABLE \"entityTypeId#c34894ba\" ALTER COLUMN \"attr\" DROP NOT NULL");
	}

	@Test
	public void getSqlCreateTable()
	{
		// ref entity with string id attribute
		Attribute refIdAttrStr = mock(Attribute.class);
		when(refIdAttrStr.getIdentifier()).thenReturn("refIdAttrStrId");
		when(refIdAttrStr.getName()).thenReturn("refIdAttrStr");
		when(refIdAttrStr.getDataType()).thenReturn(STRING);
		EntityType refEntityTypeString = mock(EntityType.class);
		when(refEntityTypeString.getId()).thenReturn("refEntityStr");
		when(refEntityTypeString.getIdAttribute()).thenReturn(refIdAttrStr);

		// ref entity with int id attribute
		Attribute refIdAttrInt = mock(Attribute.class);
		when(refIdAttrInt.getIdentifier()).thenReturn("refIdAttrInt");
		when(refIdAttrInt.getName()).thenReturn("refIdAttrInt");
		when(refIdAttrInt.getDataType()).thenReturn(INT);
		EntityType refEntityTypeInt = mock(EntityType.class);
		when(refEntityTypeInt.getId()).thenReturn("refEntityInt");
		when(refEntityTypeInt.getIdAttribute()).thenReturn(refIdAttrInt);

		// entity with attributes of all types and flavors
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(entityType.getId()).thenReturn("entityTypeId");
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("id").getMock();
		when(idAttr.getIdentifier()).thenReturn("idAttrId");
		when(idAttr.getDataType()).thenReturn(STRING);
		when(entityType.getIdAttribute()).thenReturn(idAttr);

		List<Attribute> atomicAttrs = Lists.newArrayList();
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

							Attribute attr = mock(Attribute.class);
							when(attr.getIdentifier()).thenReturn(attrNameBuilder.toString() + "Id");
							when(attr.getName()).thenReturn(attrNameBuilder.toString());
							when(attr.getDataType()).thenReturn(attrType);
							when(attr.getExpression()).thenReturn(hasExpression ? "expression" : null);
							when(attr.isUnique()).thenReturn(unique);
							when(attr.isNillable()).thenReturn(nillable);

							if (attrType == CATEGORICAL || attrType == CATEGORICAL_MREF || attrType == ONE_TO_MANY)
							{
								when(attr.getRefEntity()).thenReturn(refEntityTypeString);
							}
							else if (attrType == FILE || attrType == XREF || attrType == MREF)
							{
								when(attr.getRefEntity()).thenReturn(refEntityTypeInt);
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
		when(entityType.getAtomicAttributes()).thenReturn(atomicAttrs);

		assertEquals(PostgreSqlQueryGenerator.getSqlCreateTable(entityType),
				"CREATE TABLE \"entityTypeId#c34894ba\"(\"id\" character varying(255),\"bool\" boolean NOT NULL,\"categorical\" character varying(255) NOT NULL,\"date\" date NOT NULL,\"date_time\" timestamp with time zone NOT NULL,\"decimal\" double precision NOT NULL,\"email\" character varying(255) NOT NULL,\"enum\" character varying(255) NOT NULL,\"file\" integer NOT NULL,\"html\" text NOT NULL,\"hyperlink\" character varying(255) NOT NULL,\"int\" integer NOT NULL,\"long\" bigint NOT NULL,\"script\" text NOT NULL,\"string\" character varying(255) NOT NULL,\"text\" text NOT NULL,\"xref\" integer NOT NULL,\"bool_nillable\" boolean,\"categorical_nillable\" character varying(255),\"date_nillable\" date,\"date_time_nillable\" timestamp with time zone,\"decimal_nillable\" double precision,\"email_nillable\" character varying(255),\"enum_nillable\" character varying(255),\"file_nillable\" integer,\"html_nillable\" text,\"hyperlink_nillable\" character varying(255),\"int_nillable\" integer,\"long_nillable\" bigint,\"script_nillable\" text,\"string_nillable\" character varying(255),\"text_nillable\" text,\"xref_nillable\" integer,\"bool_unique\" boolean NOT NULL,\"categorical_unique\" character varying(255) NOT NULL,\"date_unique\" date NOT NULL,\"date_time_unique\" timestamp with time zone NOT NULL,\"decimal_unique\" double precision NOT NULL,\"email_unique\" character varying(255) NOT NULL,\"enum_unique\" character varying(255) NOT NULL,\"file_unique\" integer NOT NULL,\"html_unique\" text NOT NULL,\"hyperlink_unique\" character varying(255) NOT NULL,\"int_unique\" integer NOT NULL,\"long_unique\" bigint NOT NULL,\"script_unique\" text NOT NULL,\"string_unique\" character varying(255) NOT NULL,\"text_unique\" text NOT NULL,\"xref_unique\" integer NOT NULL,\"bool_unique_nillable\" boolean,\"categorical_unique_nillable\" character varying(255),\"date_unique_nillable\" date,\"date_time_unique_nillable\" timestamp with time zone,\"decimal_unique_nillable\" double precision,\"email_unique_nillable\" character varying(255),\"enum_unique_nillable\" character varying(255),\"file_unique_nillable\" integer,\"html_unique_nillable\" text,\"hyperlink_unique_nillable\" character varying(255),\"int_unique_nillable\" integer,\"long_unique_nillable\" bigint,\"script_unique_nillable\" text,\"string_unique_nillable\" character varying(255),\"text_unique_nillable\" text,\"xref_unique_nillable\" integer,CONSTRAINT \"entityTypeId#c34894ba_id_pkey\" PRIMARY KEY (\"id\"),CONSTRAINT \"entityTypeId#c34894ba_categorical_fkey\" FOREIGN KEY (\"categorical\") REFERENCES \"refEntityStr#305ca1a9\"(\"refIdAttrStr\"),CONSTRAINT \"entityTypeId#c34894ba_enum_chk\" CHECK (\"enum\" IN ('enum0','enum1')),CONSTRAINT \"entityTypeId#c34894ba_file_fkey\" FOREIGN KEY (\"file\") REFERENCES \"refEntityInt#78255ee1\"(\"refIdAttrInt\"),CONSTRAINT \"entityTypeId#c34894ba_xref_fkey\" FOREIGN KEY (\"xref\") REFERENCES \"refEntityInt#78255ee1\"(\"refIdAttrInt\"),CONSTRAINT \"entityTypeId#c34894ba_categorical_nillable_fkey\" FOREIGN KEY (\"categorical_nillable\") REFERENCES \"refEntityStr#305ca1a9\"(\"refIdAttrStr\"),CONSTRAINT \"entityTypeId#c34894ba_enum_nillable_chk\" CHECK (\"enum_nillable\" IN ('enum0','enum1')),CONSTRAINT \"entityTypeId#c34894ba_file_nillable_fkey\" FOREIGN KEY (\"file_nillable\") REFERENCES \"refEntityInt#78255ee1\"(\"refIdAttrInt\"),CONSTRAINT \"entityTypeId#c34894ba_xref_nillable_fkey\" FOREIGN KEY (\"xref_nillable\") REFERENCES \"refEntityInt#78255ee1\"(\"refIdAttrInt\"),CONSTRAINT \"entityTypeId#c34894ba_bool_unique_key\" UNIQUE (\"bool_unique\"),CONSTRAINT \"entityTypeId#c34894ba_categorical_unique_fkey\" FOREIGN KEY (\"categorical_unique\") REFERENCES \"refEntityStr#305ca1a9\"(\"refIdAttrStr\"),CONSTRAINT \"entityTypeId#c34894ba_categorical_unique_key\" UNIQUE (\"categorical_unique\"),CONSTRAINT \"entityTypeId#c34894ba_date_unique_key\" UNIQUE (\"date_unique\"),CONSTRAINT \"entityTypeId#c34894ba_date_time_unique_key\" UNIQUE (\"date_time_unique\"),CONSTRAINT \"entityTypeId#c34894ba_decimal_unique_key\" UNIQUE (\"decimal_unique\"),CONSTRAINT \"entityTypeId#c34894ba_email_unique_key\" UNIQUE (\"email_unique\"),CONSTRAINT \"entityTypeId#c34894ba_enum_unique_key\" UNIQUE (\"enum_unique\"),CONSTRAINT \"entityTypeId#c34894ba_enum_unique_chk\" CHECK (\"enum_unique\" IN ('enum0','enum1')),CONSTRAINT \"entityTypeId#c34894ba_file_unique_fkey\" FOREIGN KEY (\"file_unique\") REFERENCES \"refEntityInt#78255ee1\"(\"refIdAttrInt\"),CONSTRAINT \"entityTypeId#c34894ba_file_unique_key\" UNIQUE (\"file_unique\"),CONSTRAINT \"entityTypeId#c34894ba_html_unique_key\" UNIQUE (\"html_unique\"),CONSTRAINT \"entityTypeId#c34894ba_hyperlink_unique_key\" UNIQUE (\"hyperlink_unique\"),CONSTRAINT \"entityTypeId#c34894ba_int_unique_key\" UNIQUE (\"int_unique\"),CONSTRAINT \"entityTypeId#c34894ba_long_unique_key\" UNIQUE (\"long_unique\"),CONSTRAINT \"entityTypeId#c34894ba_script_unique_key\" UNIQUE (\"script_unique\"),CONSTRAINT \"entityTypeId#c34894ba_string_unique_key\" UNIQUE (\"string_unique\"),CONSTRAINT \"entityTypeId#c34894ba_text_unique_key\" UNIQUE (\"text_unique\"),CONSTRAINT \"entityTypeId#c34894ba_xref_unique_fkey\" FOREIGN KEY (\"xref_unique\") REFERENCES \"refEntityInt#78255ee1\"(\"refIdAttrInt\"),CONSTRAINT \"entityTypeId#c34894ba_xref_unique_key\" UNIQUE (\"xref_unique\"),CONSTRAINT \"entityTypeId#c34894ba_bool_unique_nillable_key\" UNIQUE (\"bool_unique_nillable\"),CONSTRAINT \"entityTypeId#c34894ba_categorical_unique_nillable_fkey\" FOREIGN KEY (\"categorical_unique_nillable\") REFERENCES \"refEntityStr#305ca1a9\"(\"refIdAttrStr\"),CONSTRAINT \"entityTypeId#c34894ba_categorical_unique_nillable_key\" UNIQUE (\"categorical_unique_nillable\"),CONSTRAINT \"entityTypeId#c34894ba_date_unique_nillable_key\" UNIQUE (\"date_unique_nillable\"),CONSTRAINT \"entityTypeId#c34894ba_date_time_unique_nillable_key\" UNIQUE (\"date_time_unique_nillable\"),CONSTRAINT \"entityTypeId#c34894ba_decimal_unique_nillable_key\" UNIQUE (\"decimal_unique_nillable\"),CONSTRAINT \"entityTypeId#c34894ba_email_unique_nillable_key\" UNIQUE (\"email_unique_nillable\"),CONSTRAINT \"entityTypeId#c34894ba_enum_unique_nillable_key\" UNIQUE (\"enum_unique_nillable\"),CONSTRAINT \"entityTypeId#c34894ba_enum_unique_nillable_chk\" CHECK (\"enum_unique_nillable\" IN ('enum0','enum1')),CONSTRAINT \"entityTypeId#c34894ba_file_unique_nillable_fkey\" FOREIGN KEY (\"file_unique_nillable\") REFERENCES \"refEntityInt#78255ee1\"(\"refIdAttrInt\"),CONSTRAINT \"entityTypeId#c34894ba_file_unique_nillable_key\" UNIQUE (\"file_unique_nillable\"),CONSTRAINT \"entityTypeId#c34894ba_html_unique_nillable_key\" UNIQUE (\"html_unique_nillable\"),CONSTRAINT \"entityTypeId#c34894ba_hyperlink_unique_nillable_key\" UNIQUE (\"hyperlink_unique_nillable\"),CONSTRAINT \"entityTypeId#c34894ba_int_unique_nillable_key\" UNIQUE (\"int_unique_nillable\"),CONSTRAINT \"entityTypeId#c34894ba_long_unique_nillable_key\" UNIQUE (\"long_unique_nillable\"),CONSTRAINT \"entityTypeId#c34894ba_script_unique_nillable_key\" UNIQUE (\"script_unique_nillable\"),CONSTRAINT \"entityTypeId#c34894ba_string_unique_nillable_key\" UNIQUE (\"string_unique_nillable\"),CONSTRAINT \"entityTypeId#c34894ba_text_unique_nillable_key\" UNIQUE (\"text_unique_nillable\"),CONSTRAINT \"entityTypeId#c34894ba_xref_unique_nillable_fkey\" FOREIGN KEY (\"xref_unique_nillable\") REFERENCES \"refEntityInt#78255ee1\"(\"refIdAttrInt\"),CONSTRAINT \"entityTypeId#c34894ba_xref_unique_nillable_key\" UNIQUE (\"xref_unique_nillable\"))");
	}

	@Test
	public void getSqlCreateFunctionValidateUpdate()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(entityType.getId()).thenReturn("entityTypeId");
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("idAttr").getMock();
		when(idAttr.getIdentifier()).thenReturn("idAttrId");
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn("attr0").getMock();
		when(attr0.getIdentifier()).thenReturn("attr0Id");
		Attribute attr1 = when(mock(Attribute.class).getName()).thenReturn("attr1").getMock();
		when(attr1.getIdentifier()).thenReturn("attr1Id");
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		String expectedSql =
				"CREATE FUNCTION \"validate_update_entityTypeId#c34894ba\"() RETURNS TRIGGER AS $$\n" + "BEGIN\n"
						+ "  IF OLD.\"attr0\" <> NEW.\"attr0\" THEN\n"
						+ "    RAISE EXCEPTION 'Updating read-only column \"attr0\" of table \"entityTypeId#c34894ba\" with id [%] is not allowed', OLD.\"idAttr\" USING ERRCODE = '23506';\n"
						+ "  END IF;\n" + "  IF OLD.\"attr1\" <> NEW.\"attr1\" THEN\n"
						+ "    RAISE EXCEPTION 'Updating read-only column \"attr1\" of table \"entityTypeId#c34894ba\" with id [%] is not allowed', OLD.\"idAttr\" USING ERRCODE = '23506';\n"
						+ "  END IF;\n" + "  RETURN NEW;\n" + "END;\n" + "$$ LANGUAGE plpgsql;";
		assertEquals(PostgreSqlQueryGenerator.getSqlCreateFunctionValidateUpdate(entityType, asList(attr0, attr1)),
				expectedSql);
	}

	@Test
	public void getSqlDropFunctionValidateUpdate()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(entityType.getId()).thenReturn("entityTypeId");
		String expectedSql = "DROP FUNCTION \"validate_update_entityTypeId#c34894ba\"();";
		assertEquals(PostgreSqlQueryGenerator.getSqlDropFunctionValidateUpdate(entityType), expectedSql);
	}

	@Test
	public void getSqlCreateUpdateTrigger()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(entityType.getId()).thenReturn("entityTypeId");
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn("attr0").getMock();
		when(attr0.getIdentifier()).thenReturn("attr0Id");
		Attribute attr1 = when(mock(Attribute.class).getName()).thenReturn("attr1").getMock();
		when(attr1.getIdentifier()).thenReturn("attr1Id");
		String expectedSql = "CREATE TRIGGER \"update_trigger_entityTypeId#c34894ba\" AFTER UPDATE ON \"entityTypeId#c34894ba\" FOR EACH ROW WHEN (OLD.\"attr0\" IS DISTINCT FROM NEW.\"attr0\" OR OLD.\"attr1\" IS DISTINCT FROM NEW.\"attr1\") EXECUTE PROCEDURE \"validate_update_entityTypeId#c34894ba\"();";
		assertEquals(PostgreSqlQueryGenerator.getSqlCreateUpdateTrigger(entityType, asList(attr0, attr1)), expectedSql);
	}

	@Test
	public void getSqlDropUpdateTrigger()
	{
		String expectedSql = "DROP TRIGGER \"update_trigger_entityTypeId#c34894ba\" ON \"entityTypeId#c34894ba\"";
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(entityType.getId()).thenReturn("entityTypeId");
		assertEquals(PostgreSqlQueryGenerator.getSqlDropUpdateTrigger(entityType), expectedSql);
	}

	@Test
	public void getSqlCreateForeignKey()
	{
		Attribute refIdAttr = when(mock(Attribute.class).getName()).thenReturn("refIdAttr").getMock();
		when(refIdAttr.getIdentifier()).thenReturn("refIdAttrId");
		EntityType refEntityType = when(mock(EntityType.class).getId()).thenReturn("refEntity").getMock();
		when(refEntityType.getId()).thenReturn("refEntityId");
		when(refEntityType.getIdAttribute()).thenReturn(refIdAttr);

		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(entityType.getId()).thenReturn("entityTypeId");
		Attribute refAttr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(refAttr.getIdentifier()).thenReturn("refAttrId");
		when(refAttr.getDataType()).thenReturn(XREF);
		when(refAttr.getRefEntity()).thenReturn(refEntityType);

		String expectedSql = "ALTER TABLE \"entityTypeId#c34894ba\" ADD CONSTRAINT \"entityTypeId#c34894ba_attr_fkey\" FOREIGN KEY (\"attr\") REFERENCES \"refEntityId#07f902bf\"(\"refIdAttr\")";
		assertEquals(PostgreSqlQueryGenerator.getSqlCreateForeignKey(entityType, refAttr), expectedSql);
	}

	@Test
	public void getSqlCreateForeignKeySelfReferencing()
	{
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("idAttr").getMock();
		when(idAttr.getIdentifier()).thenReturn("idAttrId");
		when(idAttr.getDataType()).thenReturn(STRING);
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(entityType.getId()).thenReturn("entityTypeId");
		when(entityType.getIdAttribute()).thenReturn(idAttr);

		Attribute refAttr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(refAttr.getIdentifier()).thenReturn("refAttrId");
		when(refAttr.getDataType()).thenReturn(XREF);
		when(refAttr.getRefEntity()).thenReturn(entityType);

		String expectedSql = "ALTER TABLE \"entityTypeId#c34894ba\" ADD CONSTRAINT \"entityTypeId#c34894ba_attr_fkey\" FOREIGN KEY (\"attr\") REFERENCES \"entityTypeId#c34894ba\"(\"idAttr\") DEFERRABLE INITIALLY DEFERRED";
		assertEquals(PostgreSqlQueryGenerator.getSqlCreateForeignKey(entityType, refAttr), expectedSql);
	}

	@Test
	public void getSqlDropForeignKey()
	{
		Attribute refIdAttr = when(mock(Attribute.class).getName()).thenReturn("refIdAttr").getMock();
		when(refIdAttr.getIdentifier()).thenReturn("refIdAttrId");
		EntityType refEntityType = when(mock(EntityType.class).getId()).thenReturn("refEntity").getMock();
		when(refEntityType.getId()).thenReturn("refEntityId");
		when(refEntityType.getIdAttribute()).thenReturn(refIdAttr);

		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(entityType.getId()).thenReturn("entityTypeId");
		Attribute refAttr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(refAttr.getIdentifier()).thenReturn("refAttrId");
		when(refAttr.getDataType()).thenReturn(XREF);
		when(refAttr.getRefEntity()).thenReturn(refEntityType);

		String expectedSql = "ALTER TABLE \"entityTypeId#c34894ba\" DROP CONSTRAINT \"entityTypeId#c34894ba_attr_fkey\"";
		assertEquals(PostgreSqlQueryGenerator.getSqlDropForeignKey(entityType, refAttr), expectedSql);
	}

	@Test
	public void getSqlCreateUniqueKey()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(entityType.getId()).thenReturn("entityTypeId");
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		when(attr.getDataType()).thenReturn(STRING);

		String expectedSql = "ALTER TABLE \"entityTypeId#c34894ba\" ADD CONSTRAINT \"entityTypeId#c34894ba_attr_key\" UNIQUE (\"attr\")";
		assertEquals(PostgreSqlQueryGenerator.getSqlCreateUniqueKey(entityType, attr), expectedSql);
	}

	@Test
	public void getSqlDropUniqueKey()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(entityType.getId()).thenReturn("entityTypeId");
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		when(attr.getDataType()).thenReturn(STRING);

		String expectedSql = "ALTER TABLE \"entityTypeId#c34894ba\" DROP CONSTRAINT \"entityTypeId#c34894ba_attr_key\"";
		assertEquals(PostgreSqlQueryGenerator.getSqlDropUniqueKey(entityType, attr), expectedSql);
	}

	@Test
	public void getSqlCreateCheckConstraint()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(entityType.getId()).thenReturn("entityTypeId");
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		when(attr.getDataType()).thenReturn(ENUM);
		when(attr.getEnumOptions()).thenReturn(newArrayList("enum0", "enum1", "enum2"));
		assertEquals(PostgreSqlQueryGenerator.getSqlCreateCheckConstraint(entityType, attr),
				"ALTER TABLE \"entityTypeId#c34894ba\" ADD CONSTRAINT \"entityTypeId#c34894ba_attr_chk\" CHECK (\"attr\" IN ('enum0','enum1','enum2'))");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void getSqlCreateCheckConstraintWrongDataType()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(entityType.getId()).thenReturn("entityTypeId");
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		when(attr.getDataType()).thenReturn(STRING);
		PostgreSqlQueryGenerator.getSqlCreateCheckConstraint(entityType, attr);
	}

	@Test
	public void getSqlDropCheckConstraint()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(entityType.getId()).thenReturn("entityTypeId");
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		when(attr.getDataType()).thenReturn(ENUM);
		when(attr.getEnumOptions()).thenReturn(newArrayList("enum0", "enum1", "enum2"));
		assertEquals(PostgreSqlQueryGenerator.getSqlDropCheckConstraint(entityType, attr),
				"ALTER TABLE \"entityTypeId#c34894ba\" DROP CONSTRAINT \"entityTypeId#c34894ba_attr_chk\"");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void getSqlDropCheckConstraintWrongDataType()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(entityType.getId()).thenReturn("entityTypeId");
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		when(attr.getDataType()).thenReturn(STRING);
		PostgreSqlQueryGenerator.getSqlDropCheckConstraint(entityType, attr);
	}

	@Test
	public void getSqlCreateJunctionTable()
	{
		Attribute refIdAttr = when(mock(Attribute.class).getName()).thenReturn("refIdAttr").getMock();
		when(refIdAttr.getIdentifier()).thenReturn("refIdAttrId");
		when(refIdAttr.getDataType()).thenReturn(STRING);
		EntityType refEntityType = when(mock(EntityType.class).getId()).thenReturn("refEntity").getMock();
		when(refEntityType.getId()).thenReturn("refEntityId");
		when(refEntityType.getIdAttribute()).thenReturn(refIdAttr);

		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(entityType.getId()).thenReturn("entityTypeId");
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("idAttr").getMock();
		when(idAttr.getIdentifier()).thenReturn("idAttrId");
		when(idAttr.getDataType()).thenReturn(STRING);
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		when(attr.getDataType()).thenReturn(MREF);
		when(attr.getRefEntity()).thenReturn(refEntityType);

		String expectedSql = "CREATE TABLE \"entityTypeId#c34894ba_attr\" (\"order\" INT,\"idAttr\" character varying(255) NOT NULL, \"attr\" character varying(255) NOT NULL, FOREIGN KEY (\"idAttr\") REFERENCES \"entityTypeId#c34894ba\"(\"idAttr\") ON DELETE CASCADE, FOREIGN KEY (\"attr\") REFERENCES \"refEntityId#07f902bf\"(\"refIdAttr\"), UNIQUE (\"idAttr\",\"attr\"), UNIQUE (\"order\",\"idAttr\"))";
		assertEquals(PostgreSqlQueryGenerator.getSqlCreateJunctionTable(entityType, attr), expectedSql);
	}

	@Test
	public void getSqlCreateJunctionTableSelfReferencing()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(entityType.getId()).thenReturn("entityTypeId");
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("idAttr").getMock();
		when(idAttr.getIdentifier()).thenReturn("idAttrId");
		when(idAttr.getDataType()).thenReturn(STRING);
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		when(attr.getDataType()).thenReturn(MREF);
		when(attr.getRefEntity()).thenReturn(entityType);

		String expectedSql = "CREATE TABLE \"entityTypeId#c34894ba_attr\" (\"order\" INT,\"idAttr\" character varying(255) NOT NULL, \"attr\" character varying(255) NOT NULL, FOREIGN KEY (\"idAttr\") REFERENCES \"entityTypeId#c34894ba\"(\"idAttr\") ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED, FOREIGN KEY (\"attr\") REFERENCES \"entityTypeId#c34894ba\"(\"idAttr\") DEFERRABLE INITIALLY DEFERRED, UNIQUE (\"idAttr\",\"attr\"), UNIQUE (\"order\",\"idAttr\"))";
		assertEquals(PostgreSqlQueryGenerator.getSqlCreateJunctionTable(entityType, attr), expectedSql);
	}

	@Test
	public void getJunctionTableSelect()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(entityType.getId()).thenReturn("entityTypeId");
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("idAttr").getMock();
		when(idAttr.getIdentifier()).thenReturn("idAttrId");
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		when(attr.getDataType()).thenReturn(MREF);
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		assertEquals(PostgreSqlQueryGenerator.getSqlJunctionTableSelect(entityType, attr, 3),
				"SELECT \"idAttr\",\"order\",\"attr\" FROM \"entityTypeId#c34894ba_attr\" WHERE \"idAttr\" in (?, ?, ?) ORDER BY \"idAttr\",\"order\"");
	}

	@Test
	public void getSqlInsertJunction()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(entityType.getId()).thenReturn("entityTypeId");
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("idAttr").getMock();
		when(idAttr.getIdentifier()).thenReturn("idAttrId");
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		when(attr.getDataType()).thenReturn(MREF);
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		assertEquals(PostgreSqlQueryGenerator.getSqlInsertJunction(entityType, attr),
				"INSERT INTO \"entityTypeId#c34894ba_attr\" (\"order\",\"idAttr\",\"attr\") VALUES (?,?,?)");
	}

	@Test
	public void getSqlInsertJunctionInversedBy()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(entityType.getId()).thenReturn("entityTypeId");
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("idAttr").getMock();
		when(idAttr.getIdentifier()).thenReturn("idAttrId");
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		when(attr.getDataType()).thenReturn(XREF);
		when(attr.isInversedBy()).thenReturn(true);
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		assertEquals(PostgreSqlQueryGenerator.getSqlInsertJunction(entityType, attr),
				"INSERT INTO \"entityTypeId#c34894ba_attr\" (\"order\",\"idAttr\",\"attr\") VALUES (?,?,?)");
	}

	@Test
	public void getSqlSelectXref()
	{
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		when(attr.getDataType()).thenReturn(XREF);
		when(attr.isInversedBy()).thenReturn(false);

		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("idAttr").getMock();
		when(idAttr.getIdentifier()).thenReturn("idAttrId");
		when(idAttr.getDataType()).thenReturn(STRING);
		when(idAttr.isUnique()).thenReturn(true);

		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(entityType.getId()).thenReturn("entityTypeId");
		when(entityType.getAtomicAttributes()).thenReturn(newArrayList(idAttr, attr));
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		when(entityType.getAttribute("attr")).thenReturn(attr);
		when(entityType.getAttribute("idAttr")).thenReturn(idAttr);

		@SuppressWarnings("unchecked")
		Query<Entity> q = mock(Query.class);
		List<Object> parameters = Lists.newArrayList();
		assertEquals(PostgreSqlQueryGenerator.getSqlSelect(entityType, q, parameters, true),
				"SELECT this.\"idAttr\", this.\"attr\" FROM \"entityTypeId#c34894ba\" AS this ORDER BY \"idAttr\" ASC");
		assertEquals(parameters, emptyList());
	}

	@Test
	public void getSqlSelectXrefInversed()
	{
		Attribute refAttr = when(mock(Attribute.class).getName()).thenReturn("refAttr").getMock();
		when(refAttr.getIdentifier()).thenReturn("refAttrId");
		when(refAttr.getDataType()).thenReturn(ONE_TO_MANY);

		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		when(attr.getDataType()).thenReturn(XREF);
		when(attr.isInversedBy()).thenReturn(true);
		when(attr.getInversedBy()).thenReturn(refAttr);

		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("idAttr").getMock();
		when(idAttr.getIdentifier()).thenReturn("idAttrId");
		when(idAttr.getDataType()).thenReturn(STRING);
		when(idAttr.isUnique()).thenReturn(true);

		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(entityType.getId()).thenReturn("entityTypeId");
		when(entityType.getAtomicAttributes()).thenReturn(newArrayList(idAttr, attr));
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		when(entityType.getAttribute("attr")).thenReturn(attr);
		when(entityType.getAttribute("idAttr")).thenReturn(idAttr);

		@SuppressWarnings("unchecked")
		Query<Entity> q = mock(Query.class);
		List<Object> parameters = Lists.newArrayList();
		assertEquals(PostgreSqlQueryGenerator.getSqlSelect(entityType, q, parameters, true),
				"SELECT this.\"idAttr\", this.\"attr\" FROM \"entityTypeId#c34894ba\" AS this ORDER BY \"idAttr\" ASC");
		assertEquals(parameters, emptyList());
	}

	@DataProvider(name = "getSqlSelectOneToManyMappedByProvider")
	public Iterator<Object[]> getSqlSelectOneToManyMappedByProvider()
	{
		List<Object[]> dataList = new ArrayList<>();
		dataList.add(new Object[] { new QueryImpl<>(),
				"SELECT this.\"idAttr\", (SELECT array_agg(\"refIdAttr\" ORDER BY \"refIdAttr\" ASC) FROM \"refEntityId#07f902bf\" WHERE this.\"idAttr\" = \"refEntityId#07f902bf\".\"refAttr\") AS \"attr\" FROM \"entityTypeId#c34894ba\" AS this ORDER BY \"idAttr\" ASC",
				emptyList() });
		dataList.add(new Object[] { new QueryImpl<>().in("attr", asList("ref0", "ref1")),
				"SELECT DISTINCT this.\"idAttr\", (SELECT array_agg(\"refIdAttr\" ORDER BY \"refIdAttr\" ASC) FROM \"refEntityId#07f902bf\" WHERE this.\"idAttr\" = \"refEntityId#07f902bf\".\"refAttr\") AS \"attr\" FROM \"entityTypeId#c34894ba\" AS this LEFT JOIN \"refEntityId#07f902bf\" AS \"attr_filter1\" ON (this.\"idAttr\" = \"attr_filter1\".\"refAttr\") WHERE \"attr_filter1\".\"refIdAttr\" IN (?,?) ORDER BY \"idAttr\" ASC",
				asList("ref0", "ref1") });
		Query<Entity> sortQuery = new QueryImpl<>();
		sortQuery.sort(new Sort("attr"));
		dataList.add(new Object[] { sortQuery,
				"SELECT this.\"idAttr\", (SELECT array_agg(\"refIdAttr\" ORDER BY \"refIdAttr\" ASC) FROM \"refEntityId#07f902bf\" WHERE this.\"idAttr\" = \"refEntityId#07f902bf\".\"refAttr\") AS \"attr\" FROM \"entityTypeId#c34894ba\" AS this ORDER BY \"attr\" ASC, \"idAttr\" ASC",
				emptyList() });
		return dataList.iterator();
	}

	@Test(dataProvider = "getSqlSelectOneToManyMappedByProvider")
	public void getSqlSelectOneToManyMappedBy(Query<Entity> query, String expectedSql, List<Object> expectedParameters)
	{
		Attribute refIdAttr = when(mock(Attribute.class).getName()).thenReturn("refIdAttr").getMock();
		when(refIdAttr.getIdentifier()).thenReturn("refIdAttrId");
		when(refIdAttr.getDataType()).thenReturn(STRING);
		when(refIdAttr.isUnique()).thenReturn(true);

		EntityType refEntityType = when(mock(EntityType.class).getId()).thenReturn("refEntity").getMock();
		when(refEntityType.getId()).thenReturn("refEntityId");
		when(refEntityType.getIdAttribute()).thenReturn(refIdAttr);
		when(refEntityType.getAttribute("refIdAttr")).thenReturn(refIdAttr);
		Attribute refAttr = when(mock(Attribute.class).getName()).thenReturn("refAttr").getMock();
		when(refAttr.getIdentifier()).thenReturn("refAttrId");
		when(refAttr.getDataType()).thenReturn(XREF);

		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		when(attr.getDataType()).thenReturn(ONE_TO_MANY);
		when(attr.isMappedBy()).thenReturn(true);
		when(attr.getMappedBy()).thenReturn(refAttr);
		when(attr.getRefEntity()).thenReturn(refEntityType);

		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("idAttr").getMock();
		when(idAttr.getIdentifier()).thenReturn("idAttrId");
		when(idAttr.getDataType()).thenReturn(STRING);
		when(idAttr.isUnique()).thenReturn(true);

		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(entityType.getId()).thenReturn("entityTypeId");
		when(entityType.getAttribute("attr")).thenReturn(attr);
		when(entityType.getAttribute("idAttr")).thenReturn(idAttr);
		when(entityType.getAtomicAttributes()).thenReturn(newArrayList(idAttr, attr));
		when(entityType.getIdAttribute()).thenReturn(idAttr);

		List<Object> parameters = new ArrayList<>();
		assertEquals(PostgreSqlQueryGenerator.getSqlSelect(entityType, query, parameters, true), expectedSql);
		assertEquals(parameters, expectedParameters);
	}

	@Test
	public void getSqlSelectMref()
	{
		Package package_ = when(mock(Package.class).getId()).thenReturn("org_molgenis").getMock();

		Attribute ref1IdAttr = when(mock(Attribute.class).getName()).thenReturn("ref1Id").getMock();
		when(ref1IdAttr.getIdentifier()).thenReturn("ref1IdAttrId");
		EntityType ref1Meta = when(mock(EntityType.class).getId()).thenReturn("Ref1").getMock();
		when(ref1Meta.getId()).thenReturn("ref1Id");
		when(ref1Meta.getIdAttribute()).thenReturn(ref1IdAttr);

		Attribute ref2IdAttr = when(mock(Attribute.class).getName()).thenReturn("ref2Id").getMock();
		when(ref2IdAttr.getIdentifier()).thenReturn("ref2IdAttrId");
		EntityType ref2Meta = when(mock(EntityType.class).getId()).thenReturn("Ref2").getMock();
		when(ref2Meta.getId()).thenReturn("ref2Id");
		when(ref2Meta.getIdAttribute()).thenReturn(ref2IdAttr);

		Attribute masterIdAttr = when(mock(Attribute.class).getName()).thenReturn("masterId").getMock();
		when(masterIdAttr.getIdentifier()).thenReturn("masterIdAttrId");
		when(masterIdAttr.getDataType()).thenReturn(STRING);
		Attribute mref1Attr = when(mock(Attribute.class).getName()).thenReturn("mref1").getMock();
		when(mref1Attr.getIdentifier()).thenReturn("mref1AttrId");
		when(mref1Attr.getDataType()).thenReturn(MREF);
		when(mref1Attr.getRefEntity()).thenReturn(ref1Meta);
		Attribute mref2Attr = when(mock(Attribute.class).getName()).thenReturn("mref2").getMock();
		when(mref2Attr.getIdentifier()).thenReturn("mref2AttrId");
		when(mref2Attr.getDataType()).thenReturn(MREF);
		when(mref2Attr.getRefEntity()).thenReturn(ref2Meta);

		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("org_molgenis_MasterEntity").getMock();
		when(entityType.getId()).thenReturn("entityTypeId");
		when(entityType.getPackage()).thenReturn(package_);
		when(entityType.getIdAttribute()).thenReturn(masterIdAttr);
		when(entityType.getAttribute("masterId")).thenReturn(masterIdAttr);
		when(entityType.getAttribute("mref1")).thenReturn(mref1Attr);
		when(entityType.getAttribute("mref2")).thenReturn(mref2Attr);
		when(entityType.getAtomicAttributes()).thenReturn(asList(masterIdAttr, mref1Attr, mref2Attr));

		QueryImpl<Entity> q = new QueryImpl<>();

		List<Object> parameters = Lists.newArrayList();

		String sqlSelect = PostgreSqlQueryGenerator.getSqlSelect(entityType, q, parameters, true);
		assertEquals(sqlSelect,
				"SELECT this.\"masterId\", (SELECT array_agg(DISTINCT ARRAY[\"mref1\".\"order\"::TEXT,\"mref1\".\"mref1\"::TEXT]) FROM \"entityTypeId#c34894ba_mref1\" AS \"mref1\" WHERE this.\"masterId\" = \"mref1\".\"masterId\") AS \"mref1\", (SELECT array_agg(DISTINCT ARRAY[\"mref2\".\"order\"::TEXT,\"mref2\".\"mref2\"::TEXT]) FROM \"entityTypeId#c34894ba_mref2\" AS \"mref2\" WHERE this.\"masterId\" = \"mref2\".\"masterId\") AS \"mref2\" FROM \"entityTypeId#c34894ba\" AS this ORDER BY \"masterId\" ASC");
	}

	@Test
	public void getSqlSortNoSort()
	{
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("idAttr").getMock();
		when(idAttr.getIdentifier()).thenReturn("idAttrId");
		when(idAttr.getDataType()).thenReturn(STRING);

		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(entityType.getId()).thenReturn("entityTypeId");
		when(entityType.getAtomicAttributes()).thenReturn(newArrayList(idAttr));
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		when(entityType.getAttribute("idAttr")).thenReturn(idAttr);

		@SuppressWarnings("unchecked")
		Query<Entity> q = mock(Query.class);
		when(q.getOffset()).thenReturn(10);
		when(q.getPageSize()).thenReturn(5);
		assertEquals(PostgreSqlQueryGenerator.getSqlSort(entityType, q), "ORDER BY \"idAttr\" ASC");
	}

	@Test
	public void getSqlSortSortWithUniqueAttribute()
	{
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("idAttr").getMock();
		when(idAttr.getIdentifier()).thenReturn("idAttrId");
		when(idAttr.getDataType()).thenReturn(STRING);
		when(idAttr.isUnique()).thenReturn(true);

		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		when(attr.getDataType()).thenReturn(STRING);
		when(attr.isUnique()).thenReturn(true);

		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(entityType.getId()).thenReturn("entityTypeId");
		when(entityType.getAtomicAttributes()).thenReturn(newArrayList(idAttr, attr));
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		when(entityType.getAttribute("idAttr")).thenReturn(idAttr);
		when(entityType.getAttribute("attr")).thenReturn(attr);

		@SuppressWarnings("unchecked")
		Query<Entity> q = mock(Query.class);
		Sort sort = new Sort().on("attr");
		when(q.getSort()).thenReturn(sort);
		assertEquals(PostgreSqlQueryGenerator.getSqlSort(entityType, q), "ORDER BY \"attr\" ASC");
	}

	@Test
	public void getSqlSortSortWithoutUniqueAttribute()
	{
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("idAttr").getMock();
		when(idAttr.getIdentifier()).thenReturn("idAttrId");
		when(idAttr.getDataType()).thenReturn(STRING);
		when(idAttr.isUnique()).thenReturn(true);

		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		when(attr.getDataType()).thenReturn(STRING);

		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(entityType.getId()).thenReturn("entityTypeId");
		when(entityType.getAtomicAttributes()).thenReturn(newArrayList(idAttr, attr));
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		when(entityType.getAttribute("idAttr")).thenReturn(idAttr);
		when(entityType.getAttribute("attr")).thenReturn(attr);

		@SuppressWarnings("unchecked")
		Query<Entity> q = mock(Query.class);
		Sort sort = new Sort().on("attr");
		when(q.getSort()).thenReturn(sort);
		assertEquals(PostgreSqlQueryGenerator.getSqlSort(entityType, q), "ORDER BY \"attr\" ASC, \"idAttr\" ASC");
	}

	@Test
	public void getSqlSortOnUnselectedMref()
	{
		Package package_ = when(mock(Package.class).getId()).thenReturn("org_molgenis").getMock();

		Attribute ref1IdAttr = when(mock(Attribute.class).getName()).thenReturn("ref1Id").getMock();
		when(ref1IdAttr.getIdentifier()).thenReturn("ref1IdAttrId");
		EntityType ref1Meta = when(mock(EntityType.class).getId()).thenReturn("Ref1").getMock();
		when(ref1Meta.getId()).thenReturn("ref1Id");
		when(ref1Meta.getIdAttribute()).thenReturn(ref1IdAttr);

		Attribute ref2IdAttr = when(mock(Attribute.class).getName()).thenReturn("ref2Id").getMock();
		when(ref2IdAttr.getIdentifier()).thenReturn("ref2IdAttrId");
		EntityType ref2Meta = when(mock(EntityType.class).getId()).thenReturn("Ref2").getMock();
		when(ref2Meta.getId()).thenReturn("ref2Id");
		when(ref2Meta.getIdAttribute()).thenReturn(ref2IdAttr);

		Attribute masterIdAttr = when(mock(Attribute.class).getName()).thenReturn("masterId").getMock();
		when(masterIdAttr.getIdentifier()).thenReturn("masterIdAttrId");
		when(masterIdAttr.getDataType()).thenReturn(STRING);
		Attribute mref1Attr = when(mock(Attribute.class).getName()).thenReturn("mref1").getMock();
		when(mref1Attr.getIdentifier()).thenReturn("mref1AttrId");
		when(mref1Attr.getDataType()).thenReturn(MREF);
		when(mref1Attr.getRefEntity()).thenReturn(ref1Meta);
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("org_molgenis_MasterEntity").getMock();
		when(entityType.getId()).thenReturn("entityTypeId");
		when(entityType.getPackage()).thenReturn(package_);
		when(entityType.getIdAttribute()).thenReturn(masterIdAttr);
		when(entityType.getAttribute("masterId")).thenReturn(masterIdAttr);
		when(entityType.getAttribute("mref1")).thenReturn(mref1Attr);
		when(entityType.getAtomicAttributes()).thenReturn(asList(masterIdAttr, mref1Attr));

		Fetch fetch = new Fetch().field("masterId");
		Sort sort = new Sort("mref1");

		QueryImpl<Entity> q = new QueryImpl<>();
		q.setFetch(fetch);
		q.setSort(sort);

		List<Object> parameters = Lists.newArrayList();

		String sqlSelect = PostgreSqlQueryGenerator.getSqlSelect(entityType, q, parameters, true);
		assertEquals(sqlSelect,
				"SELECT this.\"masterId\", (SELECT array_agg(DISTINCT ARRAY[\"mref1\".\"order\"::TEXT,\"mref1\".\"mref1\"::TEXT]) FROM \"entityTypeId#c34894ba_mref1\" AS \"mref1\" WHERE this.\"masterId\" = \"mref1\".\"masterId\") AS \"mref1\" FROM \"entityTypeId#c34894ba\" AS this ORDER BY \"mref1\" ASC, \"masterId\" ASC");
	}

	@DataProvider(name = "getSqlAddColumnProvider")
	public static Iterator<Object[]> getSqlAddColumnProvider()
	{
		// ref entity with string id attribute
		Attribute refIdAttrStr = mock(Attribute.class);
		when(refIdAttrStr.getIdentifier()).thenReturn("refIdAttrId");
		when(refIdAttrStr.getName()).thenReturn("refIdAttrStr");
		when(refIdAttrStr.getDataType()).thenReturn(STRING);
		EntityType refEntityTypeString = mock(EntityType.class);
		when(refEntityTypeString.getId()).thenReturn("refEntityStrId");
		when(refEntityTypeString.toString()).thenReturn("refEntityStr");
		when(refEntityTypeString.getId()).thenReturn("refEntityStr");
		when(refEntityTypeString.getIdAttribute()).thenReturn(refIdAttrStr);

		// ref entity with int id attribute
		Attribute refIdAttrInt = mock(Attribute.class);
		when(refIdAttrInt.getIdentifier()).thenReturn("refIdAttrIntId");
		when(refIdAttrInt.getName()).thenReturn("refIdAttrInt");
		when(refIdAttrInt.getDataType()).thenReturn(INT);
		EntityType refEntityTypeInt = mock(EntityType.class);
		when(refEntityTypeInt.toString()).thenReturn("refEntityInt");
		when(refEntityTypeInt.getId()).thenReturn("refEntityInt");
		when(refEntityTypeInt.getIdAttribute()).thenReturn(refIdAttrInt);

		return Arrays.asList(new Object[] { BOOL, null, EXCLUDE_DEFAULT_CONSTRAINT, true, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" boolean" },
				new Object[] { CATEGORICAL, null, EXCLUDE_DEFAULT_CONSTRAINT, true, refEntityTypeInt,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" integer,ADD CONSTRAINT \"entityTypeId#c34894ba_attr_fkey\" FOREIGN KEY (\"attr\") REFERENCES \"refEntityInt#78255ee1\"(\"refIdAttrInt\")" },
				new Object[] { DATE, null, EXCLUDE_DEFAULT_CONSTRAINT, true, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" date" },
				new Object[] { DATE_TIME, null, EXCLUDE_DEFAULT_CONSTRAINT, true, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" timestamp with time zone" },
				new Object[] { DECIMAL, null, EXCLUDE_DEFAULT_CONSTRAINT, true, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" double precision" },
				new Object[] { EMAIL, null, EXCLUDE_DEFAULT_CONSTRAINT, true, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" character varying(255)" },
				new Object[] { ENUM, null, EXCLUDE_DEFAULT_CONSTRAINT, true, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" character varying(255),ADD CONSTRAINT \"entityTypeId#c34894ba_attr_chk\" CHECK (\"attr\" IN ('enum0, enum1'))" },
				new Object[] { FILE, null, EXCLUDE_DEFAULT_CONSTRAINT, true, refEntityTypeString,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" character varying(255),ADD CONSTRAINT \"entityTypeId#c34894ba_attr_fkey\" FOREIGN KEY (\"attr\") REFERENCES \"refEntityStr#305ca1a9\"(\"refIdAttrStr\")" },
				new Object[] { HTML, null, EXCLUDE_DEFAULT_CONSTRAINT, true, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" text" },
				new Object[] { HYPERLINK, null, EXCLUDE_DEFAULT_CONSTRAINT, true, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" character varying(255)" },
				new Object[] { INT, null, EXCLUDE_DEFAULT_CONSTRAINT, true, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" integer" },
				new Object[] { LONG, null, EXCLUDE_DEFAULT_CONSTRAINT, true, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" bigint" },
				new Object[] { SCRIPT, null, EXCLUDE_DEFAULT_CONSTRAINT, true, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" text" },
				new Object[] { STRING, null, EXCLUDE_DEFAULT_CONSTRAINT, true, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" character varying(255)" },
				new Object[] { TEXT, null, EXCLUDE_DEFAULT_CONSTRAINT, true, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" text" },
				new Object[] { XREF, null, EXCLUDE_DEFAULT_CONSTRAINT, true, refEntityTypeString,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" character varying(255),ADD CONSTRAINT \"entityTypeId#c34894ba_attr_fkey\" FOREIGN KEY (\"attr\") REFERENCES \"refEntityStr#305ca1a9\"(\"refIdAttrStr\")" },
				new Object[] { BOOL, null, EXCLUDE_DEFAULT_CONSTRAINT, false, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" boolean NOT NULL" },
				new Object[] { CATEGORICAL, null, EXCLUDE_DEFAULT_CONSTRAINT, false, refEntityTypeInt,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" integer NOT NULL,ADD CONSTRAINT \"entityTypeId#c34894ba_attr_fkey\" FOREIGN KEY (\"attr\") REFERENCES \"refEntityInt#78255ee1\"(\"refIdAttrInt\")" },
				new Object[] { DATE, null, EXCLUDE_DEFAULT_CONSTRAINT, false, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" date NOT NULL" },
				new Object[] { DATE_TIME, null, EXCLUDE_DEFAULT_CONSTRAINT, false, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" timestamp with time zone NOT NULL" },
				new Object[] { DECIMAL, null, EXCLUDE_DEFAULT_CONSTRAINT, false, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" double precision NOT NULL" },
				new Object[] { EMAIL, null, EXCLUDE_DEFAULT_CONSTRAINT, false, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" character varying(255) NOT NULL" },
				new Object[] { ENUM, null, EXCLUDE_DEFAULT_CONSTRAINT, false, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" character varying(255) NOT NULL,ADD CONSTRAINT \"entityTypeId#c34894ba_attr_chk\" CHECK (\"attr\" IN ('enum0, enum1'))" },
				new Object[] { FILE, null, EXCLUDE_DEFAULT_CONSTRAINT, false, refEntityTypeString,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" character varying(255) NOT NULL,ADD CONSTRAINT \"entityTypeId#c34894ba_attr_fkey\" FOREIGN KEY (\"attr\") REFERENCES \"refEntityStr#305ca1a9\"(\"refIdAttrStr\")" },
				new Object[] { HTML, null, EXCLUDE_DEFAULT_CONSTRAINT, false, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" text NOT NULL" },
				new Object[] { HYPERLINK, null, EXCLUDE_DEFAULT_CONSTRAINT, false, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" character varying(255) NOT NULL" },
				new Object[] { INT, null, EXCLUDE_DEFAULT_CONSTRAINT, false, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" integer NOT NULL" },
				new Object[] { LONG, null, EXCLUDE_DEFAULT_CONSTRAINT, false, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" bigint NOT NULL" },
				new Object[] { SCRIPT, null, EXCLUDE_DEFAULT_CONSTRAINT, false, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" text NOT NULL" },
				new Object[] { STRING, null, EXCLUDE_DEFAULT_CONSTRAINT, false, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" character varying(255) NOT NULL" },
				new Object[] { TEXT, null, EXCLUDE_DEFAULT_CONSTRAINT, false, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" text NOT NULL" },
				new Object[] { XREF, null, EXCLUDE_DEFAULT_CONSTRAINT, false, refEntityTypeString,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" character varying(255) NOT NULL,ADD CONSTRAINT \"entityTypeId#c34894ba_attr_fkey\" FOREIGN KEY (\"attr\") REFERENCES \"refEntityStr#305ca1a9\"(\"refIdAttrStr\")" },
				new Object[] { BOOL, Boolean.TRUE.toString(), INCLUDE_DEFAULT_CONSTRAINT, false, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" boolean NOT NULL DEFAULT TRUE" },
				new Object[] { CATEGORICAL, "1", INCLUDE_DEFAULT_CONSTRAINT, false, refEntityTypeInt,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" integer NOT NULL DEFAULT 1,ADD CONSTRAINT \"entityTypeId#c34894ba_attr_fkey\" FOREIGN KEY (\"attr\") REFERENCES \"refEntityInt#78255ee1\"(\"refIdAttrInt\")" },
				new Object[] { DATE, "2010-01-13", INCLUDE_DEFAULT_CONSTRAINT, false, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" date NOT NULL DEFAULT '2010-01-13'" },
				new Object[] { DATE_TIME, "1985-08-12T06:12:13Z", INCLUDE_DEFAULT_CONSTRAINT, false, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" timestamp with time zone NOT NULL DEFAULT '1985-08-12T06:12:13Z'" },
				new Object[] { DECIMAL, "3.14", INCLUDE_DEFAULT_CONSTRAINT, false, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" double precision NOT NULL DEFAULT 3.14" },
				new Object[] { EMAIL, "mail@molgenis.org", INCLUDE_DEFAULT_CONSTRAINT, false, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" character varying(255) NOT NULL DEFAULT 'mail@molgenis.org'" },
				new Object[] { ENUM, "enum1", INCLUDE_DEFAULT_CONSTRAINT, false, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" character varying(255) NOT NULL DEFAULT 'enum1',ADD CONSTRAINT \"entityTypeId#c34894ba_attr_chk\" CHECK (\"attr\" IN ('enum0, enum1'))" },
				new Object[] { FILE, "refEntityId", INCLUDE_DEFAULT_CONSTRAINT, false, refEntityTypeString,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" character varying(255) NOT NULL DEFAULT 'refEntityId',ADD CONSTRAINT \"entityTypeId#c34894ba_attr_fkey\" FOREIGN KEY (\"attr\") REFERENCES \"refEntityStr#305ca1a9\"(\"refIdAttrStr\")" },
				new Object[] { HTML, "html", INCLUDE_DEFAULT_CONSTRAINT, false, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" text NOT NULL DEFAULT 'html'" },
				new Object[] { HYPERLINK, "http://www.molgenis.org/", INCLUDE_DEFAULT_CONSTRAINT, false, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" character varying(255) NOT NULL DEFAULT 'http://www.molgenis.org/'" },
				new Object[] { INT, "123", INCLUDE_DEFAULT_CONSTRAINT, false, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" integer NOT NULL DEFAULT 123" },
				new Object[] { LONG, "34359738368", INCLUDE_DEFAULT_CONSTRAINT, false, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" bigint NOT NULL DEFAULT 34359738368" },
				new Object[] { SCRIPT, "script", INCLUDE_DEFAULT_CONSTRAINT, false, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" text NOT NULL DEFAULT 'script'" },
				new Object[] { STRING, "string", INCLUDE_DEFAULT_CONSTRAINT, false, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" character varying(255) NOT NULL DEFAULT 'string'" },
				new Object[] { TEXT, "text", INCLUDE_DEFAULT_CONSTRAINT, false, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" text NOT NULL DEFAULT 'text'" },
				new Object[] { XREF, "entityTypeId", INCLUDE_DEFAULT_CONSTRAINT, false, refEntityTypeString,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" character varying(255) NOT NULL DEFAULT 'entityTypeId',ADD CONSTRAINT \"entityTypeId#c34894ba_attr_fkey\" FOREIGN KEY (\"attr\") REFERENCES \"refEntityStr#305ca1a9\"(\"refIdAttrStr\")" },
				new Object[] { BOOL, Boolean.TRUE.toString(), EXCLUDE_DEFAULT_CONSTRAINT, false, null,
						"ALTER TABLE \"entityTypeId#c34894ba\" ADD \"attr\" boolean NOT NULL" }).iterator();
	}

	@Test(dataProvider = "getSqlAddColumnProvider")
	public void getSqlAddColumn(AttributeType attrType, String defaultValueStr, ColumnMode columnMode, boolean nillable,
			EntityType refEntityType, String sql)
	{
		Attribute idAttr = mock(Attribute.class);
		when(idAttr.getIdentifier()).thenReturn("idAttrId");

		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn("entityTypeId");

		when(entityType.getIdAttribute()).thenReturn(idAttr);

		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		when(attr.getDataType()).thenReturn(attrType);
		when(attr.isNillable()).thenReturn(nillable);
		when(attr.getRefEntity()).thenReturn(refEntityType);
		when(attr.getEnumOptions()).thenReturn(attrType == ENUM ? newArrayList("enum0, enum1") : emptyList());
		when(attr.getDefaultValue()).thenReturn(defaultValueStr);
		assertEquals(PostgreSqlQueryGenerator.getSqlAddColumn(entityType, attr, columnMode), sql);
	}

	@DataProvider(name = "getSqlAddColumnInvalidType")
	public static Iterator<Object[]> getSqlAddColumnInvalidTypeProvider()
	{
		return Arrays.asList(new Object[] { COMPOUND, ColumnMode.EXCLUDE_DEFAULT_CONSTRAINT },
				new Object[] { CATEGORICAL_MREF, ColumnMode.EXCLUDE_DEFAULT_CONSTRAINT },
				new Object[] { MREF, ColumnMode.EXCLUDE_DEFAULT_CONSTRAINT },
				new Object[] { COMPOUND, ColumnMode.INCLUDE_DEFAULT_CONSTRAINT },
				new Object[] { CATEGORICAL_MREF, ColumnMode.INCLUDE_DEFAULT_CONSTRAINT },
				new Object[] { MREF, ColumnMode.INCLUDE_DEFAULT_CONSTRAINT }).iterator();
	}

	@Test(dataProvider = "getSqlAddColumnInvalidType", expectedExceptions = RuntimeException.class)
	public void getSqlAddColumnInvalidType(AttributeType attrType, ColumnMode columnMode)
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getDataType()).thenReturn(attrType);
		PostgreSqlQueryGenerator.getSqlAddColumn(entityType, attr, columnMode);
	}

	@Test
	public void getSqlDropColumn()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(entityType.getId()).thenReturn("entityTypeId");
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		assertEquals(PostgreSqlQueryGenerator.getSqlDropColumn(entityType, attr),
				"ALTER TABLE \"entityTypeId#c34894ba\" DROP COLUMN \"attr\"");
	}

	@Test
	public void getSqlDropColumnDefault()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(entityType.getId()).thenReturn("entityTypeId");
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		assertEquals(PostgreSqlQueryGenerator.getSqlDropColumnDefault(entityType, attr),
				"ALTER TABLE \"entityTypeId#c34894ba\" ALTER COLUMN \"attr\" DROP DEFAULT");
	}

	@DataProvider(name = "generateSqlColumnDefaultConstraint")
	public static Iterator<Object[]> generateSqlColumnDefaultConstraintProvider()
	{
		return Arrays.asList(new Object[] { null, STRING, false }, new Object[] { null, XREF, false },
				new Object[] { "str", STRING, true }, new Object[] { "refEntityId", XREF, true }).iterator();
	}

	@Test(dataProvider = "generateSqlColumnDefaultConstraint")
	public void generateSqlColumnDefaultConstraint(String defaultValue, AttributeType attributeType,
			boolean expectedResult)
	{
		Attribute attribute = mock(Attribute.class);
		when(attribute.getDefaultValue()).thenReturn(defaultValue);
		when(attribute.getDataType()).thenReturn(attributeType);
		assertEquals(PostgreSqlQueryGenerator.generateSqlColumnDefaultConstraint(attribute), expectedResult);
	}

	@Test
	public void getSqlCreateJunctionTableIndex()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(entityType.getId()).thenReturn("entityTypeId");
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		Attribute idxAttr = when(mock(Attribute.class).getName()).thenReturn("idAttr").getMock();
		when(idxAttr.getIdentifier()).thenReturn("idAttrId");
		when(entityType.getIdAttribute()).thenReturn(idxAttr);
		assertEquals(PostgreSqlQueryGenerator.getSqlCreateJunctionTableIndex(entityType, attr),
				"CREATE INDEX \"entityType#c34894ba_attr_idAttr_idx\" ON \"entityTypeId#c34894ba_attr\" (\"idAttr\")");
	}

	@Test
	public void getSqlFrom()
	{
		Package eric = createPackage("eu_bbmri_eric");
		Attribute collectionsIdAttribute = createIdAttribute("collectionsId");
		EntityType collectionsEntity = createMockEntityWithIdAttribute("eu_bbmri_eric_collections",
				collectionsIdAttribute, "collectionsId");
		Attribute typeIdAttribute = createIdAttribute("typeId");
		EntityType typeEntity = createMockEntityWithIdAttribute("eu_bbmri_eric_type", typeIdAttribute, "typeId");
		Attribute categoryIdAttribute = createIdAttribute("categoryId");
		EntityType categoryEntity = createMockEntityWithIdAttribute("eu_bbmri_eric_category", categoryIdAttribute,
				"categoryId");

		Attribute typeAttribute = createMrefAttribute("type", typeEntity);
		Attribute categoryAttribute = createMrefAttribute("category", categoryEntity);

		when(collectionsEntity.getPackage()).thenReturn(eric);
		when(collectionsEntity.getAttribute("type")).thenReturn(typeAttribute);
		when(collectionsEntity.getAttribute("category")).thenReturn(categoryAttribute);
		when(collectionsEntity.getAtomicAttributes()).thenReturn(
				asList(collectionsIdAttribute, typeAttribute, categoryAttribute));

		QueryImpl<Entity> q = new QueryImpl<>();

		List<Object> parameters = Lists.newArrayList();

		String sqlSelect = PostgreSqlQueryGenerator.getSqlSelect(collectionsEntity, q, parameters, true);
		assertEquals(sqlSelect,
				"SELECT this.\"collectionsId\", (SELECT array_agg(DISTINCT ARRAY[\"type\".\"order\"::TEXT,\"type\".\"type\"::TEXT]) FROM \"eu_bbmri_eric_collecti#4dc023e6_type\" AS \"type\" WHERE this.\"collectionsId\" = \"type\".\"collectionsId\") AS \"type\", (SELECT array_agg(DISTINCT ARRAY[\"category\".\"order\"::TEXT,\"category\".\"category\"::TEXT]) FROM \"eu_bbmri_eric_collecti#4dc023e6_category\" AS \"category\" WHERE this.\"collectionsId\" = \"category\".\"collectionsId\") AS \"category\" FROM \"eu_bbmri_eric_collections#4dc023e6\" AS this ORDER BY \"collectionsId\" ASC");
	}

	@Test
	public void getSqlWhere()
	{
		Package eric = createPackage("eu_bbmri_eric");
		Attribute collectionsIdAttribute = createIdAttribute("collectionsId");
		EntityType collectionsEntity = createMockEntityWithIdAttribute("eu_bbmri_eric_collections",
				collectionsIdAttribute, "collectionsId");
		Attribute typeIdAttribute = createIdAttribute("typeId");
		EntityType typeEntity = createMockEntityWithIdAttribute("eu_bbmri_eric_type", typeIdAttribute, "typeId");
		Attribute categoryIdAttribute = createIdAttribute("categoryId");
		EntityType categoryEntity = createMockEntityWithIdAttribute("eu_bbmri_eric_category", categoryIdAttribute,
				"categoryId");

		Attribute typeAttribute = createMrefAttribute("type", typeEntity);
		Attribute categoryAttribute = createMrefAttribute("data_categories", categoryEntity);

		when(collectionsEntity.getPackage()).thenReturn(eric);
		when(collectionsEntity.getAttribute("type")).thenReturn(typeAttribute);
		when(collectionsEntity.getAttribute("data_categories")).thenReturn(categoryAttribute);
		when(collectionsEntity.getAtomicAttributes()).thenReturn(
				asList(collectionsIdAttribute, typeAttribute, categoryAttribute));

		QueryRule typeRule = new QueryRule(NESTED,
				newArrayList(new QueryRule("type", EQUALS, "POPULATION_BASED"), new QueryRule(OR),
						new QueryRule("type", EQUALS, "QUALITY_CONTROL")));
		QueryRule categoryRule = new QueryRule(NESTED,
				newArrayList(new QueryRule("data_categories", EQUALS, "MEDICAL_RECORDS"), new QueryRule(OR),
						new QueryRule("data_categories", EQUALS, "NATIONAL_REGISTRIES")));

		QueryImpl<Entity> q = new QueryImpl<>(
				new QueryRule(NESTED, newArrayList(newArrayList(typeRule, new QueryRule(AND), categoryRule))));

		List<Object> parameters = Lists.newArrayList();

		String sqlWhere = PostgreSqlQueryGenerator.getSqlWhere(collectionsEntity, q, parameters, new AtomicInteger());
		assertEquals(sqlWhere, "((\"type_filter1\".\"type\" = ?  OR \"type_filter2\".\"type\" = ?)" + " AND "
				+ "(\"data_categories_filter3\".\"data_categories\" = ?  OR \"data_categories_filter4\".\"data_categories\" = ?))");
	}

	private Attribute createIdAttribute(String idAttributeName)
	{
		final String idAttributeIdentifier = idAttributeName + "AttrId";
		Attribute idAttribute = when(mock(Attribute.class).getName()).thenReturn(idAttributeName).getMock();
		when(idAttribute.getIdentifier()).thenReturn(idAttributeIdentifier);
		when(idAttribute.getDataType()).thenReturn(STRING);
		return idAttribute;
	}

	private EntityType createMockEntityWithIdAttribute(String entityTypeId, Attribute idAttribute,
			String idAttributeName)
	{
		EntityType result = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
		when(result.getIdAttribute()).thenReturn(idAttribute);
		when(result.getAttribute(idAttributeName)).thenReturn(idAttribute);
		return result;
	}

	private Attribute createMrefAttribute(String attributeName, EntityType refEntityMeta)
	{
		String attributeIdentifier = attributeName + "AttrId";
		Attribute result = when(mock(Attribute.class).getName()).thenReturn(attributeName).getMock();
		when(result.getIdentifier()).thenReturn(attributeIdentifier);
		when(result.getDataType()).thenReturn(MREF);
		when(result.getRefEntity()).thenReturn(refEntityMeta);
		return result;
	}

	private Package createPackage(String packageName)
	{
		return when(mock(Package.class).getId()).thenReturn(packageName).getMock();
	}
}