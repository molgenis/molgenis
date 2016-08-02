package org.molgenis.data.postgresql;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.file.model.FileMeta;
import org.molgenis.file.model.FileMetaMetaData;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.molgenis.util.MolgenisDateFormat.getDateFormat;
import static org.molgenis.util.MolgenisDateFormat.getDateTimeFormat;
import static org.testng.Assert.assertEquals;

/**
 * Created by Dennis on 8/2/2016.
 */
public class PostgreSqlUtilsTest
{
	@DataProvider(name = "getPostgreSqlValue")
	public static Iterator<Object[]> getPostgreSqlValueProvider() throws ParseException
	{
		String attrBoolName = "attrBool";
		AttributeMetaData attrBool = mock(AttributeMetaData.class);
		when(attrBool.getName()).thenReturn(attrBoolName);
		when(attrBool.getDataType()).thenReturn(BOOL);
		when(attrBool.toString()).thenReturn(attrBoolName);

		String attrBoolNillableName = "attrBoolNillable";
		AttributeMetaData attrBoolNillable = mock(AttributeMetaData.class);
		when(attrBoolNillable.getName()).thenReturn(attrBoolNillableName);
		when(attrBoolNillable.getDataType()).thenReturn(BOOL);
		when(attrBoolNillable.toString()).thenReturn(attrBoolNillableName);

		String attrCategoricalName = "attrCategorical";
		AttributeMetaData attrCategorical = mock(AttributeMetaData.class);
		when(attrCategorical.getName()).thenReturn(attrCategoricalName);
		when(attrCategorical.getDataType()).thenReturn(CATEGORICAL);
		when(attrCategorical.toString()).thenReturn(attrCategoricalName);

		String attrCategoricalNillableName = "attrCategoricalNillable";
		AttributeMetaData attrCategoricalNillable = mock(AttributeMetaData.class);
		when(attrCategoricalNillable.getName()).thenReturn(attrCategoricalNillableName);
		when(attrCategoricalNillable.getDataType()).thenReturn(CATEGORICAL);
		when(attrCategoricalNillable.toString()).thenReturn(attrCategoricalNillableName);

		String attrCategoricalMrefName = "attrCategoricalMref";
		AttributeMetaData attrCategoricalMref = mock(AttributeMetaData.class);
		when(attrCategoricalMref.getName()).thenReturn(attrCategoricalMrefName);
		when(attrCategoricalMref.getDataType()).thenReturn(CATEGORICAL_MREF);
		when(attrCategoricalMref.toString()).thenReturn(attrCategoricalMrefName);

		String attrCategoricalMrefNillableName = "attrCategoricalMrefNillable";
		AttributeMetaData attrCategoricalMrefNillable = mock(AttributeMetaData.class);
		when(attrCategoricalMrefNillable.getName()).thenReturn(attrCategoricalMrefNillableName);
		when(attrCategoricalMrefNillable.getDataType()).thenReturn(CATEGORICAL_MREF);
		when(attrCategoricalMrefNillable.toString()).thenReturn(attrCategoricalMrefNillableName);

		String attrDateName = "attrDate";
		AttributeMetaData attrDate = mock(AttributeMetaData.class);
		when(attrDate.getName()).thenReturn(attrDateName);
		when(attrDate.getDataType()).thenReturn(DATE);
		when(attrDate.toString()).thenReturn(attrDateName);

		String attrDateNillableName = "attrDateNillable";
		AttributeMetaData attrDateNillable = mock(AttributeMetaData.class);
		when(attrDateNillable.getName()).thenReturn(attrDateNillableName);
		when(attrDateNillable.getDataType()).thenReturn(DATE);
		when(attrDateNillable.toString()).thenReturn(attrDateNillableName);

		String attrDateTimeName = "attrDateTime";
		AttributeMetaData attrDateTime = mock(AttributeMetaData.class);
		when(attrDateTime.getName()).thenReturn(attrDateTimeName);
		when(attrDateTime.getDataType()).thenReturn(DATE_TIME);
		when(attrDateTime.toString()).thenReturn(attrDateTimeName);

		String attrDateTimeNillableName = "attrDateTimeNillable";
		AttributeMetaData attrDateTimeNillable = mock(AttributeMetaData.class);
		when(attrDateTimeNillable.getName()).thenReturn(attrDateTimeNillableName);
		when(attrDateTimeNillable.getDataType()).thenReturn(DATE_TIME);
		when(attrDateTimeNillable.toString()).thenReturn(attrDateTimeNillableName);

		String attrDecimalName = "attrDecimal";
		AttributeMetaData attrDecimal = mock(AttributeMetaData.class);
		when(attrDecimal.getName()).thenReturn(attrDecimalName);
		when(attrDecimal.getDataType()).thenReturn(DECIMAL);
		when(attrDecimal.toString()).thenReturn(attrDecimalName);

		String attrDecimalNillableName = "attrDecimalNillable";
		AttributeMetaData attrDecimalNillable = mock(AttributeMetaData.class);
		when(attrDecimalNillable.getName()).thenReturn(attrDecimalNillableName);
		when(attrDecimalNillable.getDataType()).thenReturn(DECIMAL);
		when(attrDecimalNillable.toString()).thenReturn(attrDecimalNillableName);

		String attrEmailName = "attrEmail";
		AttributeMetaData attrEmail = mock(AttributeMetaData.class);
		when(attrEmail.getName()).thenReturn(attrEmailName);
		when(attrEmail.getDataType()).thenReturn(EMAIL);
		when(attrEmail.toString()).thenReturn(attrEmailName);

		String attrEmailNillableName = "attrEmailNillable";
		AttributeMetaData attrEmailNillable = mock(AttributeMetaData.class);
		when(attrEmailNillable.getName()).thenReturn(attrEmailNillableName);
		when(attrEmailNillable.getDataType()).thenReturn(EMAIL);
		when(attrEmailNillable.toString()).thenReturn(attrEmailNillableName);

		String attrEnumName = "attrEnum";
		AttributeMetaData attrEnum = mock(AttributeMetaData.class);
		when(attrEnum.getName()).thenReturn(attrEnumName);
		when(attrEnum.getDataType()).thenReturn(ENUM);
		when(attrEnum.toString()).thenReturn(attrEnumName);

		String attrEnumNillableName = "attrEnumNillable";
		AttributeMetaData attrEnumNillable = mock(AttributeMetaData.class);
		when(attrEnumNillable.getName()).thenReturn(attrEnumNillableName);
		when(attrEnumNillable.getDataType()).thenReturn(ENUM);
		when(attrEnumNillable.toString()).thenReturn(attrEnumNillableName);

		String attrFileName = "attrFile";
		AttributeMetaData attrFile = mock(AttributeMetaData.class);
		when(attrFile.getName()).thenReturn(attrFileName);
		when(attrFile.getDataType()).thenReturn(FILE);
		when(attrFile.toString()).thenReturn(attrFileName);

		String attrFileNillableName = "attrFileNillable";
		AttributeMetaData attrFileNillable = mock(AttributeMetaData.class);
		when(attrFileNillable.getName()).thenReturn(attrFileNillableName);
		when(attrFileNillable.getDataType()).thenReturn(FILE);
		when(attrFileNillable.toString()).thenReturn(attrFileNillableName);

		String attrHtmlName = "attrHtml";
		AttributeMetaData attrHtml = mock(AttributeMetaData.class);
		when(attrHtml.getName()).thenReturn(attrHtmlName);
		when(attrHtml.getDataType()).thenReturn(HTML);
		when(attrHtml.toString()).thenReturn(attrHtmlName);

		String attrHtmlNillableName = "attrHtmlNillable";
		AttributeMetaData attrHtmlNillable = mock(AttributeMetaData.class);
		when(attrHtmlNillable.getName()).thenReturn(attrHtmlNillableName);
		when(attrHtmlNillable.getDataType()).thenReturn(HTML);
		when(attrHtmlNillable.toString()).thenReturn(attrHtmlNillableName);

		String attrHyperlinkName = "attrHyperlink";
		AttributeMetaData attrHyperlink = mock(AttributeMetaData.class);
		when(attrHyperlink.getName()).thenReturn(attrHyperlinkName);
		when(attrHyperlink.getDataType()).thenReturn(HYPERLINK);
		when(attrHyperlink.toString()).thenReturn(attrHyperlinkName);

		String attrHyperlinkNillableName = "attrHyperlinkNillable";
		AttributeMetaData attrHyperlinkNillable = mock(AttributeMetaData.class);
		when(attrHyperlinkNillable.getName()).thenReturn(attrHyperlinkNillableName);
		when(attrHyperlinkNillable.getDataType()).thenReturn(HYPERLINK);
		when(attrHyperlinkNillable.toString()).thenReturn(attrHyperlinkNillableName);

		String attrIntName = "attrInt";
		AttributeMetaData attrInt = mock(AttributeMetaData.class);
		when(attrInt.getName()).thenReturn(attrIntName);
		when(attrInt.getDataType()).thenReturn(INT);
		when(attrInt.toString()).thenReturn(attrIntName);

		String attrIntNillableName = "attrIntNillable";
		AttributeMetaData attrIntNillable = mock(AttributeMetaData.class);
		when(attrIntNillable.getName()).thenReturn(attrIntNillableName);
		when(attrIntNillable.getDataType()).thenReturn(INT);
		when(attrIntNillable.toString()).thenReturn(attrIntNillableName);

		String attrLongName = "attrLong";
		AttributeMetaData attrLong = mock(AttributeMetaData.class);
		when(attrLong.getName()).thenReturn(attrLongName);
		when(attrLong.getDataType()).thenReturn(LONG);
		when(attrLong.toString()).thenReturn(attrLongName);

		String attrLongNillableName = "attrLongNillable";
		AttributeMetaData attrLongNillable = mock(AttributeMetaData.class);
		when(attrLongNillable.getName()).thenReturn(attrLongNillableName);
		when(attrLongNillable.getDataType()).thenReturn(LONG);
		when(attrLongNillable.toString()).thenReturn(attrLongNillableName);

		String attrMrefName = "attrMref";
		AttributeMetaData attrMref = mock(AttributeMetaData.class);
		when(attrMref.getName()).thenReturn(attrMrefName);
		when(attrMref.getDataType()).thenReturn(MREF);
		when(attrMref.toString()).thenReturn(attrMrefName);

		String attrMrefNillableName = "attrMrefNillable";
		AttributeMetaData attrMrefNillable = mock(AttributeMetaData.class);
		when(attrMrefNillable.getName()).thenReturn(attrMrefNillableName);
		when(attrMrefNillable.getDataType()).thenReturn(MREF);
		when(attrMrefNillable.toString()).thenReturn(attrMrefNillableName);

		String attrScriptName = "attrScript";
		AttributeMetaData attrScript = mock(AttributeMetaData.class);
		when(attrScript.getName()).thenReturn(attrScriptName);
		when(attrScript.getDataType()).thenReturn(SCRIPT);
		when(attrScript.toString()).thenReturn(attrScriptName);

		String attrScriptNillableName = "attrScriptNillable";
		AttributeMetaData attrScriptNillable = mock(AttributeMetaData.class);
		when(attrScriptNillable.getName()).thenReturn(attrScriptNillableName);
		when(attrScriptNillable.getDataType()).thenReturn(SCRIPT);
		when(attrScriptNillable.toString()).thenReturn(attrScriptNillableName);

		String attrStringName = "attrString";
		AttributeMetaData attrString = mock(AttributeMetaData.class);
		when(attrString.getName()).thenReturn(attrStringName);
		when(attrString.getDataType()).thenReturn(STRING);
		when(attrString.toString()).thenReturn(attrStringName);

		String attrStringNillableName = "attrStringNillable";
		AttributeMetaData attrStringNillable = mock(AttributeMetaData.class);
		when(attrStringNillable.getName()).thenReturn(attrStringNillableName);
		when(attrStringNillable.getDataType()).thenReturn(STRING);
		when(attrStringNillable.toString()).thenReturn(attrStringNillableName);

		String attrTextName = "attrText";
		AttributeMetaData attrText = mock(AttributeMetaData.class);
		when(attrText.getName()).thenReturn(attrTextName);
		when(attrText.getDataType()).thenReturn(TEXT);
		when(attrText.toString()).thenReturn(attrTextName);

		String attrTextNillableName = "attrTextNillable";
		AttributeMetaData attrTextNillable = mock(AttributeMetaData.class);
		when(attrTextNillable.getName()).thenReturn(attrTextNillableName);
		when(attrTextNillable.getDataType()).thenReturn(TEXT);
		when(attrTextNillable.toString()).thenReturn(attrTextNillableName);

		String attrXrefName = "attrXref";
		AttributeMetaData attrXref = mock(AttributeMetaData.class);
		when(attrXref.getName()).thenReturn(attrXrefName);
		when(attrXref.getDataType()).thenReturn(XREF);
		when(attrXref.toString()).thenReturn(attrXrefName);

		String attrXrefNillableName = "attrXrefNillable";
		AttributeMetaData attrXrefNillable = mock(AttributeMetaData.class);
		when(attrXrefNillable.getName()).thenReturn(attrXrefNillableName);
		when(attrXrefNillable.getDataType()).thenReturn(XREF);
		when(attrXrefNillable.toString()).thenReturn(attrXrefNillableName);

		String attrRefStringIdName = "refStringId";
		AttributeMetaData attrRefStringId = mock(AttributeMetaData.class);
		when(attrRefStringId.getName()).thenReturn(attrRefStringIdName);
		when(attrRefStringId.getDataType()).thenReturn(STRING);

		EntityMetaData refStringIdEntityMeta = mock(EntityMetaData.class);
		when(refStringIdEntityMeta.getIdAttribute()).thenReturn(attrRefStringId);

		String attrRefIntIdName = "refIntId";
		AttributeMetaData attrRefIntId = mock(AttributeMetaData.class);
		when(attrRefIntId.getName()).thenReturn(attrRefIntIdName);
		when(attrRefIntId.getDataType()).thenReturn(INT);

		EntityMetaData refIntIdEntityMeta = mock(EntityMetaData.class);
		when(refIntIdEntityMeta.getIdAttribute()).thenReturn(attrRefIntId);

		Entity entity = mock(Entity.class);

		boolean boolValue = true;
		when(entity.getBoolean(attrBoolName)).thenReturn(boolValue);
		when(entity.getBoolean(attrBoolNillableName)).thenReturn(null);

		String categoricalValueId = "id0";
		Entity categoricalValue = when(mock(Entity.class).getEntityMetaData()).thenReturn(refStringIdEntityMeta)
				.getMock();
		when(categoricalValue.getString(attrRefStringIdName)).thenReturn(categoricalValueId);
		when(entity.getEntity(attrCategoricalName)).thenReturn(categoricalValue);
		when(entity.getEntity(attrCategoricalNillableName)).thenReturn(null);

		String categoricalMrefValueId0 = "id0";
		String categoricalMrefValueId1 = "id1";
		Entity categoricalMrefValue0 = when(mock(Entity.class).getEntityMetaData()).thenReturn(refStringIdEntityMeta)
				.getMock();
		Entity categoricalMrefValue1 = when(mock(Entity.class).getEntityMetaData()).thenReturn(refStringIdEntityMeta)
				.getMock();
		when(categoricalMrefValue0.getString(attrRefStringIdName)).thenReturn(categoricalMrefValueId0);
		when(categoricalMrefValue1.getString(attrRefStringIdName)).thenReturn(categoricalMrefValueId1);
		when(entity.getEntities(attrCategoricalMrefName))
				.thenReturn(asList(categoricalMrefValue0, categoricalMrefValue1));
		when(entity.getEntities(attrCategoricalMrefNillableName)).thenReturn(emptyList());

		Date dateValue = getDateFormat().parse("2012-12-21");
		when(entity.getUtilDate(attrDateName)).thenReturn(dateValue);
		when(entity.getUtilDate(attrDateNillableName)).thenReturn(null);

		Date dateTimeValue = getDateTimeFormat().parse("1985-08-12T11:12:13+0500");
		when(entity.getUtilDate(attrDateTimeName)).thenReturn(dateTimeValue);
		when(entity.getUtilDate(attrDateTimeNillableName)).thenReturn(null);

		Double decimalValue = 1.23;
		when(entity.getDouble(attrDecimalName)).thenReturn(decimalValue);
		when(entity.getDouble(attrDecimalNillableName)).thenReturn(null);

		String emailValue = "my@mail.com";
		when(entity.getString(attrEmailName)).thenReturn(emailValue);
		when(entity.getString(attrEmailNillableName)).thenReturn(null);

		String enumValue = "enum0";
		when(entity.getString(attrEnumName)).thenReturn(enumValue);
		when(entity.getString(attrEnumNillableName)).thenReturn(null);

		String attrRefFileIdName = FileMetaMetaData.ID;
		AttributeMetaData attrRefFileId = mock(AttributeMetaData.class);
		when(attrRefFileId.getName()).thenReturn(attrRefFileIdName);
		when(attrRefFileId.getDataType()).thenReturn(STRING);

		FileMetaMetaData fileMetaMeta = mock(FileMetaMetaData.class);
		when(fileMetaMeta.getIdAttribute()).thenReturn(attrRefFileId);

		String fileValueId = "id0";
		FileMeta fileValue = when(mock(FileMeta.class).getEntityMetaData()).thenReturn(fileMetaMeta).getMock();
		when(fileValue.getString(attrRefFileIdName)).thenReturn(fileValueId);
		when(entity.getEntity(attrFileName, FileMeta.class)).thenReturn(fileValue);
		when(entity.getEntity(attrFileNillableName, FileMeta.class)).thenReturn(null);

		String htmlValue = "<p>text</p>";
		when(entity.getString(attrHtmlName)).thenReturn(htmlValue);
		when(entity.getString(attrHtmlNillableName)).thenReturn(null);

		String hyperlinkValue = "<p>text</p>";
		when(entity.getString(attrHyperlinkName)).thenReturn(hyperlinkValue);
		when(entity.getString(attrHyperlinkNillableName)).thenReturn(null);

		Integer intValue = 123;
		when(entity.getInt(attrIntName)).thenReturn(intValue);
		when(entity.getInt(attrIntNillableName)).thenReturn(null);

		Long longValue = 123456L;
		when(entity.getLong(attrLongName)).thenReturn(longValue);
		when(entity.getLong(attrLongNillableName)).thenReturn(null);

		int mrefValueId0 = 3;
		int mrefValueId1 = 4;
		Entity mrefValue0 = when(mock(Entity.class).getEntityMetaData()).thenReturn(refIntIdEntityMeta).getMock();
		Entity mrefValue1 = when(mock(Entity.class).getEntityMetaData()).thenReturn(refIntIdEntityMeta).getMock();
		when(mrefValue0.getInt(attrRefIntIdName)).thenReturn(mrefValueId0);
		when(mrefValue1.getInt(attrRefIntIdName)).thenReturn(mrefValueId1);
		when(entity.getEntities(attrMrefName)).thenReturn(asList(mrefValue0, mrefValue1));
		when(entity.getEntities(attrMrefNillableName)).thenReturn(emptyList());

		String scriptValue = "int x = 2";
		when(entity.getString(attrScriptName)).thenReturn(scriptValue);
		when(entity.getString(attrScriptNillableName)).thenReturn(null);

		String stringValue = "str";
		when(entity.getString(attrStringName)).thenReturn(stringValue);
		when(entity.getString(attrStringNillableName)).thenReturn(null);

		String textValue = "text";
		when(entity.getString(attrTextName)).thenReturn(textValue);
		when(entity.getString(attrTextNillableName)).thenReturn(null);

		int xrefValueId = 3;
		Entity xrefValue = when(mock(Entity.class).getEntityMetaData()).thenReturn(refIntIdEntityMeta).getMock();
		when(xrefValue.getInt(attrRefIntIdName)).thenReturn(xrefValueId);
		when(entity.getEntity(attrXrefName)).thenReturn(xrefValue);
		when(entity.getEntity(attrXrefNillableName)).thenReturn(null);

		when(entity.toString()).thenReturn("entity");

		// TODO COMPOUND, FILE

		return asList(new Object[] { entity, attrBool, true }, new Object[] { entity, attrBoolNillable, null },
				new Object[] { entity, attrCategorical, categoricalValueId },
				new Object[] { entity, attrCategoricalNillable, null },
				new Object[] { entity, attrCategoricalMref, asList(categoricalMrefValueId0, categoricalMrefValueId1) },
				new Object[] { entity, attrCategoricalMrefNillable, emptyList() },
				new Object[] { entity, attrDate, new java.sql.Date(dateValue.getTime()) },
				new Object[] { entity, attrDateNillable, null },
				new Object[] { entity, attrDateTime, new java.sql.Timestamp(dateTimeValue.getTime()) },
				new Object[] { entity, attrDateTimeNillable, null }, new Object[] { entity, attrDecimal, decimalValue },
				new Object[] { entity, attrDecimalNillable, null }, new Object[] { entity, attrEmail, emailValue },
				new Object[] { entity, attrEmailNillable, null }, new Object[] { entity, attrEnum, enumValue },
				new Object[] { entity, attrEnumNillable, null }, new Object[] { entity, attrHtml, htmlValue },
				new Object[] { entity, attrFile, fileValueId }, new Object[] { entity, attrFileNillable, null },
				new Object[] { entity, attrHtmlNillable, null }, new Object[] { entity, attrHyperlink, hyperlinkValue },
				new Object[] { entity, attrHyperlinkNillable, null }, new Object[] { entity, attrInt, intValue },
				new Object[] { entity, attrIntNillable, null }, new Object[] { entity, attrLong, longValue },
				new Object[] { entity, attrLongNillable, null }, new Object[] { entity, attrScript, scriptValue },
				new Object[] { entity, attrMref, asList(mrefValueId0, mrefValueId1) },
				new Object[] { entity, attrMrefNillable, emptyList() },
				new Object[] { entity, attrScriptNillable, null }, new Object[] { entity, attrString, stringValue },
				new Object[] { entity, attrStringNillable, null }, new Object[] { entity, attrText, textValue },
				new Object[] { entity, attrTextNillable, null }, new Object[] { entity, attrXref, xrefValueId },
				new Object[] { entity, attrXrefNillable, null }).iterator();
	}

	@Test(dataProvider = "getPostgreSqlValue")
	public void getPostgreSqlValue(Entity entity, AttributeMetaData attr, Object postgreSqlValue)
	{
		assertEquals(PostgreSqlUtils.getPostgreSqlValue(entity, attr), postgreSqlValue);
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void getPostgreSqlValueCompound()
	{
		AttributeMetaData attrCompound = when(mock(AttributeMetaData.class).getDataType()).thenReturn(COMPOUND)
				.getMock();
		PostgreSqlUtils.getPostgreSqlValue(mock(Entity.class), attrCompound);
	}
}