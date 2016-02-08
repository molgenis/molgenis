package org.molgenis.integrationtest.data;

import static org.molgenis.MolgenisFieldTypes.INT;
import static org.testng.Assert.fail;

import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.validation.MolgenisValidationException;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

public abstract class AbstractValidationExpressionIT extends AbstractDataIntegrationIT
{
	public void testIt()
	{
		EditableEntityMetaData entityMetaData = new DefaultEntityMetaData("ValidationExpressionTest");
		entityMetaData.addAttribute("identifier", ROLE_ID).setNillable(false).setAuto(true);
		entityMetaData.addAttribute("intAttr").setDataType(INT);
		entityMetaData.addAttribute("validationExpressionAttr").setDataType(INT)
				.setValidationExpression("$('validationExpressionAttr').gt(10).value() && $('intAttr').lt(10).value()");
		metaDataService.addEntityMeta(entityMetaData);

		Entity entity = new DefaultEntity(entityMetaData, dataService);// Not OK
		try
		{
			dataService.add(entityMetaData.getName(), entity);
			fail("Should have thrown MolgenisValidationException");
		}
		catch (MolgenisValidationException e)
		{
			// OK
		}

		entity.set("intAttr", 9);
		entity.set("validationExpressionAttr", 10);// Not OK
		try
		{
			dataService.add(entityMetaData.getName(), entity);
			fail("Should have thrown MolgenisValidationException");
		}
		catch (MolgenisValidationException e)
		{
			// OK
		}

		entity.set("intAttr", 10); // Not OK
		entity.set("validationExpressionAttr", 11);
		try
		{
			dataService.add(entityMetaData.getName(), entity);
			fail("Should have thrown MolgenisValidationException");
		}
		catch (MolgenisValidationException e)
		{
			// OK
		}

		entity.set("intAttr", 9);
		entity.set("validationExpressionAttr", 11);// OK
		dataService.add(entityMetaData.getName(), entity);
	}
}
