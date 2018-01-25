package org.molgenis.data.postgresql;

import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.file.model.FileMetaMetaData;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.OffsetDateTime;
import java.util.Iterator;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.assertEquals;

public class PostgreSqlUtilsTest
{
	private static Attribute attrBool;
	private static Attribute attrBoolNillable;
	private static Attribute attrCategorical;
	private static Attribute attrCategoricalNillable;
	private static Attribute attrCategoricalMref;
	private static Attribute attrCategoricalMrefNillable;
	private static Attribute attrDate;
	private static Attribute attrDateNillable;
	private static Attribute attrDateTime;
	private static Attribute attrDateTimeNillable;
	private static Attribute attrDecimal;
	private static Attribute attrDecimalNillable;
	private static Attribute attrEmail;
	private static Attribute attrEmailNillable;
	private static Attribute attrEnum;
	private static Attribute attrEnumNillable;
	private static Attribute attrFile;
	private static Attribute attrFileNillable;
	private static Attribute attrHtml;
	private static Attribute attrHtmlNillable;
	private static Attribute attrHyperlink;
	private static Attribute attrHyperlinkNillable;
	private static Attribute attrInt;
	private static Attribute attrIntNillable;
	private static Attribute attrLong;
	private static Attribute attrLongNillable;
	private static Attribute attrMref;
	private static Attribute attrMrefNillable;
	private static Attribute attrScript;
	private static Attribute attrScriptNillable;
	private static Attribute attrString;
	private static Attribute attrStringNillable;
	private static Attribute attrText;
	private static Attribute attrTextNillable;
	private static Attribute attrXref;
	private static Attribute attrXrefNillable;

	private static Entity entity;

	private static boolean boolValue;
	private static Entity categoricalValue;
	private static String categoricalValueId;
	private static Entity categoricalMrefValue0;
	private static String categoricalMrefValueId0, categoricalMrefValueId1;
	private static LocalDate dateValue;
	private static Instant dateTimeValue;
	private static Double decimalValue;
	private static String emailValue;
	private static String enumValue;
	private static FileMeta fileValue;
	private static String fileValueId;
	private static String htmlValue;
	private static String hyperlinkValue;
	private static Integer intValue;
	private static Long longValue;
	private static Entity mrefValue0;
	private static int mrefValueId0, mrefValueId1;
	private static String scriptValue;
	private static String stringValue;
	private static String textValue;
	private static Entity xrefValue;
	private static int xrefValueId;
	private static OffsetDateTime offsetDateTime;

	@BeforeClass
	public static void setUpBeforeClass() throws ParseException
	{
		// create ref entities
		String attrRefStringIdName = "refStringId";
		Attribute attrRefStringId = mock(Attribute.class);
		when(attrRefStringId.getName()).thenReturn(attrRefStringIdName);
		when(attrRefStringId.getDataType()).thenReturn(STRING);

		EntityType refStringIdEntityType = mock(EntityType.class);
		when(refStringIdEntityType.getIdAttribute()).thenReturn(attrRefStringId);
		when(refStringIdEntityType.toString()).thenReturn("refStringId");

		String attrRefIntIdName = "refIntId";
		Attribute attrRefIntId = mock(Attribute.class);
		when(attrRefIntId.getName()).thenReturn(attrRefIntIdName);
		when(attrRefIntId.getDataType()).thenReturn(INT);

		EntityType refIntIdEntityType = mock(EntityType.class);
		when(refIntIdEntityType.getIdAttribute()).thenReturn(attrRefIntId);
		when(refIntIdEntityType.toString()).thenReturn("refIntId");

		String attrRefFileIdName = FileMetaMetaData.ID;
		Attribute attrRefFileId = mock(Attribute.class);
		when(attrRefFileId.getName()).thenReturn(attrRefFileIdName);
		when(attrRefFileId.getDataType()).thenReturn(STRING);

		FileMetaMetaData fileMetaMeta = mock(FileMetaMetaData.class);
		when(fileMetaMeta.getIdAttribute()).thenReturn(attrRefFileId);
		when(fileMetaMeta.toString()).thenReturn("fileMeta");

		// create attributes
		String attrBoolName = "attrBool";
		attrBool = createAttr(attrBoolName, BOOL);
		String attrBoolNillableName = "attrBoolNillable";
		attrBoolNillable = createAttr(attrBoolNillableName, BOOL);
		String attrCategoricalName = "attrCategorical";
		attrCategorical = createAttr(attrCategoricalName, CATEGORICAL, refStringIdEntityType);
		String attrCategoricalNillableName = "attrCategoricalNillable";
		attrCategoricalNillable = createAttr(attrCategoricalNillableName, CATEGORICAL, refStringIdEntityType);
		String attrCategoricalMrefName = "attrCategoricalMref";
		attrCategoricalMref = createAttr(attrCategoricalMrefName, CATEGORICAL_MREF, refStringIdEntityType);
		String attrCategoricalMrefNillableName = "attrCategoricalMrefNillable";
		attrCategoricalMrefNillable = createAttr(attrCategoricalMrefNillableName, CATEGORICAL_MREF,
				refStringIdEntityType);
		String attrDateName = "attrDate";
		attrDate = createAttr(attrDateName, DATE);
		String attrDateNillableName = "attrDateNillable";
		attrDateNillable = createAttr(attrDateNillableName, DATE);
		String attrDateTimeName = "attrDateTime";
		attrDateTime = createAttr(attrDateTimeName, DATE_TIME);
		String attrDateTimeNillableName = "attrDateTimeNillable";
		attrDateTimeNillable = createAttr(attrDateTimeNillableName, DATE_TIME);
		String attrDecimalName = "attrDecimal";
		attrDecimal = createAttr(attrDecimalName, DECIMAL);
		String attrDecimalNillableName = "attrDecimalNillable";
		attrDecimalNillable = createAttr(attrDecimalNillableName, DECIMAL);
		String attrEmailName = "attrEmail";
		attrEmail = createAttr(attrEmailName, EMAIL);
		String attrEmailNillableName = "attrEmailNillable";
		attrEmailNillable = createAttr(attrEmailNillableName, EMAIL);
		String attrEnumName = "attrEnum";
		attrEnum = createAttr(attrEnumName, ENUM);
		String attrEnumNillableName = "attrEnumNillable";
		attrEnumNillable = createAttr(attrEnumNillableName, ENUM);
		String attrFileName = "attrFile";
		attrFile = createAttr(attrFileName, FILE, fileMetaMeta);
		String attrFileNillableName = "attrFileNillable";
		attrFileNillable = createAttr(attrFileNillableName, FILE, fileMetaMeta);
		String attrHtmlName = "attrHtml";
		attrHtml = createAttr(attrHtmlName, HTML);
		String attrHtmlNillableName = "attrHtmlNillable";
		attrHtmlNillable = createAttr(attrHtmlNillableName, HTML);
		String attrHyperlinkName = "attrHyperlink";
		attrHyperlink = createAttr(attrHyperlinkName, HYPERLINK);
		String attrHyperlinkNillableName = "attrHyperlinkNillable";
		attrHyperlinkNillable = createAttr(attrHyperlinkNillableName, HYPERLINK);
		String attrIntName = "attrInt";
		attrInt = createAttr(attrIntName, INT);
		String attrIntNillableName = "attrIntNillable";
		attrIntNillable = createAttr(attrIntNillableName, INT);
		String attrLongName = "attrLong";
		attrLong = createAttr(attrLongName, LONG);
		String attrLongNillableName = "attrLongNillable";
		attrLongNillable = createAttr(attrLongNillableName, LONG);
		String attrMrefName = "attrMref";
		attrMref = createAttr(attrMrefName, MREF, refIntIdEntityType);
		String attrMrefNillableName = "attrMrefNillable";
		attrMrefNillable = createAttr(attrMrefNillableName, MREF, refIntIdEntityType);
		String attrScriptName = "attrScript";
		attrScript = createAttr(attrScriptName, SCRIPT);
		String attrScriptNillableName = "attrScriptNillable";
		attrScriptNillable = createAttr(attrScriptNillableName, SCRIPT);
		String attrStringName = "attrString";
		attrString = createAttr(attrStringName, STRING);
		String attrStringNillableName = "attrStringNillable";
		attrStringNillable = createAttr(attrStringNillableName, STRING);
		String attrTextName = "attrText";
		attrText = createAttr(attrTextName, TEXT);
		String attrTextNillableName = "attrTextNillable";
		attrTextNillable = createAttr(attrTextNillableName, TEXT);
		String attrXrefName = "attrXref";
		attrXref = createAttr(attrXrefName, XREF, refIntIdEntityType);
		String attrXrefNillableName = "attrXrefNillable";
		attrXrefNillable = createAttr(attrXrefNillableName, XREF, refIntIdEntityType);

		// create entity
		entity = mock(Entity.class);

		boolValue = true;
		when(entity.getBoolean(attrBoolName)).thenReturn(boolValue);
		when(entity.getBoolean(attrBoolNillableName)).thenReturn(null);

		categoricalValueId = "id0";
		categoricalValue = when(mock(Entity.class).getEntityType()).thenReturn(refStringIdEntityType).getMock();
		when(categoricalValue.getIdValue()).thenReturn(categoricalValueId);
		when(categoricalValue.toString()).thenReturn("categoricalValue");
		when(categoricalValue.getString(attrRefStringIdName)).thenReturn(categoricalValueId);
		when(entity.getEntity(attrCategoricalName)).thenReturn(categoricalValue);
		when(entity.getEntity(attrCategoricalNillableName)).thenReturn(null);

		categoricalMrefValueId0 = "id0";
		categoricalMrefValueId1 = "id1";
		categoricalMrefValue0 = when(mock(Entity.class).getEntityType()).thenReturn(refStringIdEntityType).getMock();
		when(categoricalMrefValue0.toString()).thenReturn("categoricalMrefValue0");
		when(categoricalMrefValue0.getIdValue()).thenReturn(categoricalMrefValueId0);
		Entity categoricalMrefValue1 = when(mock(Entity.class).getEntityType()).thenReturn(refStringIdEntityType)
																			   .getMock();
		when(categoricalMrefValue1.toString()).thenReturn("categoricalMrefValue1");
		when(categoricalMrefValue1.getIdValue()).thenReturn(categoricalMrefValueId1);
		when(categoricalMrefValue0.getString(attrRefStringIdName)).thenReturn(categoricalMrefValueId0);
		when(categoricalMrefValue1.getString(attrRefStringIdName)).thenReturn(categoricalMrefValueId1);
		when(entity.getEntities(attrCategoricalMrefName)).thenReturn(
				asList(categoricalMrefValue0, categoricalMrefValue1));
		when(entity.getEntities(attrCategoricalMrefNillableName)).thenReturn(emptyList());

		dateValue = LocalDate.of(2012, Month.DECEMBER, 21);
		when(entity.getLocalDate(attrDateName)).thenReturn(dateValue);
		when(entity.getLocalDate(attrDateNillableName)).thenReturn(null);

		offsetDateTime = OffsetDateTime.parse("1985-08-12T06:12:13Z");
		dateTimeValue = offsetDateTime.toInstant();
		when(entity.getInstant(attrDateTimeName)).thenReturn(dateTimeValue);
		when(entity.getInstant(attrDateTimeNillableName)).thenReturn(null);

		decimalValue = 1.23;
		when(entity.getDouble(attrDecimalName)).thenReturn(decimalValue);
		when(entity.getDouble(attrDecimalNillableName)).thenReturn(null);

		emailValue = "my@mail.com";
		when(entity.getString(attrEmailName)).thenReturn(emailValue);
		when(entity.getString(attrEmailNillableName)).thenReturn(null);

		enumValue = "enum0";
		when(entity.getString(attrEnumName)).thenReturn(enumValue);
		when(entity.getString(attrEnumNillableName)).thenReturn(null);

		fileValueId = "id0";
		fileValue = when(mock(FileMeta.class).getEntityType()).thenReturn(fileMetaMeta).getMock();
		when(fileValue.toString()).thenReturn("fileValue");
		when(fileValue.getIdValue()).thenReturn(fileValueId);
		when(fileValue.getString(attrRefFileIdName)).thenReturn(fileValueId);
		when(entity.getEntity(attrFileName, FileMeta.class)).thenReturn(fileValue);
		when(entity.getEntity(attrFileNillableName, FileMeta.class)).thenReturn(null);

		htmlValue = "<p>text</p>";
		when(entity.getString(attrHtmlName)).thenReturn(htmlValue);
		when(entity.getString(attrHtmlNillableName)).thenReturn(null);

		hyperlinkValue = "<p>text</p>";
		when(entity.getString(attrHyperlinkName)).thenReturn(hyperlinkValue);
		when(entity.getString(attrHyperlinkNillableName)).thenReturn(null);

		intValue = 123;
		when(entity.getInt(attrIntName)).thenReturn(intValue);
		when(entity.getInt(attrIntNillableName)).thenReturn(null);

		longValue = 123456L;
		when(entity.getLong(attrLongName)).thenReturn(longValue);
		when(entity.getLong(attrLongNillableName)).thenReturn(null);

		mrefValueId0 = 3;
		mrefValueId1 = 4;
		mrefValue0 = when(mock(Entity.class).getEntityType()).thenReturn(refIntIdEntityType).getMock();
		when(mrefValue0.toString()).thenReturn("mrefValue0");
		when(mrefValue0.getIdValue()).thenReturn(mrefValueId0);
		Entity mrefValue1 = when(mock(Entity.class).getEntityType()).thenReturn(refIntIdEntityType).getMock();
		when(mrefValue1.toString()).thenReturn("mrefValue1");
		when(mrefValue1.getIdValue()).thenReturn(mrefValueId1);
		when(mrefValue0.getInt(attrRefIntIdName)).thenReturn(mrefValueId0);
		when(mrefValue1.getInt(attrRefIntIdName)).thenReturn(mrefValueId1);
		when(entity.getEntities(attrMrefName)).thenReturn(asList(mrefValue0, mrefValue1));
		when(entity.getEntities(attrMrefNillableName)).thenReturn(emptyList());

		scriptValue = "int x = 2";
		when(entity.getString(attrScriptName)).thenReturn(scriptValue);
		when(entity.getString(attrScriptNillableName)).thenReturn(null);

		stringValue = "str";
		when(entity.getString(attrStringName)).thenReturn(stringValue);
		when(entity.getString(attrStringNillableName)).thenReturn(null);

		textValue = "text";
		when(entity.getString(attrTextName)).thenReturn(textValue);
		when(entity.getString(attrTextNillableName)).thenReturn(null);

		xrefValueId = 3;
		xrefValue = when(mock(Entity.class).getEntityType()).thenReturn(refIntIdEntityType).getMock();
		when(xrefValue.toString()).thenReturn("xrefValue");
		when(xrefValue.getIdValue()).thenReturn(xrefValueId);
		when(xrefValue.getInt(attrRefIntIdName)).thenReturn(xrefValueId);
		when(entity.getEntity(attrXrefName)).thenReturn(xrefValue);
		when(entity.getEntity(attrXrefNillableName)).thenReturn(null);

		when(entity.toString()).thenReturn("entity");
	}

	@DataProvider(name = "getPostgreSqlValue")
	public static Iterator<Object[]> getPostgreSqlValueProvider()
	{
		return asList(new Object[] { attrBool, boolValue }, new Object[] { attrBoolNillable, null },
				new Object[] { attrCategorical, categoricalValueId }, new Object[] { attrCategoricalNillable, null },
				new Object[] { attrCategoricalMref, asList(categoricalMrefValueId0, categoricalMrefValueId1) },
				new Object[] { attrCategoricalMrefNillable, emptyList() }, new Object[] { attrDate, dateValue },
				new Object[] { attrDateNillable, null }, new Object[] { attrDateTime, offsetDateTime },
				new Object[] { attrDateTimeNillable, null }, new Object[] { attrDecimal, decimalValue },
				new Object[] { attrDecimalNillable, null }, new Object[] { attrEmail, emailValue },
				new Object[] { attrEmailNillable, null }, new Object[] { attrEnum, enumValue },
				new Object[] { attrEnumNillable, null }, new Object[] { attrFile, fileValueId },
				new Object[] { attrFileNillable, null }, new Object[] { attrHtml, htmlValue },
				new Object[] { attrHtmlNillable, null }, new Object[] { attrHyperlink, hyperlinkValue },
				new Object[] { attrHyperlinkNillable, null }, new Object[] { attrInt, intValue },
				new Object[] { attrIntNillable, null }, new Object[] { attrLong, longValue },
				new Object[] { attrLongNillable, null }, new Object[] { attrScript, scriptValue },
				new Object[] { attrMref, asList(mrefValueId0, mrefValueId1) },
				new Object[] { attrMrefNillable, emptyList() }, new Object[] { attrScript, scriptValue },
				new Object[] { attrScriptNillable, null }, new Object[] { attrString, stringValue },
				new Object[] { attrStringNillable, null }, new Object[] { attrText, textValue },
				new Object[] { attrTextNillable, null }, new Object[] { attrXref, xrefValueId },
				new Object[] { attrXrefNillable, null }).iterator();
	}

	@Test(dataProvider = "getPostgreSqlValue")
	public void getPostgreSqlValue(Attribute attr, Object postgreSqlValue)
	{
		assertEquals(PostgreSqlUtils.getPostgreSqlValue(entity, attr), postgreSqlValue);
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void getPostgreSqlValueCompound()
	{
		PostgreSqlUtils.getPostgreSqlValue(mock(Entity.class), createAttr("attrCompound", COMPOUND));
	}

	@DataProvider(name = "getPostgreSqlValueQuery")
	public static Iterator<Object[]> getPostgreSqlValueQueryProvider()
	{
		return asList(new Object[] { boolValue, attrBool, boolValue }, new Object[] { null, attrBoolNillable, null },
				new Object[] { categoricalValue, attrCategorical, categoricalValueId },
				new Object[] { categoricalValueId, attrCategorical, categoricalValueId },
				new Object[] { null, attrCategoricalNillable, null },
				new Object[] { categoricalMrefValue0, attrCategoricalMref, categoricalMrefValueId0 },
				new Object[] { categoricalMrefValueId0, attrCategoricalMref, categoricalMrefValueId0 },
				new Object[] { null, attrCategoricalMrefNillable, null },
				new Object[] { dateValue, attrDate, dateValue }, new Object[] { null, attrDateNillable, null },
				new Object[] { dateTimeValue, attrDateTime, offsetDateTime },
				new Object[] { null, attrDateTimeNillable, null },
				new Object[] { decimalValue, attrDecimal, decimalValue },
				new Object[] { null, attrDecimalNillable, null }, new Object[] { emailValue, attrEmail, emailValue },
				new Object[] { null, attrEmailNillable, null },
				new Object[] { DATE /* random enum value */, attrEnum, DATE.toString() },
				new Object[] { enumValue, attrEnum, enumValue }, new Object[] { null, attrEnumNillable, null },
				new Object[] { fileValue, attrFile, fileValueId }, new Object[] { fileValueId, attrFile, fileValueId },
				new Object[] { null, attrFileNillable, null }, new Object[] { htmlValue, attrHtml, htmlValue },
				new Object[] { null, attrHtmlNillable, null },
				new Object[] { hyperlinkValue, attrHyperlink, hyperlinkValue },
				new Object[] { null, attrHyperlinkNillable, null }, new Object[] { intValue, attrInt, intValue },
				new Object[] { null, attrIntNillable, null }, new Object[] { longValue, attrLong, longValue },
				new Object[] { null, attrLongNillable, null }, new Object[] { scriptValue, attrScript, scriptValue },
				new Object[] { mrefValue0, attrMref, mrefValueId0 },
				new Object[] { mrefValueId0, attrMref, mrefValueId0 }, new Object[] { null, attrMrefNillable, null },
				new Object[] { scriptValue, attrScriptNillable, scriptValue },
				new Object[] { null, attrScriptNillable, null }, new Object[] { stringValue, attrString, stringValue },
				new Object[] { null, attrStringNillable, null }, new Object[] { textValue, attrText, textValue },
				new Object[] { null, attrTextNillable, null }, new Object[] { xrefValue, attrXref, xrefValueId },
				new Object[] { null, attrXrefNillable, null }, new Object[] { xrefValueId, attrXref, xrefValueId },
				new Object[] { null, attrXrefNillable, null }).iterator();

	}

	@Test(dataProvider = "getPostgreSqlValueQuery")
	public void getPostgreSqlValueQuery(Object value, Attribute attr, Object postgreSqlValue)
	{
		assertEquals(PostgreSqlUtils.getPostgreSqlQueryValue(value, attr), postgreSqlValue);
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void getPostgreSqlValueQueryCompound()
	{
		PostgreSqlUtils.getPostgreSqlQueryValue(null, createAttr("attrCompound", COMPOUND));
	}

	@DataProvider(name = "getPostgreSqlValueQueryException")
	public static Iterator<Object[]> getPostgreSqlValueQueryExceptionProvider()
	{
		Object valueOfWrongType = mock(Object.class);
		when(valueOfWrongType.toString()).thenReturn("valueOfWrongType");
		return asList(new Object[] { valueOfWrongType, attrBool }, new Object[] { valueOfWrongType, attrCategorical },
				new Object[] { valueOfWrongType, attrCategoricalMref, },
				new Object[] { asList(valueOfWrongType, valueOfWrongType), attrCategoricalMref, },
				new Object[] { valueOfWrongType, attrDate }, new Object[] { valueOfWrongType, attrDateTime },
				new Object[] { valueOfWrongType, attrDecimal }, new Object[] { valueOfWrongType, attrEmail },
				new Object[] { valueOfWrongType, attrEnum }, new Object[] { valueOfWrongType, attrFile },
				new Object[] { valueOfWrongType, attrFile }, new Object[] { valueOfWrongType, attrHtml },
				new Object[] { valueOfWrongType, attrHyperlink }, new Object[] { valueOfWrongType, attrInt },
				new Object[] { valueOfWrongType, attrLong }, new Object[] { valueOfWrongType, attrScript },
				new Object[] { valueOfWrongType, attrMref },
				new Object[] { asList(valueOfWrongType, valueOfWrongType), attrMref },
				new Object[] { valueOfWrongType, attrScriptNillable }, new Object[] { valueOfWrongType, attrString },
				new Object[] { valueOfWrongType, attrText }, new Object[] { valueOfWrongType, attrXref },
				new Object[] { valueOfWrongType, attrXref }).iterator();

	}

	@Test(dataProvider = "getPostgreSqlValueQueryException", expectedExceptions = MolgenisDataException.class)
	public void getPostgreSqlValueQueryException(Object value, Attribute attr)
	{
		PostgreSqlUtils.getPostgreSqlQueryValue(value, attr);
	}

	private static Attribute createAttr(String attrName, AttributeType attrType)
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getName()).thenReturn(attrName);
		when(attr.getDataType()).thenReturn(attrType);
		when(attr.toString()).thenReturn(attrName);
		return attr;
	}

	private static Attribute createAttr(String attrName, AttributeType attrType, EntityType refEntityMeta)
	{
		Attribute attr = createAttr(attrName, attrType);
		when(attr.getRefEntity()).thenReturn(refEntityMeta);
		return attr;
	}
}