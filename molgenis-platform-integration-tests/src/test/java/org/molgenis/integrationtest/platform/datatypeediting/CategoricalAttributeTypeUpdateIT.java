package org.molgenis.integrationtest.platform.datatypeediting;

import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.validation.DataTypeConstraintViolationException;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.integrationtest.platform.PlatformITConfig;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.*;

import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class CategoricalAttributeTypeUpdateIT extends AbstractAttributeTypeUpdateIT
{
	@BeforeClass
	public void setup()
	{
		super.setup(CATEGORICAL, STRING);
	}

	@AfterMethod
	public void afterMethod()
	{
		super.afterMethod(CATEGORICAL);
	}

	@AfterClass
	public void afterClass()
	{
		super.afterClass();
	}

	@DataProvider(name = "validConversionData")
	public Object[][] validConversionData()
	{
		Entity entity = dataService.findOneById("REFERENCEENTITY", "1");
		return new Object[][] { { entity, STRING, "1" }, { entity, INT, 1 }, { entity, LONG, 1L },
				{ entity, XREF, "label1" } };
	}

	/**
	 * Valid conversion cases for CATEGORICAL to:
	 * STRING, INT, LONG, XREF
	 *
	 * @param valueToConvert  The value that will be converted
	 * @param typeToConvertTo The type to convert to
	 * @param convertedValue  The expected value after converting the type
	 */
	@Test(dataProvider = "validConversionData")
	public void testValidConversion(Entity valueToConvert, AttributeType typeToConvertTo, Object convertedValue)
	{
		testTypeConversion(valueToConvert, typeToConvertTo);

		// Assert if conversion was successful
		assertEquals(getActualDataType(), typeToConvertTo);
		assertEquals(getActualValue(), convertedValue);
	}

	@DataProvider(name = "invalidConversionTestCases")
	public Object[][] invalidConversionTestCases()
	{
		Entity entity1 = dataService.findOneById("REFERENCEENTITY", "1");
		Entity entity2 = dataService.findOneById("REFERENCEENTITY", "molgenis@test.org");
		return new Object[][] { { entity1, BOOL, MolgenisDataException.class,
				"Attribute data type update from [CATEGORICAL] to [BOOL] not allowed, allowed types are [INT, LONG, STRING, XREF]" },
				{ entity1, TEXT, MolgenisDataException.class,
						"Attribute data type update from [CATEGORICAL] to [TEXT] not allowed, allowed types are [INT, LONG, STRING, XREF]" },
				{ entity1, SCRIPT, MolgenisDataException.class,
						"Attribute data type update from [CATEGORICAL] to [SCRIPT] not allowed, allowed types are [INT, LONG, STRING, XREF]" },
				{ entity2, INT, DataTypeConstraintViolationException.class,
						"type:INT or LONG value:molgenis@test.org" },
				{ entity2, LONG, DataTypeConstraintViolationException.class,
						"type:INT or LONG value:molgenis@test.org" }, { entity1, DECIMAL, MolgenisDataException.class,
						"Attribute data type update from [CATEGORICAL] to [DECIMAL] not allowed, allowed types are [INT, LONG, STRING, XREF]" },
				{ entity1, EMAIL, MolgenisDataException.class,
						"Attribute data type update from [CATEGORICAL] to [EMAIL] not allowed, allowed types are [INT, LONG, STRING, XREF]" },
				{ entity1, HYPERLINK, MolgenisDataException.class,
						"Attribute data type update from [CATEGORICAL] to [HYPERLINK] not allowed, allowed types are [INT, LONG, STRING, XREF]" },
				{ entity1, HTML, MolgenisDataException.class,
						"Attribute data type update from [CATEGORICAL] to [HTML] not allowed, allowed types are [INT, LONG, STRING, XREF]" },
				{ entity1, ENUM, MolgenisDataException.class,
						"Attribute data type update from [CATEGORICAL] to [ENUM] not allowed, allowed types are [INT, LONG, STRING, XREF]" },
				{ entity1, DATE, MolgenisDataException.class,
						"Attribute data type update from [CATEGORICAL] to [DATE] not allowed, allowed types are [INT, LONG, STRING, XREF]" },
				{ entity1, DATE_TIME, MolgenisDataException.class,
						"Attribute data type update from [CATEGORICAL] to [DATE_TIME] not allowed, allowed types are [INT, LONG, STRING, XREF]" },
				{ entity1, MREF, MolgenisDataException.class,
						"Attribute data type update from [CATEGORICAL] to [MREF] not allowed, allowed types are [INT, LONG, STRING, XREF]" },
				{ entity1, CATEGORICAL_MREF, MolgenisDataException.class,
						"Attribute data type update from [CATEGORICAL] to [CATEGORICAL_MREF] not allowed, allowed types are [INT, LONG, STRING, XREF]" },
				{ entity1, FILE, MolgenisDataException.class,
						"Attribute data type update from [CATEGORICAL] to [FILE] not allowed, allowed types are [INT, LONG, STRING, XREF]" },
				{ entity1, COMPOUND, MolgenisDataException.class,
						"Attribute data type update from [CATEGORICAL] to [COMPOUND] not allowed, allowed types are [INT, LONG, STRING, XREF]" },
				{ entity1, ONE_TO_MANY, MolgenisValidationException.class,
						"Invalid [xref] value [] for attribute [Referenced entity] of entity [mainAttribute] with type [sys_md_Attribute]. Offended validation expression: $('refEntityType').isNull().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/).not()).or($('refEntityType').isNull().not().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/))).value().Invalid [xref] value [] for attribute [Mapped by] of entity [mainAttribute] with type [sys_md_Attribute]. Offended validation expression: $('mappedBy').isNull().and($('type').eq('onetomany').not()).or($('mappedBy').isNull().not().and($('type').eq('onetomany'))).value()" } };
	}

	/**
	 * Invalid conversion cases for CATEGORICAL to:
	 * BOOL, TEXT, SCRIPT INT, LONG, DECIMAL, EMAIL, HYPERLINK, HTML, ENUM, DATE, DATE_TIME, MREF, CATEGORICAL_MREF, FILE, COMPOUND, ONE_TO_MANY
	 *
	 * @param valueToConvert   The value that will be converted
	 * @param typeToConvertTo  The type to convert to
	 * @param exceptionClass   The expected class of the exception that will be thrown
	 * @param exceptionMessage The expected exception message
	 */
	@Test(dataProvider = "invalidConversionTestCases")
	public void testInvalidConversion(Entity valueToConvert, AttributeType typeToConvertTo, Class exceptionClass,
			String exceptionMessage)
	{
		try
		{
			testTypeConversion(valueToConvert, typeToConvertTo);
			fail("Conversion should have failed");
		}
		catch (Exception exception)
		{
			System.out.println(exception.getClass());
			assertTrue(exception.getClass().isAssignableFrom(exceptionClass));
			assertEquals(exception.getMessage(), exceptionMessage);
		}
	}
}
