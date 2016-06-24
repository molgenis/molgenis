package org.molgenis.data.elasticsearch.index;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.testng.Assert.assertEquals;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.fieldtypes.EnumField;
import org.molgenis.util.MolgenisDateFormat;
import org.testng.annotations.Test;

public class EntityToSourceConverterTest
{
	@Test
	public void convert() throws ParseException
	{
		String entityName = "entity";
		String refEntityName = "refentity";

		String idAttributeName = "id";

		String refLabelAttributeName = "reflabel";
		String refMrefAttributeName = "refmref";
		DefaultEntityMetaData refEntityMetaData = new DefaultEntityMetaData(refEntityName);
		refEntityMetaData.addAttribute(idAttributeName, ROLE_ID);
		refEntityMetaData.addAttribute(refLabelAttributeName, ROLE_LABEL).setUnique(true);
		refEntityMetaData.addAttribute(refMrefAttributeName).setDataType(MolgenisFieldTypes.MREF).setNillable(true)
				.setRefEntity(refEntityMetaData);

		String boolAttributeName = "xbool";
		String categoricalAttributeName = "xcategorical";
		String compoundAttributeName = "xcompound";
		String compoundPart0AttributeName = "xcompoundpart0";
		String compoundPart1AttributeName = "xcompoundpart1";
		String dateAttributeName = "xdate";
		String dateTimeAttributeName = "xdatetime";
		String decimalAttributeName = "xdecimal";
		String emailAttributeName = "xemail";
		String enumAttributeName = "xenum";
		String htmlAttributeName = "xhtml";
		String hyperlinkAttributeName = "xhyperlink";
		String intAttributeName = "xint";
		String longAttributeName = "xlong";
		String mrefAttributeName = "xmref";
		String scriptAttributeName = "xscript";
		String stringAttributeName = "xstring";
		String textAttributeName = "xtext";
		String xrefAttributeName = "xxref";

		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData("entity");
		entityMetaData.addAttribute(idAttributeName, ROLE_ID);
		entityMetaData.addAttribute(boolAttributeName).setDataType(MolgenisFieldTypes.BOOL);
		entityMetaData.addAttribute(categoricalAttributeName).setDataType(MolgenisFieldTypes.CATEGORICAL)
				.setRefEntity(refEntityMetaData);
		DefaultAttributeMetaData compoundPart0Attribute = new DefaultAttributeMetaData(compoundPart0AttributeName)
				.setDataType(MolgenisFieldTypes.STRING);
		DefaultAttributeMetaData compoundPart1Attribute = new DefaultAttributeMetaData(compoundPart1AttributeName)
				.setDataType(MolgenisFieldTypes.STRING);
		entityMetaData.addAttribute(compoundAttributeName).setDataType(MolgenisFieldTypes.COMPOUND)
				.setAttributesMetaData(
						Arrays.<AttributeMetaData> asList(compoundPart0Attribute, compoundPart1Attribute));
		entityMetaData.addAttribute(dateAttributeName).setDataType(MolgenisFieldTypes.DATE);
		entityMetaData.addAttribute(dateTimeAttributeName).setDataType(MolgenisFieldTypes.DATETIME);
		entityMetaData.addAttribute(decimalAttributeName).setDataType(MolgenisFieldTypes.DECIMAL);
		entityMetaData.addAttribute(emailAttributeName).setDataType(MolgenisFieldTypes.EMAIL);
		entityMetaData.addAttribute(enumAttributeName).setDataType(new EnumField());
		entityMetaData.addAttribute(htmlAttributeName).setDataType(MolgenisFieldTypes.HTML);
		entityMetaData.addAttribute(hyperlinkAttributeName).setDataType(MolgenisFieldTypes.HYPERLINK);
		entityMetaData.addAttribute(intAttributeName).setDataType(MolgenisFieldTypes.INT);
		entityMetaData.addAttribute(longAttributeName).setDataType(MolgenisFieldTypes.LONG);
		entityMetaData.addAttribute(mrefAttributeName).setDataType(MolgenisFieldTypes.MREF)
				.setRefEntity(refEntityMetaData);
		entityMetaData.addAttribute(scriptAttributeName).setDataType(MolgenisFieldTypes.SCRIPT);
		entityMetaData.addAttribute(stringAttributeName).setDataType(MolgenisFieldTypes.STRING);
		entityMetaData.addAttribute(textAttributeName).setDataType(MolgenisFieldTypes.TEXT);
		entityMetaData.addAttribute(xrefAttributeName).setDataType(MolgenisFieldTypes.XREF)
				.setRefEntity(refEntityMetaData);

		MapEntity refEntity0 = new MapEntity(idAttributeName);
		String refIdValue0 = "refid0";
		String refIdValue1 = "refid1";
		String refLabelValue0 = "label0";
		String refLabelValue1 = "label1";
		refEntity0.set(idAttributeName, refIdValue0);
		refEntity0.set(refLabelAttributeName, refLabelValue0);
		MapEntity refEntity1 = new MapEntity(idAttributeName);
		refEntity1.set(idAttributeName, refIdValue1);
		refEntity1.set(refLabelAttributeName, refLabelValue1);
		refEntity1.set(refMrefAttributeName, Arrays.asList(refEntity0, refEntity1));

		String idValue = "entityid";
		Boolean boolValue = Boolean.TRUE;
		Entity categoricalValue = refEntity0;
		String compoundPart0Value = "compoundpart0";
		String compoundPart1Value = "compoundpart1";
		String dateValueStr = "2014-09-03";
		Date dateValue = MolgenisDateFormat.getDateFormat().parse(dateValueStr);
		String dateTimeValueStr = "2014-09-03T08:02:10+0200";
		Date dateTimeValue = MolgenisDateFormat.getDateTimeFormat().parse(dateTimeValueStr);
		Double decimalValue = Double.valueOf(1.23);
		String emailValue = "test@email.com";
		String enumValue = "enumval";
		String htmlValue = "<h1>html</h1>";
		String hyperlinkValue = "http://www.website.com/";
		Integer intValue = Integer.valueOf(1);
		Long longValue = Long.valueOf(12147483647l);
		List<MapEntity> mrefValue = Arrays.asList(refEntity0, refEntity1);
		String scriptValue = "a cool R script";
		String stringValue = "string";
		String textValue = "some interesting text";
		Entity xrefValue = refEntity1;

		MapEntity entity = new MapEntity(idAttributeName);
		entity.set(idAttributeName, idValue);
		entity.set(boolAttributeName, boolValue);
		entity.set(categoricalAttributeName, categoricalValue);
		entity.set(compoundPart0AttributeName, compoundPart0Value);
		entity.set(compoundPart1AttributeName, compoundPart1Value);
		entity.set(dateAttributeName, dateValue);
		entity.set(dateTimeAttributeName, dateTimeValue);
		entity.set(decimalAttributeName, decimalValue);
		entity.set(emailAttributeName, emailValue);
		entity.set(enumAttributeName, enumValue);
		entity.set(htmlAttributeName, htmlValue);
		entity.set(hyperlinkAttributeName, hyperlinkValue);
		entity.set(intAttributeName, intValue);
		entity.set(longAttributeName, longValue);
		entity.set(mrefAttributeName, mrefValue);
		entity.set(scriptAttributeName, scriptValue);
		entity.set(stringAttributeName, stringValue);
		entity.set(textAttributeName, textValue);
		entity.set(xrefAttributeName, xrefValue);

		Map<String, Object> expectedRefEntity0Value = new HashMap<String, Object>();
		expectedRefEntity0Value.put(idAttributeName, refIdValue0);
		expectedRefEntity0Value.put(refLabelAttributeName, refLabelValue0);
		expectedRefEntity0Value.put(refMrefAttributeName, null);

		Map<String, Object> expectedRefEntity1Value = new HashMap<String, Object>();
		expectedRefEntity1Value.put(idAttributeName, refIdValue1);
		expectedRefEntity1Value.put(refLabelAttributeName, refLabelValue1);
		expectedRefEntity1Value.put(refMrefAttributeName, Arrays.asList(refIdValue0, refIdValue1));

		DataService dataService = mock(DataService.class);
		when(dataService.getEntityMetaData(entityName)).thenReturn(entityMetaData);
		when(dataService.getEntityMetaData(refEntityName)).thenReturn(refEntityMetaData);

		Map<String, Object> expectedSource = new HashMap<String, Object>();
		expectedSource.put(idAttributeName, idValue);
		expectedSource.put(boolAttributeName, boolValue);
		expectedSource.put(categoricalAttributeName, expectedRefEntity0Value);
		expectedSource.put(dateAttributeName, dateValueStr);
		expectedSource.put(dateTimeAttributeName, dateTimeValueStr);
		expectedSource.put(decimalAttributeName, decimalValue);
		expectedSource.put(emailAttributeName, emailValue);
		expectedSource.put(enumAttributeName, enumValue);
		expectedSource.put(htmlAttributeName, htmlValue);
		expectedSource.put(hyperlinkAttributeName, hyperlinkValue);
		expectedSource.put(intAttributeName, intValue);
		expectedSource.put(longAttributeName, longValue);
		expectedSource.put(mrefAttributeName, Arrays.asList(expectedRefEntity0Value, expectedRefEntity1Value));
		expectedSource.put(scriptAttributeName, scriptValue);
		expectedSource.put(stringAttributeName, stringValue);
		expectedSource.put(textAttributeName, textValue);
		expectedSource.put(xrefAttributeName, expectedRefEntity1Value);
		expectedSource.put(compoundPart0AttributeName, compoundPart0Value);
		expectedSource.put(compoundPart1AttributeName, compoundPart1Value);

		Map<String, Object> source = new EntityToSourceConverter().convert(entity, entityMetaData);
		assertEquals(source, expectedSource);
	}
}
