package org.molgenis.data.support;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Set;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.util.MolgenisDateFormat;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class TransformedEntityTest
{
	private Entity transformedEntity;
	private EntityMetaData transformedEntityMetaData;

	private String idAttributeName = "id";
	private String boolAttributeName = "xbool";
	private String categoricalAttributeName = "xcategorical";
	private String compoundAttributeName = "xcompound";
	private String compoundPart0AttributeName = "xcompoundpart0";
	private String compoundPart1AttributeName = "xcompoundpart1";
	private String dateAttributeName = "xdate";
	private String dateTimeAttributeName = "xdatetime";
	private String decimalAttributeName = "xdecimal";
	private String emailAttributeName = "xemail";
	private String enumAttributeName = "xenum";
	private String htmlAttributeName = "xhtml";
	private String hyperlinkAttributeName = "xhyperlink";
	private String intAttributeName = "xint";
	private String longAttributeName = "xlong";
	private String mrefAttributeName = "xmref";
	private String scriptAttributeName = "xscript";
	private String stringAttributeName = "xstring";
	private String textAttributeName = "xtext";
	private String xrefAttributeName = "xxref";

	private String refIdAttributeName = "refid";

	@BeforeMethod
	public void setUp()
	{
		MapEntity mapEntity = new MapEntity();
		mapEntity.set(idAttributeName, "id0");
		mapEntity.set(boolAttributeName, "true");
		mapEntity.set(categoricalAttributeName, "ref0");
		mapEntity.set(compoundAttributeName, "compound");
		mapEntity.set(compoundPart0AttributeName, "0,1,2");
		mapEntity.set(compoundPart1AttributeName, "str0,str1,str2");
		mapEntity.set(dateAttributeName, "2014-09-03");
		mapEntity.set(dateTimeAttributeName, "2014-09-03T08:02:10+0200");
		mapEntity.set(decimalAttributeName, "1.23");
		mapEntity.set(emailAttributeName, "email");
		mapEntity.set(enumAttributeName, "enum");
		mapEntity.set(htmlAttributeName, "html");
		mapEntity.set(hyperlinkAttributeName, "hyperlink");
		mapEntity.set(intAttributeName, "1");
		mapEntity.set(longAttributeName, "2147483647");
		mapEntity.set(mrefAttributeName, "ref0,ref1");
		mapEntity.set(scriptAttributeName, "script");
		mapEntity.set(stringAttributeName, "string");
		mapEntity.set(textAttributeName, "text");
		mapEntity.set(xrefAttributeName, "ref1");
		mapEntity.set("attribute-not-meta-data", "some value");

		DefaultEntityMetaData refEntityMetaData = new DefaultEntityMetaData("refentity");
		refEntityMetaData.addAttribute(refIdAttributeName).setDataType(MolgenisFieldTypes.STRING).setIdAttribute(true);

		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData("entity");
		entityMetaData.addAttribute(idAttributeName).setDataType(MolgenisFieldTypes.STRING).setIdAttribute(true);
		entityMetaData.addAttribute(boolAttributeName).setDataType(MolgenisFieldTypes.BOOL);
		entityMetaData.addAttribute(categoricalAttributeName).setDataType(MolgenisFieldTypes.CATEGORICAL)
				.setRefEntity(refEntityMetaData);
		;
		entityMetaData.addAttribute(compoundAttributeName).setDataType(MolgenisFieldTypes.COMPOUND);
		entityMetaData.addAttribute(compoundPart0AttributeName).setDataType(MolgenisFieldTypes.STRING);
		entityMetaData.addAttribute(compoundPart1AttributeName).setDataType(MolgenisFieldTypes.STRING);
		entityMetaData.addAttribute(dateAttributeName).setDataType(MolgenisFieldTypes.DATE);
		entityMetaData.addAttribute(dateTimeAttributeName).setDataType(MolgenisFieldTypes.DATETIME);
		entityMetaData.addAttribute(decimalAttributeName).setDataType(MolgenisFieldTypes.DECIMAL);
		entityMetaData.addAttribute(emailAttributeName).setDataType(MolgenisFieldTypes.EMAIL);
		entityMetaData.addAttribute(enumAttributeName).setDataType(MolgenisFieldTypes.ENUM);
		entityMetaData.addAttribute(htmlAttributeName).setDataType(MolgenisFieldTypes.HTML);
		entityMetaData.addAttribute(hyperlinkAttributeName).setDataType(MolgenisFieldTypes.HYPERLINK);
		entityMetaData.addAttribute(intAttributeName).setDataType(MolgenisFieldTypes.INT);
		entityMetaData.addAttribute(longAttributeName).setDataType(MolgenisFieldTypes.LONG);
		entityMetaData.addAttribute(mrefAttributeName).setDataType(MolgenisFieldTypes.MREF)
				.setRefEntity(refEntityMetaData);
		;
		entityMetaData.addAttribute(stringAttributeName).setDataType(MolgenisFieldTypes.STRING).setLabelAttribute(true);
		entityMetaData.addAttribute(textAttributeName).setDataType(MolgenisFieldTypes.TEXT);
		entityMetaData.addAttribute(xrefAttributeName).setDataType(MolgenisFieldTypes.XREF)
				.setRefEntity(refEntityMetaData);
		transformedEntityMetaData = entityMetaData;

		DataService dataService = mock(DataService.class);
		transformedEntity = new TransformedEntity(mapEntity, entityMetaData, dataService);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void TransformedEntity()
	{
		new TransformedEntity(null, null, null);
	}

	@Test
	public void get()
	{
		// check if typed values are returned
		assertEquals(transformedEntity.get(boolAttributeName), Boolean.valueOf(true));
	}

	@Test
	public void getAttributeNames()
	{
		Set<String> actualAttributeNames = Sets.newHashSet(transformedEntity.getAttributeNames());
		Set<String> expectedAttributeNames = Sets.newHashSet(Iterables.transform(
				transformedEntityMetaData.getAtomicAttributes(), new Function<AttributeMetaData, String>()
				{
					@Override
					public String apply(AttributeMetaData attributeMetaData)
					{
						return attributeMetaData.getName();
					}
				}));
		expectedAttributeNames.add(compoundAttributeName);
		System.out.println(actualAttributeNames);
		System.out.println(expectedAttributeNames);
		assertEquals(actualAttributeNames, expectedAttributeNames);
	}

	@Test
	public void getBoolean()
	{
		assertEquals(transformedEntity.getBoolean(boolAttributeName), Boolean.valueOf(true));
	}

	@Test
	public void getDateDataType() throws ParseException
	{
		assertEquals(transformedEntity.getDate(dateAttributeName),
				MolgenisDateFormat.getDateFormat().parse("2014-09-03"));
	}

	@Test
	public void getDateDataTimeType() throws ParseException
	{
		assertEquals(transformedEntity.getDate(dateTimeAttributeName),
				MolgenisDateFormat.getDateTimeFormat().parse("2014-09-03T08:02:10+0200"));
	}

	@Test
	public void getDouble()
	{
		assertEquals(transformedEntity.getDouble(decimalAttributeName), 1.23, 1E-6);
	}

	@Test
	public void getEntities()
	{
		Iterable<Entity> entities = transformedEntity.getEntities(mrefAttributeName);
		Set<Object> actualEntityIds = Sets.newHashSet(Iterables.transform(entities, new Function<Entity, Object>()
		{

			@Override
			public Object apply(Entity entity)
			{
				return entity.getIdValue();
			}
		}));
		assertEquals(actualEntityIds, Sets.newHashSet("ref0", "ref1"));
	}

	@Test
	public void getEntityCategorical()
	{
		assertEquals("ref0", transformedEntity.getEntity(categoricalAttributeName).getIdValue());
	}

	@Test
	public void getEntityXref()
	{
		assertEquals("ref1", transformedEntity.getEntity(xrefAttributeName).getIdValue());
	}

	@Test
	public void getEntityMetaData()
	{
		assertEquals(transformedEntity.getEntityMetaData(), transformedEntityMetaData);
	}

	@Test
	public void getIdValue()
	{
		assertEquals(transformedEntity.getIdValue(), "id0");
	}

	@Test
	public void getInt()
	{
		assertEquals(transformedEntity.getInt(intAttributeName), Integer.valueOf(1));
	}

	@Test
	public void getIntList()
	{
		assertEquals(transformedEntity.getIntList(compoundPart0AttributeName), Arrays.asList(0, 1, 2));
	}

	@Test
	public void getLabelAttributeNames()
	{
		assertEquals(transformedEntity.getLabelAttributeNames(), Arrays.asList(stringAttributeName));
	}

	@Test
	public void getLabelValue()
	{
		assertEquals(transformedEntity.getLabelValue(), "string");
	}

	@Test
	public void getList()
	{
		assertEquals(transformedEntity.getList(compoundPart1AttributeName), Arrays.asList("str0", "str1", "str2"));
	}

	@Test
	public void getLong()
	{
		assertEquals(transformedEntity.getLong(intAttributeName), Long.valueOf(1));
	}

	@Test
	public void getString()
	{
		assertEquals(transformedEntity.getString(stringAttributeName), "string");
	}

	@Test
	public void getTimestampDate() throws ParseException
	{
		assertEquals(transformedEntity.getTimestamp(dateAttributeName), new Timestamp(MolgenisDateFormat
				.getDateFormat().parse("2014-09-03").getTime()));
	}

	@Test
	public void getTimestampDateTime() throws ParseException
	{
		assertEquals(transformedEntity.getTimestamp(dateTimeAttributeName), new Timestamp(MolgenisDateFormat
				.getDateTimeFormat().parse("2014-09-03T08:02:10+0200").getTime()));
	}

	@Test
	public void getUtilDateDataType() throws ParseException
	{
		assertEquals(transformedEntity.getDate(dateAttributeName),
				MolgenisDateFormat.getDateFormat().parse("2014-09-03"));
	}

	@Test
	public void getUtilDateDataTimeType() throws ParseException
	{
		assertEquals(transformedEntity.getDate(dateTimeAttributeName),
				MolgenisDateFormat.getDateTimeFormat().parse("2014-09-03T08:02:10+0200"));
	}
}
