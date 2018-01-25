package org.molgenis.data.validation;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.QueryRule.Operator.*;
import static org.molgenis.data.meta.AttributeType.*;

public class QueryValidatorTest
{
	private QueryValidator queryValidator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		EntityManager entityManager = mock(EntityManager.class);
		this.queryValidator = new QueryValidator(entityManager);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testQueryValidator()
	{
		new QueryValidator(null);
	}

	@DataProvider(name = "validateValidProvider")
	public static Iterator<Object[]> validateValidProvider()
	{
		List<Object[]> queries = new ArrayList<>(256);

		EnumSet.of(EQUALS).forEach(operator ->
		{
			// BOOL
			Entity boolEntityType = createEntityType(BOOL);
			asList(Boolean.TRUE, Boolean.FALSE, null, "true", "false", "True", "False").forEach(
					value -> queries.add(new Object[] { boolEntityType, new QueryImpl<>().eq("attr", value) }));

			// CATEGORICAL, XREF, CATEGORICAL_MREF, MREF, ONE_TO_MANY
			EnumSet.of(STRING, INT, LONG, EMAIL, HYPERLINK)
				   .forEach(refIdAttrType -> EnumSet.of(CATEGORICAL, XREF, CATEGORICAL_MREF, MREF, ONE_TO_MANY)
													.forEach(refAttrType ->
													{
														Entity refEntityType = createEntityType(refAttrType,
																refIdAttrType);
														asList("1", 1, 1L, null).forEach(idValue -> queries.add(
																new Object[] { refEntityType,
																		new QueryImpl<>().eq("attr", idValue) }));

														Entity refEntity = when(
																mock(Entity.class).getIdValue()).thenReturn("1")
																								.getMock();
														queries.add(new Object[] { refEntityType,
																new QueryImpl<>().eq("attr", refEntity) });
													}));

			// DATE
			Entity dateEntityType = createEntityType(DATE);
			asList(LocalDate.now(), "2016-11-25", null).forEach(
					value -> queries.add(new Object[] { dateEntityType, new QueryImpl<>().eq("attr", value) }));

			// DATE_TIME
			Entity dateTimeEntityType = createEntityType(DATE_TIME);
			asList(Instant.now(), "1985-08-12T11:12:13+0500", null).forEach(
					value -> queries.add(new Object[] { dateTimeEntityType, new QueryImpl<>().eq("attr", value) }));

			// DECIMAL
			Entity decimalEntityType = createEntityType(DECIMAL);
			asList(1.23, "1.23", 1, 1L, null).forEach(
					value -> queries.add(new Object[] { decimalEntityType, new QueryImpl<>().eq("attr", value) }));

			// EMAIL, HTML, HYPERLINK, SCRIPT, STRING, TEXT
			EnumSet.of(EMAIL, HTML, HYPERLINK, SCRIPT, STRING, TEXT).forEach(attrType ->
			{
				Entity entityType = createEntityType(attrType);
				asList("abc", 1, 1L, 1.23, null).forEach(
						value -> queries.add(new Object[] { entityType, new QueryImpl<>().eq("attr", value) }));
			});

			// INT, LONG
			EnumSet.of(INT, LONG).forEach(attrType ->
			{
				Entity entityType = createEntityType(attrType);
				asList(1, 1L, "1", null).forEach(
						value -> queries.add(new Object[] { entityType, new QueryImpl<>().eq("attr", value) }));
			});

			// FILE
			Entity fileEntityType = createEntityType(FILE, STRING);
			asList("file0", mock(FileMeta.class), null).forEach(
					idValue -> queries.add(new Object[] { fileEntityType, new QueryImpl<>().eq("attr", idValue) }));

			// ENUM
			Entity enumEntityType = createEntityType(ENUM);
			asList(TestEnum.ENUM0, TestEnum.ENUM1, "ENUM0", "ENUM1", null).forEach(
					value -> queries.add(new Object[] { enumEntityType, new QueryImpl<>().eq("attr", value) }));
		});

		EnumSet.of(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL).forEach(operator ->
		{
			Entity entityType = createEntityType(INT);
			QueryImpl<Entity> query = new QueryImpl<>();
			query.addRule(new QueryRule("attr", operator, 1));
			queries.add(new Object[] { entityType, query });
		});

		EnumSet.of(FUZZY_MATCH, FUZZY_MATCH_NGRAM, LIKE).forEach(operator ->
		{
			Entity entityType = createEntityType(STRING);
			QueryImpl<Entity> query = new QueryImpl<>();
			query.addRule(new QueryRule("attr", operator, "abc"));
			queries.add(new Object[] { entityType, query });
		});

		EnumSet.of(IN, RANGE).forEach(operator ->
		{
			Entity entityType = createEntityType(INT);
			QueryImpl<Entity> query = new QueryImpl<>();
			query.addRule(new QueryRule("attr", operator, asList(1, 2)));
			queries.add(new Object[] { entityType, query });
		});

		return queries.iterator();
	}

	private enum TestEnum
	{
		ENUM0, ENUM1
	}

	private static Entity createEntityType(AttributeType attrType)
	{
		return createEntityType(attrType, null);
	}

	private static Entity createEntityType(AttributeType attrType, AttributeType refAttrType)
	{
		String attrName = "attr";
		Attribute attr = when(mock(Attribute.class).getDataType()).thenReturn(attrType).getMock();
		when(attr.getName()).thenReturn(attrName);
		if (attrType == ENUM)
		{
			when(attr.getEnumOptions()).thenReturn(asList("ENUM0", "ENUM1"));
		}
		EntityType entityType = when(mock(EntityType.class).getAttribute(attrName)).thenReturn(attr).getMock();
		when(entityType.toString()).thenReturn(attrType.toString());

		if (refAttrType == null && EnumSet.of(CATEGORICAL, CATEGORICAL_MREF, XREF, MREF).contains(attrType))
		{
			refAttrType = INT;
		}
		if (refAttrType != null)
		{
			Attribute refIdAttr = when(mock(Attribute.class).getDataType()).thenReturn(refAttrType).getMock();
			EntityType refEntityType = when(mock(EntityType.class).getIdAttribute()).thenReturn(refIdAttr).getMock();
			when(attr.getRefEntity()).thenReturn(refEntityType);
		}
		return entityType;
	}

	@Test(dataProvider = "validateValidProvider")
	public void testValidateValid(EntityType entityType, Query<Entity> query)
	{
		queryValidator.validate(query, entityType);
		// test passes if not exception occurred
	}

	@DataProvider(name = "validateInvalidProvider")
	public static Iterator<Object[]> validateInvalidProvider()
	{
		List<Object[]> queries = new ArrayList<>(6);
		EnumSet.of(BOOL, DECIMAL, INT, LONG, DATE, DATE_TIME, ENUM)
			   .forEach(attrType -> queries.add(
					   new Object[] { new QueryImpl().eq("attr", "invalid"), createEntityType(attrType) }));
		EnumSet.of(BOOL, DECIMAL, INT, LONG, DATE, DATE_TIME, ENUM, XREF, MREF, CATEGORICAL, CATEGORICAL_MREF)
			   .forEach(attrType -> queries.add(
					   new Object[] { new QueryImpl().eq("attr", new Object()), createEntityType(attrType) }));
		queries.add(new Object[] { new QueryImpl().eq("unknownAttr", "str"), createEntityType(STRING) });
		queries.add(new Object[] { new QueryImpl().eq("attr", "str"), createEntityType(COMPOUND) });
		return queries.iterator();
	}

	@Test(dataProvider = "validateInvalidProvider", expectedExceptions = MolgenisValidationException.class)
	public void testValidateInvalid(Query<Entity> query, EntityType entityType)
	{
		queryValidator.validate(query, entityType);
	}

}