package org.molgenis.data.postgresql;

import org.molgenis.data.*;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
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
import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.assertEquals;

public class PostgreSqlQueryGeneratorTest
{
	@Test
	public void getSqlSetNotNull()
	{
		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("entity").getMock();
		when(entityType.getFullyQualifiedName()).thenReturn("entityName");
		when(entityType.getName()).thenReturn("entity");
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		when(attr.isNillable()).thenReturn(true);
		assertEquals(PostgreSqlQueryGenerator.getSqlSetNotNull(entityType, attr),
				"ALTER TABLE \"entity#65a8f5cd\" ALTER COLUMN \"attr\" SET NOT NULL");
	}

	@Test
	public void getSqlDropNotNull()
	{
		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("entity").getMock();
		when(entityType.getFullyQualifiedName()).thenReturn("entityName");
		when(entityType.getName()).thenReturn("entity");
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		when(attr.isNillable()).thenReturn(false);
		assertEquals(PostgreSqlQueryGenerator.getSqlDropNotNull(entityType, attr),
				"ALTER TABLE \"entity#65a8f5cd\" ALTER COLUMN \"attr\" DROP NOT NULL");
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
		when(refEntityTypeString.getFullyQualifiedName()).thenReturn("refEntityStr");
		when(refEntityTypeString.getName()).thenReturn("refEntityStr");
		when(refEntityTypeString.getFullyQualifiedName()).thenReturn("refEntityStr");
		when(refEntityTypeString.getIdAttribute()).thenReturn(refIdAttrStr);

		// ref entity with int id attribute
		Attribute refIdAttrInt = mock(Attribute.class);
		when(refIdAttrInt.getIdentifier()).thenReturn("refIdAttrInt");
		when(refIdAttrInt.getName()).thenReturn("refIdAttrInt");
		when(refIdAttrInt.getDataType()).thenReturn(INT);
		EntityType refEntityTypeInt = mock(EntityType.class);
		when(refEntityTypeInt.getFullyQualifiedName()).thenReturn("refEntityInt");
		when(refEntityTypeInt.getName()).thenReturn("refEntityInt");
		when(refEntityTypeInt.getFullyQualifiedName()).thenReturn("refEntityInt");
		when(refEntityTypeInt.getIdAttribute()).thenReturn(refIdAttrInt);

		// entity with attributes of all types and flavors
		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("entity").getMock();
		when(entityType.getFullyQualifiedName()).thenReturn("entityName");
		when(entityType.getName()).thenReturn("entity");
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
				"CREATE TABLE \"entity#65a8f5cd\"(\"id\" character varying(255),\"bool\" boolean NOT NULL,\"categorical\" character varying(255) NOT NULL,\"date\" date NOT NULL,\"date_time\" timestamp NOT NULL,\"decimal\" double precision NOT NULL,\"email\" character varying(255) NOT NULL,\"enum\" character varying(255) NOT NULL,\"file\" integer NOT NULL,\"html\" text NOT NULL,\"hyperlink\" character varying(255) NOT NULL,\"int\" integer NOT NULL,\"long\" bigint NOT NULL,\"script\" text NOT NULL,\"string\" character varying(255) NOT NULL,\"text\" text NOT NULL,\"xref\" integer NOT NULL,\"bool_nillable\" boolean,\"categorical_nillable\" character varying(255),\"date_nillable\" date,\"date_time_nillable\" timestamp,\"decimal_nillable\" double precision,\"email_nillable\" character varying(255),\"enum_nillable\" character varying(255),\"file_nillable\" integer,\"html_nillable\" text,\"hyperlink_nillable\" character varying(255),\"int_nillable\" integer,\"long_nillable\" bigint,\"script_nillable\" text,\"string_nillable\" character varying(255),\"text_nillable\" text,\"xref_nillable\" integer,\"bool_unique\" boolean NOT NULL,\"categorical_unique\" character varying(255) NOT NULL,\"date_unique\" date NOT NULL,\"date_time_unique\" timestamp NOT NULL,\"decimal_unique\" double precision NOT NULL,\"email_unique\" character varying(255) NOT NULL,\"enum_unique\" character varying(255) NOT NULL,\"file_unique\" integer NOT NULL,\"html_unique\" text NOT NULL,\"hyperlink_unique\" character varying(255) NOT NULL,\"int_unique\" integer NOT NULL,\"long_unique\" bigint NOT NULL,\"script_unique\" text NOT NULL,\"string_unique\" character varying(255) NOT NULL,\"text_unique\" text NOT NULL,\"xref_unique\" integer NOT NULL,\"bool_unique_nillable\" boolean,\"categorical_unique_nillable\" character varying(255),\"date_unique_nillable\" date,\"date_time_unique_nillable\" timestamp,\"decimal_unique_nillable\" double precision,\"email_unique_nillable\" character varying(255),\"enum_unique_nillable\" character varying(255),\"file_unique_nillable\" integer,\"html_unique_nillable\" text,\"hyperlink_unique_nillable\" character varying(255),\"int_unique_nillable\" integer,\"long_unique_nillable\" bigint,\"script_unique_nillable\" text,\"string_unique_nillable\" character varying(255),\"text_unique_nillable\" text,\"xref_unique_nillable\" integer,CONSTRAINT \"entity#65a8f5cd_id_pkey\" PRIMARY KEY (\"id\"),CONSTRAINT \"entity#65a8f5cd_categorical_fkey\" FOREIGN KEY (\"categorical\") REFERENCES \"refEntityStr#305ca1a9\"(\"refIdAttrStr\"),CONSTRAINT \"entity#65a8f5cd_enum_chk\" CHECK (\"enum\" IN ('enum0','enum1')),CONSTRAINT \"entity#65a8f5cd_file_fkey\" FOREIGN KEY (\"file\") REFERENCES \"refEntityInt#78255ee1\"(\"refIdAttrInt\"),CONSTRAINT \"entity#65a8f5cd_xref_fkey\" FOREIGN KEY (\"xref\") REFERENCES \"refEntityInt#78255ee1\"(\"refIdAttrInt\"),CONSTRAINT \"entity#65a8f5cd_categorical_nillable_fkey\" FOREIGN KEY (\"categorical_nillable\") REFERENCES \"refEntityStr#305ca1a9\"(\"refIdAttrStr\"),CONSTRAINT \"entity#65a8f5cd_enum_nillable_chk\" CHECK (\"enum_nillable\" IN ('enum0','enum1')),CONSTRAINT \"entity#65a8f5cd_file_nillable_fkey\" FOREIGN KEY (\"file_nillable\") REFERENCES \"refEntityInt#78255ee1\"(\"refIdAttrInt\"),CONSTRAINT \"entity#65a8f5cd_xref_nillable_fkey\" FOREIGN KEY (\"xref_nillable\") REFERENCES \"refEntityInt#78255ee1\"(\"refIdAttrInt\"),CONSTRAINT \"entity#65a8f5cd_bool_unique_key\" UNIQUE (\"bool_unique\"),CONSTRAINT \"entity#65a8f5cd_categorical_unique_fkey\" FOREIGN KEY (\"categorical_unique\") REFERENCES \"refEntityStr#305ca1a9\"(\"refIdAttrStr\"),CONSTRAINT \"entity#65a8f5cd_categorical_unique_key\" UNIQUE (\"categorical_unique\"),CONSTRAINT \"entity#65a8f5cd_date_unique_key\" UNIQUE (\"date_unique\"),CONSTRAINT \"entity#65a8f5cd_date_time_unique_key\" UNIQUE (\"date_time_unique\"),CONSTRAINT \"entity#65a8f5cd_decimal_unique_key\" UNIQUE (\"decimal_unique\"),CONSTRAINT \"entity#65a8f5cd_email_unique_key\" UNIQUE (\"email_unique\"),CONSTRAINT \"entity#65a8f5cd_enum_unique_key\" UNIQUE (\"enum_unique\"),CONSTRAINT \"entity#65a8f5cd_enum_unique_chk\" CHECK (\"enum_unique\" IN ('enum0','enum1')),CONSTRAINT \"entity#65a8f5cd_file_unique_fkey\" FOREIGN KEY (\"file_unique\") REFERENCES \"refEntityInt#78255ee1\"(\"refIdAttrInt\"),CONSTRAINT \"entity#65a8f5cd_file_unique_key\" UNIQUE (\"file_unique\"),CONSTRAINT \"entity#65a8f5cd_html_unique_key\" UNIQUE (\"html_unique\"),CONSTRAINT \"entity#65a8f5cd_hyperlink_unique_key\" UNIQUE (\"hyperlink_unique\"),CONSTRAINT \"entity#65a8f5cd_int_unique_key\" UNIQUE (\"int_unique\"),CONSTRAINT \"entity#65a8f5cd_long_unique_key\" UNIQUE (\"long_unique\"),CONSTRAINT \"entity#65a8f5cd_script_unique_key\" UNIQUE (\"script_unique\"),CONSTRAINT \"entity#65a8f5cd_string_unique_key\" UNIQUE (\"string_unique\"),CONSTRAINT \"entity#65a8f5cd_text_unique_key\" UNIQUE (\"text_unique\"),CONSTRAINT \"entity#65a8f5cd_xref_unique_fkey\" FOREIGN KEY (\"xref_unique\") REFERENCES \"refEntityInt#78255ee1\"(\"refIdAttrInt\"),CONSTRAINT \"entity#65a8f5cd_xref_unique_key\" UNIQUE (\"xref_unique\"),CONSTRAINT \"entity#65a8f5cd_bool_unique_nillable_key\" UNIQUE (\"bool_unique_nillable\"),CONSTRAINT \"entity#65a8f5cd_categorical_unique_nillable_fkey\" FOREIGN KEY (\"categorical_unique_nillable\") REFERENCES \"refEntityStr#305ca1a9\"(\"refIdAttrStr\"),CONSTRAINT \"entity#65a8f5cd_categorical_unique_nillable_key\" UNIQUE (\"categorical_unique_nillable\"),CONSTRAINT \"entity#65a8f5cd_date_unique_nillable_key\" UNIQUE (\"date_unique_nillable\"),CONSTRAINT \"entity#65a8f5cd_date_time_unique_nillable_key\" UNIQUE (\"date_time_unique_nillable\"),CONSTRAINT \"entity#65a8f5cd_decimal_unique_nillable_key\" UNIQUE (\"decimal_unique_nillable\"),CONSTRAINT \"entity#65a8f5cd_email_unique_nillable_key\" UNIQUE (\"email_unique_nillable\"),CONSTRAINT \"entity#65a8f5cd_enum_unique_nillable_key\" UNIQUE (\"enum_unique_nillable\"),CONSTRAINT \"entity#65a8f5cd_enum_unique_nillable_chk\" CHECK (\"enum_unique_nillable\" IN ('enum0','enum1')),CONSTRAINT \"entity#65a8f5cd_file_unique_nillable_fkey\" FOREIGN KEY (\"file_unique_nillable\") REFERENCES \"refEntityInt#78255ee1\"(\"refIdAttrInt\"),CONSTRAINT \"entity#65a8f5cd_file_unique_nillable_key\" UNIQUE (\"file_unique_nillable\"),CONSTRAINT \"entity#65a8f5cd_html_unique_nillable_key\" UNIQUE (\"html_unique_nillable\"),CONSTRAINT \"entity#65a8f5cd_hyperlink_unique_nillable_key\" UNIQUE (\"hyperlink_unique_nillable\"),CONSTRAINT \"entity#65a8f5cd_int_unique_nillable_key\" UNIQUE (\"int_unique_nillable\"),CONSTRAINT \"entity#65a8f5cd_long_unique_nillable_key\" UNIQUE (\"long_unique_nillable\"),CONSTRAINT \"entity#65a8f5cd_script_unique_nillable_key\" UNIQUE (\"script_unique_nillable\"),CONSTRAINT \"entity#65a8f5cd_string_unique_nillable_key\" UNIQUE (\"string_unique_nillable\"),CONSTRAINT \"entity#65a8f5cd_text_unique_nillable_key\" UNIQUE (\"text_unique_nillable\"),CONSTRAINT \"entity#65a8f5cd_xref_unique_nillable_fkey\" FOREIGN KEY (\"xref_unique_nillable\") REFERENCES \"refEntityInt#78255ee1\"(\"refIdAttrInt\"),CONSTRAINT \"entity#65a8f5cd_xref_unique_nillable_key\" UNIQUE (\"xref_unique_nillable\"))");
	}

	@Test
	public void getSqlCreateFunctionValidateUpdate()
	{
		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("entity").getMock();
		when(entityType.getFullyQualifiedName()).thenReturn("entityName");
		when(entityType.getName()).thenReturn("entity");
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("idAttr").getMock();
		when(idAttr.getIdentifier()).thenReturn("idAttrId");
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn("attr0").getMock();
		when(attr0.getIdentifier()).thenReturn("attr0Id");
		Attribute attr1 = when(mock(Attribute.class).getName()).thenReturn("attr1").getMock();
		when(attr1.getIdentifier()).thenReturn("attr1Id");
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		String expectedSql = "CREATE FUNCTION \"validate_update_entity#65a8f5cd\"() RETURNS TRIGGER AS $$\n" + "BEGIN\n"
				+ "  IF OLD.\"attr0\" <> NEW.\"attr0\" THEN\n"
				+ "    RAISE EXCEPTION 'Updating read-only column \"attr0\" of table \"entity#65a8f5cd\" with id [%] is not allowed', OLD.\"idAttr\" USING ERRCODE = '23506';\n"
				+ "  END IF;\n" + "  IF OLD.\"attr1\" <> NEW.\"attr1\" THEN\n"
				+ "    RAISE EXCEPTION 'Updating read-only column \"attr1\" of table \"entity#65a8f5cd\" with id [%] is not allowed', OLD.\"idAttr\" USING ERRCODE = '23506';\n"
				+ "  END IF;\n" + "  RETURN NEW;\n" + "END;\n" + "$$ LANGUAGE plpgsql;";
		assertEquals(PostgreSqlQueryGenerator.getSqlCreateFunctionValidateUpdate(entityType, asList(attr0, attr1)),
				expectedSql);
	}

	@Test
	public void getSqlDropFunctionValidateUpdate()
	{
		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("entity").getMock();
		when(entityType.getFullyQualifiedName()).thenReturn("entityName");
		when(entityType.getName()).thenReturn("entity");
		String expectedSql = "DROP FUNCTION \"validate_update_entity#65a8f5cd\"();";
		assertEquals(PostgreSqlQueryGenerator.getSqlDropFunctionValidateUpdate(entityType), expectedSql);
	}

	@Test
	public void getSqlCreateUpdateTrigger()
	{
		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("entity").getMock();
		when(entityType.getFullyQualifiedName()).thenReturn("entityName");
		when(entityType.getName()).thenReturn("entity");
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn("attr0").getMock();
		when(attr0.getIdentifier()).thenReturn("attr0Id");
		Attribute attr1 = when(mock(Attribute.class).getName()).thenReturn("attr1").getMock();
		when(attr1.getIdentifier()).thenReturn("attr1Id");
		String expectedSql = "CREATE TRIGGER \"update_trigger_entity#65a8f5cd\" AFTER UPDATE ON \"entity#65a8f5cd\" FOR EACH ROW WHEN (OLD.\"attr0\" IS DISTINCT FROM NEW.\"attr0\" OR OLD.\"attr1\" IS DISTINCT FROM NEW.\"attr1\") EXECUTE PROCEDURE \"validate_update_entity#65a8f5cd\"();";
		assertEquals(PostgreSqlQueryGenerator.getSqlCreateUpdateTrigger(entityType, asList(attr0, attr1)), expectedSql);
	}

	@Test
	public void getSqlDropUpdateTrigger()
	{
		String expectedSql = "DROP TRIGGER \"update_trigger_entity#65a8f5cd\" ON \"entity#65a8f5cd\"";
		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("entity").getMock();
		when(entityType.getFullyQualifiedName()).thenReturn("entityName");
		when(entityType.getName()).thenReturn("entity");
		assertEquals(PostgreSqlQueryGenerator.getSqlDropUpdateTrigger(entityType), expectedSql);
	}

	@Test
	public void getSqlCreateForeignKey()
	{
		Attribute refIdAttr = when(mock(Attribute.class).getName()).thenReturn("refIdAttr").getMock();
		when(refIdAttr.getIdentifier()).thenReturn("refIdAttrId");
		EntityType refEntityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("refEntity")
				.getMock();
		when(refEntityType.getName()).thenReturn("refEntity");
		when(refEntityType.getFullyQualifiedName()).thenReturn("refEntityId");
		when(refEntityType.getIdAttribute()).thenReturn(refIdAttr);

		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("entity").getMock();
		when(entityType.getFullyQualifiedName()).thenReturn("entityName");
		when(entityType.getName()).thenReturn("entity");
		Attribute refAttr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(refAttr.getIdentifier()).thenReturn("refAttrId");
		when(refAttr.getDataType()).thenReturn(XREF);
		when(refAttr.getRefEntity()).thenReturn(refEntityType);

		String expectedSql = "ALTER TABLE \"entity#65a8f5cd\" ADD CONSTRAINT \"entity#65a8f5cd_attr_fkey\" FOREIGN KEY (\"attr\") REFERENCES \"refEntity#07f902bf\"(\"refIdAttr\")";
		assertEquals(PostgreSqlQueryGenerator.getSqlCreateForeignKey(entityType, refAttr), expectedSql);
	}

	@Test
	public void getSqlCreateForeignKeySelfReferencing()
	{
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("idAttr").getMock();
		when(idAttr.getIdentifier()).thenReturn("idAttrId");
		when(idAttr.getDataType()).thenReturn(STRING);
		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("entity").getMock();
		when(entityType.getFullyQualifiedName()).thenReturn("entityName");
		when(entityType.getName()).thenReturn("entity");
		when(entityType.getIdAttribute()).thenReturn(idAttr);

		Attribute refAttr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(refAttr.getIdentifier()).thenReturn("refAttrId");
		when(refAttr.getDataType()).thenReturn(XREF);
		when(refAttr.getRefEntity()).thenReturn(entityType);

		String expectedSql = "ALTER TABLE \"entity#65a8f5cd\" ADD CONSTRAINT \"entity#65a8f5cd_attr_fkey\" FOREIGN KEY (\"attr\") REFERENCES \"entity#65a8f5cd\"(\"idAttr\") DEFERRABLE INITIALLY DEFERRED";
		assertEquals(PostgreSqlQueryGenerator.getSqlCreateForeignKey(entityType, refAttr), expectedSql);
	}

	@Test
	public void getSqlDropForeignKey()
	{
		Attribute refIdAttr = when(mock(Attribute.class).getName()).thenReturn("refIdAttr").getMock();
		when(refIdAttr.getIdentifier()).thenReturn("refIdAttrId");
		EntityType refEntityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("refEntity")
				.getMock();
		when(refEntityType.getFullyQualifiedName()).thenReturn("refEntityId");
		when(refEntityType.getName()).thenReturn("refEntity");
		when(refEntityType.getIdAttribute()).thenReturn(refIdAttr);

		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("entity").getMock();
		when(entityType.getFullyQualifiedName()).thenReturn("entityName");
		when(entityType.getName()).thenReturn("entity");
		Attribute refAttr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(refAttr.getIdentifier()).thenReturn("refAttrId");
		when(refAttr.getDataType()).thenReturn(XREF);
		when(refAttr.getRefEntity()).thenReturn(refEntityType);

		String expectedSql = "ALTER TABLE \"entity#65a8f5cd\" DROP CONSTRAINT \"entity#65a8f5cd_attr_fkey\"";
		assertEquals(PostgreSqlQueryGenerator.getSqlDropForeignKey(entityType, refAttr), expectedSql);
	}

	@Test
	public void getSqlCreateUniqueKey()
	{
		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("entity").getMock();
		when(entityType.getFullyQualifiedName()).thenReturn("entityName");
		when(entityType.getName()).thenReturn("entity");
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		when(attr.getDataType()).thenReturn(STRING);

		String expectedSql = "ALTER TABLE \"entity#65a8f5cd\" ADD CONSTRAINT \"entity#65a8f5cd_attr_key\" UNIQUE (\"attr\")";
		assertEquals(PostgreSqlQueryGenerator.getSqlCreateUniqueKey(entityType, attr), expectedSql);
	}

	@Test
	public void getSqlDropUniqueKey()
	{
		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("entity").getMock();
		when(entityType.getFullyQualifiedName()).thenReturn("entityName");
		when(entityType.getName()).thenReturn("entity");
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		when(attr.getDataType()).thenReturn(STRING);

		String expectedSql = "ALTER TABLE \"entity#65a8f5cd\" DROP CONSTRAINT \"entity#65a8f5cd_attr_key\"";
		assertEquals(PostgreSqlQueryGenerator.getSqlDropUniqueKey(entityType, attr), expectedSql);
	}

	@Test
	public void getSqlCreateCheckConstraint()
	{
		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("entity").getMock();
		when(entityType.getFullyQualifiedName()).thenReturn("entityName");
		when(entityType.getName()).thenReturn("entity");
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		when(attr.getDataType()).thenReturn(ENUM);
		when(attr.getEnumOptions()).thenReturn(newArrayList("enum0", "enum1", "enum2"));
		assertEquals(PostgreSqlQueryGenerator.getSqlCreateCheckConstraint(entityType, attr),
				"ALTER TABLE \"entity#65a8f5cd\" ADD CONSTRAINT \"entity#65a8f5cd_attr_chk\" CHECK (\"attr\" IN ('enum0','enum1','enum2'))");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void getSqlCreateCheckConstraintWrongDataType()
	{
		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("entity").getMock();
		when(entityType.getFullyQualifiedName()).thenReturn("entityName");
		when(entityType.getName()).thenReturn("entity");
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		when(attr.getDataType()).thenReturn(STRING);
		PostgreSqlQueryGenerator.getSqlCreateCheckConstraint(entityType, attr);
	}

	@Test
	public void getSqlDropCheckConstraint()
	{
		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("entity").getMock();
		when(entityType.getFullyQualifiedName()).thenReturn("entityName");
		when(entityType.getName()).thenReturn("entity");
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		when(attr.getDataType()).thenReturn(ENUM);
		when(attr.getEnumOptions()).thenReturn(newArrayList("enum0", "enum1", "enum2"));
		assertEquals(PostgreSqlQueryGenerator.getSqlDropCheckConstraint(entityType, attr),
				"ALTER TABLE \"entity#65a8f5cd\" DROP CONSTRAINT \"entity#65a8f5cd_attr_chk\"");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void getSqlDropCheckConstraintWrongDataType()
	{
		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("entity").getMock();
		when(entityType.getFullyQualifiedName()).thenReturn("entityName");
		when(entityType.getName()).thenReturn("entity");
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
		EntityType refEntityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("refEntity")
				.getMock();
		when(refEntityType.getFullyQualifiedName()).thenReturn("refEntityId");
		when(refEntityType.getName()).thenReturn("refEntity");
		when(refEntityType.getIdAttribute()).thenReturn(refIdAttr);

		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("entity").getMock();
		when(entityType.getFullyQualifiedName()).thenReturn("entityName");
		when(entityType.getName()).thenReturn("entity");
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("idAttr").getMock();
		when(idAttr.getIdentifier()).thenReturn("idAttrId");
		when(idAttr.getDataType()).thenReturn(STRING);
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		when(attr.getDataType()).thenReturn(MREF);
		when(attr.getRefEntity()).thenReturn(refEntityType);

		String expectedSql = "CREATE TABLE \"entity#65a8f5cd_attr\" (\"order\" INT,\"idAttr\" character varying(255) NOT NULL, \"attr\" character varying(255) NOT NULL, FOREIGN KEY (\"idAttr\") REFERENCES \"entity#65a8f5cd\"(\"idAttr\") ON DELETE CASCADE, FOREIGN KEY (\"attr\") REFERENCES \"refEntity#07f902bf\"(\"refIdAttr\") ON DELETE CASCADE, UNIQUE (\"idAttr\",\"attr\"), UNIQUE (\"order\",\"idAttr\"))";
		assertEquals(PostgreSqlQueryGenerator.getSqlCreateJunctionTable(entityType, attr), expectedSql);
	}

	@Test
	public void getSqlCreateJunctionTableSelfReferencing()
	{
		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("entity").getMock();
		when(entityType.getFullyQualifiedName()).thenReturn("entityName");
		when(entityType.getName()).thenReturn("entity");
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("idAttr").getMock();
		when(idAttr.getIdentifier()).thenReturn("idAttrId");
		when(idAttr.getDataType()).thenReturn(STRING);
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		when(attr.getDataType()).thenReturn(MREF);
		when(attr.getRefEntity()).thenReturn(entityType);

		String expectedSql = "CREATE TABLE \"entity#65a8f5cd_attr\" (\"order\" INT,\"idAttr\" character varying(255) NOT NULL, \"attr\" character varying(255) NOT NULL, FOREIGN KEY (\"idAttr\") REFERENCES \"entity#65a8f5cd\"(\"idAttr\") ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED, FOREIGN KEY (\"attr\") REFERENCES \"entity#65a8f5cd\"(\"idAttr\") ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED, UNIQUE (\"idAttr\",\"attr\"), UNIQUE (\"order\",\"idAttr\"))";
		assertEquals(PostgreSqlQueryGenerator.getSqlCreateJunctionTable(entityType, attr), expectedSql);
	}

	@Test
	public void getJunctionTableSelect()
	{
		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("entity").getMock();
		when(entityType.getFullyQualifiedName()).thenReturn("entityName");
		when(entityType.getName()).thenReturn("entityName");
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("idAttr").getMock();
		when(idAttr.getIdentifier()).thenReturn("idAttrId");
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		when(attr.getDataType()).thenReturn(MREF);
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		assertEquals(PostgreSqlQueryGenerator.getSqlJunctionTableSelect(entityType, attr, 3),
				"SELECT \"idAttr\",\"order\",\"attr\" FROM \"entityName#65a8f5cd_attr\" WHERE \"idAttr\" in (?, ?, ?) ORDER BY \"idAttr\",\"order\"");
	}

	@Test
	public void getSqlInsertJunction()
	{
		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("entity").getMock();
		when(entityType.getFullyQualifiedName()).thenReturn("entityName");
		when(entityType.getName()).thenReturn("entityName");
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("idAttr").getMock();
		when(idAttr.getIdentifier()).thenReturn("idAttrId");
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		when(attr.getDataType()).thenReturn(MREF);
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		assertEquals(PostgreSqlQueryGenerator.getSqlInsertJunction(entityType, attr),
				"INSERT INTO \"entityName#65a8f5cd_attr\" (\"order\",\"idAttr\",\"attr\") VALUES (?,?,?)");
	}

	@Test
	public void getSqlInsertJunctionInversedBy()
	{
		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("entity").getMock();
		when(entityType.getFullyQualifiedName()).thenReturn("entityName");
		when(entityType.getName()).thenReturn("entityName");
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("idAttr").getMock();
		when(idAttr.getIdentifier()).thenReturn("idAttrId");
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		when(attr.getDataType()).thenReturn(XREF);
		when(attr.isInversedBy()).thenReturn(true);
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		assertEquals(PostgreSqlQueryGenerator.getSqlInsertJunction(entityType, attr),
				"INSERT INTO \"entityName#65a8f5cd_attr\" (\"order\",\"idAttr\",\"attr\") VALUES (?,?,?)");
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

		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("entity").getMock();
		when(entityType.getFullyQualifiedName()).thenReturn("entityName");
		when(entityType.getName()).thenReturn("entityName");
		when(entityType.getAtomicAttributes()).thenReturn(newArrayList(idAttr, attr));
		when(entityType.getIdAttribute()).thenReturn(idAttr);

		//noinspection unchecked
		Query<Entity> q = mock(Query.class);
		List<Object> parameters = Lists.newArrayList();
		assertEquals(PostgreSqlQueryGenerator.getSqlSelect(entityType, q, parameters, true),
				"SELECT this.\"idAttr\", this.\"attr\" FROM \"entityName#65a8f5cd\" AS this");
		assertEquals(parameters, Collections.emptyList());
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

		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("entity").getMock();
		when(entityType.getFullyQualifiedName()).thenReturn("entityName");
		when(entityType.getName()).thenReturn("entityName");
		when(entityType.getAtomicAttributes()).thenReturn(newArrayList(idAttr, attr));
		when(entityType.getIdAttribute()).thenReturn(idAttr);

		//noinspection unchecked
		Query<Entity> q = mock(Query.class);
		List<Object> parameters = Lists.newArrayList();
		assertEquals(PostgreSqlQueryGenerator.getSqlSelect(entityType, q, parameters, true),
				"SELECT this.\"idAttr\", this.\"attr\" FROM \"entityName#65a8f5cd\" AS this");
		assertEquals(parameters, Collections.emptyList());
	}

	@Test
	public void getSqlSelectOneToManyMappedBy()
	{
		Attribute refIdAttr = when(mock(Attribute.class).getName()).thenReturn("refIdAttr").getMock();
		when(refIdAttr.getIdentifier()).thenReturn("refIdAttrId");
		when(refIdAttr.getDataType()).thenReturn(STRING);
		EntityType refEntityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("refEntity")
				.getMock();
		when(refEntityType.getFullyQualifiedName()).thenReturn("refEntityId");
		when(refEntityType.getName()).thenReturn("refEntity");
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

		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("entity").getMock();
		when(entityType.getFullyQualifiedName()).thenReturn("entityName");
		when(entityType.getName()).thenReturn("entity");
		when(entityType.getAtomicAttributes()).thenReturn(newArrayList(idAttr, attr));
		when(entityType.getIdAttribute()).thenReturn(idAttr);

		//noinspection unchecked
		Query<Entity> q = mock(Query.class);
		List<Object> parameters = Lists.newArrayList();
		assertEquals(PostgreSqlQueryGenerator.getSqlSelect(entityType, q, parameters, true),
				"SELECT this.\"idAttr\", (SELECT array_agg(\"refIdAttr\" ORDER BY \"refIdAttr\" ASC) FROM \"refEntity#07f902bf\" WHERE this.\"idAttr\" = \"refEntity#07f902bf\".\"refAttr\") AS \"attr\" FROM \"entity#65a8f5cd\" AS this");
		assertEquals(parameters, Collections.emptyList());
	}

	@Test
	public void getSqlSelectMref()
	{
		Package package_ = when(mock(Package.class).getFullyQualifiedName()).thenReturn("org_molgenis").getMock();

		Attribute ref1IdAttr = when(mock(Attribute.class).getName()).thenReturn("ref1Id").getMock();
		when(ref1IdAttr.getIdentifier()).thenReturn("ref1IdAttrId");
		EntityType ref1Meta = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("Ref1").getMock();
		when(ref1Meta.getFullyQualifiedName()).thenReturn("ref1Id");
		when(ref1Meta.getName()).thenReturn("Ref1");
		when(ref1Meta.getIdAttribute()).thenReturn(ref1IdAttr);

		Attribute ref2IdAttr = when(mock(Attribute.class).getName()).thenReturn("ref2Id").getMock();
		when(ref2IdAttr.getIdentifier()).thenReturn("ref2IdAttrId");
		EntityType ref2Meta = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("Ref2").getMock();
		when(ref2Meta.getFullyQualifiedName()).thenReturn("ref2Id");
		when(ref2Meta.getName()).thenReturn("Ref2");
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

		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName())
				.thenReturn("org_molgenis_MasterEntity")
				.getMock();
		when(entityType.getFullyQualifiedName()).thenReturn("entityName");
		when(entityType.getName()).thenReturn("entityName");
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
				"SELECT this.\"masterId\", (SELECT array_agg(DISTINCT ARRAY[\"mref1\".\"order\"::TEXT,\"mref1\".\"mref1\"::TEXT]) FROM \"entityName#65a8f5cd_mref1\" AS \"mref1\" WHERE this.\"masterId\" = \"mref1\".\"masterId\") AS \"mref1\", (SELECT array_agg(DISTINCT ARRAY[\"mref2\".\"order\"::TEXT,\"mref2\".\"mref2\"::TEXT]) FROM \"entityName#65a8f5cd_mref2\" AS \"mref2\" WHERE this.\"masterId\" = \"mref2\".\"masterId\") AS \"mref2\" FROM \"entityName#65a8f5cd\" AS this");
	}

	@Test
	public void getSqlSortOnUnselectedMref() throws Exception
	{
		Package package_ = when(mock(Package.class).getFullyQualifiedName()).thenReturn("org_molgenis").getMock();

		Attribute ref1IdAttr = when(mock(Attribute.class).getName()).thenReturn("ref1Id").getMock();
		when(ref1IdAttr.getIdentifier()).thenReturn("ref1IdAttrId");
		EntityType ref1Meta = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("Ref1").getMock();
		when(ref1Meta.getFullyQualifiedName()).thenReturn("ref1Id");
		when(ref1Meta.getName()).thenReturn("Ref1");
		when(ref1Meta.getIdAttribute()).thenReturn(ref1IdAttr);

		Attribute ref2IdAttr = when(mock(Attribute.class).getName()).thenReturn("ref2Id").getMock();
		when(ref2IdAttr.getIdentifier()).thenReturn("ref2IdAttrId");
		EntityType ref2Meta = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("Ref2").getMock();
		when(ref2Meta.getFullyQualifiedName()).thenReturn("ref2Id");
		when(ref2Meta.getName()).thenReturn("Ref2");
		when(ref2Meta.getIdAttribute()).thenReturn(ref2IdAttr);

		Attribute masterIdAttr = when(mock(Attribute.class).getName()).thenReturn("masterId").getMock();
		when(masterIdAttr.getIdentifier()).thenReturn("masterIdAttrId");
		when(masterIdAttr.getDataType()).thenReturn(STRING);
		Attribute mref1Attr = when(mock(Attribute.class).getName()).thenReturn("mref1").getMock();
		when(mref1Attr.getIdentifier()).thenReturn("mref1AttrId");
		when(mref1Attr.getDataType()).thenReturn(MREF);
		when(mref1Attr.getRefEntity()).thenReturn(ref1Meta);
		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName())
				.thenReturn("org_molgenis_MasterEntity")
				.getMock();
		when(entityType.getFullyQualifiedName()).thenReturn("entityName");
		when(entityType.getName()).thenReturn("entityName");
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
				"SELECT this.\"masterId\", (SELECT array_agg(DISTINCT ARRAY[\"mref1\".\"order\"::TEXT,\"mref1\".\"mref1\"::TEXT]) FROM \"entityName#65a8f5cd_mref1\" AS \"mref1\" WHERE this.\"masterId\" = \"mref1\".\"masterId\") AS \"mref1\" FROM \"entityName#65a8f5cd\" AS this ORDER BY \"mref1\" ASC");
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
		when(refEntityTypeString.getFullyQualifiedName()).thenReturn("refEntityStrId");
		when(refEntityTypeString.toString()).thenReturn("refEntityStr");
		when(refEntityTypeString.getFullyQualifiedName()).thenReturn("refEntityStr");
		when(refEntityTypeString.getName()).thenReturn("refEntityStr");
		when(refEntityTypeString.getIdAttribute()).thenReturn(refIdAttrStr);

		// ref entity with int id attribute
		Attribute refIdAttrInt = mock(Attribute.class);
		when(refIdAttrInt.getIdentifier()).thenReturn("refIdAttrIntId");
		when(refIdAttrInt.getName()).thenReturn("refIdAttrInt");
		when(refIdAttrInt.getDataType()).thenReturn(INT);
		EntityType refEntityTypeInt = mock(EntityType.class);
		when(refEntityTypeInt.getFullyQualifiedName()).thenReturn("refEntityIntId");
		when(refEntityTypeInt.toString()).thenReturn("refEntityInt");
		when(refEntityTypeInt.getFullyQualifiedName()).thenReturn("refEntityInt");
		when(refEntityTypeInt.getName()).thenReturn("refEntityInt");
		when(refEntityTypeInt.getIdAttribute()).thenReturn(refIdAttrInt);

		return Arrays.asList(new Object[] { BOOL, true, null, "ALTER TABLE \"entity#65a8f5cd\" ADD \"attr\" boolean" },
				new Object[] { CATEGORICAL, true, refEntityTypeInt,
						"ALTER TABLE \"entity#65a8f5cd\" ADD \"attr\" integer,ADD CONSTRAINT \"entity#65a8f5cd_attr_fkey\" FOREIGN KEY (\"attr\") REFERENCES \"refEntityInt#78255ee1\"(\"refIdAttrInt\")" },
				new Object[] { DATE, true, null, "ALTER TABLE \"entity#65a8f5cd\" ADD \"attr\" date" },
				new Object[] { DATE_TIME, true, null, "ALTER TABLE \"entity#65a8f5cd\" ADD \"attr\" timestamp" },
				new Object[] { DECIMAL, true, null, "ALTER TABLE \"entity#65a8f5cd\" ADD \"attr\" double precision" },
				new Object[] { EMAIL, true, null,
						"ALTER TABLE \"entity#65a8f5cd\" ADD \"attr\" character varying(255)" },
				new Object[] { ENUM, true, null,
						"ALTER TABLE \"entity#65a8f5cd\" ADD \"attr\" character varying(255),ADD CONSTRAINT \"entity#65a8f5cd_attr_chk\" CHECK (\"attr\" IN ('enum0, enum1'))" },
				new Object[] { FILE, true, refEntityTypeString,
						"ALTER TABLE \"entity#65a8f5cd\" ADD \"attr\" character varying(255),ADD CONSTRAINT \"entity#65a8f5cd_attr_fkey\" FOREIGN KEY (\"attr\") REFERENCES \"refEntityStr#305ca1a9\"(\"refIdAttrStr\")" },
				new Object[] { HTML, true, null, "ALTER TABLE \"entity#65a8f5cd\" ADD \"attr\" text" },
				new Object[] { HYPERLINK, true, null,
						"ALTER TABLE \"entity#65a8f5cd\" ADD \"attr\" character varying(255)" },
				new Object[] { INT, true, null, "ALTER TABLE \"entity#65a8f5cd\" ADD \"attr\" integer" },
				new Object[] { LONG, true, null, "ALTER TABLE \"entity#65a8f5cd\" ADD \"attr\" bigint" },
				new Object[] { SCRIPT, true, null, "ALTER TABLE \"entity#65a8f5cd\" ADD \"attr\" text" },
				new Object[] { STRING, true, null,
						"ALTER TABLE \"entity#65a8f5cd\" ADD \"attr\" character varying(255)" },
				new Object[] { TEXT, true, null, "ALTER TABLE \"entity#65a8f5cd\" ADD \"attr\" text" },
				new Object[] { XREF, true, refEntityTypeString,
						"ALTER TABLE \"entity#65a8f5cd\" ADD \"attr\" character varying(255),ADD CONSTRAINT \"entity#65a8f5cd_attr_fkey\" FOREIGN KEY (\"attr\") REFERENCES \"refEntityStr#305ca1a9\"(\"refIdAttrStr\")" },
				new Object[] { BOOL, false, null, "ALTER TABLE \"entity#65a8f5cd\" ADD \"attr\" boolean NOT NULL" },
				new Object[] { CATEGORICAL, false, refEntityTypeInt,
						"ALTER TABLE \"entity#65a8f5cd\" ADD \"attr\" integer NOT NULL,ADD CONSTRAINT \"entity#65a8f5cd_attr_fkey\" FOREIGN KEY (\"attr\") REFERENCES \"refEntityInt#78255ee1\"(\"refIdAttrInt\")" },
				new Object[] { DATE, false, null, "ALTER TABLE \"entity#65a8f5cd\" ADD \"attr\" date NOT NULL" },
				new Object[] { DATE_TIME, false, null,
						"ALTER TABLE \"entity#65a8f5cd\" ADD \"attr\" timestamp NOT NULL" },
				new Object[] { DECIMAL, false, null,
						"ALTER TABLE \"entity#65a8f5cd\" ADD \"attr\" double precision NOT NULL" },
				new Object[] { EMAIL, false, null,
						"ALTER TABLE \"entity#65a8f5cd\" ADD \"attr\" character varying(255) NOT NULL" },
				new Object[] { ENUM, false, null,
						"ALTER TABLE \"entity#65a8f5cd\" ADD \"attr\" character varying(255) NOT NULL,ADD CONSTRAINT \"entity#65a8f5cd_attr_chk\" CHECK (\"attr\" IN ('enum0, enum1'))" },
				new Object[] { FILE, false, refEntityTypeString,
						"ALTER TABLE \"entity#65a8f5cd\" ADD \"attr\" character varying(255) NOT NULL,ADD CONSTRAINT \"entity#65a8f5cd_attr_fkey\" FOREIGN KEY (\"attr\") REFERENCES \"refEntityStr#305ca1a9\"(\"refIdAttrStr\")" },
				new Object[] { HTML, false, null, "ALTER TABLE \"entity#65a8f5cd\" ADD \"attr\" text NOT NULL" },
				new Object[] { HYPERLINK, false, null,
						"ALTER TABLE \"entity#65a8f5cd\" ADD \"attr\" character varying(255) NOT NULL" },
				new Object[] { INT, false, null, "ALTER TABLE \"entity#65a8f5cd\" ADD \"attr\" integer NOT NULL" },
				new Object[] { LONG, false, null, "ALTER TABLE \"entity#65a8f5cd\" ADD \"attr\" bigint NOT NULL" },
				new Object[] { SCRIPT, false, null, "ALTER TABLE \"entity#65a8f5cd\" ADD \"attr\" text NOT NULL" },
				new Object[] { STRING, false, null,
						"ALTER TABLE \"entity#65a8f5cd\" ADD \"attr\" character varying(255) NOT NULL" },
				new Object[] { TEXT, false, null, "ALTER TABLE \"entity#65a8f5cd\" ADD \"attr\" text NOT NULL" },
				new Object[] { XREF, false, refEntityTypeString,
						"ALTER TABLE \"entity#65a8f5cd\" ADD \"attr\" character varying(255) NOT NULL,ADD CONSTRAINT \"entity#65a8f5cd_attr_fkey\" FOREIGN KEY (\"attr\") REFERENCES \"refEntityStr#305ca1a9\"(\"refIdAttrStr\")" })
				.iterator();
	}

	@Test(dataProvider = "getSqlAddColumnProvider")
	public void getSqlAddColumn(AttributeType attrType, boolean nillable, EntityType refEntityType, String sql)
	{
		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("entity").getMock();
		when(entityType.getFullyQualifiedName()).thenReturn("entityName");
		when(entityType.getName()).thenReturn("entity");
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("id").getMock();
		when(idAttr.getIdentifier()).thenReturn("idAttrId");
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		when(attr.getDataType()).thenReturn(attrType);
		when(attr.isNillable()).thenReturn(nillable);
		when(attr.getRefEntity()).thenReturn(refEntityType);
		when(attr.getEnumOptions())
				.thenReturn(attrType == ENUM ? newArrayList("enum0, enum1") : Collections.emptyList());
		assertEquals(PostgreSqlQueryGenerator.getSqlAddColumn(entityType, attr), sql);
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
		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("entity").getMock();
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getDataType()).thenReturn(attrType);
		PostgreSqlQueryGenerator.getSqlAddColumn(entityType, attr);
	}

	@Test
	public void getSqlDropColumn()
	{
		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("entity").getMock();
		when(entityType.getFullyQualifiedName()).thenReturn("entityName");
		when(entityType.getName()).thenReturn("entity");
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		assertEquals(PostgreSqlQueryGenerator.getSqlDropColumn(entityType, attr),
				"ALTER TABLE \"entity#65a8f5cd\" DROP COLUMN \"attr\"");
	}

	@Test
	public void getSqlCreateJunctionTableIndex()
	{
		EntityType entityType = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("entity").getMock();
		when(entityType.getFullyQualifiedName()).thenReturn("entityName");
		when(entityType.getName()).thenReturn("entity");
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getIdentifier()).thenReturn("attrId");
		Attribute idxAttr = when(mock(Attribute.class).getName()).thenReturn("idAttr").getMock();
		when(idxAttr.getIdentifier()).thenReturn("idAttrId");
		when(entityType.getIdAttribute()).thenReturn(idxAttr);
		assertEquals(PostgreSqlQueryGenerator.getSqlCreateJunctionTableIndex(entityType, attr),
				"CREATE INDEX \"entity#65a8f5cd_attr_idAttr_idx\" ON \"entity#65a8f5cd_attr\" (\"idAttr\")");
	}
}