package org.molgenis.integrationtest.data;

import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.MolgenisFieldTypes.DATE;
import static org.molgenis.MolgenisFieldTypes.DATETIME;
import static org.molgenis.MolgenisFieldTypes.DECIMAL;
import static org.molgenis.MolgenisFieldTypes.INT;
import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.Arrays;

import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.Range;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.fieldtypes.EnumField;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

public abstract class AbstractDataValidationTest extends AbstractDataIntegrationTest
{
	private EditableEntityMetaData entityMetaData;
	private Entity entity;

	@BeforeClass
	public void beforeClass()
	{
		EditableEntityMetaData refEntityMetaData = new DefaultEntityMetaData("RefEntity");
		refEntityMetaData.addAttribute("identifier").setIdAttribute(true).setNillable(false).setAuto(true);
		metaDataService.addEntityMeta(refEntityMetaData);

		entityMetaData = new DefaultEntityMetaData("DataValidationTest");
		entityMetaData.addAttribute("identifier").setIdAttribute(true).setNillable(false).setAuto(true);
		entityMetaData.addAttribute("intAttr").setDataType(INT);
		entityMetaData.addAttribute("boolAttr").setDataType(BOOL);
		entityMetaData.addAttribute("dateAttr").setDataType(DATE);
		entityMetaData.addAttribute("datetimeAttr").setDataType(DATETIME);
		entityMetaData.addAttribute("decimalAttr").setDataType(DECIMAL);
		entityMetaData.addAttribute("xrefAttr").setDataType(XREF).setRefEntity(refEntityMetaData);
		entityMetaData.addAttribute("mrefAttr").setDataType(MREF).setRefEntity(refEntityMetaData);
		entityMetaData.addAttribute("rangeAttr").setDataType(INT).setRange(new Range(1L, 10L));

		EnumField enumField = new EnumField();
		enumField.setEnumOptions(Arrays.asList("ONE", "TWO"));
		entityMetaData.addAttribute("enumAttr").setDataType(enumField);

		metaDataService.addEntityMeta(entityMetaData);
	}

	@BeforeMethod
	public void beforeMethod()
	{
		entity = new DefaultEntity(entityMetaData, dataService);
	}

	public void testInt()
	{
		entity.set("intAttr", "bogus");
		add();
	}

	public void testBool()
	{
		entity.set("boolAttr", 1);
		add();
	}

	public void testDate()
	{
		entity.set("dateAttr", "bogus");
		add();
	}

	public void testDateTime()
	{
		entity.set("datetimeAttr", 1);
		add();
	}

	public void testDecimal()
	{
		entity.set("decimalAttr", "bogus");
		add();
	}

	public void testEnum()
	{
		entity.set("enumAttr", "bogus");
		add();
	}

	public void testXref()
	{
		entity.set("xrefAttr", "bogus");
		try
		{
			add();
			fail("Should have thrown UnknownEntityException");
		}
		catch (UnknownEntityException e)
		{
			// TODO, is this the desired behaviour?, should this not be ValidationException?
		}
	}

	public void testMref()
	{
		entity.set("mrefAttr", "bogus");
		add();
	}

	public void testRange()
	{
		entity.set("rangeAttr", 99);
		add();
	}

	public void testNotNillable()
	{
		EditableEntityMetaData entityMetaData1 = new DefaultEntityMetaData("NotNillableTest");
		entityMetaData1.addAttribute("identifier").setIdAttribute(true).setNillable(false);
		entityMetaData1.addAttribute("stringAttr").setNillable(false);
		metaDataService.addEntityMeta(entityMetaData1);

		Entity entity1 = new DefaultEntity(entityMetaData1, dataService);
		entity1.set("identifier", "one");

		try
		{
			dataService.add(entityMetaData1.getName(), entity1);
			fail("Should have thrown MolgenisValidationException");
		}
		catch (MolgenisValidationException e)
		{
			assertEquals(e.getMessage(),
					"The attribute 'stringAttr' of entity 'NotNillableTest' can not be null. (entity 1)");
		}
	}

	private void add()
	{
		try
		{
			dataService.add(entityMetaData.getName(), entity);
			fail("Should have thrown MolgenisValidationException");
		}
		catch (MolgenisValidationException e)
		{
			// OK
		}
	}
}
